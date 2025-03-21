/*
 *  Kernel.scala
 *  (ScalaSVM)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *  Copyright (c) 2013-2014 Shixiong Zhu.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.svm

import java.io.PrintStream
import java.io.ByteArrayOutputStream

object KernelType extends Enumeration {
  type KernelType = Value
  val LINEAR      = Value("linear")
  val POLY        = Value("poly")
  val RBF         = Value("rbf")
  val SIGMOID     = Value("sigmoid")
  val PRECOMPUTED = Value

  def dot(x: List[Node], y: List[Node]): Double = {
    if (x == Nil || y == Nil) 0
    else {
      if (x.head.index == y.head.index) {
        x.head.value * y.head.value + dot(x.tail, y.tail)
      } else {
        if (x.head.index < y.head.index) dot(x.tail, y)
        else dot(x, y.tail)
      }
    }
  }

  def powInt(base: Double, times: Int) = {
    require(times >= 0, s"Parameter times $times must be >= 0")
    var tmp = base
    var ret = 1.0
    var t   = times
    while (t > 0) {
      if ((t & 1) == 1) ret *= tmp
      tmp *= tmp
      t >>= 1
    }
    ret
  }
}

import KernelType._

sealed trait Kernel {
  // def tpe: KernelType
  /** Calculates the kernel product from a given matrix of features. */
  def apply(x: List[Node], y: List[Node]): Double

  def id: String

  def write(ps: PrintStream): Unit = {
    ps.println(s"kernel_type $id")
    writeData(ps)
  }

  def writeString: String = {
    val baos  = new ByteArrayOutputStream
    val ps    = new PrintStream(baos)
    write(ps)
    ps.flush()
    baos.toString("UTF-8")
  }

  protected def writeData(ps: PrintStream): Unit
}

case object LinearKernel extends Kernel {
  final val id = "linear"

  override def apply(x: List[Node], y: List[Node]): Double = dot(x, y)

  protected def writeData(ps: PrintStream) = ()
}

object PolynomialKernel {
  final val id = "polynomial"
}
case class PolynomialKernel(gamma: Double, coef0: Double = 0, degree: Int = 3) extends Kernel {
  require(degree >= 0) // Why degree == 0 is valid?

  def id = PolynomialKernel.id

  override def apply(x: List[Node], y: List[Node]): Double = powInt(gamma * dot(x, y) + coef0, degree)

  protected def writeData(ps: PrintStream): Unit = {
    ps.println(s"degree $degree")
    ps.println(s"gamma $gamma")
    ps.println(s"coef0 $coef0")
  }
}

object RBFKernel {
  final val id = "rbf"
}
/** @see http://en.wikipedia.org/wiki/Support_vector_machine#Nonlinear_classification
  * @see http://en.wikipedia.org/RBF_kernel
  */
case class RBFKernel(gamma: Double) extends Kernel {
  require(gamma >= 0) // Why gamma == 0 is valid?

  def id = RBFKernel.id

  override def apply(x: List[Node], y: List[Node]): Double = {
    def rbf(x: List[Node], y: List[Node], sum: Double): Double = {
      if (x == Nil && y == Nil) math.exp(-gamma * sum)
      else if (x == Nil)
        rbf(Nil, y.tail, sum + y.head.value * y.head.value)
      else if (y == Nil)
        rbf(x.tail, Nil, sum + x.head.value * x.head.value)
      else {
        if (x.head.index == y.head.index) {
          rbf(x.tail, y.tail, (x.head.value - y.head.value) * (x.head.value - y.head.value) + sum)
        } else {
          if (x.head.index < y.head.index) rbf(x.tail, y, sum + x.head.value * x.head.value)
          else rbf(x, y.tail, sum + y.head.value * y.head.value)
        }
      }
    }
    rbf(x, y, 0)
  }

  protected def writeData(ps: PrintStream) = ps.println(s"gamma $gamma")
}

object SigmoidKernel {
  final val id = "sigmoid"
}
/** @see http://en.wikipedia.org/wiki/Support_vector_machine#Nonlinear_classification */
case class SigmoidKernel(gamma: Double, coef0: Double = 0) extends Kernel {
  require(gamma >= 0) // Why gamma == 0 is valid?

  def id = SigmoidKernel.id

  override def apply(x: List[Node], y: List[Node]): Double = math.tanh(gamma * dot(x, y) + coef0)

  protected def writeData(ps: PrintStream): Unit = {
    ps.println(s"gamma $gamma")
    ps.println(s"coef0 $coef0")
  }
}

case object PrecomputedKernel extends Kernel {
  final val id = "precomputed"

  override def apply(x: List[Node], y: List[Node]): Double = {
    // TODO
    ???
  }

  protected def writeData(ps: PrintStream) = ()
}