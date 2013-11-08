package de.sciss

package object svm {
  val Vec       = collection.immutable.IndexedSeq
  type Vec[+A]  = collection.immutable.IndexedSeq[A]

  def info(what: => String): Unit = println(what) // TODO: use proper logging
}
