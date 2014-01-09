package de.sciss.lucre.macros

import scala.annotation.StaticAnnotation
import scala.reflect.macros._
import language.experimental.macros

class mkCompanion extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro mkCompanionMacro.impl
}
object mkCompanionMacro {
  def impl(c: BlackboxContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val inputs = annottees.map(_.tree).toList
    println("----inputs----")
    inputs.zipWithIndex.foreach { case (in, idx) =>
      println(s"-- $idx: ${in.getClass}\n$in")
    }
    println("--------")
    val (annottee, expandees) = inputs match {
      case (param: ValDef ) :: (rest @ (_ :: _)) => (param, rest)
      case (param: TypeDef) :: (rest @ (_ :: _)) => (param, rest)
      case _ => (EmptyTree, inputs)
    }
    // println((annottee, expandees))
    val outputs = expandees
    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}