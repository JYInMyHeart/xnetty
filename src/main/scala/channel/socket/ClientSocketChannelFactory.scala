package channel.socket

import channel.{Channel, ChannelFactory, ChannelPipeline}

trait ClientSocketChannelFactory extends ChannelFactory {
  override def newChannel(pipeline: ChannelPipeline): SocketChannel
}
