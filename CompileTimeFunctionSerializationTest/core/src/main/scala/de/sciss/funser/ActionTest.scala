package de.sciss.funser

object ActionTest {
  def main(args: Array[String]): Unit = {
    val (fun, source) = Action {
      println("Hello world!") // yes
    }
    println("Source code:\n")
    println(source)
    println("Executing:")
    fun() // .execute(new Action.Universe {})
  }
}
