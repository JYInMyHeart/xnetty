package buffer
import java.nio.channels.GatheringByteChannel
import java.nio.{ByteBuffer, ByteOrder}

case class LittleEndianHeapChannelBuffer(length: Int)
    extends HeapChannelBuffer(length) {

  def this(array: Array[Byte]) {
    this(array.length)
  }
  override def capacity: Int = ???

  override def order: ByteOrder = ???

  override def getByte(index: Int): Option[Byte] = ???

  override def getShort(index: Int): Option[Short] = ???

  override def getMedium(index: Int): Option[Int] = ???

  override def getInt(index: Int): Option[Int] = ???

  override def getLong(index: Int): Option[Long] = ???

  override def getBytes(index: Int,
                        dstIndex: Int,
                        length: Int,
                        dst: ChannelBuffer): Unit = ???

  override def getBytes(index: Int,
                        dstIndex: Int,
                        length: Int,
                        dst: Array[Byte]): Unit = ???

  override def getBytes(index: Int, dst: ByteBuffer): Unit = ???

  override def copy(index: Int, length: Int): ChannelBuffer = ???

  override def slice(index: Int, length: Int): Option[ChannelBuffer] = ???

  override def duplicate: ChannelBuffer = ???

  override def indexOf(from: Int, to: Int, value: Byte): Int = ???

  override def setByte(index: Int, value: Byte): Unit = ???

  override def setShort(index: Int, value: Short): Unit = ???

  override def setMedium(index: Int, value: Int): Unit = ???

  override def setInt(index: Int, value: Int): Unit = ???

  override def setLong(index: Int, value: Long): Unit = ???

  override def setBytes(index: Int,
                        srcIndex: Int,
                        length: Int,
                        src: ChannelBuffer): Unit = ???

  override def setBytes(index: Int,
                        srcIndex: Int,
                        length: Int,
                        src: Array[Byte]): Unit = ???

  override def setBytes(index: Int, src: ByteBuffer): Unit = ???

  override def toByteBuffer(index: Int, length: Int): ByteBuffer = ???

  override def getBytes(index: Int,
                        out: GatheringByteChannel,
                        length: Int): Int = ???
}
