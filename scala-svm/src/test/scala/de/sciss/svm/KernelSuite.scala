package de.sciss.svm

import scala.math.{exp, tanh, pow}
import org.scalatest._
import org.junit.Assert._
import KernelType._
import AssertUtil._

/*
  to run only this suite:

  test-only de.sciss.svm.KernelTypeSuite
 */
class KernelTypeSuite extends FunSuite {
  test("dot") {
    val x = List(Node(1, 0.4), Node(2, 0.5))
    val y = List(Node(1, 0.9), Node(3, 0.4))
    svmAssertEquals(0.36, dot(x, y))
  }

  test("dot with normal") {
    val x = List(Node(1, 0.4), Node(2, 0.5))
    val y = List(Node(1, 0.9), Node(2, 0.4))
    svmAssertEquals(0.56, dot(x, y))
  }

  test("dot with empty") {
    val x = List(Node(1, 0.4), Node(2, 0.5))
    val y = List(Node(4, 0.9), Node(3, 0.4))
    svmAssertEquals(0, dot(x, y))
  }

  test("powi") {
    svmAssertEquals(16, powInt(2.0, 4))
    svmAssertEquals(1, powInt(2.0, 0))
    svmAssertEquals(1.21, powInt(1.1, 2))
    svmAssertEquals(1.23 * 1.23 * 1.23 * 1.23 * 1.23, powInt(1.23, 5))
  }
}

/*
  to run only this suite:

  test-only de.sciss.svm.LinearKernelSuite
 */
class LinearKernelSuite extends FunSuite {

  val DELTA = 10e-6

  test("toString") {
    assertEquals("kernel_type linear\n", LinearKernel.writeString)
  }

}

/*
  to run only this suite:

  test-only de.sciss.svm.PolynomialKernelSuite
 */
class PolynomialKernelSuite extends FunSuite {

  val DELTA = 10e-6

  test("toString") {
    assertEquals("kernel_type polynomial\n" +
      "degree 3\n" +
      "gamma 1.0\n" +
      "coef0 2.0\n", new PolynomialKernel(1.0, 2.0, 3).writeString)
  }

  test("apply") {
    val x = List(Node(1, 0.4), Node(2, 0.5))
    val y = List(Node(1, 0.9), Node(3, 0.4))

    svmAssertEquals(pow(1.0 * 0.36 + 2.0, 3), new PolynomialKernel(1.0, 2.0, 3).apply(x, y))
  }
}

/*
  to run only this suite:

  test-only de.sciss.svm.RBFKernelSuite
 */
class RBFKernelSuite extends FunSuite {

  test("toString") {
    assertEquals("kernel_type rbf\n" +
      "gamma 1.0\n", new RBFKernel(1.0).writeString)
  }

  test("apply") {
    val x = List(Node(1, 0.4), Node(2, 0.5))
    val y = List(Node(1, 0.9), Node(3, 0.4))

    svmAssertEquals(exp(-2.0 * 0.66), new RBFKernel(2.0).apply(x, y))
  }
}

/*
  to run only this suite:

  test-only de.sciss.svm.SigmoidKernelSuite
 */
class SigmoidKernelSuite extends FunSuite {

  test("toString") {
    assertEquals("kernel_type sigmoid\n" +
      "gamma 1.0\n" +
      "coef0 2.0\n", new SigmoidKernel(1.0, 2.0).writeString)
  }

  test("apply") {
    val x = List(Node(1, 0.4), Node(2, 0.5))
    val y = List(Node(1, 0.9), Node(3, 0.4))

    svmAssertEquals(tanh(1.0 * 0.36 + 2.0), new SigmoidKernel(1.0, 2.0).apply(x, y))
  }
}
