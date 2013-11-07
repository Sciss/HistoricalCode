package me.iamzsx.scala.svm

object AstroTest extends App {
  val base  = "http://www.csie.ntu.edu.tw/~cjlin/papers/guide/data"
  val train = io.Source.fromURL(s"$base/train.1", "UTF-8")
  val test  = io.Source.fromURL(s"$base/test.1" , "UTF-8")

  val param =
  // val problem = SVMProblem.read()
}
