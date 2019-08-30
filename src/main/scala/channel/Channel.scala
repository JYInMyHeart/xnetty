package channel

import java.net.SocketAddress
import java.util.UUID

trait Channel {
  def getId(): UUID
  def getFactory: ChannelFactory
  def getParent: Channel
  def getConfig: ChannelConfig
  def getPipeline: ChannelPipeline

  def isOpen: Boolean
  def isBound: Boolean
  def isConnected: Boolean

  def getLocalAddress: SocketAddress
  def getRemoteAddress: SocketAddress

  def write(message: => Any): ChannelFuture
  def write(message: => Any, remoteAddress: SocketAddress): ChannelFuture

  def bind(localAddress: SocketAddress): ChannelFuture
  def connect(remoteAddress: SocketAddress): ChannelFuture
  def disconnect(): ChannelFuture
  def close(): ChannelFuture

  def getInterestOps: Int
  def isReadable: Boolean
  def isWritable: Boolean
  def setInterestOps(interestOps: Int): Option[ChannelFuture]
  def setReadable(readable: Boolean): ChannelFuture
}
object Channel {
  val OP_NONE: Int = 0
  val OP_READ: Int = 1
  val OP_WRITE: Int = 4
  val OP_READ_WRITE: Int = OP_READ | OP_WRITE
}
