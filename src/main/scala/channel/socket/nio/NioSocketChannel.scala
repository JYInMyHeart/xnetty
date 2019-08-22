package channel.socket.nio

import java.net.{InetSocketAddress, SocketAddress}
import java.nio.channels.SocketChannel

import channel.{
  AbstractChannel,
  Channel,
  ChannelFactory,
  ChannelFuture,
  ChannelPipeline,
  ChannelSink,
  MessageEvent
}

import scala.collection.mutable

abstract class NioSocketChannel(val socketChannel: SocketChannel,
                                val config: NioSocketChannelConfig,
                                val parent: Channel,
                                val factory: ChannelFactory,
                                val pipeline: ChannelPipeline,
                                val sink: ChannelSink)
    extends AbstractChannel(parent, factory, pipeline, sink)
    with channel.socket.SocketChannel {
  val writeBuffer: mutable.Queue[MessageEvent] = mutable.Queue[MessageEvent]()
  var currentWriteEvent: MessageEvent = _
  var currentWriteIndex: Int = _

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

  override def write(message: => String,
                     remoteAddress: SocketAddress): ChannelFuture = {
    if (remoteAddress == null || remoteAddress == getRemoteAddress)
      super.write(message, null)
    else
      getUnsupportedOperationFuture
  }
}
