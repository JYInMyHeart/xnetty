package bootstrap

import channel.{
  ChannelFactory,
  ChannelHandler,
  ChannelPipeline,
  ChannelPipelineFactory,
  Channels
}

import scala.collection.mutable

class Bootstrap(channelFactory: ChannelFactory) {
  val channelPipeline: ChannelPipeline =
    Channels.pipeline
  val channelPipelineFactory: ChannelPipelineFactory =
    Channels.pipelineFactory(channelPipeline)
  val options: mutable.Map[String, String] =
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

  def getOptions: mutable.Map[String, String] = {
    val map = mutable.TreeMap[String, String]()
    map ++= options
    map
  }

  def getOption(name: String): Any = {
    options.get(name) match {
      case None        => throw new NoSuchElementException
      case Some(value) => value
    }
  }
}
