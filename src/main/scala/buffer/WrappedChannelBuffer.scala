package buffer

trait WrappedChannelBuffer extends ChannelBuffer {
  def unwrap: ChannelBuffer
}
