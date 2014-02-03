package de.sciss.svm
package model

import scala.collection.breakOut

trait ClassificationModel extends Model {

  def supportVectors  : Vec[Vec[SupportVector]]
  def coefficients    : Coefficients
  def rho             : Vec[Double]
  def probA           : Vec[Double]
  def probB           : Vec[Double]
  def label           : Vec[Int]

  val numClasses: Int = supportVectors.size

  // TODO
  require(numClasses * (numClasses - 1) / 2 == rho.size)

  override def predictValues(x: List[Node]): (Double, Vec[Double]) = {
    val kValue          = supportVectors.map(_.map(sv => param.kernel(x, sv.vector)))
    val n               = numClasses
    val votes           = Array.fill(n)(0)
    val decisionValues  = Array.fill(n * (n - 1) / 2)(0.0)
    var p = 0
    for {
      i <- 0     until n
      j <- i + 1 until n
    } {
      val ki            = kValue(i)
      val kj            = kValue(j)
      val svi           = supportVectors(i)
      val svj           = supportVectors(j)
      val (ci, cj)      = coefficients.get(i, j)
      val si            = (for (k <- 0 until svi.size) yield ci(k) * ki(k)).sum
      val sj            = (for (k <- 0 until svj.size) yield cj(k) * kj(k)).sum
      val sum           = si + sj - rho(p)
      val sel           = if (sum > 0) i else j
      votes(sel)        = votes(sel) + 1
      decisionValues(p) = sum
      p += 1
    }
    val li = votes.maxBy(votes(_))
    (label(li), decisionValues.toIndexedSeq)
  }

  override def predictProbability(instance: Instance): (Double, Vec[Double]) =
    if (probA.nonEmpty && probB.nonEmpty) {
      val n             = numClasses
      val (_, decisionValues) = predictValues(instance.x)
      val pairwiseProb  = Array.ofDim[Double](n, n)
      val minProb       = 1.0e-7
      var k = 0
      for {
        i <- 0     until n
        j <- i + 1 until n
      } {
        pairwiseProb(i)(j) = sigmoidPredict(decisionValues(k), probA(k), probB(k)) max minProb min (1 - minProb)
        pairwiseProb(j)(i) = 1 - pairwiseProb(i)(j)
        k += 1
      }

      val estimates = multiClassProbability(n, pairwiseProb.map(_.toIndexedSeq)(breakOut))

      var probMaxIdx = 0
      for (i <- 1 until n) {
        if (estimates(i) > estimates(probMaxIdx))
          probMaxIdx = i
      }
      label(probMaxIdx).toDouble -> estimates
    } else {
      // predict(instance) -> Vec.empty
      predictValues(instance.x)
    }

  // http://www.csie.ntu.edu.tw/~cjlin/papers/svmprob/svmprob.pdf
  // XXX TODO clean up this mess
  private def multiClassProbability(k: Int, r: Vec[Vec[Double]]): Vec[Double] = {
    assert(k != 0)
    val maxIter = 100 max k
    val eps     = 0.005 / k
    val Q       = Array.ofDim[Double](k, k)
    val Qp      = Array.ofDim[Double](k)
    
    val p       = new Array[Double](k)

    for (t <- 0 until k) {
      p(t) = 1.0 / k
      Q(t)(t) = 0
      for (j <- 0 until t) {
        Q(t)(t) += r(j)(t) * r(j)(t)
        Q(t)(j) = Q(j)(t)
      }
      for (j <- t + 1 until k) {
        Q(t)(t) += r(j)(t) * r(j)(t)
        Q(t)(j) = -r(j)(t) * r(t)(j)
      }
    }

    var pQp = 0.0
    import scala.util.control.Breaks._

    var iter = 0
    breakable {
      while (iter < maxIter) {
        pQp = 0.0

        for (t <- 0 until k) {
          Qp(t) = 0
          for (j <- 0 until k) {
            Qp(t) += Q(t)(j) * p(j)
          }
          pQp += p(t) * Qp(t)
        }
        val maxError = Qp.map(t => (t - pQp).abs).max
        if (maxError < eps) break()

        for (t <- 0 until k) {
          val diff = (-Qp(t) + pQp) / Q(t)(t)
          p(t) += diff
          pQp = (pQp + diff * (diff * Q(t)(t) + 2 * Qp(t))) / (1 + diff) / (1 + diff)
          for (j <- 0 until k) {
            Qp(j) = (Qp(j) + diff * Q(t)(j)) / (1 + diff)
            p(j) /= (1 + diff)
          }
        }
        iter += 1
      }
    }

    if (iter >= maxIter) {
      println("Exceeds maxIter in multiClassProbability")
    }
    
    p.toIndexedSeq
  }

  private def sigmoidPredict(decisionValue: Double, A: Double, B: Double): Double = {
    val fApB = decisionValue * A + B
    // 1-p used later; avoid catastrophic cancellation
    if (fApB >= 0)
      math.exp(-fApB) / (1.0 + math.exp(-fApB))
    else
      1.0 / (1 + math.exp(fApB))
  }
  
  override def toString = Array(
    param.toString,
    "total_sv " + supportVectors.size,
    "rho " + rho.mkString(" ")).mkString("\n")
}

class CSVCModel(/* val numClasses: Int, */
                val param         : Parameters,
                val supportVectors: Vec[Vec[SupportVector]],
                val coefficients  : Coefficients,
                val rho           : Vec[Double],
                val probA         : Vec[Double],
                val probB         : Vec[Double],
                val label         : Vec[Int])
  extends ClassificationModel {

  def tpe = SVM.Classification.C
}

class NuSVCModel(/* val numClasses: Int, */
                 val param          : Parameters,
                 val supportVectors : Vec[Vec[SupportVector]],
                 val coefficients   : Coefficients,
                 val rho            : Vec[Double],
                 val probA          : Vec[Double],
                 val probB          : Vec[Double],
                 val label          : Vec[Int])
  extends ClassificationModel {

  def tpe = SVM.Classification.Nu
}