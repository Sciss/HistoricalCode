package de.sciss.svm
package train

import collection.breakOut

private[train] trait ClassificationTrainer extends Trainer {
  case class Grouped(numClasses: Int, label: Array[Int], start: Array[Int], count: Array[Int])

  def info(what: => String): Unit = println(what)

  def train(param: SVMParameter, problem: Problem): Model = {
    val l = problem.size

    // group training data of the same class
    val map     = problem.groupClasses // groupClasses(problem)
    val keys    = map.keys.toIndexedSeq.sorted
    val groups  = keys.map(map)

    // TODO:
    // Labels are ordered by their first occurrence in the training set.
    // However, for two-class sets with -1/+1 labels and -1 appears first,
    // we swap labels to ensure that internally the binary SVM has positive data corresponding to the +1 instances.

    val numClasses = keys.size
    if(numClasses == 1)
      info("WARNING: training data in only one class. See README for details.\n")

    val x = new Array[List[Node]](l)
    //    for (i <- 0 until l)
    //      x(i) = problem.x(perm(i))

    // calculate weighted C

    val weightedC = Array.tabulate[Double](numClasses) { ci =>
      param.C * param.weights.getOrElse[Double](keys(ci), 1.0)
    }

    // train k*(k-1)/2 models

    // val nonzero = new Array[Boolean](l)
    // val f = new Array[DecisionFunction](numClasses * (numClasses - 1) / 2)

    // class -> index
    var nonZero = Map.empty[Int, Set[Int]] withDefaultValue Set.empty

    require(!param.probability, "Not yet implemented: probability")
    //    double *probA=NULL,*probB=NULL;
    //    if (param->probability)
    //    {
    //      probA=Malloc(double,numClasses*(numClasses-1)/2);
    //      probB=Malloc(double,numClasses*(numClasses-1)/2);
    //    }

    val f = (groups zip weightedC).tails.map {
      case (gi, wi) +: gij =>
        val ci  = gi.size
        val si  = gi.instances.map(_.copy(y = 1))
        gij.map { case (gj, wj) =>
          val sj  = gj.instances.map(_.copy(y = -1))
          val sij = si ++ sj
          val sp  = new Problem(sij)

          trainOne(param, sp, wi, wj)
        }

      case _ => Vec.empty
    } .toIndexedSeq

    //    val f = (0 until numClasses).map { i =>
    //      val gi  = groups(i)
    //      val ci  = gi.size
    //      val si  = gi.instances.map(_.copy(y = 1))
    //
    //      (i+1 until numClasses).map { j =>
    //        if(param.probability) {
    //          sys.error("Not yet implemented: probability") // svm_binary_svc_probability(&sub_prob,param,weighted_C[i],weighted_C[j],probA[p],probB[p]);
    //        }
    //
    //        val gj  = groups(j)
    //        val sij = si ++ gj.instances.map(_.copy(y = -1))
    //        val sp  = new Problem(sij)
    //
    //        val res = trainOne(param, sp, weightedC(i),weightedC(j))
    //        for (k <- 0 until ci) {
    //          val nzi = nonZero(i)
    //          if(!nzi(k) && math.abs(res.alpha(k)) > 0)
    //            nonZero += i -> (nzi + k)
    //        }
    //        for (k <- 0 until gj.size) {
    //          val nzj = nonZero(j)
    //          if(!nzj(k) && math.abs(res.alpha(ci+k)) > 0)
    //            nonZero += j -> (nzj + k)
    //        }
    //        res
    //      }
    //    }

    // build output

    // model->numClasses = numClasses

    //    case class SupportVector(
    //      vector      : List[Node],
    //      coefficient : Double,
    //      index       : Int)

    //    model->label = Malloc(int,numClasses);
    //    for(i=0;i<numClasses;i++)
    //      model->label[i] = label[i];

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

    info(s"Total nSV = $totalSV")

    //    // model->l = total_sv;
    //    // model->SV = Malloc(svm_node *,total_sv);
    //    val modelSV = new Array[SupportVector](totalSV)
    //    // model->sv_indices = Malloc(int,total_sv);
    //    // val modelSVIndices = new Array[Int](total_sv)
    //    var p = 0
    //    for (i <- 0 until l) {
    //      if (nonzero(i)) {
    //        modelSV(p) = x(i)
    //        // modelSVIndices(p) = perm(i) + 1
    //        p += 1
    //      }
    //    }

    val modelSV = (0 until numClasses).map { i =>
      val nz  = nonZero(i)
      val gi  = groups(i)
      val ci  = gi.size
      (0 until ci).collect {
        case j if nz(j) => SupportVector(gi.x(j), 0.0, j)  // ??? correct
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

    val rho: Array[Double] = ??? // f.map(_.rho)(breakOut)

    new Model(numClasses = numClasses, param = param, supportVectors = modelSV, rho = rho) {
      def predictValues(x: List[Node]): Double = ???
    }
  }
}
