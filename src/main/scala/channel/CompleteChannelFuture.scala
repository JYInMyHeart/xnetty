package channel
import scala.concurrent.duration.TimeUnit

abstract class CompleteChannelFuture(channel: Channel) extends ChannelFuture {
  override def getChannel: Channel = channel

  override def isCancelled: Boolean = false

  override def isDone: Boolean = true

  override def cancel(): Boolean = false

  override def setSuccess(): Unit = {}

  override def setFailure(cause: Throwable): Unit = {}

  override def addListener(listener: ChannelFutureListener): Unit = {
    listener.operationComplete(this)
  }

  override def removeListener(listener: ChannelFutureListener): Unit = {}

  override def await(): ChannelFuture = this

  override def awaitUninterruptibly(): ChannelFuture = this

  override def await(timeout: Long, unit: TimeUnit): Boolean = true

  override def await(timeoutMillis: Long): Boolean = true

  override def awaitUninterruptibly(timeout: Long, unit: TimeUnit): Boolean =
    true

  override def awaitUninterruptibly(timeout: Long): Boolean = true
}
