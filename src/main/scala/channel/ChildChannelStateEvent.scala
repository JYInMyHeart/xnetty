package channel

trait ChildChannelStateEvent extends ChannelEvent {
  def getChildChannel: Channel
}
