package de.sciss.funser

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** Looking at `logImpl` of
  * https://github.com/retronym/macrocosm/blob/master/src/main/scala/com/github/retronym/macrocosm/Macrocosm.scala
  */
object MySource {
  def apply(a: Any): String = macro applyImpl

  def applyImpl(c: blackbox.Context)(a: c.Expr[Any]): c.Expr[String] = {
    import c.universe._
    val aCode = c.Expr[String](Literal(Constant(show(a.tree))))
    c.universe.reify {
      aCode.splice
    }
  }
}
