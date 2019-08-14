package channel

import java.net.SocketAddress

object Channels {
  def bind(channel: AbstractChannel,
           localAddress: SocketAddress): ChannelFuture = {
    val _future = future(channel)
    channel.getPipeline.sendDownstream(
      DefaultChannelStateEvent(channel,
                               _future,
                               ChannelState.BOUND,
                               localAddress))
    _future
  }

  def future(channel: Channel, cancellable: Boolean): ChannelFuture = {
    DefaultChannelFuture(channel, cancellable)
  }

  def future(channel: Channel) = {
    future(channel, false)
  }
}
