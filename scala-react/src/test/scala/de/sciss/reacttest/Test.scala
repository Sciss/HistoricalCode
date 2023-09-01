package de.sciss.reacttest

object Test extends scala.react.Domain with App {
  val engine    = new Engine
  val scheduler = new ManualScheduler
  start()

  val v2   = Var(0)
  val v1   = Lazy { v2() + 10 }
  val f    = Lazy { v1() + v2() }

  schedule {
    new Observing {
      observe(f) { p =>
        println(s"Observed: $p")
      }
    }
    v2() = 5
  }
  runTurn()
}