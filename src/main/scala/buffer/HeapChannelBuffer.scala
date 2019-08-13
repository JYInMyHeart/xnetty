package buffer
import java.nio.ByteBuffer

abstract class HeapChannelBuffer(protected val array: Array[Byte])
    extends AbstractChannelBuffer {
  def this(length: Int) = {
    this(Array.ofDim[Byte](length))
  }
  def this(array: Array[Byte], readerIndex: Int, writerIndex: Int) = {
    this(array)
    setIndex(readerIndex, writerIndex)
  }

  override def capacity: Int = array.length

  override def getByte(index: Int): Option[Byte] = Some(array(index))

  override def getBytes(index: Int,
                        dstIndex: Int,
                        length: Int,
                        dst: ChannelBuffer): Unit = {
    dst match {
      case buffer: HeapChannelBuffer =>
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
      case buffer: HeapChannelBuffer =>
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

  override def slice(index: Int, length: Int): Option[ChannelBuffer] = {
    Some(index match {
      case 0 =>
        length match {
          case x if x == this.array.length =>
            duplicate
          case _ =>
            TruncatedChannelBuffer(this, length)
        }
      case _ =>
        SlicedChannelBuffer(this, index, length)
    })
  }

  override def toByteBuffer(index: Int, length: Int): ByteBuffer =
    ByteBuffer.wrap(array, index, length)

}
