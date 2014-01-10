package de.sciss.lucre.macros

object TestType extends App {
  object Foo
  @mkCompanion class Foo

  @mkCompanion class Bar

  assert(Foo.hasFoo == 33)
  assert(Bar.hasFoo == 33)
  println("Passed.")
}
