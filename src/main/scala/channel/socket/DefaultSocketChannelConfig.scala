package channel.socket

import java.net.Socket

import channel.ChannelPipelineFactory

import scala.collection.mutable

class DefaultSocketChannelConfig(socket: Socket) extends SocketChannelConfig {
  @volatile var connectTimeoutMillis: Int = 10000

  def setPipelineFactory(f: ChannelPipelineFactory): Unit = {}

  def setWriteTimeoutMillis(i: Int): Unit = {}

  def setOption(key: String, value: Any): Boolean = {
    (key, value) match {
      case ("receiveBufferSize", i: Int) =>
        setReceiveBufferSize(i)
      case ("sendBufferSize", i: Int) =>
        setSendBufferSize(i)
      case ("tcpNoDelay", b: Boolean) =>
        setTcpNoDelay(b)
      case ("keepAlive", b: Boolean) =>
        setKeepAlive(b)
      case ("reuseAddress", b: Boolean) =>
        setReuseAddress(b)
      case ("soLinger", i: Int) =>
        setSoLinger(i)
      case ("trafficClass", i: Int) =>
        setTrafficCLass(i)
      case ("writeTimeoutMillis", i: Int) =>
        setWriteTimeoutMillis(i)
      case ("connectTimeoutMillis", i: Int) =>
        connectTimeoutMillis = i
      case ("pipelineFactory", f: ChannelPipelineFactory) =>
        setPipelineFactory(f)
    }
    true
  }

  def setOptions(map: mutable.Map[String, Any]): Unit =
    map.map { case (k, v) => setOption(k, v) }

  override def getReceiveBufferSize: Int = socket.getReceiveBufferSize

  override def getSendBufferSize: Int = socket.getSendBufferSize

  override def getSoLinger: Int = socket.getSoLinger

  override def getTrafficClass: Int = socket.getTrafficClass

  override def isKeepAlive: Boolean = socket.getKeepAlive

  override def isReuseAddress: Boolean = socket.getReuseAddress

  override def isTcpNoDelay: Boolean = socket.getTcpNoDelay

  override def setKeepAlive(keepAlive: Boolean): Unit =
    socket.setKeepAlive(keepAlive)

  override def setPerformancePreferences(connectionTime: Int,
                                         latency: Int,
                                         bandwidth: Int): Unit =
    socket.setPerformancePreferences(connectionTime, latency, bandwidth)

  override def setReceiveBufferSize(receiveBufferSize: Int): Unit =
    socket.setReceiveBufferSize(receiveBufferSize)

  override def setReuseAddress(reuseAddress: Boolean): Unit =
    socket.setReuseAddress(reuseAddress)

  override def setSendBufferSize(sendBufferSize: Int): Unit =
    socket.setSendBufferSize(sendBufferSize)

  override def setSoLinger(soLinger: Int): Unit =
    if (soLinger < 0)
      socket.setSoLinger(false, 0)
    else
      socket.setSoLinger(true, soLinger)

  override def setTcpNoDelay(tcpNoDelay: Boolean): Unit =
    socket.setTcpNoDelay(tcpNoDelay)

  override def setTrafficCLass(trafficClass: Int): Unit =
    socket.setTrafficClass(trafficClass)

  override def getPipelineFactory: ChannelPipelineFactory = null

  override def getConnectTimeoutMillis: Int = connectTimeoutMillis

  override def setConnectTimeoutMillis(connectTimeoutMillis: Int): Unit = {
    if (connectTimeoutMillis < 0)
      throw new IllegalArgumentException(
        s"connectTimeoutMillis: $connectTimeoutMillis")
    this.connectTimeoutMillis = connectTimeoutMillis
  }

  override def getWriteTimeoutMillis: Int = 0
}
