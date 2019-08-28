package channel.socket.nio

import java.net.Socket

import channel.socket.DefaultSocketChannelConfig

case class DefaultNioSocketChannelConfig(socket: Socket)
    extends DefaultSocketChannelConfig(socket)
    with NioSocketChannelConfig {
  @volatile private[this] var predictor: ReceiveBufferSizePredictor = _
  @volatile private[this] var readWriteFair: Boolean = _
  @volatile private[this] var writeSpinCount: Int = 16
  override def setOption(key: String, value: Any): Boolean = {
    if (super.setOption(key, value)) return true
    (key, value) match {
      case ("readWriteFair", b: Boolean) =>
        setReadWriteFair(b)
      case ("writeSpinCount", i: Int) =>
        setWriteSpinCount(i)
      case ("receiveBufferSizePredictor",
            receiveBufferSizePredictor: ReceiveBufferSizePredictor) =>
        setReceiveBufferSizePredictor(receiveBufferSizePredictor)
      case _ =>
        return false
    }
    true
  }

  override def getWriteSpinCount: Int = writeSpinCount

  override def setWriteSpinCount(writeSpinCount: Int): Unit = {
    if (writeSpinCount <= 0)
      throw new IllegalArgumentException(
        s"writeSpinCount must be a positive integer.")
    this.writeSpinCount = writeSpinCount
  }

  def setReceiveBufferSizePredictor(
      predictor: ReceiveBufferSizePredictor): Unit = {
    this.predictor = predictor
  }

  def getReceiveBufferSizePredictor: ReceiveBufferSizePredictor = predictor

  override def isReadWriteFair: Boolean = readWriteFair

  override def setReadWriteFair(fair: Boolean): Unit = this.readWriteFair = fair
}
