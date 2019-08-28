package channel.socket.nio

import java.io.IOException
import java.nio.channels.SocketChannel

import channel.{
  Channel,
  ChannelException,
  ChannelFactory,
  ChannelFuture,
  ChannelPipeline,
  ChannelSink,
  Channels
}

case class NioClientSocketChannel(factory: ChannelFactory,
                                  pipeline: ChannelPipeline,
                                  sink: ChannelSink,
                                  parent: Channel)
    extends NioSocketChannel(NioClientSocketChannel.newSocket,
                             null,
                             factory,
                             pipeline,
                             sink) {
  def this(factory: ChannelFactory,
           pipeline: ChannelPipeline,
           sink: ChannelSink) = {
    this(factory, pipeline, sink, null)
    Channels.fireChannelOpen(this)
  }

  @volatile var worker: NioWorker = _
  @volatile var connectFuture: ChannelFuture = _
  @volatile var boundManually: Boolean = _
  override def getWorker: NioWorker = worker

  override def setWorker(_worker: NioWorker): Unit = {
    if (worker == null)
      worker = _worker
    else if (worker != _worker)
      throw new IllegalStateException("Should not reach here.")
  }
}

object NioClientSocketChannel {
  def newSocket: SocketChannel = {
    var socket: SocketChannel = null
    var success = false
    try {
      socket = SocketChannel.open()
      socket.configureBlocking(false)
      success = true
    } catch {
      case e: IOException =>
        throw new ChannelException(
          "Failed to open a socket or enter non-blocking mode.",
          e)
    } finally {
      if (!success) {
        try {
          socket.close()
        } catch {
          case e: IOException =>
            println(
              s"Failed to close a partially initialized socket. ${e.getMessage}")
        }
      }
    }
    socket
  }
}
