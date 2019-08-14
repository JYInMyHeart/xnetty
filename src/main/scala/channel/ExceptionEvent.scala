package channel

trait ExceptionEvent extends ChannelEvent {
  def getCause: Throwable
}
