package channel

import java.net.{InetSocketAddress, SocketAddress}

import buffer.ChannelBuffer
import channel.socket.nio.{NioClientSocketChannel, NioSocketChannel}

object Channels {
  def fireChannelBound(channel: NioClientSocketChannel,
                       localAddress: InetSocketAddress): Unit = {
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.BOUND,
                               localAddress))
  }

  def fireMessageReceived(channel: Channel, message: Any): Unit = {
    fireMessageReceived(channel, message, null)
  }

  def fireMessageReceived(channel: Channel,
                          message: Any,
                          remoteAddress: SocketAddress): Unit = {
    channel.getPipeline.sendUpstream(
      DefaultMessageEvent(channel,
                          succeededFuture(channel),
                          message,
                          remoteAddress))
  }

  def fireChannelInterestChanged(channel: NioSocketChannel,
                                 interestOps: Int): Unit = {
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.INTEREST_OPS,
                               interestOps))
  }

  def fireExceptionCaught(channel: Channel, t: Throwable): Unit =
    channel.getPipeline.sendUpstream(
      DefaultExceptionEvent(channel, succeededFuture(channel), t))

  def fireChannelClosed(channel: Channel): Unit =
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.OPEN,
                               false))

  def fireChannelUnbound(channel: Channel): Unit =
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.BOUND,
                               null))

  def fireChannelDisconnected(channel: Channel): Unit = {
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.CONNECTED,
                               null))
  }

  def fireChannelConnected(channel: NioSocketChannel,
                           remoteAddress: InetSocketAddress): Unit = {
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.BOUND,
                               remoteAddress))
  }

  def firChannelBound(channel: NioSocketChannel,
                      localAddress: InetSocketAddress): Unit = {
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.CONNECTED,
                               localAddress))
  }

  def close(channel: Channel): ChannelFuture = {
    val _future = future(channel)
    channel.getPipeline.sendDownstream(
      DefaultChannelStateEvent(channel, _future, ChannelState.OPEN, false))
    _future
  }

  def write(channel: Channel,
            message: Any,
            remoteAddress: SocketAddress): ChannelFuture = {
    val _future = future(channel)
    channel.getPipeline.sendDownstream(
      DefaultMessageEvent(channel, _future, message, remoteAddress))
    _future
  }

  def write(channel: Channel, message: => Any): ChannelFuture =
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

  def fireChildChannelStateChanged(parent: Channel, channel: Channel): Unit = {
    parent.getPipeline.sendUpstream(
      DefaultChildChannelStateEvent(parent, succeededFuture(parent), channel))
  }

  def fireChannelOpen(channel: Channel): Unit = {
    if (channel.getParent != null) {
      fireChildChannelStateChanged(channel.getParent, channel)
    }
    channel.getPipeline.sendUpstream(
      DefaultChannelStateEvent(channel,
                               succeededFuture(channel),
                               ChannelState.OPEN,
                               true))
  }

  def succeededFuture(channel: Channel): ChannelFuture = {
    channel match {
      case c: AbstractChannel =>
        c.succeededFuture
      case _ =>
        SucceededChannelFuture(channel)
    }
  }
}
