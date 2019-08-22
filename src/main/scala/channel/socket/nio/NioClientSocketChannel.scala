package channel.socket.nio

import channel.socket.SocketChannel
import channel.{ChannelFactory, ChannelPipeline, ChannelSink}

case class NioClientSocketChannel(factory: ChannelFactory,
                                  pipeline: ChannelPipeline,
                                  sink: ChannelSink)
    extends NioSocketChannel(null, factory, pipeline, sink) {}

object NioClientSocketChannel {
  def newSocket: SocketChannel = {}
}
