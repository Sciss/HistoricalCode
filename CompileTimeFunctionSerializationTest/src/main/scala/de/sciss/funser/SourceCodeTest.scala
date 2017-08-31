package de.sciss.funser

object SourceCodeTest {
  def main(args: Array[String]): Unit = {
    // ok, this works great
    val test = sourcecode.Text[Int] {
      val i = 2
      val j = 3 // hello
      i * j
    }
    println(test.source)
  }
}
