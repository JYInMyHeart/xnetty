package channel

trait ServerSocketChannelConfig extends ChannelConfig {
  def getBacklog: Int
  def setBacklog(backlog: Int): Unit
  def isReuseAddress: Boolean
  def setReuseAddress(reuseAddress: Boolean): Unit
  def getReceiveBufferSize: Int
  def setReceiveBufferSize(receiveBufferSize: Int): Unit
  def setPerformancePreferences(connectionTime: Int,
                                latency: Int,
                                bandwidth: Int): Unit
}
