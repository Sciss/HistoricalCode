package de.sciss

package object svm {
  val Vec       = collection.immutable.IndexedSeq
  type Vec[+A]  = collection.immutable.IndexedSeq[A]

  final val Inf = Double.PositiveInfinity

   // TODO: use proper logging
  def logInfo(what: => String): Unit = Console.out.print(what)
  def logWarn(what: => String): Unit = Console.err.print(s"\nWARNING: $what")
}