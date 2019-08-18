package channel

import scala.collection.mutable

trait ChannelConfig {
  def setOptions(options: mutable.Map[String, String]): Unit
  def getPipelineFactory: ChannelPipelineFactory
  def setPipelineFactory(pipelineFactory: ChannelPipelineFactory)
  def getConnectTimeoutMillis: Int
  def setConnectTimeoutMillis(connectTimeoutMillis: Int): Unit
  def getWriteTimeoutMillis: Int
  def setWriteTimeoutMillis(writeTimeoutMillis: Int): Unit
}
