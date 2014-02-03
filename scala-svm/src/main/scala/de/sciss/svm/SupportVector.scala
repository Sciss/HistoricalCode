package de.sciss.svm

import scala.util.control.NonFatal
import java.io.IOException
import scala.collection.breakOut

object SupportVector {
  def fromString(s: String): SupportVector = try {
    val tokens              = s.trim().split(" ")
    val vector: List[Node]  = tokens.map(Node.fromString)(breakOut)
    SupportVector(vector, 0)
  } catch {
    case NonFatal(e) => throw new IOException(s"Invalid input: $s", e)
  }
}
case class SupportVector(vector: List[Node], index: Int)
