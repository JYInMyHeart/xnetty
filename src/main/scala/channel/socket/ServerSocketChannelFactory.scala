package channel.socket

import channel.{Channel, ChannelFactory, ChannelPipeline}

trait ServerSocketChannelFactory extends ChannelFactory {
  override def newChannel(pipeline: ChannelPipeline): ServerSocketChannel
}
