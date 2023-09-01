package de.sciss.funser

object SourceCodeTest3 {
  def main(args: Array[String]): Unit = {
    // ok, this works ok, just it doesn't preserve the actual
    // source, including formatting, comments etc.

    val test = MySource {
      val i = 2
      val j = 3 // hello
      i * j
    }
    println(test)
  }
}
