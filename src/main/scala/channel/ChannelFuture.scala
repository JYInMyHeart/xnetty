package channel

import scala.concurrent.duration.TimeUnit

trait ChannelFuture {
  def getChannel: Channel

  def isDone: Boolean
  def isCancelled: Boolean
  def isSuccess: Boolean

  def getCause: Throwable

  def cancel(): Boolean

  def setSuccess(): Unit
  def setFailure(cause: Throwable): Unit

  def addListener(listener: ChannelFutureListener): Unit
  def removeListener(listener: ChannelFutureListener): Unit

  def await(): ChannelFuture
  def awaitUninterruptibly(): ChannelFuture
  def await(timeout: Long, unit: TimeUnit): Boolean
  def await(timeoutMillis: Long): Boolean
  def awaitUninterruptibly(timeout: Long, unit: TimeUnit): Boolean
  def awaitUninterruptibly(timeout: Long): Boolean
}
