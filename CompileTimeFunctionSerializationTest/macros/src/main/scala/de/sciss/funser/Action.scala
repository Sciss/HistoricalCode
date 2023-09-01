package de.sciss.funser

import de.sciss.funser.{ActionImpl => Impl}

import scala.language.experimental.macros

object Action {
  def compile(source: Code.Action)(implicit compiler: Code.Compiler): Action =
    Impl.compile(source)

  def apply(body: Unit): Action = macro Impl.applyImpl

  def newConst(name: String, jar: Array[Byte], source: String): Action =
    Impl.newConst(name, jar, source = source)

  def newConst(name: String, jar: String, source: String): Action = {
    newConst(name, jar.getBytes("ISO-8859-1"), source)
  }

  trait Universe

//  trait Body {
//    def apply(universe: Universe): Unit
//  }

  type Body = Function0[Unit]

  type Action = (Body, String)
}
//trait Action {
//  def execute(universe: Action.Universe): Unit
//
//  def source: String
//}
