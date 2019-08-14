package channel

trait ChannelUpstreamHandler extends ChannelHandler {
  def handleUpstream(ctx: ChannelHandlerContext, event: ChannelEvent)
}
