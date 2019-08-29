package channel

abstract class AbstractChannelSink extends ChannelSink {
  override def exceptionCaught(pipeline: ChannelPipeline,
                               event: ChannelEvent,
                               cause: ChannelPipelineException): Unit = {
    var actualCause = cause.getCause
    if (actualCause != null)
      actualCause = cause

    Channels.fireExceptionCaught(event.getChannel, actualCause)
  }
}
