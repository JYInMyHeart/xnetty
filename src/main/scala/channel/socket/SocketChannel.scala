package channel.socket

import java.net.InetSocketAddress

import channel.Channel

trait SocketChannel extends Channel {
  override def getConfig: SocketChannelConfig
  override def getLocalAddress: InetSocketAddress
  override def getRemoteAddress: InetSocketAddress
}
