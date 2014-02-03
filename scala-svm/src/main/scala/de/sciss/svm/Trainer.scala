package de.sciss.svm

import scala.math.abs

trait Trainer {
  def train(problem: Problem, param: Parameters): Model

  protected def solver: FormulationSolver

  def trainOne(problem: Problem, param: Parameters, Cp: Double, Cn: Double): DecisionFunction = {
    val solution = solver.solve(problem, param, Cp = Cp, Cn = Cn)

    println(s"obj = ${solution.obj}, rho = ${solution.rho}")

    val nSV = solution.alpha.count(abs(_) > 0)

    var nBSV = 0
    for (
      i <- 0 until problem.size if abs(solution.alpha(i)) > 0
    ) {
      if (problem.y(i) > 0) {
        if (abs(solution.alpha(i)) >= solution.upperBoundP)
          nBSV += 1
      } else {
        if (abs(solution.alpha(i)) >= solution.upperBoundN)
          nBSV += 1
      }
    }

    println("nSV = " + nSV + ", nBSV = " + nBSV)

    DecisionFunction(solution.alpha, solution.rho)
  }
}

///** <pre>
//  * val param = new Parameters(new LinearKernel)
//  * val svm = SVM("one_class")
//  * val problem = SVMProblem.get(param, ...)
//  * val model = svm.trainer.train(param, problem)
//  * val y = model.predict(...)
//  * </pre>
//  *
//  * @author szhu
//  */
//object SVM {
//
//  //  def apply(name: String): SVM = name match {
//  //    case OneClassSVM  .name => OneClassSVM
//  //    case EpsilonSVRSVM.name => EpsilonSVRSVM
//  //    case NuSVRSVM     .name => NuSVRSVM
//  //    // TODO
//  //    case _ => throw new IllegalArgumentException("Invalid SVM type: " + name)
//  //  }
//
//  def oneClass(kernel: Kernel, nu: Double, eps: Double): (Type, Parameters) =
//    (Type.OneClass, new Parameters(kernel))
//}

// val C_SVC = Value("c_svc")
// val NU_SVC = Value("nu_svc")
//object OneClassSVM extends SVM {
//  val name    = "one_class"
//  val trainer = new OneClassTrainer
//}
//
//object EpsilonSVRSVM extends SVM {
//  val name    = "epsilon_svr"
//  val trainer = new EpsilonSVRTrainer
//}
//
//object NuSVRSVM extends SVM {
//  val name    = "nu_svr"
//  val trainer = new NuSVRTrainer
//}

//class MultipleClassSVM extends SVM {
//
//  def train(param: Parameters): SVMModel = {
//    val numClasses = 1;
//    null
//  }
//}