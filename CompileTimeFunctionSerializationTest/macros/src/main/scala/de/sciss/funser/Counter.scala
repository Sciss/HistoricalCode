package de.sciss.funser

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Counter {
  def apply(): Int = macro applyImpl

  private[this] var value = 0

  def applyImpl(c: blackbox.Context)(): c.Expr[Int] = {
    import c.universe._
    value += 1
    c.Expr[Int](Literal(Constant(value)))
  }
}
