package example

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

object Echo {

  object EchoClient {
    def main(args: Array[String]): Unit = {
      val host = "localhost"
      val port = 9999
      val channelFactory = new channel.socket.nio.NioClientSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool())
      val bootstrap = channel.ClientBootstrap(channelFactory)
      val handler = new EchoHandler(8)
      bootstrap.channelPipeline.addLast("handler", handler)
      bootstrap.setOption("tcpNoDelay", true)
      bootstrap.setOption("keepAlive", true)
      bootstrap.connect(new InetSocketAddress(host, port), null)
      ThroughputMonitor(handler).start()
    }
  }

  case class ThroughputMonitor(handler: EchoHandler) extends Thread {
    override def run(): Unit = {
      var oldCounter = handler.transferredBytes.get()
      var startTime = System.currentTimeMillis()
      while (true) {
        try {
          Thread.sleep(3000)
        } catch {
          case e: InterruptedException =>
            println(e)
        }

        val endTime = System.currentTimeMillis()
        val newCounter = handler.transferredBytes.get()
        printf(
          "%4.3f MiB/s%n",
          (newCounter - oldCounter) * 1000 / (endTime - startTime) / 1048576.0)
        oldCounter = newCounter
        startTime = endTime
      }
    }
  }
  import buffer.{ChannelBuffer, ChannelBuffers}
  case class EchoHandler(firstMessageSize: Int,
                         firstMessage: ChannelBuffer,
                         transferredBytes: AtomicLong)
      extends channel.SimpleChannelHandler {
    def this(firstMessageSize: Int) = {
      this(firstMessageSize,
           ChannelBuffers.buffer(firstMessageSize),
           new AtomicLong())
      for (i <- 0 until firstMessage.capacity) firstMessage.writeByte(i.toByte)
    }

    override def handleUpstream(_ctx: channel.ChannelHandlerContext,
                                event: channel.ChannelEvent): Unit = {
      if (event.isInstanceOf[channel.ChannelStateEvent])
        println(event.toString)
      super.handleUpstream(_ctx, event)
    }

    override def channelConnected(context: channel.ChannelHandlerContext,
                                  event: channel.ChannelStateEvent): Unit = {
      event.getChannel.write(firstMessage)
    }

    override def messageReceived(context: channel.ChannelHandlerContext,
                                 event: channel.MessageEvent): Unit = {
      transferredBytes.addAndGet(
        event.getMessage.asInstanceOf[ChannelBuffer].readableBytes)
      event.getChannel.write(event.getMessage)
    }

    override def exceptionCaught(_ctx: channel.ChannelHandlerContext,
                                 e: channel.ExceptionEvent): Unit = {
      println(s"Unexpected exception from downstream ${e.getCause}")
      e.getChannel.close()
    }
  }
}
