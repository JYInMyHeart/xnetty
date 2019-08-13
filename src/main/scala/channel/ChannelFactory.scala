package channel

trait ChannelFactory {
  def newChannel(pipeline: ChannelPipeline): Channel
}
