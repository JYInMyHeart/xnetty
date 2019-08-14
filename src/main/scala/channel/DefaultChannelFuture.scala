package channel
import scala.concurrent.duration.TimeUnit

case class DefaultChannelFuture(channel: Channel, cancellable: Boolean)
    extends ChannelFuture {
  override def getChannel: Channel = ???

  override def isDone: Boolean = ???

  override def isCancelled: Boolean = ???

  override def isSuccess: Boolean = ???

  override def getCause: Throwable = ???

  override def cancel(): Boolean = ???

  override def setSuccess(): Unit = ???

  override def setFailure(cause: Throwable): Unit = ???

  override def addListener(listener: ChannelFutureListener): Unit = ???

  override def removeListener(listener: ChannelFutureListener): Unit = ???

  override def await(): ChannelFuture = ???

  override def awaitUninterruptibly(): ChannelFuture = ???

  override def await(timeout: Long, unit: TimeUnit): Boolean = ???

  override def await(timeoutMillis: Long): Boolean = ???

  override def awaitUninterruptibly(timeout: Long, unit: TimeUnit): Boolean =
    ???

  override def awaitUninterruptibly(timeout: Long): Boolean = ???
}
