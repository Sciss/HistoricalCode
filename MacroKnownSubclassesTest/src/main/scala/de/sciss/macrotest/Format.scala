package de.sciss.macrotest

import scala.reflect.macros.Context
import language.experimental.macros

object Format {
  def apply[A](): Unit = macro applyImpl[A]

  def applyImpl[A: c.WeakTypeTag](c: Context)(): c.Expr[Unit] = {
    import c.universe._
    val aTpeW   = c.weakTypeOf[A]
    val aClazz  = aTpeW.typeSymbol.asClass
    val subs    = aClazz.knownDirectSubclasses
    println(subs.mkString("--- sub classes:\n  '", "'\n  '", "'\n---"))
    c.Expr[Unit](EmptyTree)
  }
}