package channel

case class FailedChannelFuture(channel: Channel, cause: Throwable)
    extends CompleteChannelFuture(channel) {
  override def isSuccess: Boolean = false

  override def getCause: Throwable = cause
}
