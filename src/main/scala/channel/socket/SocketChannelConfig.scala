package channel.socket

import channel.ChannelConfig

trait SocketChannelConfig extends ChannelConfig {
  def isTcpNoDelay: Boolean
  def setTcpNoDelay(tcpNoDelay: Boolean): Unit
  def getSoLinger: Int
  def setSoLinger(soLinger: Int): Unit
  def getSendBufferSize: Int
  def setSendBufferSize(sendBufferSize: Int): Unit
  def getReceiveBufferSize: Int
  def setReceiveBufferSize(receiveBufferSize: Int): Unit
  def isKeepAlive: Boolean
  def setKeepAlive(keepAlive: Boolean): Unit
  def getTrafficClass: Int
  def setTrafficCLass(trafficClass: Int): Unit
  def isReuseAddress: Boolean
  def setReuseAddress(reuseAddress: Boolean): Unit
  def setPerformancePreferences(connectionTime: Int,
                                latency: Int,
                                bandwidth: Int): Unit
}
