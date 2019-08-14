package channel

import java.net.SocketAddress

trait MessageEvent extends ChannelEvent {
  def getMessage: Any
  def getRemoteAddress: SocketAddress
}
