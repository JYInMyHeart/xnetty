package buffer
import java.nio.ByteOrder

case class BigEndianHeapChannelBuffer(length: Int)
    extends HeapChannelBuffer(length) {
  override def order: ByteOrder = ???

  override def getShort(index: Int): Option[Short] = ???

  override def getMedium(index: Int): Option[Int] = ???

  override def getInt(index: Int): Option[Int] = ???

  override def getLong(index: Int): Option[Long] = ???

  override def copy(index: Int, length: Int): ChannelBuffer = ???

  override def duplicate: ChannelBuffer = ???

  override def indexOf(from: Int, to: Int, value: Byte): Int = ???

  override def setShort(index: Int, value: Short): Unit = ???

  override def setMedium(index: Int, value: Int): Unit = ???

  override def setInt(index: Int, value: Int): Unit = ???

  override def setLong(index: Int, value: Long): Unit = ???
}
