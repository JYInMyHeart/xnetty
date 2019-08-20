package bootstrap
import java.net.SocketAddress
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}

import channel.{
  Channel,
  ChannelException,
  ChannelFactory,
  ChannelFuture,
  ChannelHandler,
  ChannelHandlerContext,
  ChannelPipelineCoverage,
  ChannelStateEvent,
  Channels,
  ChildChannelStateEvent,
  ExceptionEvent,
  SimpleChannelHandler
}

import scala.collection.mutable

case class ServerBootStrap(channelFactory: ChannelFactory)
    extends Bootstrap(channelFactory) {
  @volatile var parentHandler: ChannelHandler = _

  def bind(): Channel = {
    val localAddress = getOption("localAddress").asInstanceOf[SocketAddress]
    if (localAddress == null)
      throw new IllegalArgumentException("localAddress option is not set!")
    bind(localAddress)
  }

  def bind(address: SocketAddress): Channel = {
    val futureQueue = new LinkedBlockingQueue[ChannelFuture]()
    val _pipeline = Channels.pipeline
    _pipeline.addLast("binder", new Binder(address, futureQueue))
    val _parentHandler = parentHandler
    if (_parentHandler != null)
      _pipeline.addLast("userHandler", _parentHandler)
    val channel = channelFactory.newChannel(_pipeline)
    var _future: ChannelFuture = null
    while (_future == null) {
      try {
        _future = futureQueue.poll(Int.MaxValue, TimeUnit.SECONDS)
      } catch {
        case _: InterruptedException =>
      }
    }
    _future.awaitUninterruptibly()
    if (_future.isSuccess) {
      _future.getChannel.close().awaitUninterruptibly()
      throw new ChannelException(s"Failed to bind to $address",
                                 _future.getCause)
    }
    channel
  }

  @ChannelPipelineCoverage("one")
  private sealed class Binder(address: SocketAddress,
                              futureQueue: BlockingQueue[ChannelFuture])
      extends SimpleChannelHandler {
    private[this] val childOptions: mutable.Map[String, String] =
      mutable.HashMap()

    override def channelOpen(context: ChannelHandlerContext,
                             event: ChannelStateEvent): Unit = {
      event.getChannel.getConfig.setPipelineFactory(channelPipelineFactory)

      val allOptions = getOptions
      val parentOptions = mutable.HashMap[String, String]()
      childOptions ++= allOptions
        .filter { case (k, _) => k.startsWith("child.") }
        .map { case (k, v) => k.substring(6) -> v }
      parentOptions ++= allOptions.filter {
        case (k, _) => !k.startsWith("child.") && k != "pipelineFactory"
      }
      event.getChannel.getConfig.setOptions(parentOptions)
      futureQueue.offer(event.getChannel.bind(address))
      context.sendUpstream(event)
    }

    override def childChannelOpen(context: ChannelHandlerContext,
                                  event: ChildChannelStateEvent): Unit = {
      event.getChildChannel.getConfig.setOptions(childOptions)
      context.sendUpstream(event)
    }

    override def exceptionCaught(_ctx: ChannelHandlerContext,
                                 e: ExceptionEvent): Unit = {
      _ctx.sendUpstream(e)
    }
  }

}
