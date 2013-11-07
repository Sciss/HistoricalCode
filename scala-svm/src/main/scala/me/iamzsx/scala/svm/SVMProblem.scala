package me.iamzsx.scala.svm

import java.io.IOException

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class SVMNode(
  val index: Int,
  val value: Double) {

  override def toString = index + ":" + value
}

object SVMNode {
  def apply(index: Int, value: Double) = new SVMNode(index, value)
}

class Instance(
  val x: List[SVMNode],
  val y: Double) {

  override def toString = "(" + y + "|" + x.mkString(", ") + ")"
}

object Instance {
  def apply(x: List[SVMNode], y: Double) = new Instance(x, y)
}

class SVMProblem(val instances: Array[Instance]) {
  require(instances.size > 0)

  val size    = instances.size
  lazy val xs: Array[List[SVMNode]] = instances.map(_.x)
  lazy val ys: Array[Double]        = instances.map(_.y)

  def x(i: Int): List[SVMNode]  = instances(i).x
  def y(i: Int): Double         = instances(i).y

  def groupClasses: Map[Double, SVMProblem] =
    instances.groupBy(_.y).map(x => (x._1, SVMProblem(x._2)))

  override def toString = instances.mkString("\n")
}

object SVMProblem {

  def apply(instances: Array[Instance]) = new SVMProblem(instances)

  def get(param: SVMParameter, source: Source): SVMProblem = {
    val instances = ArrayBuffer[Instance]()
    var maxIndex = 0
    for (line <- source.getLines().map(_.trim)) {
      val splits = line.split('\t')
      if (splits.size <= 1) {
        // Do we need to support no feature?
        throw new IOException("Invalid input: " + line)
      }
      val (Array(label), features) = splits.splitAt(1)
      val y = label.toDouble
      var featureMaxIndex = 0
      val x = features.map((feature: String) => {
        val splits = feature.split(':')
        if (splits.size != 2) {
          throw new IOException("Invalid input: " + line)
        }
        val index = splits(0).toInt
        if (index <= featureMaxIndex) {
          throw new IOException("Index must be in order and unique: " + line)
        } else {
          featureMaxIndex = index
        }
        val value = splits(1).toDouble
        SVMNode(index, value)
      }).toList
      instances += Instance(x, y)
      maxIndex = maxIndex max featureMaxIndex
    }
    if (param.gamma == 0 && maxIndex > 0) {
      param.gamma = 1.0 / maxIndex
    }
    new SVMProblem(instances.toArray)
  }
}
