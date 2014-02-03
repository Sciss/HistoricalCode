/*
 *  OneClassSolver.scala
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
package solve

object OneClassSolver extends FormulationSolver {
  def solve(problem: Problem, param: Parameters, Cp: Double, Cn: Double): Solution = {
    val len   = problem.size
    val nd    = param.nu * len
    val n     = nd.toInt

    val alpha = Vec.tabulate(len) {
      case i if i < n             => 1.0
      case i if i == n && i < len => nd - n
      case _                      => 0.0
    }

    val zeros = Vec.fill(len)(0.0)
    val ones  = Vec.fill(len)(1)

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
