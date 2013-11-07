package de.sciss.svm

import java.io.IOException

import scala.io.Source

/** Element of a feature vector.
  *
  * @param index  feature index or category
  * @param value  feature value
  */
case class Node(index: Int, value: Double) {
  override def toString = index + ":" + value
}

/** A data sample or instance.
  *
  * @param x  the feature vector
  * @param y  the label
  */
case class Instance(x: List[Node],y: Int /* Double */) {
  override def toString = "(" + y + "|" + x.mkString(", ") + ")"
}

/** An `Problem` is wrapping a collection of data instances. */
class Problem(val instances: Array[Instance]) {
  require(instances.length > 0, s"Instances cannot be empty")

  val size: Int = instances.length
  /** The feature vectors of all instances. */
  lazy val xs: Array[List[Node]] = instances.map(_.x)
  /** The labels of all instances. */
  lazy val ys: Array[Int]           = instances.map(_.y)

  /** Queries the feature vector of a given instance
    *
    * @param idx  index of the instance. Must be `>= 0` and `< size`
    */
  def x(idx: Int): List[Node] = instances(idx).x
  /** Queries the label of a given instance
    *
    * @param idx  index of the instance. Must be `>= 0` and `< size`
    */
  def y(idx: Int): Int = instances(idx).y

  def groupClasses: Map[Int, Problem] = instances.groupBy(_.y).mapValues(Problem.apply)

  override def toString = instances.mkString("\n")
}

object Problem {
  def apply(instances: Array[Instance]): Problem = new Problem(instances)

  /** Decodes a text description of input data.
    * 
    * The input data must be formatted as follows: Each entry or instance occupies one line,
    * lines are separated by newline characters. An individual is a sequence of values
    * separated by a tab character `\t`. The first column is the label, the remaining columns
    * form the feature vector. The label must be an integer number, each element of the feature
    * vector is two numbers separated by a colon, giving `index:value`. The feature index
    * must be a readable as an integer, the value as a double. Feature indices must be greate
    * than zero and in increasing order.
    *
    * An example data file can be found at http://www.csie.ntu.edu.tw/~cjlin/papers/guide/data/test.1
    * 
    * @param param    the SVM configuration to use. If the `gamma` value is zero, it will be
    *                 replaced by `1.0 / featMax`
    * @param source   input data, formatted as described above
    * @return         the problem consisting of the given configuration and decoded input data
    */
  def read(param: SVMParameter, source: Source): Problem = {
    val instances = Array.newBuilder[Instance]
    var maxIndex = 0
    for (line <- source.getLines().map(_.trim)) {
      val columns = line.split('\t')
      if (columns.size <= 1) {
        // Do we need to support no feature?
        throw new IOException("Invalid input: Feature vector empty in line " + line)
      }
      val (Array(label), features) = columns.splitAt(1)
      val y0 = label.toDouble
      val y  = y0.toInt // allows `+1` which causes problem with straight `.toInt`
      if (y != y0) throw new IOException(s"Labels must be integer numbers ($y0)")
      var featureMaxIndex = 0
      val x = features.map { feature =>
        val splits = feature.split(':')
        if (splits.size != 2) {
          throw new IOException(s"Invalid input, feature component must be `index:value`: $feature in line " + line)
        }
        val index = splits(0).toInt
        if (index <= featureMaxIndex) {
          throw new IOException(s"Index must be in order and unique: $index in line " + line)
        } else {
          featureMaxIndex = index
        }
        val value = splits(1).toDouble
        Node(index, value)
      } .toList
      instances += Instance(x, y)
      maxIndex = maxIndex max featureMaxIndex
    }
    if (param.gamma == 0 && maxIndex > 0) {
      param.gamma = 1.0 / maxIndex
    }
    new Problem(instances.result())
  }
}
