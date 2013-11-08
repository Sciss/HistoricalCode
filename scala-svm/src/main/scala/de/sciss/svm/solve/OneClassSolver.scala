package de.sciss.svm
package solve

private[svm] object OneClassSolver extends FormulationSolver {
  def solve(problem: Problem, param: Parameters, Cp: Double, Cn: Double): Solution = {
    val l     = problem.size
    val nd    = param.nu * l
    val n     = nd.toInt

    val alpha = Vec.tabulate(l) {
      case i if i < n           => 1.0
      case i if i == n && i < l => nd - n
      case _                    => 0.0
    }

    val zeros = Vec.fill(l)(0.0)
    val ones  = Vec.fill(l)(1)

    //    s.Solve(l, ONE_CLASS_Q(*prob,*param), zeros, ones,
    //   		alpha, 1.0, 1.0, param->eps, si, param->shrinking);

    val solver = new Solver(
      problem = problem,
      param   = param,
      Q       = new OneClassQMatrix(problem, param),
      p       = zeros,
      y       = ones,
      alpha   = alpha,
      Cp      = Cp,
      Cn      = Cn)

    solver.solve()
  }
}
