package channel

class ChannelException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {}
case class ChannelPipelineException(message: String, cause: Throwable)
    extends ChannelException(message, cause) {
  def this(cause: Throwable) = {
    this(null, cause)
  }
}
