package de.sciss.svm

/** 1. SVC: support vector classification (two-class and multi-class).
  * 2. SVR: support vector regression.
  * 3. One-class SVM.
  */
abstract class QMatrix {
  def swapIndex(i: Int, j: Int): Unit
  def apply    (i: Int, j: Int): Double

  protected def swap[T](array: scala.collection.mutable.Seq[T], i: Int, j: Int): Unit = {
    val temp = array(i)
    array(i) = array(j)
    array(j) = temp
  }
}

class OneClassQMatrix(val problem: Problem, val param: Parameters) extends QMatrix {
  val x: Array[List[Node]] = problem.xs.toArray // .clone()
  val qd  = Array.tabulate(problem.size)(i => param.kernel(x(i), x(i)))
  val y   = problem.ys.toArray // clone()

  def swapIndex(i: Int, j: Int): Unit = {
    swap(x , i, j)
    swap(y , i, j)
    swap(qd, i, j)
  }

  override def apply(i: Int, j: Int): Double =
    if (i == j)
      qd(i)
    else
      param.kernel(x(i), x(j))
}

class Solver(problem: Problem,
             param  : Parameters,
             Q      : QMatrix,
             p      : Vec[Double],
             y      : Vec[Int],
             alpha  : Vec[Double],
             Cp     : Double,
             Cn     : Double) {

  private val epsilon = param.eps
  // private val shrinking = param.shrinking

  val LOWER_BOUND = 2
  val UPPER_BOUND = 1
  val FREE        = 3

  val TAU         = 1e-12

  private val len: Int = problem.size

  private val _alpha: Array[Double] = alpha.toArray

  var activeSize = 1

  private def getAlphaStatus(i: Int) = {
    if      (_alpha(i) >= getC(i)) UPPER_BOUND
    else if (_alpha(i) <= 0      ) LOWER_BOUND
    else                           FREE
  }

  private def getC(i: Int): Double = if (y(i) > 0) Cp else Cn

  private def isUpperBound(i: Int) = getAlphaStatus(i) == UPPER_BOUND
  private def isLowerBound(i: Int) = getAlphaStatus(i) == LOWER_BOUND
  private def isFree      (i: Int) = getAlphaStatus(i) == FREE

  var counter = 1

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
    for (
      i <- 0 until len if !isLowerBound(i)
    ) {
      for (j <- 0 until len) {
        G(j) += _alpha(i) * Q(i, j)
      }
      if (isUpperBound(i)) {
        for (j <- 0 until len) {
          GBar(j) += getC(i) * Q(i, j)
        }
      }
    }
  }

  def doShrinking() = ()

  def calculateRho: Double = {
    var ub        = Double.MaxValue
    var lb        = Double.MinValue
    var sumFree   = 0.0
    var numFree   = 0
    for (i <- 0 until activeSize) {
      val yG = y(i) * G(i)
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
        yield _alpha(i) * (G(i) + p(i))
    }.sum / 2

  def optimize(): Unit = {
    val maxIter = maxIteration

    for (iter <- 0 until maxIter) {
      counter = counter - 1
      if (counter == 0) {
        counter = len min 1000
        //        if (shrinking) {
        //          doShrinking
        //        }
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
        quadCoef = TAU
      }
      // val delta = -y(i) * G(i) + y(j) * G(j)
      val oldAi = _alpha(i)
      val oldAj = _alpha(j)
      _alpha(i) = _alpha(i) + (-G(i) + y(i) * y(j) * G(j)) / quadCoef
      _alpha(j) = _alpha(j) - (-y(i) * y(j) * G(i) + G(j)) / quadCoef

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
        G(t) = G(t) + Q(t, i) * deltaAi + Q(t, j) * deltaAj
      }
    }
  }

  def isValidWorkingSet(i: Int, j: Int) = j != -1

  private val G: Array[Double] = p.toArray // clone()
  val GBar  = new Array[Double](len) // Array.fill(len)(0.0)

  def selectWorkingSet(): (Int, Int) = {
    var maxG = Double.MinValue
    var i = -1

    for (t <- 0 until len) {
      if (y(t) == +1) {
        if (!isUpperBound(t) && -G(t) >= maxG) {
          i = t
          maxG = -G(t)
        }
      } else {
        if (!isLowerBound(t) && G(t) >= maxG) {
          i = t
          maxG = G(t)
        }
      }
    }

    var j       = -1
    var minObj  = Double.MaxValue
    var minG    = Double.MaxValue

    for (t <- 0 until len) {
      if ((y(i) == +1 && !isLowerBound(t)) ||
          (y(i) == -1 && !isUpperBound(t))) {
        val gradDiff = maxG + y(t) * G(t)
        if (-y(t) * G(t) <= minG) {
          minG = -y(t) * G(t)
        }
        if (gradDiff > 0) {
          val quadCoef = Q(i, i) + Q(t, t) - 2 * y(i) * y(t) * Q(i, t)
          val objDiff = if (quadCoef <= 0) {
            -(gradDiff * gradDiff) / TAU
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

    if (maxG - minG < epsilon) (-1, -1) else (i, j)
  }

  private def maxIteration: Int = {
    val iter = if (len > Int.MaxValue / 100)
      Int.MaxValue // overflow
    else len * 100
    iter max 10000000 // We run at least 10000000 times
  }

  private def reconstructGradient(): Unit = {
    if (activeSize != len) {
      for (j <- activeSize until len) {
        G(j) = GBar(j) + p(j)
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
          G(i) += _alpha(j) * Q(i, j)
        }
      } else {
        for {
          i <- 0 until activeSize if isFree(i)
          j <- activeSize until len
        } {
          G(j) += _alpha(i) * Q(i, j)
        }
      }
    }
  }
}

object Solver {

  def solveOneClass(problem: Problem, param: Parameters): Solution = {
    val n = (param.nu * problem.size).toInt

    val alpha = Vec.tabulate(problem.size) {
      case i if i < n                       => 1.0
      case i if i == n && i < problem.size  => param.nu * problem.size - n
      case _                                => 0.0
    }

    val zeros = Vec.fill(problem.size)(0.0)
    val ones  = Vec.fill(problem.size)(1)

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

  def solveEpsilonSVR(problem: Problem, param: EpsilonSVRSVMParamter): Solution = {
    val alpha2     = Vec.fill    (2 * problem.size)(0.0)
    val linearTerm = Vec.tabulate(2 * problem.size) {
      case i if i < problem.size  => param.p - problem.y(i)
      case i                      => param.p + problem.y(i - problem.size)
    }
    val y = Vec.tabulate(2 * problem.size) {
      case i if i < problem.size  =>  1
      case _                      => -1
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

    val alpha2     = Vec.fill    (2 * problem.size)(0.0)
    val linearTerm = Vec.tabulate(2 * problem.size) {
      case i if i < problem.size  => -problem.y(i)                .toDouble
      case i                      =>  problem.y(i - problem.size) .toDouble
    }
    val y = Vec.tabulate(2 * problem.size) {
      case i if i < problem.size  =>  1
      case _                      => -1
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

trait FormulationSolver {
  def solve(problem: Problem, param: Parameters): Solution
}

class OneClassSolver extends FormulationSolver {
  def solve(problem: Problem, param: Parameters) = Solver.solveOneClass(problem, param)
}

case class Solution(
  obj         : Double,
  rho         : Double,
  upperBoundP : Double,
  upperBoundN : Double,
  r           : Double,
  alpha       : Vec[Double])
