package de.sciss.citarj2017

import de.sciss.file._

import scala.meta._
import scala.meta.dialects.Scala210

object Test {
  def main(args: Array[String]): Unit = {
    val baseDir = file("/") / "data" / "temp" / "WritingMachine"
    val srcDir  = baseDir / "src" / "main" / "scala"
    val pkgDir  = srcDir / "de" / "sciss" / "grapheme"
    val exF     = pkgDir / "Database.scala"
    require (exF.isFile)

    val parsed: Parsed[Source] = Scala210(exF).parse[Source]
    val root    = parsed.get.children.head
    println(root.children.size)
    root.children.foreach { t => println("\n--------------------------\n"); println(t.structure) }
  }
}
