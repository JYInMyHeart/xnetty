package channel.socket.nio

trait ReceiveBufferSizePredictor {
  def nextReceiveBufferSize: Int
  def previousReceiveBufferSize(previousReceiveBufferSize: Int): Unit
}
