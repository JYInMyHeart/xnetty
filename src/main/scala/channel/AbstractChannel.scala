package channel

import java.net.SocketAddress
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractChannel(parent: Channel,
                               factory: ChannelFactory,
                               pipeline: ChannelPipeline,
                               sink: ChannelSink,
                               succeededFuture: ChannelFuture =
                                 SucceededChannelFuture(this))
    extends Channel
    with Comparable[Channel] {

  private[this] val id = UUID.randomUUID()
  private[this] val closed: AtomicBoolean = new AtomicBoolean()
  private[this] var interestOps: Int = OP_READ
  private[this] var strVal: String = _

  def this(parent: Channel,
           factory: ChannelFactory,
           pipeline: ChannelPipeline,
           sink: ChannelSink) {
    this(parent, factory, pipeline, sink)
    pipeline.attach(this, sink)
  }

  override def getId(): UUID = id

  override def getParent: Channel = parent

  override def getFactory: ChannelFactory = factory

  override def getPipeline: ChannelPipeline = pipeline

  protected def getSucceededFuture: ChannelFuture = succeededFuture

  protected def getUnsupportedOperationFuture: ChannelFuture =
    FailedChannelFuture(this, new UnsupportedOperationException())

  override def hashCode(): Int = System.identityHashCode(this)

  override def equals(obj: Any): Boolean = this == obj

  override def compareTo(o: Channel): Int =
    System.identityHashCode(this) - System.identityHashCode(o)

  override def isOpen: Boolean = !closed.get()

  protected def setClosed: Boolean = closed.compareAndSet(false, true)

  override def bind(localAddress: SocketAddress): ChannelFuture =
    Channels.bind(this, localAddress)

}
