package channel

trait ChannelHandlerContext {
  def getPipeline: ChannelPipeline

  def getName: String
  def getHandler: ChannelHandler
  def canHandleUpstream: Boolean
  def canHandleDownstream: Boolean

  def sendUpstream(event: ChannelEvent): Unit
  def sendDownstream(event: ChannelEvent): Unit
}
