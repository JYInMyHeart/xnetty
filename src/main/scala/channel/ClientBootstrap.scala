package channel

import java.net.SocketAddress
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}

import bootstrap.Bootstrap

case class ClientBootstrap(channelFactory: ChannelFactory)
    extends Bootstrap(channelFactory) {
  def connect(): ChannelFuture = {
    val remoteAddress = getOption("remoteAddress").asInstanceOf[SocketAddress]
    if (remoteAddress == null)
      throw new IllegalArgumentException("remoteAddress option is not set.")
    val localAddress = getOption("localAddress").asInstanceOf[SocketAddress]
    connect(remoteAddress, localAddress)
  }

  def connect(remoteAddress: SocketAddress,
              localAddress: SocketAddress): ChannelFuture = {
    val futureQueue = new LinkedBlockingQueue[ChannelFuture]()
    val _pipeline = channelPipelineFactory.getPipeline
    _pipeline.addFirst("connector",
                       new Connector(remoteAddress, localAddress, futureQueue))
    channelFactory.newChannel(_pipeline)
    var _future: ChannelFuture = null
    while (_future == null) {
      try {
        _future = futureQueue.poll(Int.MaxValue, TimeUnit.SECONDS)
      } catch {
        case _: InterruptedException =>
      }
    }
    _pipeline.remove(_pipeline.get("connector").get)
    _future
  }

  private[this] sealed class Connector(
      remoteAddress: SocketAddress,
      localAddress: SocketAddress,
      futureQueue: BlockingQueue[ChannelFuture])
      extends SimpleChannelHandler {
    @volatile private[this] var finished: Boolean = _

    override def channelOpen(context: ChannelHandlerContext,
                             event: ChannelStateEvent): Unit = {
      context.sendUpstream(event)
      event.getChannel.getConfig.setOptions(getOptions)
      if (localAddress != null)
        event.getChannel.bind(localAddress)
      else {
        futureQueue.offer(event.getChannel.connect(remoteAddress))
        finished = true
      }
    }

    override def channelBound(context: ChannelHandlerContext,
                              event: ChannelStateEvent): Unit = {
      if (localAddress != null) {
        futureQueue.offer(event.getChannel.connect(remoteAddress))
        finished = true
      }
    }

    override def exceptionCaught(_ctx: ChannelHandlerContext,
                                 e: ExceptionEvent): Unit = {
      _ctx.sendUpstream(e)
      if (!finished) {
        e.getChannel.close()
        futureQueue.offer(FailedChannelFuture(e.getChannel, e.getCause))
        finished = true
      }
    }
  }
}
