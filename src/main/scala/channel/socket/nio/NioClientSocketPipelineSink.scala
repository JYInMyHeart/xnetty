package channel.socket.nio

import java.net.SocketAddress
import java.nio.channels.Selector
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

import channel.{
  AbstractChannelSink,
  ChannelEvent,
  ChannelFuture,
  ChannelPipeline,
  ChannelState,
  ChannelStateEvent,
  Channels,
  MessageEvent
}

case class NioClientSocketPipelineSink(
    bossExecutor: Executor,
    var workers: Array[NioWorker],
    workerIndex: AtomicInteger = new AtomicInteger(),
    nextId: AtomicInteger = new AtomicInteger())
    extends AbstractChannelSink {
  lazy val boss: Boss = Boss()
  lazy val id = nextId.incrementAndGet()
  def this(bossExecutor: Executor, workerExecutor: Executor, workerCount: Int) {
    this(bossExecutor, Array.ofDim[NioWorker](0))
    workers = (for (i <- 0 until workerCount)
      yield NioWorker(id, i + 1, workerExecutor)).toArray
  }

  sealed case class Boss() extends Runnable {
    val started: AtomicInteger = new AtomicInteger()
    @volatile var selector: Selector = _

    def register(channel: NioSocketChannel): Unit = {}

    override def run(): Unit = {}
  }

  def bind(channel: NioClientSocketChannel,
           future: ChannelFuture,
           address: SocketAddress): Unit = {
    try {
      channel.socketChannel.socket().bind(address)
      channel.boundManually = true
      future.setSuccess()
      Channels.fireChannelBound(channel, channel.getLocalAddress)
    } catch {
      case t: Throwable =>
        future.setFailure(t)
        Channels.fireExceptionCaught(channel, t)
    }
  }

  def nextWorker() =
    workers(math.abs(workerIndex.getAndIncrement() % workers.length))

  def connect(channel: NioClientSocketChannel,
              future: ChannelFuture,
              address: SocketAddress): Unit = {
    try {
      if (channel.socketChannel.connect(address)) {
        val worker = nextWorker()
        channel.setWorker(worker)
        worker.register(channel, future)
      } else {
        future.addListener(_future => if (_future.isCancelled) channel.close())
        channel.connectFuture = future
        boss.register(channel)
      }
    } catch {
      case t: Throwable =>
        Channels.fireExceptionCaught(channel, t)
    }
  }

  override def eventSunk(pipeline: ChannelPipeline,
                         event: ChannelEvent): Unit = {
    event match {
      case e: ChannelStateEvent =>
        val channel = e.getChannel.asInstanceOf[NioClientSocketChannel]
        val future = e.getFuture
        val state = e.getState
        val value = e.getValue

        (state, value) match {
          case (ChannelState.OPEN, b: Boolean) =>
            if (!b) NioWorker.close(channel, future)
          case (ChannelState.BOUND, v @ _) =>
            if (v != null)
              bind(channel, future, v.asInstanceOf[SocketAddress])
            else
              NioWorker.close(channel, future)
          case (ChannelState.CONNECTED, v @ _) =>
            if (v != null)
              connect(channel, future, v.asInstanceOf[SocketAddress])
            else
              NioWorker.close(channel, future)
          case (ChannelState.INTEREST_OPS, i: Int) =>
            NioWorker.setInterestOps(channel, future, i)
        }
      case e: MessageEvent =>
        val channel = e.getChannel.asInstanceOf[NioSocketChannel]
        channel.writeBuffer offer e
        NioWorker.write(channel)

    }
  }
}
