package buffer

import java.nio.ByteBuffer

abstract class AbstractChannelBuffer extends ChannelBuffer {
  override private[this] lazy val hashCode: Int = hashCode()
  private[this] var realWriterIndex: Int = _
  private[this] var realReaderIndex: Int = _
  private[this] var markedReaderIndex: Int = _
  private[this] var markedWriterIndex: Int = _

  override def readerIndex: Int = realReaderIndex

  override def readerIndex(readerIndex: Int): Unit = {
    if (readerIndex < 0 || readerIndex > realWriterIndex)
      throw new IndexOutOfBoundsException
    realReaderIndex = readerIndex
  }
  override def writerIndex: Int = realWriterIndex

  override def writeIndex(writerIndex: Int): Unit = {
    if (writerIndex < realReaderIndex || writerIndex > capacity)
      throw new IndexOutOfBoundsException
    realWriterIndex = writerIndex
  }

  override def setIndex(readerIndex: Int, writerIndex: Int): Unit = {
    if (readerIndex < 0
        || readerIndex > realWriterIndex
        || writerIndex < realReaderIndex
        || writerIndex > capacity)
      throw new IndexOutOfBoundsException
    realWriterIndex = writerIndex
    realReaderIndex = readerIndex
  }

  override def clear(): Unit = {
    realWriterIndex = 0
    realReaderIndex = 0
  }

  override def readable: Boolean = readableBytes > 0

  override def writable: Boolean = writableBytes > 0

  override def readableBytes: Int = realWriterIndex - realReaderIndex

  override def writableBytes: Int = capacity - realWriterIndex

  override def markReaderIndex(): Unit = markedReaderIndex = realReaderIndex

  override def resetReaderIndex(): Unit = realReaderIndex = markedReaderIndex

  override def markWriterIndex(): Unit = markedWriterIndex = realWriterIndex

  override def resetWriterIndex(): Unit = realWriterIndex = markedWriterIndex

  override def discardReadBytes(): Unit = {
    realReaderIndex match {
      case 0 =>
      case _ =>
        this =
          setBytes(0, realReaderIndex, realWriterIndex - realReaderIndex, this)
        realWriterIndex -= realReaderIndex
        markedReaderIndex = math.max(markedReaderIndex - realReaderIndex, 0)
        markedWriterIndex = math.max(markedWriterIndex - realReaderIndex, 0)
        realReaderIndex = 0
    }
  }

  override def getBytes(index: Int, dst: Array[Byte]): Unit = {
    getBytes(index, 0, dst.length, dst)
  }

  override def getBytes(index: Int, dst: ChannelBuffer): Unit = {
    getBytes(index, dst.readerIndex, dst.readableBytes, dst)
  }

  override def setBytes(index: Int, src: Array[Byte]): Unit = {
    setBytes(index, 0, src.length, src)
  }

  override def setBytes(index: Int, src: ChannelBuffer): Unit = {
    setBytes(index, src.readerIndex, src.readableBytes, src)
  }

  override def readByte(): Option[Byte] = {
    if (realReaderIndex == realWriterIndex)
      throw new IndexOutOfBoundsException
    val b = getByte(realReaderIndex)
    realReaderIndex += 1
    b
  }
  override def readShort(): Option[Short] = {
    checkReadableBytes(2)
    val v = getShort(realReaderIndex)
    realReaderIndex += 2
    v
  }

  override def readMedium(): Option[Int] = {
    checkReadableBytes(3)
    val v = getMedium(realReaderIndex)
    realReaderIndex += 3
    v
  }

  override def readInt(): Option[Int] = {
    checkReadableBytes(4)
    val v = getInt(realReaderIndex)
    realReaderIndex += 4
    v
  }

  override def readLong(): Option[Long] = {
    checkReadableBytes(8)
    val v = getLong(realReaderIndex)
    realReaderIndex += 8
    v
  }

  override def readBytes(): ChannelBuffer = readBytes(readableBytes)

  override def readBytes(length: Int): ChannelBuffer = {
    checkReadableBytes(length)
    if (length == 0)
      ChannelBuffer.EMPTY_BUFFER
    val buf = ChannelBuffers.buffer(length)
    buf.writeBytes(realReaderIndex, length, this)
    realReaderIndex += length
    buf
  }

  override def writeByte(value: Byte): Unit = {
    setByte(realWriterIndex, value)
    realWriterIndex += 1
  }

  override def writeShort(value: Short): Unit = {
    setShort(realWriterIndex, value)
    realWriterIndex += 2
  }

  override def writeMedium(value: Int): Unit = {
    setMedium(realWriterIndex, value)
    realWriterIndex += 3
  }

  override def writeInt(value: Int): Unit = {
    setInt(realWriterIndex, value)
    realWriterIndex += 4
  }

  override def writeLong(value: Long): Unit = {
    setLong(realWriterIndex, value)
    realWriterIndex += 8
  }

  override def writeBytes(src: ChannelBuffer): Unit =
    writeBytes(src, src.readableBytes)

  def writeBytes(src: ChannelBuffer, length: Int): Unit = {
    writeBytes(src.readerIndex, length, src)
    src.readerIndex(src.readerIndex + length)
  }
  override def writeBytes(srcIndex: Int,
                          length: Int,
                          src: ChannelBuffer): Unit = {
    setBytes(realWriterIndex, srcIndex, length, src)
    realWriterIndex += length
  }

  override def writeBytes(src: Array[Byte]): Unit =
    writeBytes(0, src.length, src)

  override def writeBytes(srcIndex: Int,
                          length: Int,
                          src: Array[Byte]): Unit = {
    setBytes(realWriterIndex, srcIndex, length, src)
    realWriterIndex += length
  }

  override def writeBytes(src: ByteBuffer): Unit = {
    val length = src.remaining()
    setBytes(realReaderIndex, src)
    realWriterIndex += length
  }

  override def compareTo(o: ChannelBuffer): Int =
    ChannelBuffers.compare(this, o)

  override def copy: ChannelBuffer = copy(realReaderIndex, readableBytes)

  override def slice: ChannelBuffer =
    slice(realReaderIndex, readableBytes) match {
      case Some(value) => value
      case None        => ChannelBuffer.EMPTY_BUFFER
    }

  override def hashCode(): Int = {
    ChannelBuffers.hashCode(this)
  }

  protected def checkReadableBytes(minimumReadableBytes: Int): Unit = {
    if (readableBytes < minimumReadableBytes)
      throw new IndexOutOfBoundsException
  }

}
