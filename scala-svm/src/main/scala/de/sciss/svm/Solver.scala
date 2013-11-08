package de.sciss.svm

import de.sciss.svm.solve.OneClassSolver

object Solver {
  sealed trait AlphaStatus
  case object LowerBound extends AlphaStatus
  case object UpperBound extends AlphaStatus
  case object Free       extends AlphaStatus

  final val Tau         = 1e-12

  def solveOneClass(problem: Problem, param: Parameters): Solution =
    OneClassSolver.solve(problem, param, 0.0, 0.0)

  def solveEpsilonSVR(problem: Problem, param: EpsilonSVRSVMParamter): Solution = {
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
      alpha    = alpha2,
      Cp        = param.C,
      Cn        = param.C)

    solver.solve()

    // TODO
  }

  def solveNuSVR(problem: Problem, param: EpsilonSVRSVMParamter): Solution = {
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

  private var activeSize  = len
  private val activeSet   = (0 until len).toArray

  private def alphaStatus(i: Int): AlphaStatus = {
    if      (_alpha(i) >= getC(i)) UpperBound
    else if (_alpha(i) <= 0      ) LowerBound
    else                           Free
  }

  private def getC(i: Int): Double = if (y(i) > 0) Cp else Cn

  private def isUpperBound(i: Int) = alphaStatus(i) == UpperBound
  private def isLowerBound(i: Int) = alphaStatus(i) == LowerBound
  private def isFree      (i: Int) = alphaStatus(i) == Free

  private var counter = math.min(len, 1000) + 1

  private var unshrink = false

  //  void Solver::Solve(int l, const QMatrix& Q, const double *p_, const schar *y_,
  //  		   double *alpha_, double Cp, double Cn, double eps,
  //  		   SolutionInfo* si, int shrinking)
  def solve(): Solution = {
    init()
    optimize()

    new Solution(
      obj         = calculateObjectiveValue,
      rho         = calculateRho,
      upperBoundP = Cp,
      upperBoundN = Cn,
      r           = 0,
      alpha       = _alpha.toIndexedSeq)
  }

  def init(): Unit = {
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

  def doShrinking(): Unit = {
    //    int i;
    //   	double Gmax1 = -INF;		// max { -y_i * grad(f)_i | i in I_up (\alpha) }
    //   	double Gmax2 = -INF;		// max {  y_i * grad(f)_i | i in I_low(\alpha) }

    var gradMax1  = Double.NegativeInfinity
    var gradMax2  = Double.NegativeInfinity

    // find maximal violating pair first
    for (i <- 0 until activeSize) {
      if (y(i) == 1) {
        if (!isUpperBound(i)) gradMax1 = math.max(gradMax1, -grad(i))
        if (!isLowerBound(i)) gradMax2 = math.max(gradMax2,  grad(i))
      } else {
        if (!isUpperBound(i)) gradMax2 = math.max(gradMax2, -grad(i))
        if (!isLowerBound(i)) gradMax1 = math.max(gradMax1,  grad(i))
      }
    }

    if (!unshrink && gradMax1 + gradMax2 <= eps * 10) {
      unshrink = true
      reconstructGradient()
      activeSize = len
      info("*")
    }

    def beShrunk(j: Int): Boolean =
    	if(isUpperBound(j)) {
    		if (y(j) == 1)
    			-grad(j) > gradMax1
    		else
    			-grad(j) > gradMax2
    	} else if(isLowerBound(j)) {
    		if (y(j) == 1)
    			grad(j) > gradMax2
    		else
          grad(j) > gradMax1
    	} else false

    var i = 0
    while (i < activeSize) {
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
    Q.swapIndex(i, j)
    ??? // swap(y, i, j)
    swap(grad, i, j)
    ??? // swap(alphaStatus, i, j)
    ??? // swap(alpha, i, j)
    ??? // swap(p, i, j)
    swap(activeSet, i, j)
    swap(gradBar, i, j)
  }

  def calculateRho: Double = {
    var ub        = Double.MaxValue
    var lb        = Double.MinValue
    var sumFree   = 0.0
    var numFree   = 0
    for (i <- 0 until activeSize) {
      val yG = y(i) * grad(i)
      if (isUpperBound(i)) {
        if (y(i) == -1) ub = ub min yG else lb = lb max yG
      } else if (isLowerBound(i)) {
        if (y(i) == +1) ub = ub min yG else lb = lb max yG
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

  def calculateObjectiveValue: Double =
    {
      for (i <- 0 until len)
        yield _alpha(i) * (grad(i) + p(i))
    }.sum / 2

  def optimize(): Unit = {
    val maxIter = maxIteration

    for (iter <- 0 until maxIter) {
      counter -= 1
      if (counter == 0) {
        counter = math.min(len, 1000)
        if (shrinking) doShrinking()
        println(".")
      }

      // TODO
      val (i, j) = selectWorkingSet()
      if (!isValidWorkingSet(i, j)) {
        reconstructGradient()

        activeSize = len
        // TODO

        val (i, j) = selectWorkingSet()
        if (!isValidWorkingSet(i, j)) {
          return
        } else {
          counter = 1
        }
      }

      var quadCoef = Q(i, i) + Q(j, j) - 2 * y(i) * y(j) * Q(i, j)
      if (quadCoef <= 0) {
        quadCoef = Tau
      }
      // val delta = -y(i) * G(i) + y(j) * G(j)
      val oldAi = _alpha(i)
      val oldAj = _alpha(j)
      _alpha(i) = _alpha(i) + (-grad(i) + y(i) * y(j) * grad(j)) / quadCoef
      _alpha(j) = _alpha(j) - (-y(i) * y(j) * grad(i) + grad(j)) / quadCoef

      val sum = y(i) * oldAi + y(j) * oldAj

      if (_alpha(i) > getC(i)) _alpha(i) = getC(i)
      if (_alpha(i) < 0) _alpha(i) = 0
      _alpha(j) = y(j) * (sum - y(i) * _alpha(i))

      if (_alpha(j) > getC(j)) _alpha(j) = getC(j)
      if (_alpha(j) < 0) _alpha(j) = 0
      _alpha(i) = y(i) * (sum - y(i) * _alpha(j))
      val deltaAi = _alpha(i) - oldAi
      val deltaAj = _alpha(j) - oldAj

      for (t <- 1 until activeSize) {
        grad(t) = grad(t) + Q(t, i) * deltaAi + Q(t, j) * deltaAj
      }
    }
  }

  def isValidWorkingSet(i: Int, j: Int) = j != -1

  private val grad: Array[Double] = p.toArray // clone()
  private val gradBar  = new Array[Double](len) // Array.fill(len)(0.0)

  def selectWorkingSet(): (Int, Int) = {
    var maxG = Double.MinValue
    var i = -1

    for (t <- 0 until len) {
      if (y(t) == +1) {
        if (!isUpperBound(t) && -grad(t) >= maxG) {
          i = t
          maxG = -grad(t)
        }
      } else {
        if (!isLowerBound(t) && grad(t) >= maxG) {
          i = t
          maxG = grad(t)
        }
      }
    }

    var j       = -1
    var minObj  = Double.MaxValue
    var minG    = Double.MaxValue

    for (t <- 0 until len) {
      if ((y(i) == +1 && !isLowerBound(t)) ||
          (y(i) == -1 && !isUpperBound(t))) {
        val gradDiff = maxG + y(t) * grad(t)
        if (-y(t) * grad(t) <= minG) {
          minG = -y(t) * grad(t)
        }
        if (gradDiff > 0) {
          val quadCoef = Q(i, i) + Q(t, t) - 2 * y(i) * y(t) * Q(i, t)
          val objDiff = if (quadCoef <= 0) {
            -(gradDiff * gradDiff) / Tau
          } else {
            -(gradDiff * gradDiff) / quadCoef
          }
          if (objDiff <= minObj) {
            j = t
            minObj = objDiff
          }
        }
      }
    }

    if (maxG - minG < eps) (-1, -1) else (i, j)
  }

  private def maxIteration: Int = {
    val iter = if (len > Int.MaxValue / 100)
      Int.MaxValue // overflow
    else len * 100
    math.max(iter, 10000000) // We run at most 10000000 times
  }

  private def reconstructGradient(): Unit = {
    if (activeSize != len) {
      for (j <- activeSize until len) {
        grad(j) = gradBar(j) + p(j)
      }

      val nr_free = (0 until activeSize).count(isFree)
      if (2 * nr_free < activeSize) {
        // TODO
        println("WARNING: using -h 0 may be faster")
      }

      if (nr_free * len > 2 * activeSize * (len - activeSize)) {
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
}

trait FormulationSolver {
  def solve(problem: Problem, param: Parameters, Cp: Double, Cn: Double): Solution
}