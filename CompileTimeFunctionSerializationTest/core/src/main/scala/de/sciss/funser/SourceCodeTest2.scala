package de.sciss.funser

object SourceCodeTest2 {
  def main(args: Array[String]): Unit = {
    // ok, this works great, as well.
    // I see this as superior to lihaoyi's approach
    // as we don't hack into the file system.

    val test = SlickDemoMacros.sourceExpr[Int] {
      val i = 2
      val j = 3 // hello
      i * j
    }
    println(test._1)
  }
}
