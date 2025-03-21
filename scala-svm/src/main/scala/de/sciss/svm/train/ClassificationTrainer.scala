/*
 *  ClassificationTrainer.scala
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
package train

private[train] trait ClassificationTrainer extends Trainer {
  protected def tpe: Type

  private def DEBUG: Boolean = true   // AbstractMethodError if we make this a `val`!

  // case class Grouped(numClasses: Int, label: Array[Int], start: Array[Int], count: Array[Int])

  def train(problem: Problem, param: Parameters): model.ClassificationModel = {
    // group training data of the same class
    val groups0     = problem.groupClasses
    val numClasses  = groups0.size

    if (DEBUG) println("Aqui1.")

    // Labels are ordered by their first occurrence in the training set.
    // However, for two-class sets with -1/+1 labels and -1 appears first,
    // we swap labels to ensure that internally the binary SVM has positive data corresponding to the +1 instances.
    val groups = if (numClasses == 2 && groups0(0).y(0) == -1 && groups0(1).y(0) == 1) {
      groups0.reverse
    } else {
      groups0
    }
    val classes = groups.map(_.y(0))

    if(numClasses == 1)
      logInfo("WARNING: training data in only one class. See README for details.\n")

    // calculate weighted C

    val weightedC = Array.tabulate[Double](numClasses) { ci =>
      param.C * param.weights.getOrElse(classes(ci), 1.0)
    }

//    if (DEBUG) {
//      println(s"classes = ${classes.mkString(",")}; weightedC = ${weightedC.mkString(",")}")
//    }

    // train k*(k-1)/2 models

    // class -> index

    require(!param.probability, "Not yet implemented: probability")
    //    double *probA=NULL,*probB=NULL;
    //    if (param->probability)
    //    {
    //      probA=Malloc(double,numClasses*(numClasses-1)/2);
    //      probB=Malloc(double,numClasses*(numClasses-1)/2);
    //    }

//    if (DEBUG) {
//      val f0 = (groups zip weightedC).tails
//      println(s"zipped size = ${f0.size}")
//    }

    // pairwise permutations of classified problems
    val f = (groups zip weightedC).tails.take(numClasses - 1).map {
      case (gi, wi) +: gij =>
        val si    = gi.instances.map(_.copy(y = +1))
        gij.map { case (gj, wj) =>
          val sj  = gj.instances.map(_.copy(y = -1))
          val sij = si ++ sj

//          if (DEBUG) {
//            println("---permutation---")
//            sij.zipWithIndex.foreach { case (sub, i) =>
//              println(s"[$i] ${sub.y} " + sub.x.map(n => s"${n.index}:${n.value}").mkString(" "))
//            }
//          }

          val sp  = new Problem(sij)

          trainOne(sp, param, wi, wj)
        }

      // case _ => Vec.empty
    } .toIndexedSeq // XXX TODO: use collection.breakOut

    println("f:")
    f.foreach(println)

    // DDD
//    println(s"numClasses $numClasses; f.size ${f.size}; f(0).size ${f(0).size}; f total ${f.map(_.size).sum}")

    val nonZero = f.map { fi =>
      fi.map { fij =>
        fij.alpha.exists(_ > 0)
      }
    }

    // build output

    // model->numClasses = numClasses

    //    if(param->probability)
    //    {
    //      model->probA = Malloc(double,numClasses*(numClasses-1)/2);
    //      model->probB = Malloc(double,numClasses*(numClasses-1)/2);
    //      for(i=0;i<numClasses*(numClasses-1)/2;i++)
    //      {
    //        model->probA[i] = probA[i];
    //        model->probB[i] = probB[i];
    //      }
    //    }
    //    else
    //    {
    //      model->probA=NULL;
    //      model->probB=NULL;
    //    }

    val nzCount   = Array.tabulate(numClasses)(nonZero(_).size)
    // val modelNSV  = nzCount.clone()
    val totalSV   = nzCount.sum

    logInfo(s"Total nSV = $totalSV\n")

    val modelSV: Vec[Vec[SupportVector]] = (groups zip nonZero).zipWithIndex.map { case ((gi, nzi), i) =>
      (gi.xs zip nzi).zipWithIndex.collect {
        case ((x, true), j) => SupportVector(x, j)  // TODO: correct?
      }
    }

    val nz_start = new Array[Int](numClasses)
    nz_start(0) = 0
    for (i <- 1 until numClasses) {
      nz_start(i) = nz_start(i-1) + nzCount(i-1)
    }

    // model->sv_coef = Malloc(double *,numClasses-1);
    val modelSVCoef = new Array[Array[Double]](numClasses - 1)

    for (i <- 0 until numClasses - 1)
      modelSVCoef(i) = new Array[Double](totalSV)

    var p = 0
    for (i <- 0 until numClasses) {
      for (j <- i+1 until numClasses) {
        // classifier (i,j): coefficients with
        // i are in sv_coef[j-1][nz_start[i]...],
        // j are in sv_coef[i][nz_start[j]...]

        val ci: Int = groups(i).size // count[i];
        val cj: Int = groups(j).size // count[j];

        var q = nz_start(i)
        for(k <- 0 until ci) {
          if(nonZero(i)(k)) {
            modelSVCoef(j-1)(q) = f(i)(j).alpha(k)
            q += 1
          }
        }
        q = nz_start(j)
        for (k <- 0 until cj ) {
          if(nonZero(j)(k)) {
            modelSVCoef(i)(q) = f(i)(j).alpha(ci+k)
            q += 1
          }
        }
        p += 1
      }
    }

    val rho: Vec[Double] = f.flatMap(_.map(_.rho))

    val coefficients = Coefficients(Vec.empty) // Coefficients(modelSV.map(_.map(_ => 0.0)))  // XXX TODO correct?
    val probA = Vec.empty // XXX TODO correct?
    val probB = Vec.empty // XXX TODO correct?
    mkModel(classes = classes, param = param, supportVectors = modelSV,
      coefficients = coefficients, rho = rho, probA = probA, probB = probB)
  }

  protected def mkModel(classes: Vec[Int],
                        param: Parameters,
                        supportVectors: Vec[Vec[SupportVector]],
                        coefficients: Coefficients,
                        rho: Vec[Double],
                        probA: Vec[Double], probB: Vec[Double]): model.ClassificationModel
}