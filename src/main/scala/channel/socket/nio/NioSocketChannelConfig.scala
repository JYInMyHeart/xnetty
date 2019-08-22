package channel.socket.nio

import channel.socket.SocketChannelConfig

trait NioSocketChannelConfig extends SocketChannelConfig {
  def getWriteSpinCount: Int
  def setWriteSpinCount(writeSpinCount: Int): Unit

  override def getReceiveBufferSize: ReceiveBufferSizePredictor

  override def setReceiveBufferSize(
      receiveBufferSize: ReceiveBufferSizePredictor): Unit

  def isReadWriteFair: Boolean
  def setReadWriteFair(fair: Boolean): Unit
}
