package channel.socket

import java.net.InetSocketAddress

import channel.Channel

trait ServerSocketChannel extends Channel {
  override def getConfig: ServerSocketChannelConfig
  override def getLocalAddress: InetSocketAddress
  override def getRemoteAddress: InetSocketAddress

}
