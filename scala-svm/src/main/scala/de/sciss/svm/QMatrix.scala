package de.sciss.svm

object QMatrix {
  //  def swap[T](array: scala.collection.mutable.Seq[T], i: Int, j: Int): Unit = {
  //    val temp = array(i)
  //    array(i) = array(j)
  //    array(j) = temp
  //  }

  def swap(array: Array[Int], i: Int, j: Int): Unit = {
    val temp = array(i)
    array(i) = array(j)
    array(j) = temp
  }

  def swap(array: Array[Double], i: Int, j: Int): Unit = {
    val temp = array(i)
    array(i) = array(j)
    array(j) = temp
  }

  def swap[T <: AnyRef](array: Array[T], i: Int, j: Int): Unit = {
    val temp = array(i)
    array(i) = array(j)
    array(j) = temp
  }
}

/** 1. SVC: support vector classification (two-class and multi-class).
  * 2. SVR: support vector regression.
  * 3. One-class SVM.
  */
trait QMatrix {
  //  protected def problem: Problem
  //  protected val x: Array[List[Node]]  = problem.xs.toArray
  //  protected val y: Array[Int]         = problem.ys.toArray

  def apply    (i: Int, j: Int): Double
  def swapIndex(i: Int, j: Int): Unit
}

class OneClassQMatrix(val problem: Problem, val param: Parameters) extends QMatrix {
  import QMatrix.swap

  private[this] val px: Array[List[Node]]  = problem.xs.toArray
  // cache diagonal... TODO: why?
  private[this] val qd  = Array.tabulate(problem.size)(i => param.kernel(px(i), px(i)))
  private[this] val py  = problem.ys.toArray // clone()

  def swapIndex(i: Int, j: Int): Unit = {
    swap(px, i, j)
    swap(py, i, j)
    swap(qd, i, j)
  }

  override def apply(i: Int, j: Int): Double =
    if (i == j) qd(i)
    else        param.kernel(px(i), px(j))
}

class ClassificationQMatrix(val problem: Problem, val param: Parameters, y: Vec[Int]) extends QMatrix {
  import QMatrix.swap

  def swapIndex(i: Int, j: Int): Unit = ???

  private[this] val x: Array[List[Node]]  = problem.xs.toArray

  def apply(i: Int, j: Int): Double =
    y(i) * y(j) * param.kernel(x(i), x(j))
}