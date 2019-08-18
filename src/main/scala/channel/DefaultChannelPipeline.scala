package channel

import java.lang.annotation.AnnotationFormatError

import org.jetbrains.annotations.Contract

import scala.collection.mutable

case class DefaultChannelPipeline(var channel: Channel,
                                  var channelSink: ChannelSink)
    extends ChannelPipeline {

  def this() {
    this(null, null)
  }

  private[this] final val discardingSink = new ChannelSink {
    override def eventSunk(pipeline: ChannelPipeline,
                           event: ChannelEvent): Unit = {}

    override def exceptionCaught(pipeline: ChannelPipeline,
                                 event: ChannelEvent,
                                 cause: ChannelPipelineException): Unit =
      throw cause
  }

  @volatile private[this] var head: DefaultChannelHandlerContext = _
  @volatile private[this] var tail: DefaultChannelHandlerContext = _
  private[this] val ctx: mutable.Map[String, DefaultChannelHandlerContext] =
    mutable.LinkedHashMap()

  override def getChannel: Channel = channel

  override def getSink: ChannelSink =
    if (channelSink == null) discardingSink else channelSink

  override def attach(channel: Channel, sink: ChannelSink): Unit = {
    assert(channel != null && sink != null)
    this.channel = channel
    this.channelSink = sink
  }

  def init(name: String, handler: ChannelHandler): Unit = {
    val _ctx = new DefaultChannelHandlerContext(null, null, handler, name)
    head = _ctx
    tail = _ctx
    ctx += name -> _ctx
  }

  @Contract(pure = true)
  def checkDuplicateName(name: String): Boolean = {
    ctx.contains(name)
  }

  override def addFirst(name: String, handler: ChannelHandler): Unit = {
    this.synchronized {
      if (ctx.isEmpty)
        init(name, handler)
      else {
        if (checkDuplicateName(name))
          throw new IllegalArgumentException("Duplicate handler name.")
        val oldHead = head
        val newHead =
          new DefaultChannelHandlerContext(null, oldHead, handler, name)
        oldHead.prev = newHead
        head = newHead
        ctx += name -> newHead
      }
    }
  }

  override def addLast(name: String, handler: ChannelHandler): Unit = {
    this.synchronized {
      if (ctx.isEmpty)
        init(name, handler)
      else {
        if (checkDuplicateName(name))
          throw new IllegalArgumentException("Duplicate handler name.")
        val oldTail = tail
        val newTail =
          new DefaultChannelHandlerContext(oldTail, null, handler, name)
        oldTail.next = newTail
        tail = newTail
        ctx += name -> newTail
      }
    }
  }

  override def addBefore(baseName: String,
                         name: String,
                         handler: ChannelHandler): Unit = {
    this.synchronized {
      val _ctx =
        getContextOrDie(baseName).asInstanceOf[DefaultChannelHandlerContext]
      if (_ctx == head)
        addFirst(name, handler)
      else {
        if (checkDuplicateName(name))
          throw new IllegalArgumentException("Duplicate handler name.")
        val newCtx =
          new DefaultChannelHandlerContext(_ctx.prev, _ctx.next, handler, name)
        _ctx.prev.next = newCtx
        _ctx.prev = newCtx
        ctx += name -> newCtx
      }
    }
  }

  override def addAfter(baseName: String,
                        name: String,
                        handler: ChannelHandler): Unit = {
    this.synchronized {
      val _ctx =
        getContextOrDie(baseName).asInstanceOf[DefaultChannelHandlerContext]
      if (_ctx == tail)
        addLast(name, handler)
      else {
        if (checkDuplicateName(name))
          throw new IllegalArgumentException("Duplicate handler name.")
        val newCtx =
          new DefaultChannelHandlerContext(_ctx, _ctx.next, handler, name)
        _ctx.next.prev = newCtx
        _ctx.next = newCtx
        ctx += name -> newCtx
      }
    }
  }

  override def remove(handler: ChannelHandler): Unit = {
    this.synchronized {
      remove(
        getContextOrDie(handler).asInstanceOf[DefaultChannelHandlerContext])
    }
  }

  override def remove[T <: ChannelHandler](
      handlerType: Class[T]): ChannelHandler = {
    this.synchronized {
      remove(
        getContextOrDie(handlerType)
          .asInstanceOf[DefaultChannelHandlerContext]).getHandler
    }
  }

  override def remove(name: String): ChannelHandler = {
    this.synchronized {
      remove(getContextOrDie(name).asInstanceOf[DefaultChannelHandlerContext]).getHandler
    }
  }

  private[this] def remove(
      _ctx: DefaultChannelHandlerContext): DefaultChannelHandlerContext = {
    if (tail == head) {
      tail = null
      head = null
      ctx.clear()
    } else if (_ctx == head) {
      removeFirst()
    } else if (_ctx == tail) {
      removeLast()
    } else {
      val _prev = _ctx.prev
      val _next = _ctx.next
      _prev.next = _next
      _next.prev = _prev
      ctx -= _ctx.name
    }
    _ctx
  }

  override def removeFirst(): ChannelHandler = {
    this.synchronized {
      head match {
        case null => throw new NoSuchElementException()
        case _ =>
          val oldHead = head
          val _next = head.next
          _next.prev = null
          head = _next
          ctx -= oldHead.name
          return oldHead.getHandler
      }
    }
  }

  override def removeLast(): ChannelHandler = {
    this.synchronized {
      tail match {
        case null => throw new NoSuchElementException()
        case _ =>
          val oldTail = tail
          val _prev = tail.prev
          _prev.next = null
          tail = _prev
          ctx -= oldTail.name
          return oldTail.getHandler
      }
    }
  }

  override def replace(oldName: String,
                       newName: String,
                       newHandler: ChannelHandler): ChannelHandler = {
    this.synchronized {
      replace(
        getContextOrDie(oldName).asInstanceOf[DefaultChannelHandlerContext],
        newName,
        newHandler)
    }
  }

  override def replace[T <: ChannelHandler](
      oldHandlerType: Class[T],
      newName: String,
      newHandler: ChannelHandler): ChannelHandler = {
    replace(
      getContext(oldHandlerType).asInstanceOf[DefaultChannelHandlerContext],
      newName,
      newHandler)
  }

  override def replace(oldHandler: ChannelHandler,
                       newName: String,
                       newHandler: ChannelHandler): Unit = {
    getContext(oldHandler) match {
      case Some(_ctx: DefaultChannelHandlerContext) =>
        replace(_ctx, newName, newHandler)
      case _ =>
        throw new IllegalArgumentException(s"Invalid handlers: $oldHandler")
    }

  }

  private[this] def replace(_ctx: DefaultChannelHandlerContext,
                            newName: String,
                            newHandler: ChannelHandler): ChannelHandler = {
    _ctx match {
      case c if c == head =>
        removeFirst()
        addFirst(newName, newHandler)
      case c if c == tail =>
        removeLast()
        addLast(newName, newHandler)
      case _ =>
        val sameName = _ctx.getName == newName
        if (!sameName && checkDuplicateName(newName))
          throw new IllegalArgumentException("Duplicate handler name.")
        val _prev = _ctx.prev
        val _next = _ctx.next
        val newCtx =
          new DefaultChannelHandlerContext(_prev, _next, newHandler, newName)
        _prev.next = newCtx
        _next.prev = newCtx
        if (!sameName) {
          ctx -= _ctx.name
          ctx += newName -> newCtx
        }
    }
    _ctx.getHandler
  }

  override def getFirst: ChannelHandler = head.getHandler

  override def getLast: ChannelHandler = tail.getHandler

  override def get(name: String): Option[ChannelHandler] =
    ctx.get(name) match {
      case None        => None
      case Some(value) => Some(value.getHandler)
    }

  override def get[T <: ChannelHandler](
      handlerType: Class[T]): Option[ChannelHandler] =
    getContext(handlerType) match {
      case None        => None
      case Some(value) => Some(value.getHandler)
    }

  override def getContext(name: String): Option[ChannelHandlerContext] = {
    ctx.get(name)
  }

  override def getContext(
      handler: ChannelHandler): Option[ChannelHandlerContext] = {
    assert(handler != null)
    if (ctx.isEmpty) return None
    var _ctx = head
    while (_ctx != null) {
      if (_ctx.getHandler == handler)
        return Some(_ctx)
      _ctx = _ctx.next
    }
    None
  }

  override def getContext[T <: ChannelHandler](
      handlerType: Class[T]): Option[ChannelHandlerContext] = {
    assert(handlerType != null)
    if (ctx.isEmpty) return None
    var _ctx = head
    while (_ctx != null) {
      if (_ctx.getHandler.getClass == handlerType)
        return Some(_ctx)
      _ctx = _ctx.next
    }
    None
  }

  override def toMap(): mutable.Map[String, ChannelHandler] = {
    ctx.map { case (_, _ctx) => _ctx.name -> _ctx.handler }
  }

  sealed case class DefaultChannelHandlerContext(
      var prev: DefaultChannelHandlerContext,
      var next: DefaultChannelHandlerContext,
      handler: ChannelHandler,
      name: String,
      canHandleUpstream: Boolean,
      canHandleDownstream: Boolean)
      extends ChannelHandlerContext {

    def this(prev: DefaultChannelHandlerContext,
             next: DefaultChannelHandlerContext,
             handler: ChannelHandler,
             name: String) = {
      this(prev,
           next,
           handler,
           name,
           handler.isInstanceOf[ChannelUpstreamHandler],
           handler.isInstanceOf[ChannelDownstreamHandler])
      if (!canHandleDownstream && !canHandleUpstream)
        throw new IllegalArgumentException(
          s"handler must be either ${classOf[ChannelDownstreamHandler].getName} " +
            s"or ${classOf[ChannelUpstreamHandler].getName}."
        )

      val coverage =
        handler.getClass.getAnnotation(classOf[ChannelPipelineCoverage])
      if (coverage == null)
        println(
          "Handler '%s' doesn't have a '%s' annotation with its class declaration. It is recommended to add the annotation to tell if one handler instance can handle more than one pipeline (\"%s\") or not (\"%s\")"
            .format(handler.getClass.getName,
                    classOf[ChannelPipelineCoverage].getSimpleName,
                    ChannelPipelineCoverage.ALL,
                    ChannelPipelineCoverage.ONE))
      else {
        val coverageValue = coverage.value()
        if (coverageValue == null)
          throw new AnnotationFormatError(
            "%s annotation value is undefined for type: %s".format(
              classOf[ChannelPipelineCoverage].getSimpleName,
              handler.getClass.getName))
      }
    }

    def getPipeline: ChannelPipeline = DefaultChannelPipeline.this
    def getCanHandleDownstream: Boolean = canHandleDownstream
    def getCanHandleUpstream: Boolean = canHandleUpstream

    override def getHandler: ChannelHandler = handler

    override def getName: String = name

    override def sendDownstream(event: ChannelEvent): Unit = {
      val _prev = getActualDownstreamContext(this.prev)
      if (_prev == null) {
        try {
          getSink.eventSunk(DefaultChannelPipeline.this, event)
        } catch {
          case t: Throwable => notifyException(event, t)
        }
      } else
        DefaultChannelPipeline.this.sendDownstream(_prev, event)
    }

    override def sendUpstream(event: ChannelEvent): Unit = {
      val _next = getActualUpstreamContext(this.next)
      if (_next == null)
        DefaultChannelPipeline.this.sendUpstream(_next, event)
    }

  }

  def sendDownstream(_ctx: DefaultChannelHandlerContext,
                     event: ChannelEvent): Unit = {
    try {
      _ctx.getHandler
        .asInstanceOf[ChannelDownstreamHandler]
        .handleDownstream(_ctx, event)
    } catch {
      case throwable: Throwable => notifyException(event, throwable)
    }
  }

  override def sendDownstream(event: ChannelEvent): Unit = {
    val _tail = getActualDownstreamContext(this.tail)
    if (_tail == null) {
      try {
        getSink.eventSunk(this, event)
        return
      } catch {
        case t: Throwable => notifyException(event, t)
      }
    }
    sendDownstream(_tail, event)
  }

  def sendUpstream(_ctx: DefaultChannelHandlerContext,
                   event: ChannelEvent): Unit = {
    try {
      _ctx.getHandler
        .asInstanceOf[ChannelUpstreamHandler]
        .handleUpstream(_ctx, event)
    } catch {
      case throwable: Throwable => notifyException(event, throwable)
    }
  }

  override def sendUpstream(event: ChannelEvent): Unit = {
    val _head = getActualDownstreamContext(this.head)
    if (_head == null) {
      println(s"The pipeline contains no upstream handlers; discarding: $event")
    }
    sendUpstream(_head, event)
  }

  def getActualUpstreamContext(
      ctx: DefaultChannelHandlerContext): DefaultChannelHandlerContext = {
    if (ctx == null) return null
    var realCtx = ctx
    while (!realCtx.canHandleUpstream) {
      realCtx = realCtx.next
      if (realCtx == null)
        return null
    }
    realCtx
  }

  def getActualDownstreamContext(
      ctx: DefaultChannelHandlerContext): DefaultChannelHandlerContext = {
    if (ctx == null) return null
    var realCtx = ctx
    while (!realCtx.canHandleDownstream) {
      realCtx = realCtx.prev
      if (realCtx == null)
        return null
    }
    realCtx
  }
  def notifyException(event: ChannelEvent, t: Throwable): Unit = {
    t match {
      case e: ChannelPipelineException =>
        channelSink.exceptionCaught(this, event, e)
      case e @ _ =>
        channelSink.exceptionCaught(this,
                                    event,
                                    new ChannelPipelineException(e))
    }
  }

  private[this] def getContextOrDie(baseName: String): ChannelHandlerContext = {
    val _ctx = getContext(baseName)
    _ctx match {
      case None        => throw new NoSuchElementException(baseName)
      case Some(value) => value
    }
  }
  private[this] def getContextOrDie(
      handler: ChannelHandler): ChannelHandlerContext = {
    val _ctx = getContext(handler)
    _ctx match {
      case None        => throw new NoSuchElementException(handler.getClass.getName)
      case Some(value) => value
    }
  }
  private[this] def getContextOrDie[A <: ChannelHandler](
      handlerType: Class[A]): ChannelHandlerContext = {
    val _ctx = getContext(handlerType)
    _ctx match {
      case None        => throw new NoSuchElementException(handlerType.getName)
      case Some(value) => value
    }
  }
}
