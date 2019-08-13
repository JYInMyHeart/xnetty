package buffer
import java.nio.ByteBuffer

abstract class HeapChannelBUffer(protected val array: Array[Byte])
    extends AbstractChannelBuffer {
  def this(length: Int) = {
    this(Array.ofDim(length))
  }
  def this(array: Array[Byte], readerIndex: Int, writerIndex: Int) = {
    this(array)
    setIndex(readerIndex, writerIndex)
  }

  override def capacity: Int = array.length

  override def getByte(index: Int): Byte = array(index)

  override def getBytes(index: Int,
                        dstIndex: Int,
                        length: Int,
                        dst: ChannelBuffer): Unit = {
    dst match {
      case buffer: HeapChannelBUffer =>
        getBytes(index, dstIndex, length, buffer.array)
      case _ =>
        dst.setBytes(dstIndex, index, length, array)
    }
  }

  override def getBytes(index: Int,
                        dstIndex: Int,
                        length: Int,
                        dst: Array[Byte]): Unit =
    Array.copy(array, index, dst, dstIndex, length)

  override def getBytes(index: Int, dst: ByteBuffer): Unit =
    dst.put(array, index, math.min(capacity - index, dst.remaining()))

  override def setByte(index: Int, value: Byte): Unit =
    array(index) = value

  override def setBytes(index: Int,
                        srcIndex: Int,
                        length: Int,
                        src: ChannelBuffer): Unit =
    src match {
      case buffer: HeapChannelBUffer =>
        setBytes(index, srcIndex, length, buffer.array)
      case _ =>
        src.getBytes(srcIndex, index, length, array)
    }

  override def setBytes(index: Int,
                        srcIndex: Int,
                        length: Int,
                        src: Array[Byte]): Unit =
    Array.copy(src, srcIndex, array, index, length)

  override def setBytes(index: Int, src: ByteBuffer): Unit =
    src.get(array, index, src.remaining())

  override def slice(index: Int, length: Int): ChannelBuffer = {
    index match {
      case 0 =>
        length match {
          case array.length =>
            duplicate
          case _ =>
            TruncatedChannelBuffer(this, length)
        }
      case _ =>
        SliceChannelBuffer(this, index, length)
    }
  }

  def toByteBuffer(index: Int, length: Int) =
    ByteBuffer.wrap(array, index, length)

}
