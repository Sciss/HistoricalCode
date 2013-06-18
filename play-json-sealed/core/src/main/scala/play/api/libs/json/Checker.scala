package play.api.libs.json

import scala.reflect.macros.Context
import language.experimental.macros

object Checker {
  def apply[A]: Unit = macro applyImpl[A]

  def applyImpl[A: c.WeakTypeTag](c: Context): c.Expr[Unit] = {
    val tpe = c.weakTypeOf[A].typeSymbol.asClass
    require (tpe.isSealed)
    tpe.typeSignature // SI-7046
    require (tpe.knownDirectSubclasses.nonEmpty, "Did not find sub classes")

    import c.universe._
    c.Expr[Unit](reify {} .tree)
  }
}