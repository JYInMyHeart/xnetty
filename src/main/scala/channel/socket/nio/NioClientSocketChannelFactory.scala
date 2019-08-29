package channel.socket.nio

import java.util.concurrent.Executor

import channel.{ChannelPipeline, ChannelSink}
import channel.socket.{ClientSocketChannelFactory, SocketChannel}

case class NioClientSocketChannelFactory(bossExecutor: Executor,
                                         workerExecutor: Executor,
                                         workerCount: Int,
                                         sink: ChannelSink)
    extends ClientSocketChannelFactory() {
  def this(bossExecutor: Executor, workerExecutor: Executor) {
    this(bossExecutor,
         workerExecutor,
         Runtime.getRuntime.availableProcessors(),
         new NioClientSocketPipelineSink(bossExecutor,
                                         workerExecutor,
                                         workerCount))
  }

  override def newChannel(pipeline: ChannelPipeline): SocketChannel =
    NioClientSocketChannel(this, pipeline, sink)
}
