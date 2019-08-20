package example

object Echo {
  class EchoClient {
    val host = "localhost"
    val port = 9999
    val channelFactory = NioClientSocketChannelFactory()
  }
}
