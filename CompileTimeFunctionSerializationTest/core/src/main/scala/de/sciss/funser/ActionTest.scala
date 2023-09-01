package de.sciss.funser

object ActionTest {
  def main(args: Array[String]): Unit = {
    val foo = "FOO"

    val (fun, source) = Action {
      // val s = "Hello world!" // another comment
      println(foo)  // yes!
    }
    println("Source code:\n")
    println(source)
    println("Executing:")
    fun() // .execute(new Action.Universe {})
  }
}
