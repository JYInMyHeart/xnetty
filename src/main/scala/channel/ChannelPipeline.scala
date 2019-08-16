package channel

import org.jetbrains.annotations.Contract

trait ChannelPipeline {
  def addFirst(name: String, handler: ChannelHandler): Unit
  def addLast(name: String, handler: ChannelHandler): Unit
  def addBefore(baseName: String, name: String, handler: ChannelHandler)
  def addAfter(baseName: String, name: String, handler: ChannelHandler)

  def remove(handler: ChannelHandler): Unit
  def remove(name: String): ChannelHandler
  def remove[T <: ChannelHandler](handlerType: Class[T]): ChannelHandler
  def removeFirst(): ChannelHandler
  def removeLast(): ChannelHandler

  def replace(oldHandler: ChannelHandler,
              newName: String,
              newHandler: ChannelHandler): Unit
  def replace(oldName: String,
              newName: String,
              newHandler: ChannelHandler): ChannelHandler
  def replace[T <: ChannelHandler](oldHandlerType: Class[T],
                                   newName: String,
                                   newHandler: ChannelHandler): ChannelHandler

  @Contract(pure = true) def getFirst: ChannelHandler
  @Contract(pure = true) def getLast: ChannelHandler

  @Contract(pure = true) def get(name: String): Option[ChannelHandler]
  @Contract(pure = true) def get[T <: ChannelHandler](
      handlerType: Class[T]): Option[ChannelHandler]

  def getContext(handler: ChannelHandler): Option[ChannelHandlerContext]
  def getContext(name: String): Option[ChannelHandlerContext]
  def getContext[T <: ChannelHandler](
      handlerType: Class[T]): Option[ChannelHandlerContext]

  def sendUpstream(event: ChannelEvent): Unit
  def sendDownstream(event: ChannelEvent): Unit

  def getChannel: Channel
  def getSink: ChannelSink
  def attach(channel: Channel, sink: ChannelSink)

  def toMap(): Map[String, ChannelHandler]

}
