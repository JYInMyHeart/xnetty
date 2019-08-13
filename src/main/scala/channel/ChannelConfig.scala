package channel

trait ChannelConfig {
  def setOptions(options: Map[String, Boolean]): Unit
  def getPipelineFactory: ChannelPipelineFactory
  def setPipelineFactory(pipelineFactory: ChannelPipelineFactory)
  def getConnectTimeoutMillis: Int
  def setConnectTimeoutMillis(connectTimeoutMillis: Int): Unit
  def getWriteTimeoutMillis: Int
  def setWriteTimeoutMillis(writeTimeoutMillis: Int): Unit
}
