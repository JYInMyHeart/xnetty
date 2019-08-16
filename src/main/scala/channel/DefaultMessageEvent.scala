package channel

import java.net.SocketAddress

case class DefaultMessageEvent[T](channel: Channel,
                                  future: ChannelFuture,
                                  message: T,
                                  remoteAddress: SocketAddress)
    extends DefaultChannelEvent(channel, future)
    with MessageEvent {

  override def getMessage: Any = message

  override def getRemoteAddress: SocketAddress = remoteAddress

  override def toString: String =
    if (remoteAddress == null)
      s"${super.toString} - (message: $message)"
    else
      s"${super.toString} - (message: $message , $remoteAddress )"
}
