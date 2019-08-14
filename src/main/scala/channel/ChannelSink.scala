package channel

trait ChannelSink {
  def eventSunk(pipeline: ChannelPipeline, event: ChannelEvent): Unit
  def exceptionCaught(pipeline: ChannelPipeline,
                      event: ChannelEvent,
                      cause: ChannelPipelineException)
}
