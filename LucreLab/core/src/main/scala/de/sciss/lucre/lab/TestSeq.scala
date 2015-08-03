package de.sciss.lucre.lab

trait MutableSeqOps[-A] {
  def insert(idx: Int, elem: A): Unit

  def update(idx: Int, elem: A): Unit

  def removeAt(idx: Int): Unit
}

object TestSeq {
  trait Mutable[A] extends TestSeq[A] with MutableSeqOps[A]
}
trait TestSeq[+A] {
  def size: Int

  def apply(idx: Int): A

  def indexOf[A1 >: A](elem: A1): Int

  def map[B](fun: A => B): TestSeq[B]

  // `<: MutableSeqOps` is so that the result can be reactively coupled
  def filter[A1 >: A, That <: MutableSeqOps[A1]](p: A1 => Boolean)(implicit cb: MyCanBuild[A1, That]): That

  def foreach(fun: A => Unit): Unit

  def toList: List[A]
}

trait MyCanBuild[A, That] {
  def += (elem: A): Unit
  def result: That
}