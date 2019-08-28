package channel.socket.nio

import channel.socket.SocketChannelConfig

trait NioSocketChannelConfig extends SocketChannelConfig {
  def getWriteSpinCount: Int
  def setWriteSpinCount(writeSpinCount: Int): Unit

  def getReceiveBufferSizePredictor: ReceiveBufferSizePredictor

  def setReceiveBufferSizePredictor(
      receiveBufferSize: ReceiveBufferSizePredictor): Unit

  def isReadWriteFair: Boolean
  def setReadWriteFair(fair: Boolean): Unit
}
