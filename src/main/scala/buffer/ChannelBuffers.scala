package buffer

import java.nio.ByteOrder

object ChannelBuffers {
  val BIG_ENDIAN: ByteOrder = ByteOrder.BIG_ENDIAN
  val LITTLE_ENDIAN: ByteOrder = ByteOrder.LITTLE_ENDIAN

  def buffer(length: Int): ChannelBuffer = buffer(BIG_ENDIAN, length)

  def buffer(order: ByteOrder, length: Int): ChannelBuffer = {
    if (length == 0)
      ChannelBuffer.EMPTY_BUFFER
    order match {
      case BIG_ENDIAN =>
        BigEndianHeapChannelBuffer(length)
      case LITTLE_ENDIAN =>
        LittleEndianHeapChannelBuffer(length)
      case _ =>
        throw new NullPointerException("endianness")
    }
  }

  def hashCode(channelBuffer: ChannelBuffer): Int = {
    val aLen = channelBuffer.readableBytes
    val intCount = aLen >>> 2
    val byteCount = aLen & 3

    var hashCode = 1
    var arrayIndex = channelBuffer.readerIndex
    for (_ <- intCount until 0 by -1) {
      hashCode = 31 * hashCode + channelBuffer.getInt(arrayIndex).getOrElse(0)
      arrayIndex += 4
    }
    for (_ <- byteCount until 0 by -1) {
      hashCode = 31 * hashCode + channelBuffer.getByte(arrayIndex).getOrElse(0)
      arrayIndex += 1
    }
    if (hashCode == 0) hashCode = 1
    hashCode
  }

  def equals(bufferA: ChannelBuffer, bufferB: ChannelBuffer): Boolean = {
    val aLen = bufferA.readableBytes
    if (aLen != bufferB.readableBytes)
      return false

    val longCount = aLen >>> 3
    val byteCount = aLen & 7

    var aIndex = bufferA.readerIndex
    var bIndex = bufferB.readerIndex

    for (_ <- longCount until 0 by -1) {
      if (bufferA.getLong(aIndex) != bufferB.getLong(bIndex)) {
        return false
      }
      aIndex += 8
      bIndex += 8
    }

    for (_ <- byteCount until 0 by -1) {
      if (bufferA.getByte(aIndex) != bufferB.getByte(aIndex))
        return false
      aIndex += 1
      bIndex += 1
    }
    true
  }

  def compare(bufferA: ChannelBuffer, bufferB: ChannelBuffer): Int = {
    val aLen = bufferA.readableBytes
    val bLen = bufferB.readableBytes
    val len = math.min(aLen, bLen)
    val longCount = len >>> 3
    val byteCount = len & 7

    var aIndex = bufferA.readerIndex
    var bIndex = bufferB.readerIndex

    def compareAfter(c: Option[Int], offset: Int): Option[Int] = {
      c match {
        case None =>
          aIndex += offset
          bIndex += offset
          None
        case Some(value) =>
          value match {
            case 0 =>
              aIndex += offset
              bIndex += offset
              None
            case other =>
              Some(other)
          }
      }
    }

    for (_ <- longCount until 0 by -1) {
      val va = bufferA.getLong(aIndex)
      val vb = bufferB.getLong(bIndex)
      compareAfter(compareOption(va, vb), 8) match {
        case Some(value) => return value
        case None        =>
      }
    }

    for (_ <- byteCount until 0 by -1) {
      val va = bufferA.getByte(aIndex)
      val vb = bufferB.getByte(bIndex)
      compareAfter(compareOption(va, vb), 1) match {
        case Some(value) => return value
        case None        =>
      }
    }
    aLen - bLen
  }

  private def compareOption[A <: Comparable[A]](o1: Option[A],
                                                o2: Option[A]): Option[Int] =
    for (v1 <- o1; v2 <- o2) yield v1.compareTo(v2)
}
