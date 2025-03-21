package de.sciss.svm

import org.junit.Assert._
import scala.io.Source
import java.io.IOException
import AssertUtil._
import org.scalatest.{BeforeAndAfter, FunSuite}

/*
  to run only this suite:

  test-only de.sciss.svm.ProblemSuite
 */
class ProblemSuite extends FunSuite with BeforeAndAfter {
  val DELTA = 10e-6

  var param: Parameters = _

  before {
    param = new Parameters(LinearKernel, 0, 0)
  }

  test("get") {
    val source  = Source.fromString("-1\t1:1.000000\t2:22.080000\t3:11.460000")
    val x       = Array(List(Node(1, 1.0), Node(2, 22.08), Node(3, 11.46)))
    val y       = Array(-1.0)
    val problem = Problem.read(param, source)

    assertEquals   (1, problem.size)
    svmAssertEquals(y, problem.ys .map(_.toDouble))
    svmAssertEquals(x, problem.xs)
    assertEquals(param.gamma, 1.0 / 3, DELTA)
  }

  test("get with multiple lines") {
    val source = Source.fromString("-1\t1:1.000000\t2:22.080000\t3:11.460000\n+1\t1:19")
    val x = Array(
      List(Node(1, 1.0), Node(2, 22.08), Node(3, 11.46)),
      List(Node(1, 19.0)))
    val y = Array(-1.0, 1.0)
    val problem = Problem.read(param, source)

    assertEquals   (2, problem.size)
    svmAssertEquals(y, problem.ys .map(_.toDouble))
    svmAssertEquals(x, problem.xs)
    assertEquals(param.gamma, 1.0 / 3, DELTA)
  }

  test("get with param.gamma 0.1") {
    param.gamma = 0.1
    val source = Source.fromString("-1\t1:1.000000\t2:22.080000\t3:11.460000")
    /* val x = */ Array(List(Node(1, 1.0), Node(2, 22.08), Node(3, 11.46)))
    /* val y = */ Array(-1.0)
    /* val problem = */ Problem.read(param, source)
    assertEquals(param.gamma, 0.1, DELTA)
  }

  intercept[IOException] {
    val source = Source.fromString("-1\t1:1.000000\t2:22.080000\t3:11.460000\n+1")
    Problem.read(param, source)
  }

  intercept[IOException] {
    val source = Source.fromString("-1\t11.000000\t2:22.080000\t3:11.460000")
    Problem.read(param, source)
  }

  intercept[IOException] {
    val source = Source.fromString("-1\t4:1.000000\t2:22.080000\t3:11.460000")
    Problem.read(param, source)
  }

  intercept[IOException] {
    val source = Source.fromString("-1\t2:1.000000\t2:22.080000\t3:11.460000")
    Problem.read(param, source)
  }
}