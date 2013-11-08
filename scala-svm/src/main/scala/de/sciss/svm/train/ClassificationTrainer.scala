package de.sciss.svm
package train

import collection.breakOut

private[train] trait ClassificationTrainer extends Trainer {
  protected def tpe: Type

  case class Grouped(numClasses: Int, label: Array[Int], start: Array[Int], count: Array[Int])

  def train(param: Parameters, problem: Problem): Model = {
    // group training data of the same class
    val map     = problem.groupClasses // groupClasses(problem)
    val classes = map.keys.toIndexedSeq.sorted
    val groups  = classes.map(map)

    // TODO:
    // Labels are ordered by their first occurrence in the training set.
    // However, for two-class sets with -1/+1 labels and -1 appears first,
    // we swap labels to ensure that internally the binary SVM has positive data corresponding to the +1 instances.

    val numClasses = classes.size
    if(numClasses == 1)
      logInfo("WARNING: training data in only one class. See README for details.\n")

    // calculate weighted C

    val weightedC = Array.tabulate[Double](numClasses) { ci =>
      param.C * param.weights.getOrElse(classes(ci), 1.0)
    }

    // train k*(k-1)/2 models

    // class -> index

    require(!param.probability, "Not yet implemented: probability")
    //    double *probA=NULL,*probB=NULL;
    //    if (param->probability)
    //    {
    //      probA=Malloc(double,numClasses*(numClasses-1)/2);
    //      probB=Malloc(double,numClasses*(numClasses-1)/2);
    //    }

    val f = (groups zip weightedC).tails.take(numClasses - 1).map {
      case (gi, wi) +: gij =>
        val si  = gi.instances.map(_.copy(y = 1))
        gij.map { case (gj, wj) =>
          val sj  = gj.instances.map(_.copy(y = -1))
          val sij = si ++ sj
          val sp  = new Problem(sij)

          trainOne(param, sp, wi, wj)
        }

      // case _ => Vec.empty
    } .toIndexedSeq

    println("f:")
    f.foreach(println)

    // DDD
    println(s"numClasses $numClasses; f.size ${f.size}; f(0).size ${f(0).size}; f total ${f.map(_.size).sum}")

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

    val modelSV = (groups zip nonZero).zipWithIndex.map { case ((gi, nzi), i) =>
      (gi.xs zip nzi).zipWithIndex.collect {
        case ((x, true), j) => SupportVector(x, 0.0, j)  // TODO: correct?
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

    new Model(tpe = tpe, classes = classes, param = param, supportVectors = modelSV, rho = rho) {
      def predictValues(x: List[Node]): Double = ???
    }
  }
}