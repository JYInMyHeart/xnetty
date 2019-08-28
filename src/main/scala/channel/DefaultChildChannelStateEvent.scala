package channel

case class DefaultChildChannelStateEvent(channel: Channel,
                                         future: ChannelFuture,
                                         childChannel: Channel)
    extends DefaultChannelEvent(channel, future)
    with ChildChannelStateEvent {
  override def getChildChannel: Channel = childChannel

  override def toString(): String = {
    s"${super.toString} - childId: ${childChannel.getId().toString}, childState: ${if (childChannel.isOpen) "OPEN" else "CLOSE"})"
  }
}
