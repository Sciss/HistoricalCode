package play.api.libs.json

import scala.reflect.macros.Context
import language.experimental.macros

trait A {
  def foo(): Unit
}

object AImpl {
  def body: A = macro bodyImpl
  def bodyImpl(c: Context): c.Expr[A] = {
    import c.universe._
    val r = reify { new A { def foo() { println("schoko" )}}}
    c.Expr[A](r.tree)
  }
}
