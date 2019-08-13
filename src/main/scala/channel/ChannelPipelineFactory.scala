package channel

trait ChannelPipelineFactory {
  def getPipeline: ChannelPipeline
}
