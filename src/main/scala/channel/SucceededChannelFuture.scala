package channel

case class SucceededChannelFuture(channel: Channel)
    extends CompleteChannelFuture(channel) {

  override def isSuccess: Boolean = true

  override def getCause: Throwable = null
}
