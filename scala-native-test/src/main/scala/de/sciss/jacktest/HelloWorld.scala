package de.sciss.jacktest

import scalanative.native._
import scalanative.native.stdio._

object HelloWorld {
  def mainXXX(args: Array[String]): Unit =
    puts(c"Hello from scala-native\n")
}
