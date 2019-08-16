package channel

import java.net.SocketAddress
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractChannel(parent: Channel,
                               factory: ChannelFactory,
                               pipeline: ChannelPipeline,
                               sink: ChannelSink,
                               var succeededFuture: ChannelFuture)
    extends Channel
    with Comparable[Channel] {

  private[this] val id = UUID.randomUUID()
  private[this] val closed: AtomicBoolean = new AtomicBoolean()
  private[this] var interestOps: Int = Channel.OP_READ
  private[this] var strVal: String = _

  def this(parent: Channel,
           factory: ChannelFactory,
           pipeline: ChannelPipeline,
           sink: ChannelSink) {
    this(parent, factory, pipeline, sink, null)
    succeededFuture = SucceededChannelFuture(this)
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

  protected def setClosed(): Boolean = closed.compareAndSet(false, true)

  override def bind(localAddress: SocketAddress): ChannelFuture =
    Channels.bind(this, localAddress)

  override def connect(remoteAddress: SocketAddress): ChannelFuture =
    Channels.connect(this, remoteAddress)

  override def disconnect(): ChannelFuture =
    Channels.disconnect(this)

  override def getInterestOps: Int = interestOps

  override def setInterestOps(interestOps: Int): Option[ChannelFuture] =
    Channels.setInterestOps(this, interestOps)

  def setInterestOpsNow(interestOps: Int): Unit = this.interestOps = interestOps

  override def isReadable: Boolean = (getInterestOps & Channel.OP_READ) != 0

  override def isWritable: Boolean = (getInterestOps & Channel.OP_WRITE) == 0

  def setReadable(readable: Boolean): ChannelFuture =
    if (readable) {
      setInterestOps(getInterestOps | Channel.OP_READ) match {
        case Some(value) => value
        case None        => FailedChannelFuture(this, new RuntimeException)
      }
    } else {
      setInterestOps(getInterestOps & ~Channel.OP_READ) match {
        case Some(value) => value
        case None        => FailedChannelFuture(this, new RuntimeException)
      }
    }

  override def write(message: => String): ChannelFuture =
    Channels.write(this, message)

  override def write(message: => String,
                     remoteAddress: SocketAddress): ChannelFuture =
    Channels.write(this, message, remoteAddress)

  override def toString: String = {
    if (strVal != null)
      return strVal

    val buffer = StringBuilder.newBuilder
    buffer
      .append(this.getClass.getSimpleName)
      .append("(id: ")
      .append(id.toString)

    if (isConnected) {
      buffer.append(", ")
      if (getParent == null) {
        buffer.append(getLocalAddress)
        buffer.append(" => ")
        buffer.append(getRemoteAddress)
      } else {
        buffer.append(getLocalAddress)
        buffer.append(" => ")
        buffer.append(getRemoteAddress)
      }
    } else if (isBound) {
      buffer.append(", ")
      buffer.append(getLocalAddress)
    }

    buffer.append(")")
    strVal = buffer.toString
    strVal
  }

}
