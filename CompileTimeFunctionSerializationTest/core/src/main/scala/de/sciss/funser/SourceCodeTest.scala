package de.sciss.funser

object SourceCodeTest {
  def main(args: Array[String]): Unit = {
    // ok, this works great.
    // alternatively, check this: https://gist.github.com/Sciss/f13867bc1821d6a2007c0c0df868f7f7
    
    val test = sourcecode.Text[Int] {
      val i = 2
      val j = 3 // hello
      i * j
    }
    println(test.source)
  }
}
