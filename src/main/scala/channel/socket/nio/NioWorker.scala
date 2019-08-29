package channel.socket.nio

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.{
  ClosedChannelException,
  ReadableByteChannel,
  SelectionKey,
  Selector
}
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

import buffer.{ChannelBuffer, ChannelBuffers}
import channel.{Channel, ChannelException, ChannelFuture, Channels}

case class NioWorker(bossId: Int,
                     id: Int,
                     executor: Executor,
                     started: AtomicBoolean = new AtomicBoolean())
    extends Runnable {
  @volatile var thread: Thread = _
  @volatile var selector: Selector = _

  def register(channel: NioSocketChannel, future: ChannelFuture): Unit = {
    val firstChannel = started.compareAndSet(false, true)
    var _selector: Selector = null
    if (firstChannel) {
      try {
        selector = Selector.open()
        _selector = selector
      } catch {
        case e: IOException =>
          throw new ChannelException("Failed to create selector.", e)
      }
    } else {
      _selector = selector
      if (_selector == null) {
        while (_selector == null) {
          Thread.`yield`()
          _selector = selector
        }
      }
    }

    if (firstChannel) {
      try {
        channel.socketChannel.register(_selector, SelectionKey.OP_READ, channel)
        if (future != null)
          future.setSuccess()
      } catch {
        case e: ClosedChannelException =>
          future.setFailure(e)
          throw new ChannelException(
            "Failed to register a socket to the selector.",
            e)
      }

      val server = !channel.isInstanceOf[NioClientSocketChannel]
      if (server) {
        Channels.fireChannelOpen(channel)
      }

      Channels.firChannelBound(channel, channel.getLocalAddress)
      Channels.fireChannelConnected(channel, channel.getRemoteAddress)

      val threadName = (if (server) "New I/O server worker #"
                        else "New I/O client worker #") + bossId + '-' + id
      executor.execute(NamePreservingRunnable(this, threadName))
    }
  }

  sealed case class NamePreservingRunnable(runnable: Runnable, newName: String)
      extends Runnable {
    override def run(): Unit = {
      val currentThread = Thread.currentThread()
      val oldName = currentThread.getName
      if (newName != null)
        setName(currentThread, newName)
      try {
        runnable.run()
      } finally {
        setName(currentThread, oldName)
      }
    }
    private[this] def setName(thread: Thread, name: String): Unit = {
      try {
        thread.setName(name)
      } catch {
        case e: Exception =>
          println("Failed to set the current thread name.", e)
      }
    }
  }

  override def run(): Unit = {
    thread = Thread.currentThread()
    var shutdown = false
    val _selector = selector
    while (true) {
      this.synchronized {}
      try {
        val selectedKeyCount = _selector.select(500)
        if (selectedKeyCount > 0)
          NioWorker.processSelectedKeys(_selector.selectedKeys())

        if (_selector.keys().isEmpty) {
          if (shutdown) {
            this.synchronized {
              if (_selector.keys().isEmpty) {
                try {
                  _selector.close()
                } catch {
                  case e: IOException =>
                    println(s"Failed to close a selector. $e")
                } finally {
                  this.selector = null
                }
              } else {
                shutdown = true
              }
            }
          } else {
            shutdown = true
          }
        } else {
          shutdown = false
        }
      } catch {
        case t: Throwable =>
          println(s"Unexpected exception in the selector loop $t")
          try {
            Thread.sleep(1000)
          } catch {
            case _: InterruptedException =>
          }
      }
    }
  }
}

object NioWorker {
  import scala.collection.JavaConverters._
  def processSelectedKeys(selectedKeys: java.util.Set[SelectionKey]): Unit = {
    val iterator = selectedKeys.iterator()
    for (key <- iterator.asScala) {
      iterator.remove()
      if (!key.isValid) {
        close(key)
      } else {
        if (key.isReadable) {
          read(key)
        }

        if (!key.isValid) {
          close(key)
        } else {
          if (key.isWritable)
            write(key)
        }

      }
    }
  }

  def close(key: SelectionKey): Unit = {
    val ch = key.attachment().asInstanceOf[NioSocketChannel]
    close(ch, ch.succeededFuture)
  }

  def close(channel: NioSocketChannel, future: ChannelFuture): Unit = {
    val worker = channel.getWorker
    worker match {
      case null =>
      case _ =>
        val _selector = worker.selector
        val key = channel.socketChannel.keyFor(_selector)
        if (key != null) key.cancel()
    }

    val connected = channel.isConnected
    val bound = channel.isBound

    try {
      channel.socketChannel.close()
      future.setSuccess()
      if (channel.setClosed()) {
        if (connected) {
          if (channel.getInterestOps != Channel.OP_WRITE) {
            channel.setInterestOpsNow(Channel.OP_WRITE)
            Channels.fireChannelInterestChanged(channel, Channel.OP_WRITE)
          }
          Channels.fireChannelDisconnected(channel)
        }
        if (bound) {
          Channels.fireChannelUnbound(channel)
        }
        Channels.fireChannelClosed(channel)
      }
    } catch {
      case t: Throwable =>
        future.setFailure(t)
        Channels.fireExceptionCaught(channel, t)
    }

  }

  def read(key: SelectionKey): Unit = {
    import scala.util.control.Breaks._
    val ch = key.channel().asInstanceOf[ReadableByteChannel]
    val channel = key.attachment().asInstanceOf[NioSocketChannel]
    val predictor = channel.getConfig.getReceiveBufferSizePredictor
    val buf = ByteBuffer.allocate(predictor.nextReceiveBufferSize)

    var ret = 0
    var readBytes = 0
    var failure = true
    try {
      breakable {
        while (true) {
          ret = ch.read(buf)
          if (ret > 0) {
            readBytes += ret
            if (!buf.hasRemaining()) {
              break()
            }
          } else {
            break()
          }
        }
      }
      failure = false
    } catch {
      case t: Throwable =>
        Channels.fireExceptionCaught(channel, t)
    }

    if (readBytes > 0) {
      predictor.previousReceiveBufferSize(readBytes)

      val buffer = if (readBytes == buf.capacity()) {
        ChannelBuffers.wrappedBuffer(buf.array())
      } else {
        ChannelBuffers.wrappedBuffer(buf.array(), 0, readBytes)
      }
      Channels.fireMessageReceived(channel, buffer)
    }

    if (ret < 0 || failure)
      close(key)

  }
  def write(key: SelectionKey): Unit = {
    val ch = key.attachment().asInstanceOf[NioSocketChannel]
    write(ch)
  }

  def setOpWrite(channel: NioSocketChannel, opWrite: Boolean): Unit = {
    val worker = channel.getWorker
    if (worker == null) {
      val cause = new IllegalStateException(
        "Channel not connected yet (null worker)")
      Channels.fireExceptionCaught(channel, cause)
      return
    }

    val _selector = worker.selector
    val key = channel.socketChannel.keyFor(_selector)
    if (!key.isValid) {
      close(key)
      return
    }
    var interestOps: Int = 0
    var changed = false
    if (opWrite) {
      if (Thread.currentThread() == worker.thread) {
        interestOps = key.interestOps()
        if ((interestOps & SelectionKey.OP_WRITE) == 0) {
          interestOps |= SelectionKey.OP_WRITE
          key.interestOps(interestOps)
          changed = true
        }
      } else {
        worker.synchronized {
          _selector.wakeup()
          interestOps = key.interestOps()
          if ((interestOps & SelectionKey.OP_WRITE) == 0) {
            interestOps |= SelectionKey.OP_WRITE
            key.interestOps(interestOps)
            changed = true
          }
        }
      }
    } else {
      if (Thread.currentThread() == worker.thread) {
        interestOps = key.interestOps()
        if ((interestOps & SelectionKey.OP_WRITE) != 0) {
          interestOps &= ~SelectionKey.OP_WRITE
          key.interestOps(interestOps)
          changed = true
        }
      } else {
        worker.synchronized {
          _selector.wakeup()
          interestOps = key.interestOps()
          if ((interestOps & SelectionKey.OP_WRITE) != 0) {
            interestOps &= ~SelectionKey.OP_WRITE
            key.interestOps(interestOps)
            changed = true
          }
        }
      }
    }
    if (changed) {
      channel.setInterestOpsNow(interestOps)
      Channels.fireChannelInterestChanged(channel, interestOps)
    }
  }

  def write(channel: NioSocketChannel): Unit = {
    if (channel.writeBuffer.isEmpty && channel.currentWriteEvent == null) return

    var addOpWrite = false
    var removeOpWrite = false

    val maxWrittenBytes = if (channel.getConfig.isReadWriteFair) {
      val previousReceiveBufferSize =
        channel.getConfig.getReceiveBufferSizePredictor.nextReceiveBufferSize
      previousReceiveBufferSize + previousReceiveBufferSize >>> 1
    } else
      Int.MaxValue

    var writtenBytes = 0
    import util.control.Breaks._
    channel.writeBuffer.synchronized {
      breakable {
        while (true) {
          if (channel.writeBuffer.isEmpty && channel.currentWriteEvent == null) {
            removeOpWrite = true
            break()
          }

          val buffer = if (channel.currentWriteEvent == null) {
            channel.currentWriteEvent = channel.writeBuffer.poll()
            val buf = channel.currentWriteEvent.getMessage
              .asInstanceOf[ChannelBuffer]
            channel.currentWriteIndex = buf.readerIndex
            buf
          } else {
            channel.currentWriteEvent.getMessage.asInstanceOf[ChannelBuffer]
          }

          var localWrittenBytes = 0
          try {
            breakable {
              for (_ <- channel.getConfig.getWriteSpinCount until 0 by -1) {
                localWrittenBytes = buffer.getBytes(
                  channel.currentWriteIndex,
                  channel.socketChannel,
                  math.min(maxWrittenBytes - writtenBytes,
                           buffer.writerIndex - channel.currentWriteIndex))
                if (localWrittenBytes != 0) {
                  break()
                }
              }
            }
          } catch {
            case t: Throwable =>
              channel.currentWriteEvent.getFuture.setFailure(t)
              Channels.fireExceptionCaught(channel, t)
          }
          writtenBytes += localWrittenBytes
          channel.currentWriteIndex += localWrittenBytes
          if (channel.currentWriteIndex == buffer.writerIndex) {
            channel.currentWriteEvent.getFuture.setSuccess()
            channel.currentWriteEvent = null
          } else {
            addOpWrite = true
            break()
          }
        }

      }

    }

    if (addOpWrite)
      setOpWrite(channel, opWrite = true)
    else if (removeOpWrite)
      setOpWrite(channel, opWrite = false)
  }

  def setInterestOps(channel: NioSocketChannel,
                     future: ChannelFuture,
                     interestOps: Int): Unit = {
    val worker = channel.getWorker
    if (worker == null) {
      val cause = new IllegalArgumentException(
        s"Channel not connected yet (null worker)")
      future.setFailure(cause)
      Channels.fireExceptionCaught(channel, cause)
      return
    }
    val _selector = worker.selector
    val key = channel.socketChannel.keyFor(_selector)
    if (key == null || _selector == null) {
      val cause = new IllegalArgumentException(
        s"Channel not connected yet (SelectionKey not found)")
      future.setFailure(cause)
      Channels.fireExceptionCaught(channel, cause)
    }

    var changed = false
    try {
      if (Thread.currentThread() == worker.thread)
        if (key.interestOps() != interestOps) {
          key.interestOps(interestOps)
          changed = true
        } else {
          this.synchronized {
            _selector.wakeup()
            if (key.interestOps() != interestOps) {
              key.interestOps(interestOps)
              changed = true
            }
          }
        }
      future.setSuccess()
      if (changed) {
        channel.setInterestOpsNow(interestOps)
        Channels.fireChannelInterestChanged(channel, interestOps)
      }
    } catch {
      case t: Throwable =>
        future.setFailure(t)
        Channels.fireExceptionCaught(channel, t)
    }
  }

}
