package de.sciss.picklingtest

import scala.pickling._

object Main extends App {
  val f: Foo = new impl.FooImpl("bar", 1234)
  import binary.pickleFormat
  val p = f.pickle
  val g = p.unpickle[Foo]
  println(g)
}
