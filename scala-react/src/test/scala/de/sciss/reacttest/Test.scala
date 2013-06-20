package de.sciss.reacttest

import scala.react.Domain

object Test extends Domain with App {
  val engine    = new Engine
  val scheduler = new ManualScheduler

  val v1, v2    = Var(0)
  lazy val f    = Strict { v1() + v2() }

  start()
  runTurn()

  schedule { f; println(f.now) }

  runTurn()

  // println(f.now)
}