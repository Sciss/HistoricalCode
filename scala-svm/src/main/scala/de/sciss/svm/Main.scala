package de.sciss.svm

object Main extends App {
  private val argsVec = args.toIndexedSeq
  argsVec match {
    case "train" +: rest =>
      CommandTrain(rest)

    case other +: _ =>
      println(s"Illegal command '$other'!\n")
      printCommands()

    case _ =>
      printCommands()
  }

  def printCommands(): Unit = {
    println(
      """Available commands:
        |
        |  train
        |""".stripMargin)
    sys.exit(1)
  }
}
