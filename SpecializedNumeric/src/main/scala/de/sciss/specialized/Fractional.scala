// same as scala.math.Fractional, however specialized
package de.sciss.specialized
import _root_.scala.{Equiv => _, Fractional => _, Integral => _, None => _, Numeric => _, Option => _, Ordered => _, Ordering => _, PartialOrdering => _, Some => _ }

/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

//package scala.math

/**
 * @since 2.8
 */
trait Fractional[@specialized(Int, Float, Long, Double) T] extends Numeric[T] {
  def div(x: T, y: T): T
  
  class FractionalOps(lhs: T) extends Ops(lhs) {
    def /(rhs: T) = div(lhs, rhs)
  }
  override implicit def mkNumericOps(lhs: T): FractionalOps =
    new FractionalOps(lhs)
}

object Fractional {
  trait ExtraImplicits {
    implicit def infixFractionalOps[@specialized(Int, Float, Long, Double) T](x: T)(implicit num: Fractional[T]): Fractional[T]#FractionalOps = new num.FractionalOps(x)
  }
  object Implicits extends ExtraImplicits
}