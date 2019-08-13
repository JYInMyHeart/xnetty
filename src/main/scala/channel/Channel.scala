package channel

import java.util.UUID

trait Channel {
  val OP_NONE = 0
  val OP_READ = 1
  val OP_WRITE = 4
  val OP_READ_WRITE = OP_READ | OP_WRITE

  def getId(): UUID

}
