package buffer

import java.nio.{ByteBuffer, ByteOrder}

import org.jetbrains.annotations.Contract

trait ChannelBuffer extends Comparable[ChannelBuffer] {

  @Contract(pure = true) def capacity: Int

  @Contract(pure = true) def order: ByteOrder

  @Contract(pure = true) def readerIndex: Int

  def readerIndex(readerIndex: Int): Unit

  @Contract(pure = true) def writerIndex: Int

  def writeIndex(writerIndex: Int): Unit

  def setIndex(readerIndex: Int, writerIndex: Int): Unit

  @Contract(pure = true) def readableBytes: Int

  @Contract(pure = true) def writableBytes: Int

  @Contract(pure = true) def readable: Boolean

  @Contract(pure = true) def writable: Boolean

  def clear(): Unit

  def markReaderIndex(): Unit

  def resetReaderIndex(): Unit

  def markWriterIndex(): Unit

  def resetWriterIndex(): Unit

  def discardReadBytes(): Unit

  @Contract(pure = true) def getByte(index: Int): Option[Byte]
  @Contract(pure = true) def getShort(index: Int): Option[Short]
  @Contract(pure = true) def getMedium(index: Int): Option[Int]
  @Contract(pure = true) def getInt(index: Int): Option[Int]
  @Contract(pure = true) def getLong(index: Int): Option[Long]

  //noinspection AccessorLikeMethodIsUnit
  def getBytes(index: Int, dst: ChannelBuffer): Unit
  //noinspection AccessorLikeMethodIsUnit
  def getBytes(index: Int, dstIndex: Int, length: Int, dst: ChannelBuffer): Unit
  //noinspection AccessorLikeMethodIsUnit
  def getBytes(index: Int, dst: Array[Byte]): Unit
  //noinspection AccessorLikeMethodIsUnit
  def getBytes(index: Int, dstIndex: Int, length: Int, dst: Array[Byte]): Unit
  //noinspection AccessorLikeMethodIsUnit
  def getBytes(index: Int, dst: ByteBuffer): Unit

  def readByte(): Option[Byte]
  def readShort(): Option[Short]
  def readMedium(): Option[Int]
  def readInt(): Option[Int]
  def readLong(): Option[Long]

  def writeByte(value: Byte): Unit
  def writeShort(value: Short): Unit
  def writeMedium(value: Int): Unit
  def writeInt(value: Int): Unit
  def writeLong(value: Long): Unit
  @Contract(pure = true) def copy: ChannelBuffer
  @Contract(pure = true) def copy(index: Int, length: Int): ChannelBuffer
  @Contract(pure = true) def slice: ChannelBuffer
  @Contract(pure = true) def slice(index: Int,
                                   length: Int): Option[ChannelBuffer]
  @Contract(pure = true) def duplicate: ChannelBuffer

  def indexOf(from: Int, to: Int, value: Byte): Int
//  def indexOf(from:Int,to:Int,indexFinder:ChannelBufferIndexFinder):Int
  def setByte(index: Int, value: Byte): Unit
  def setShort(index: Int, value: Short): Unit
  def setMedium(index: Int, value: Int): Unit
  def setInt(index: Int, value: Int): Unit
  def setLong(index: Int, value: Long): Unit
  def setBytes(index: Int, src: ChannelBuffer): Unit
  def setBytes(index: Int, srcIndex: Int, length: Int, src: ChannelBuffer): Unit
  def setBytes(index: Int, src: Array[Byte]): Unit
  def setBytes(index: Int, srcIndex: Int, length: Int, src: Array[Byte]): Unit
  def setBytes(index: Int, src: ByteBuffer): Unit

  def readBytes(): ChannelBuffer
  def readBytes(length: Int): ChannelBuffer
//  def readBytes(endIndexFinder:ChannelBufferIndexFinder):ChannelBuffer
  def writeBytes(src: ChannelBuffer): Unit
  def writeBytes(srcIndex: Int, length: Int, src: ChannelBuffer): Unit
  def writeBytes(src: Array[Byte]): Unit
  def writeBytes(srcIndex: Int, length: Int, src: Array[Byte]): Unit
  def writeBytes(src: ByteBuffer): Unit

  @Contract(pure = true) def hashCode(): Int
  @Contract(pure = true) def equals(obj: Any): Boolean
  @Contract(pure = true) def compareTo(o: ChannelBuffer): Int
  @Contract(pure = true) def toString: String

}

object ChannelBuffer {
  val EMPTY_BUFFER: ChannelBuffer = BigEndianHeapChannelBuffer(0)
}
