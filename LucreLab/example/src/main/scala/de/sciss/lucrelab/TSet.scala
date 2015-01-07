package de.sciss.lucrelab

import scala.collection.generic.CanBuildFrom

object TSet {
  def empty[S <: Sys[S], A](implicit tx: S#Tx): TSet.Mod[A] = impl.TSetImpl.empty[S, A]
  def apply[S <: Sys[S], A](xs: A*)(implicit tx: S#Tx): TSet.Mod[A] = {
    val res = empty[S, A]
    xs.foreach(res.add)
    res
  }

  object Mod {
    def unapply[A](set: TSet[A]): Option[TSet.Mod[A]] = ???
  }
  trait Mod[A] extends TSet[A] {
    def add   (elem: A): Boolean
    def remove(elem: A): Boolean
  }
}
trait TSet[+A] {
  // def mod[A1 >: A]: Option[TSet.Mod[A1]]

  def contains[A1 >: A](elem: A1): Boolean

  def filter(p: A => Boolean): TSet[A]

  def to[That](implicit cbf: CanBuildFrom[Nothing, A, That]): That
}
