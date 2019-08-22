package channel.socket.nio

import java.io.IOException
import java.nio.channels.{ClosedChannelException, SelectionKey, Selector}
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

import channel.{ChannelException, ChannelFuture}

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
        fireChannelOpen(channel)
      }

      firChannelBound(channel, channel.getLocalAddress)
      fireChannelConnected(channel, channel.getRemoteAddress)

      val threadName = (if (server) "New I/O server worker #"
                        else "New I/O client worker #") + bossId + '-' + id
      executor.execute(new NamePreservingRunnable(this, threadName))
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
}
