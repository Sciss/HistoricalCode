package de.sciss.svm

import org.junit.Assert._
import scala.io.Source
import AssertUtil._
import org.scalatest.FunSuite

/*
  to run only this suite:

  test-only de.sciss.svm.TrainerSuite
 */
class TrainerSuite extends FunSuite {   // XXX FAILS

  test("1 train case") {
    val param   = new Parameters(LinearKernel)

    val source  = Source.fromString("-1\t1:1.0\t2:22.08\t3:11.46")
    val problem = Problem.read(param, source)

    val sol       = Solver.solveOneClass      (problem, param)
    val model     = SVM.OneClass.trainer.train(problem, param)
    svmAssertEquals(309.929000, model.rho)
    svmAssertEquals(Vec(new SupportVector(problem.x(0), 1)), model.supportVector)
    svmAssertEquals(Vec(0.5), model.coefficientVector)
  }

  test("2 train case") {
    val param     = new Parameters(LinearKernel)

    val source    = Source.fromString("-1\t1:1.0\t2:22.08\t3:11.46\n" +
                                      "+1\t1:2.0\t2:22.08\t3:11.46")
    val problem   = Problem.read(param, source)

    val solution  = Solver.solveOneClass(problem, param)
    svmAssertEquals(309.929000, solution.obj)
    svmAssertEquals(620.358000, solution.rho)
    assertEquals   (2, solution.alpha.size)
    svmAssertEquals(1, solution.alpha(0))
    svmAssertEquals(0, solution.alpha(1))
    svmAssertEquals(1, solution.upperBoundP)
    svmAssertEquals(1, solution.upperBoundN)
    svmAssertEquals(0, solution.r)
  }
}