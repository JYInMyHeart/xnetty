package channel.socket

import java.net.ServerSocket

import channel.ChannelPipelineFactory

import scala.collection.mutable

case class DefaultServerSocketChannelConfig(socket: ServerSocket)
    extends ServerSocketChannelConfig {
  @volatile var backlog: Int = _
  @volatile var pipelineFactory: ChannelPipelineFactory = _

  override def setOptions(map: mutable.Map[String, Any]): Unit =
    map.map { case (k, v) => setOption(k, v) }

  def setOption(str: String, value: Any): Boolean = {
    (str, value) match {
      case ("receiveBufferSize", i: Int) => setReceiveBufferSize(i)
      case ("reuseAddress", b: Boolean)  => setReuseAddress(b)
      case ("backlog", i: Int)           => setBacklog(i)
      case _                             => return false
    }
    true
  }
  override def getReceiveBufferSize: Int = socket.getReceiveBufferSize

  override def setPerformancePreferences(connectionTime: Int,
                                         latency: Int,
                                         bandwidth: Int): Unit =
    socket.setPerformancePreferences(connectionTime, latency, bandwidth)

  override def isReuseAddress: Boolean = socket.getReuseAddress
  override def setReceiveBufferSize(receiveBufferSize: Int): Unit =
    socket.setReceiveBufferSize(receiveBufferSize)

  override def setReuseAddress(reuseAddress: Boolean): Unit =
    socket.setReuseAddress(reuseAddress)

  override def setPipelineFactory(
      _pipelineFactory: ChannelPipelineFactory): Unit =
    pipelineFactory = _pipelineFactory

  override def getPipelineFactory: ChannelPipelineFactory = pipelineFactory

  override def getBacklog: Int = backlog

  override def setBacklog(_backlog: Int): Unit = {
    if (_backlog < 1) {
      throw new IllegalArgumentException
    }
    backlog = _backlog
  }

  override def getConnectTimeoutMillis: Int = 0

  override def setConnectTimeoutMillis(connectTimeoutMillis: Int): Unit = {}

  override def getWriteTimeoutMillis: Int = 0

  override def setWriteTimeoutMillis(writeTimeoutMillis: Int): Unit = {}
}
