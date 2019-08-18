package bootstrap

import channel.{
  ChannelFactory,
  ChannelHandler,
  ChannelPipeline,
  ChannelPipelineFactory,
  Channels
}

import scala.collection.mutable

abstract class Bootstrap(@volatile channelFactory: ChannelFactory) {
  @volatile val channelPipeline: ChannelPipeline = Channels.pipeline
  @volatile val channelPipelineFactory: ChannelPipelineFactory =
    Channels.pipelineFactory(channelPipeline)
  @volatile val options: mutable.Map[String, String] =
    mutable.LinkedHashMap()

  def setPipelineAsMap(
      pipelineMap: mutable.LinkedHashMap[String, ChannelHandler]): Unit = {
    assert(pipelineMap != null)
    pipelineMap.foreach {
      case (name, handler) => channelPipeline.addLast(name, handler)
    }
  }

  def getPipelineAsMap: mutable.Map[String, ChannelHandler] = {
    channelPipeline.toMap()
  }

  def getOptions: mutable.Map[String, String] =
    mutable.TreeMap[String, String].apply(options.toList)
}
