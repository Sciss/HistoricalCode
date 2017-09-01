package de.sciss.funser

import de.sciss.funser.{ActionImpl => Impl}

import scala.language.experimental.macros

object Action {
  def compile(source: Code.Action)(implicit compiler: Code.Compiler): Action =
    Impl.compile(source)

  def apply(body: Unit): Action = macro Impl.applyImpl

//  def apply[S <: Sys[S]](name: String, jar: Array[Byte])(implicit tx: S#Tx): Action[S] =
//    Impl.newConst(name, jar)

  trait Universe

  trait Body {
    def apply(universe: Universe): Unit
  }
}
trait Action {
  def execute(universe: Action.Universe): Unit

  def source: String
}
