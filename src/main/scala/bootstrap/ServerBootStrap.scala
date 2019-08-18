package bootstrap
import java.net.SocketAddress
import java.util.concurrent.{BlockingQueue, TimeUnit}

import channel.{
  Channel,
  ChannelException,
  ChannelFactory,
  ChannelFuture,
  ChannelHandler,
  ChannelPipelineFactory,
  Channels
}

import scala.sys.process.processInternal.LinkedBlockingQueue

case class ServerBootStrap(@volatile channelFactory: ChannelFactory)
    extends Bootstrap(channelFactory) {
  @volatile var parentHandler: ChannelHandler = _

  def bind(): Channel = {
    val localAddress = options("localAddress")
    if (localAddress == null)
      throw new IllegalArgumentException("localAddress option is not set!")
    bind(localAddress)
  }

  def bind(address: SocketAddress): Channel = {
    val futureQueue = new LinkedBlockingQueue[ChannelFuture]()
    val _pipeline = Channels.pipeline
    _pipeline.addLast("binder", new Binder(address, futureQueue))
    val parentHandler = getParentHandler
    if (parentHandler != null)
      _pipeline.addLast("userHandler", parentHandler)
    val channel = getFactory.newChannel(_pipeline)
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
      throw new ChannelException(s"Failed to bind to ${address}",
                                 _future.getCause)
    }
    channel
  }

  private sealed class Binder(address: SocketAddress,
                              futures: BlockingQueue[ChannelFuture])
      extends SimpleChannelHandler() {}
}
