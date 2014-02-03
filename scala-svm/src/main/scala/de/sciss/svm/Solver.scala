/*
 *  Solver.scala
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

import de.sciss.svm.solve.OneClassSolver
import math.{min, max}
import collection.breakOut
import scala.annotation.elidable

object Solver {
  final val LowerBound = 0
  final val UpperBound = 1
  final val Free       = 2

  final val Tau        = 1e-12

  def solveOneClass(problem: Problem, param: Parameters): Solution =
    OneClassSolver.solve(problem, param, 1.0, 1.0)

  def solveEpsilonSVR(problem: Problem, param: EpsilonSVRSVMParameter): Solution = {
    val len         = problem.size
    val len2        = len * 2
    val alpha2      = Vec.fill    (len2)(0.0)
    val linearTerm  = Vec.tabulate(len2) {
      case i if i < len => param.p - problem.y(i)
      case i            => param.p + problem.y(i - len)
    }
    val y = Vec.tabulate(len2) {
      case i if i < len =>  1
      case _            => -1
    }

    val solver = new Solver(
      problem   = problem,
      param     = param,
      Q         = new OneClassQMatrix(problem, param), // TODO
      p         = linearTerm,
      y         = y,
      alpha     = alpha2,
      Cp        = param.C,
      Cn        = param.C)

    solver.solve()

    // TODO
  }

  def solveNuSVR(problem: Problem, param: EpsilonSVRSVMParameter): Solution = {
    // var sum = param.C * param.nu * problem.size / 2
    val len         = problem.size
    val len2        = len * 2

    val alpha2      = Vec.fill    (len2)(0.0)
    val linearTerm  = Vec.tabulate(len2) {
      case i if i < len => -problem.y(i)      .toDouble
      case i            =>  problem.y(i - len).toDouble
    }
    val y = Vec.tabulate(len2) {
      case i if i < len =>  1
      case _            => -1
    }

    val solver = new Solver(
      problem = problem,
      param   = param,
      Q       = new OneClassQMatrix(problem, param), // TODO
      p       = linearTerm,
      y       = y,
      alpha   = alpha2,
      Cp      = param.C,
      Cn      = param.C)

    solver.solve()
  }
}

class Solver(problem: Problem,
             param  : Parameters,
             Q      : QMatrix,
             p      : Vec[Double],
             y      : Vec[Int],
             alpha  : Vec[Double],
             Cp     : Double,
             Cn     : Double) {
  import Solver._

  import param.{eps, shrinking}
  import problem.{size => len}

  private val _alpha: Array[Double] = alpha.toArray
  private val _y                    = y.toArray
  private val _p                    = p.toArray

  private var activeSize  = len
  private val activeSet   = (0 until len).toArray
  private val alphaStatus = Array.tabulate(len)(updateAlphaStatus)

  private def updateAlphaStatus(i: Int): Int =
    if      (_alpha(i) >= getC(i)) UpperBound
    else if (_alpha(i) <= 0      ) LowerBound
    else                           Free

  private def getC(i: Int): Double = if (_y(i) > 0) Cp else Cn

  private def isUpperBound(i: Int) = alphaStatus(i) == UpperBound
  private def isLowerBound(i: Int) = alphaStatus(i) == LowerBound
  private def isFree      (i: Int) = alphaStatus(i) == Free

  private var counter   = min(len, 1000) + 1
  private var unshrink  = false

  //  void Solver::Solve(int l, const QMatrix& Q, const double *p_, const schar *y_,
  //  		   double *alpha_, double Cp, double Cn, double eps,
  //  		   SolutionInfo* si, int shrinking)
  def solve(): Solution = {
    init()
    val iter      = optimize()

    val solution  = new Solution(
      obj         = calculateObjectiveValue(),
      rho         = calculateRho(),
      upperBoundP = Cp,
      upperBoundN = Cn,
      r           = 0,
      alpha       = activeSet.map(_alpha)(breakOut))

    logInfo(s"\noptimization finished, #iter = $iter\n")

    solution
  }

  private def init(): Unit = {
    // initialize	gradient
    for(i <- 0 until len if !isLowerBound(i)) {
      val ai = _alpha(i)
      for (j <- 0 until len) {
        grad(j) += ai * Q(i, j)
      }
      if (isUpperBound(i)) {
        for (j <- 0 until len) {
          gradBar(j) += getC(i) * Q(i, j)
        }
      }
    }
  }

  private def doShrinking(): Unit = {
    //    int i;
    //   	double Gmax1 = -INF;		// max { -y_i * grad(f)_i | i in I_up (\alpha) }
    //   	double Gmax2 = -INF;		// max {  y_i * grad(f)_i | i in I_low(\alpha) }

    var gradMax1  = -Inf
    var gradMax2  = -Inf

    // find maximal violating pair first
    for (i <- 0 until activeSize) {
      if (_y(i) == 1) {
        if (!isUpperBound(i)) gradMax1 = max(gradMax1, -grad(i))
        if (!isLowerBound(i)) gradMax2 = max(gradMax2,  grad(i))
      } else {
        if (!isUpperBound(i)) gradMax2 = max(gradMax2, -grad(i))
        if (!isLowerBound(i)) gradMax1 = max(gradMax1,  grad(i))
      }
    }

    if (!unshrink && gradMax1 + gradMax2 <= eps * 10) {
      unshrink = true
      reconstructGradient()
      activeSize = len
      logInfo("*")
    }

    def beShrunk(j: Int): Boolean =
    	if(isUpperBound(j)) {
    		if (_y(j) == 1)
    			-grad(j) > gradMax1
    		else
    			-grad(j) > gradMax2
    	} else if(isLowerBound(j)) {
    		if (_y(j) == 1)
    			grad(j) > gradMax2
    		else
          grad(j) > gradMax1
    	} else false

    var i = 0
    while (i < activeSize) {  // WARNING: activeSize is mutated in the loop
      if (beShrunk(i)) {
        activeSize -= 1
        var continue = true
        while (continue && activeSize > i) {
          if (!beShrunk(activeSize)) {
            swapIndex(i, activeSize)
            continue = false
          }
          activeSize -= 1
        }
      }
      i += 1
    }
  }

  private def swapIndex(i: Int, j: Int): Unit = {
    import QMatrix.swap
    Q.swapIndex      (i, j)
    swap(_y         , i, j)
    swap(grad       , i, j)
    swap(alphaStatus, i, j)
    swap(_alpha     , i, j)
    swap(_p         , i, j)
    swap(activeSet  , i, j)
    swap(gradBar    , i, j)
  }

  private def calculateRho(): Double = {
    var ub        = Inf
    var lb        = -Inf
    var sumFree   = 0.0
    var numFree   = 0
    for (i <- 0 until activeSize) {
      val yG = _y(i) * grad(i)
      if (isUpperBound(i)) {
        if (_y(i) == -1) ub = min(ub, yG) else lb = max(lb, yG)
      } else if (isLowerBound(i)) {
        if (_y(i) ==  1) ub = min(ub, yG) else lb = max(lb, yG)
      } else {
        numFree += 1
        sumFree += yG
      }
    }

    if (numFree > 0) {
      sumFree / numFree
    } else {
      (ub + lb) / 2
    }
  }

  @elidable(elidable.INFO) private def debug(what: => String): Unit = println(s"[DEBUG] $what")

  private def calculateObjectiveValue(): Double =
    (0 until len).iterator.map(i => _alpha(i) * (grad(i) + _p(i))).sum / 2

  private def optimize(): Int = {
    val maxIter = maxIteration
    var iter    = 0

    while (iter < maxIter) {
      // debug(s"iter $iter")

      counter -= 1
      if (counter == 0) {
        counter = min(len, 1000)
        if (shrinking) doShrinking()
        println(".")
      }

      val (i, j) = selectWorkingSet().getOrElse {
  			// reconstruct the whole gradient
        reconstructGradient()
	  		// reset active set size and check
        activeSize = len
        logInfo("*")

        val ij = selectWorkingSet().getOrElse(return iter)
        counter = 1
        ij
      }

      iter += 1

      val ci    = getC(i)
      val cj    = getC(j)
      val oldAi = _alpha(i)
      val oldAj = _alpha(j)

      if (_y(i) != _y(j)) {
        val quadCoef = {
          val tmp = Q(i, i) + Q(j, j) + 2 * _y(i) * _y(j) * Q(i, j)
          if (tmp <= 0) Tau else tmp
        }

        val delta  = (-grad(i) - grad(j)) / quadCoef
        val diff   = oldAi - oldAj
        _alpha(i) += delta
        _alpha(j) += delta

        if (diff > 0) {
          if (_alpha(j) < 0) {
            _alpha(j) = 0
            _alpha(i) = diff
          }
        } else {
          if (_alpha(i) < 0) {
            _alpha(i) = 0
            _alpha(j) = -diff
          }
        }
        if (diff > ci - cj) {
          if (_alpha(i) > ci) {
            _alpha(i) = ci
            _alpha(j) = ci - diff
          }
        } else {
          if (_alpha(j) > cj) {
            _alpha(j) = cj
            _alpha(i) = cj + diff
          }
        }

      } else {  // _y(i) == _y(j)
        val quadCoef = {
          val tmp = Q(i, i) + Q(j, j) - 2 * _y(i) * _y(j) * Q(i, j)
          if (tmp <= 0) Tau else tmp
        }

        val delta  = (grad(i) - grad(j)) / quadCoef
        val sum    = oldAi + oldAj
        _alpha(i) -= delta
        _alpha(j) += delta

        if (sum > ci) {
          if (_alpha(i) > ci) {
            _alpha(i) = ci
            _alpha(j) = sum - ci
          }
        } else {
          if (_alpha(j) < 0) {
            _alpha(j) = 0
            _alpha(i) = sum
          }
        }
        if (sum > cj) {
          if (_alpha(j) > cj) {
            _alpha(j) = cj
            _alpha(i) = sum - cj
          }
        } else {
          if (_alpha(i) < 0) {
            _alpha(i) = 0
            _alpha(j) = sum
          }
        }
      }

  		// update grad
      val deltaAi = _alpha(i) - oldAi
      val deltaAj = _alpha(j) - oldAj

      for (k <- 1 until activeSize) {
        grad(k) += Q(i, k) * deltaAi + Q(j, k) * deltaAj
      }

  		// update alpha_status and G_bar
			val ui = isUpperBound(i)
      val uj = isUpperBound(j)
			updateAlphaStatus(i)
      updateAlphaStatus(j)

			if (ui != isUpperBound(i)) {
        val cis = if (ui) -ci else ci
        for (k <- 0 until len) gradBar(k) += cis * Q(i, k)
			}
			if (uj != isUpperBound(j)) {
        val cjs = if (uj) -cj else cj
        for (k <- 0 until len) gradBar(k) += cjs * Q(j, k)
			}
    }

    if(activeSize < len) {
      // reconstruct the whole gradient to calculate objective value
      reconstructGradient()
      activeSize = len
      logInfo("*")
    }
    logWarn("reaching max number of iterations\n")

    iter
  }

  // @inline private def isValidWorkingSet(i: Int, j: Int) = j != -1

  private val grad    = _p.toArray
  private val gradBar = new Array[Double](len)

  // the return value's `_2` is `-1` if already optimal
  private def selectWorkingSet(): Option[(Int, Int)] = {
    // return i,j such that
    // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
    // j: minimizes the decrease of obj value
    //    (if quadratic coefficient <= 0, replace it with tau)
    //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

    var maxGrad     = -Inf
    var mxi         = -1

    for (i <- 0 until activeSize) {
      if (_y(i) == 1) {
        val gim = -grad(i)
        if (!isUpperBound(i) && gim >= maxGrad) {
          mxi     = i
          maxGrad = gim
        }
      } else {
        val gi = grad(i)
        if (!isLowerBound(i) && gi >= maxGrad) {
          mxi     = i
          maxGrad = gi
        }
      }
    }

    debug(s"selectWorkingSet; activeSize = $activeSize; maxGrad $maxGrad; mxi $mxi")

    var minObj      = Inf
    var maxGrad2    = -Inf
    var mni         = -1

    for (j <- 0 until activeSize) {
      val isPos = _y(j) == 1
      if ((isPos && !isLowerBound(j)) ||
         (!isPos && !isUpperBound(j))) {

        val gj        = grad(j)
        val gjs       = if (isPos) gj else -gj
        val gradDiff  = maxGrad + gjs
        if (gjs >= maxGrad2) maxGrad2 = gjs

        if (gradDiff > 0) {
          val yf          = if (isPos) -2.0 else 2.0
          val quadCoef    = Q(mxi, mxi) + Q(j, j) + yf * _y(mxi) * Q(mxi, j)
          val gradDiffSqM = -(gradDiff * gradDiff)
          val objDiff     = gradDiffSqM / (if (quadCoef > 0) quadCoef else Tau)

          if (objDiff <= minObj) {
            mni     = j
            minObj  = objDiff
          }
        }
      }
    }

    // debug(s"selectWorkingSet; maxGrad2 $maxGrad2; mni $mni")

    if (maxGrad + maxGrad2 < eps) None else Some(mxi, mni)
  }

  private def maxIteration: Int = {
    val iter = if (len > Int.MaxValue / 100)
      Int.MaxValue // overflow
    else len * 100
    max(iter, 10000000) // We run at most 10000000 times
  }

  private def reconstructGradient(): Unit = {
    if (activeSize == len) return

    for (j <- activeSize until len) {
      grad(j) = gradBar(j) + _p(j)
    }

    val numFree = (0 until activeSize).count(isFree)
    if (2 * numFree < activeSize)
      logWarn("using -h 0 may be faster\n")

    if (numFree * len > 2 * activeSize * (len - activeSize)) {
      for {
        i <- activeSize until len
        j <- 0 until activeSize if isFree(j)
      } {
        grad(i) += _alpha(j) * Q(i, j)
      }
    } else {
      for {
        i <- 0 until activeSize if isFree(i)
        j <- activeSize until len
      } {
        grad(j) += _alpha(i) * Q(i, j)
      }
    }
  }
}

trait FormulationSolver {
  def solve(problem: Problem, param: Parameters, Cp: Double, Cn: Double): Solution
}