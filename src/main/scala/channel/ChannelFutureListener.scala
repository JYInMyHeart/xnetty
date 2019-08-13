package channel

import java.util.EventListener

trait ChannelFutureListener extends EventListener {
  def operationComplete(future: ChannelFuture): Unit
}
object ChannelFutureListener {
  val CLOSE: ChannelFutureListener =
    (future: ChannelFuture) => future.getChannel.close()
}
