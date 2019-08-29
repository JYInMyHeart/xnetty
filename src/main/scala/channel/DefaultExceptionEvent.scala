package channel

case class DefaultExceptionEvent(channel: Channel,
                                 future: ChannelFuture,
                                 cause: Throwable)
    extends DefaultChannelEvent(channel, future)
    with ExceptionEvent {
  override def getCause: Throwable = cause

  override def toString: String =
    s"${super.toString} - (cause: ${cause.getClass.getSimpleName})"
}
