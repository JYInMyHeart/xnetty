package channel

trait ChannelDownstreamHandler extends ChannelHandler {
  def handleDownstream(ctx: ChannelHandlerContext, event: ChannelEvent)
}
