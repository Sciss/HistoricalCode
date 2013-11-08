package de.sciss.svm
package solve

private[svm] object OneClassSolver extends FormulationSolver {
  def solve(problem: Problem, param: Parameters): Solution = {
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

    val solver = new Solver(
      problem = problem,
      param   = param,
      Q       = new OneClassQMatrix(problem, param),
      p       = zeros,
      y       = ones,
      alpha  = alpha,
      Cp      = 1.0,
      Cn      = 1.0)

    solver.solve()
  }
}
