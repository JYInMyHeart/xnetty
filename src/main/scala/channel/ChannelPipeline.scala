package channel

trait ChannelPipeline {
  def addFirst(name: String, handler: ChannelHandler): Unit
}
