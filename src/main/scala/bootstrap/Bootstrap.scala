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
  val options: mutable.Map[String, Any] =
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

  def getOptions: mutable.Map[String, Any] = {
    val map = mutable.TreeMap[String, Any]()
    map ++= options
    map
  }

  def getOption(name: String): Any = {
    options.get(name) match {
      case None        => throw new NoSuchElementException
      case Some(value) => value
    }
  }

  def setOption(key: String, value: Any): Unit = {
    value match {
      case null => options -= key
      case _    => options += (key -> value)
    }
  }
}
