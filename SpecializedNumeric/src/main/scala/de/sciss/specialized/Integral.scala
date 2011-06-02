// same as scala.math.Integral, however specialized
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
trait Integral[@specialized(Int, Float, Long, Double) T] extends Numeric[T] {
  def quot(x: T, y: T): T
  def rem(x: T, y: T): T
  
  class IntegralOps(lhs: T) extends Ops(lhs) {
    def /(rhs: T) = quot(lhs, rhs)
    def %(rhs: T) = rem(lhs, rhs)
    def /%(rhs: T) = (quot(lhs, rhs), rem(lhs, rhs))
  }
  override implicit def mkNumericOps(lhs: T): IntegralOps = new IntegralOps(lhs)
}

object Integral {
  trait ExtraImplicits {
    /** The regrettable design of Numeric/Integral/Fractional has them all
     *  bumping into one another when searching for this implicit, so they
     *  are exiled into their own companions.
     */
    implicit def infixIntegralOps[@specialized(Int, Float, Long, Double) T](x: T)(implicit num: Integral[T]): Integral[T]#IntegralOps = new num.IntegralOps(x)
  }
  object Implicits extends ExtraImplicits
}