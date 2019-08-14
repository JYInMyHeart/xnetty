package channel

trait ChannelEvent {
  def getChannel: Channel
  def getFuture: ChannelFuture
}
