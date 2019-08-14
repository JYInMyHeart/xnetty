package channel

import channel.ChannelState.ChannelState

case class DefaultChannelStateEvent(channel: Channel,
                                    future: ChannelFuture,
                                    state: ChannelState,
                                    value: Any)
    extends DefaultChannelEvent(channel, future)
    with ChannelStateEvent {

  override def getState: ChannelState = state

  override def getValue: Any = value
}
