package de.sciss.picklingtest

import scala.pickling._

object Main extends App {
  val f: Foo = new impl.FooImpl("bar", 1234)
  import binary.pickleFormat
  val p = f.pickle
  val g = p.unpickle[Foo]
  println(g)

  val h: Foo = new impl.FooImpl2("bar" -> 1234)
  val q = h.pickle
  val j = q.unpickle[Foo]
  println(j)
}
