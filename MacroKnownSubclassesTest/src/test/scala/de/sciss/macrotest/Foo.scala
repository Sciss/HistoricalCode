package de.sciss.macrotest

object Baz {
  Format[Foo]()   // doesn't find sub class `Bar`
}

sealed trait Foo
case class Bar() extends Foo