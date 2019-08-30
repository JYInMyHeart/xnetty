package channel.socket.nio

import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.SocketChannel
import java.util
import java.util.concurrent.ConcurrentLinkedQueue

import channel.{
  AbstractChannel,
  Channel,
  ChannelFactory,
  ChannelFuture,
  ChannelPipeline,
  ChannelSink,
  MessageEvent
}

abstract class NioSocketChannel(val socketChannel: SocketChannel,
                                parent: Channel,
                                factory: ChannelFactory,
                                pipeline: ChannelPipeline,
                                sink: ChannelSink)
    extends AbstractChannel(parent, factory, pipeline, sink)
    with channel.socket.SocketChannel {
  val writeBuffer: util.Queue[MessageEvent] =
    new ConcurrentLinkedQueue[MessageEvent]()
  var currentWriteEvent: MessageEvent = _
  var currentWriteIndex: Int = _
  lazy val config: NioSocketChannelConfig = DefaultNioSocketChannelConfig(
    socketChannel.socket())

  def getWorker: NioWorker
  def setWorker(worker: NioWorker): Unit

  override def getLocalAddress: InetSocketAddress =
    socketChannel.socket().getLocalAddress.asInstanceOf[InetSocketAddress]

  override def getRemoteAddress: InetSocketAddress =
    socketChannel
      .socket()
      .getRemoteSocketAddress
      .asInstanceOf[InetSocketAddress]

  override def isBound: Boolean = socketChannel.socket().isBound

  override def isConnected: Boolean = socketChannel.socket().isConnected

  override def write(message: => Any,
                     remoteAddress: SocketAddress): ChannelFuture = {
    if (remoteAddress == null || remoteAddress == getRemoteAddress)
      super.write(message, null)
    else
      getUnsupportedOperationFuture
  }

  override def getConfig: NioSocketChannelConfig = config

  override def setClosed(): Boolean = super.setClosed()

}
