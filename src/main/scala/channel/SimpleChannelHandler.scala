package channel

class SimpleChannelHandler extends ChannelUpstreamHandler {

  override def handleUpstream(_ctx: ChannelHandlerContext,
                              event: ChannelEvent): Unit = {
    event match {
      case e: MessageEvent =>
        messageReceived(_ctx, e)
      case e: ChildChannelStateEvent =>
        if (e.getChildChannel.isOpen) {
          childChannelOpen(_ctx, e)
        } else {
          childChannelClosed(_ctx, e)
        }
      case e: ChannelStateEvent =>
        e.getState match {
          case ChannelState.OPEN =>
            e.getValue match {
              case true  => channelOpen(_ctx, e)
              case false => channelClosed(_ctx, e)
            }
          case ChannelState.CONNECTED =>
            e.getValue match {
              case null => channelDisConnected(_ctx, e)
              case _    => channelConnected(_ctx, e)
            }
          case ChannelState.BOUND =>
            e.getValue match {
              case null => ChannelUnbound(_ctx, e)
              case _    => channelBound(_ctx, e)
            }
          case ChannelState.INTEREST_OPS =>
            channelInterestChanged(_ctx, e)
        }
      case e: ExceptionEvent =>
        exceptionCaught(_ctx, e)
      case _ =>
        _ctx.sendUpstream(event)
    }
  }

  def channelBound(context: ChannelHandlerContext,
                   event: ChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }

  def channelClosed(context: ChannelHandlerContext,
                    event: ChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def channelConnected(context: ChannelHandlerContext,
                       event: ChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def channelInterestChanged(context: ChannelHandlerContext,
                             event: ChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def ChannelUnbound(context: ChannelHandlerContext,
                     event: ChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def channelDisConnected(context: ChannelHandlerContext,
                          event: ChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def channelOpen(context: ChannelHandlerContext,
                  event: ChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def childChannelClosed(context: ChannelHandlerContext,
                         event: ChildChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def childChannelOpen(context: ChannelHandlerContext,
                       event: ChildChannelStateEvent): Unit = {
    context.sendUpstream(event)
  }
  def messageReceived(context: ChannelHandlerContext,
                      event: MessageEvent): Unit = {
    context.sendUpstream(event)
  }
  def exceptionCaught(_ctx: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    if (this == _ctx.getPipeline.getLast)
      println(
        s"Exception,please implement ${getClass.getName}.exceptionCaught() for proper handling. ${e.getCause}")
    _ctx.sendUpstream(e)
  }

}
