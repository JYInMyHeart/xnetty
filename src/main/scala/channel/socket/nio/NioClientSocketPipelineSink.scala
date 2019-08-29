package channel.socket.nio

import java.net.SocketAddress
import java.nio.channels.Selector
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

import channel.{
  AbstractChannelSink,
  ChannelEvent,
  ChannelPipeline,
  ChannelState,
  ChannelStateEvent,
  MessageEvent
}

case class NioClientSocketPipelineSink(bossExecutor: Executor,
                                       workers: Array[NioWorker],
                                       workerIndex: AtomicInteger,
                                       nextId: AtomicInteger)
    extends AbstractChannelSink {
  lazy val boss: Boss = Boss()
  val id = nextId.incrementAndGet()
  def this(bossExecutor: Executor, workerExecutor: Executor, workerCount: Int) {
    this(bossExecutor,
         (for (i <- 0 until workerCount)
           yield NioWorker(id, i + 1, workerExecutor)).toArray,
         new AtomicInteger(),
         new AtomicInteger())
  }

  sealed case class Boss() extends Runnable {
    val started: AtomicInteger = new AtomicInteger()
    @volatile var selector: Selector = _

    def register(channel: NioSocketChannel): Unit = {}

    override def run(): Unit = {}
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
