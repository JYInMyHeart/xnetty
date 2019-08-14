package channel

import channel.ChannelState.ChannelState

trait ChannelStateEvent extends ChannelEvent {
  def getState: ChannelState
  def getValue: Any
}

object ChannelState {
  type ChannelState = Int
  final val OPEN: ChannelState = 1
  final val BOUND: ChannelState = 2
  final val CONNECTED: ChannelState = 3
  final val INTEREST_OPS: ChannelState = 4
}
