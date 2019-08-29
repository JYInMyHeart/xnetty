package buffer
import java.nio.channels.GatheringByteChannel
import java.nio.{ByteBuffer, ByteOrder}

case class TruncatedChannelBuffer(
    buffer: ChannelBuffer,
    length: Int
) extends AbstractChannelBuffer
    with WrappedChannelBuffer {

  def this(buffer: ChannelBuffer, length: Int, flag: Boolean = true) {
    this(buffer, length)
    writeIndex(length)
  }

  override def unwrap: ChannelBuffer = buffer

  override def capacity: Int = length

  override def order: ByteOrder = buffer.order

  override def getByte(index: Int): Option[Byte] =
    checkIndex(index) match {
      case None      => buffer.getByte(index)
      case Some(msg) => None
    }

  override def getShort(index: Int): Option[Short] =
    checkIndex(index, 2) match {
      case None      => buffer.getShort(index)
      case Some(msg) => None
    }

  override def getMedium(index: Int): Option[Int] =
    checkIndex(index, 3) match {
      case None      => buffer.getMedium(index)
      case Some(msg) => None
    }

  override def getInt(index: Int): Option[Int] =
    checkIndex(index, 4) match {
      case None      => buffer.getInt(index)
      case Some(msg) => None
    }

  override def getLong(index: Int): Option[Long] =
    checkIndex(index, 8) match {
      case None      => buffer.getLong(index)
      case Some(msg) => None
    }

  override def getBytes(index: Int,
                        dstIndex: Int,
                        length: Int,
                        dst: ChannelBuffer): Unit =
    checkIndex(index, length) match {
      case None      => buffer.getBytes(index, dstIndex, length, dst)
      case Some(msg) => println(msg)
    }

  override def getBytes(index: Int,
                        dstIndex: Int,
                        length: Int,
                        dst: Array[Byte]): Unit =
    checkIndex(index, length) match {
      case None      => buffer.getBytes(index, dstIndex, length, dst)
      case Some(msg) => println(msg)
    }

  override def getBytes(index: Int, dst: ByteBuffer): Unit =
    checkIndex(index, dst.remaining()) match {
      case None      => buffer.getBytes(index, dst)
      case Some(msg) => println(msg)
    }

  override def getBytes(index: Int,
                        out: GatheringByteChannel,
                        length: Int): Int = {
    checkIndex(index, length) match {
      case None    => buffer.getBytes(index, out, length)
      case Some(_) => -1
    }
  }

  override def slice(index: Int, length: Int): Option[ChannelBuffer] =
    checkIndex(index, 8) match {
      case None      => buffer.slice(index, length)
      case Some(msg) => None
    }

  override def duplicate: ChannelBuffer = TruncatedChannelBuffer(buffer, length)

  override def copy(index: Int, length: Int): ChannelBuffer =
    buffer.copy(index, length)

  override def indexOf(from: Int, to: Int, value: Byte): Int = ???

  override def setByte(index: Int, value: Byte): Unit =
    checkIndex(index) match {
      case None      => buffer.setByte(index, value)
      case Some(msg) => println(msg)
    }

  override def setShort(index: Int, value: Short): Unit =
    checkIndex(index, 2) match {
      case None      => buffer.setShort(index, value)
      case Some(msg) => println(msg)
    }

  override def setMedium(index: Int, value: Int): Unit =
    checkIndex(index, 3) match {
      case None      => buffer.setMedium(index, value)
      case Some(msg) => println(msg)
    }

  override def setInt(index: Int, value: Int): Unit =
    checkIndex(index, 4) match {
      case None      => buffer.setInt(index, value)
      case Some(msg) => println(msg)
    }

  override def setLong(index: Int, value: Long): Unit =
    checkIndex(index, 8) match {
      case None      => buffer.setLong(index, value)
      case Some(msg) => println(msg)
    }

  override def setBytes(index: Int,
                        srcIndex: Int,
                        length: Int,
                        src: ChannelBuffer): Unit =
    checkIndex(index, length) match {
      case None      => buffer.setBytes(index, srcIndex, length, src)
      case Some(msg) => println(msg)
    }

  override def setBytes(index: Int,
                        srcIndex: Int,
                        length: Int,
                        src: Array[Byte]): Unit =
    checkIndex(index, length) match {
      case None      => buffer.setBytes(index, srcIndex, length, src)
      case Some(msg) => println(msg)
    }

  override def setBytes(index: Int, src: ByteBuffer): Unit =
    checkIndex(index, src.remaining()) match {
      case None      => buffer.setBytes(index, src)
      case Some(msg) => println(msg)
    }

  override def toByteBuffer(index: Int, length: Int): ByteBuffer =
    checkIndex(index, length) match {
      case None      => buffer.toByteBuffer(index, length)
      case Some(msg) => ByteBuffer.allocate(0)
    }

  private[this] def checkIndex(index: Int): Option[String] = {
    if (length < 0)
      return Some(s"length is negative: $length")
    if (index + length < capacity)
      return Some(s"index out of bound!")
    None
  }

  private[this] def checkIndex(index: Int, length: Int): Option[String] = {
    if (index < 0 || index > capacity)
      return Some(s"index out of bound!")
    None
  }

}
