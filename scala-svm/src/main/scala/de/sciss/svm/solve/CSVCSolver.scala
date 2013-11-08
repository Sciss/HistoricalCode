package de.sciss.svm
package solve

private[svm] object CSVCSolver extends FormulationSolver {
  //  const svm_problem *prob, const svm_parameter* param,
  // 	double *alpha, Solver::SolutionInfo* si, double Cp, double Cn)
  def solve(problem: Problem, param: Parameters, Cp: Double, Cn: Double): Solution = {
    val len       = problem.size
    val minusOnes = Vec.fill(len)(-1.0) // new Array[Double](l)
    val alpha     = Vec.fill(len)( 0.0)

    val y         = Vec.tabulate(len) {
      case i if problem.y(i) > 0  =>  1
      case _                      => -1
    }

    val solver = new Solver(
      problem = problem,
      param   = param,
      Q       = new ClassificationQMatrix(problem, param, y),
      p       = minusOnes,
      y       = y,
      alpha   = alpha,
      Cp      = Cp,
      Cn      = Cn)

    val solution  = solver.solve()
    val sumAlpha  = solution.alpha.sum

    //   	s.Solve(l, SVC_Q(*prob,*param,y), minus_ones, y,
    //   		alpha, Cp, Cn, param->eps, si, param->shrinking);

    if (Cp == Cn) logInfo(s"nu = ${sumAlpha / (Cp * len)}\n")

    val alphaNew = (solution.alpha zip y).map { case (ai, yi) => ai * yi }
    solution.copy(alpha = alphaNew)
  }
}
