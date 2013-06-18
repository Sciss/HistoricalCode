package play.api.libs.json

import scala.reflect.macros.Context

sealed trait Meta[A]

object Recursive {
  // def apply[A]

  def applyImpl[A: c.WeakTypeTag](c: Context): c.Expr[Meta[A]] = {
    import c.universe._
    val aTpeW   = c.weakTypeOf[A]
    val aClazz  = aTpeW.typeSymbol.asClass


    ???
  }
}