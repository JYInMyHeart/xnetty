package channel

class DefaultChannelEvent(channel: Channel, future: ChannelFuture)
    extends ChannelEvent {
  override def getChannel: Channel = channel

  override def getFuture: ChannelFuture = future

  override def toString: String = channel.toString
}
