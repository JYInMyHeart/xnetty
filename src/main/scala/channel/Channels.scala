package channel

import java.net.SocketAddress

object Channels {
  def write(channel: Channel,
            message: String,
            remoteAddress: SocketAddress): ChannelFuture = {
    val _future = future(channel)
    channel.getPipeline.sendDownstream(
      DefaultMessageEvent(channel, _future, message, remoteAddress))
    _future
  }

  def write(channel: Channel, message: String): ChannelFuture =
    write(channel, message, null)

  def setInterestOps(channel: Channel,
                     interestOps: Int): Option[ChannelFuture] = {
    if (!validateInterestOps(interestOps) || !validateDownstreamInterestOps(
          channel,
          interestOps))
      return None

    val _future = future(channel)
    channel.getPipeline.sendDownstream(
      DefaultChannelStateEvent(channel,
                               _future,
                               ChannelState.INTEREST_OPS,
                               interestOps))
    Some(_future)
  }

  def disconnect(channel: AbstractChannel): ChannelFuture = {
    val _future = future(channel)
    channel.getPipeline.sendDownstream(
      DefaultChannelStateEvent(channel, _future, ChannelState.CONNECTED, null))
    _future
  }

  def connect(channel: AbstractChannel,
              remoteAddress: SocketAddress): ChannelFuture = {
    val _future = future(channel, cancellable = true)
    channel.getPipeline.sendDownstream(
      DefaultChannelStateEvent(channel,
                               _future,
                               ChannelState.CONNECTED,
                               remoteAddress))
    _future
  }

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

  def future(channel: Channel): ChannelFuture = {
    future(channel, cancellable = false)
  }

  def pipelineFactory(_pipeline: ChannelPipeline): ChannelPipelineFactory =
    new ChannelPipelineFactory {
      override def getPipeline: ChannelPipeline = pipeline(_pipeline)
    }

  def pipeline(_pipeline: ChannelPipeline): ChannelPipeline = {
    val newPipeline = pipeline
    _pipeline.toMap().foreach { case (k, v) => newPipeline.addLast(k, v) }
    newPipeline
  }

  def pipeline: ChannelPipeline = new DefaultChannelPipeline()

  def validateInterestOps(interestOps: Int): Boolean =
    interestOps match {
      case Channel.OP_NONE | Channel.OP_READ | Channel.OP_WRITE |
          Channel.OP_READ_WRITE =>
        true
      case _ => false
    }

  def validateDownstreamInterestOps(channel: Channel,
                                    interestOps: Int): Boolean =
    ((channel.getInterestOps ^ interestOps) & Channel.OP_WRITE) == 0
}
