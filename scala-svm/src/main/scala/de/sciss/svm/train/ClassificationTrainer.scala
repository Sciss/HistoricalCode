//package de.sciss.svm
//package train
//
//import collection.mutable.ArrayBuffer
//import scala.util.control.Breaks
//
////object ClassificationTrainer {
////  case class Grouped(numClasses: Int, label: Array[Int], start: Array[Int], count: Array[Int], perm: Array[Int])
////
////  // label: label name, start: begin of each class, count: #data of classes, perm: indices to the original data
////  // perm, length l, must be allocated before calling this subroutine
////  // static void svm_group_classes(const svm_problem *prob, int *numClasses_ret, int **label_ret, int **start_ret, int **count_ret, int *perm)
////  def groupClasses(problem: Problem): Grouped = {
////    val l           = problem.size
////    val perm        = new Array[Int](l)
////    var numClasses  = 0
////    val label       = ArrayBuffer.empty[Int]
////    val count       = ArrayBuffer.empty[Int]
////    val dataLabel   = new Array[Int](l)     // for each row, new class label from 0 to numClasses - 1
////
////    for (i <- 0 until l) {
////      val thisLabel = problem.y(i).toInt
////      var j = 0
////      Breaks.breakable {
////        while (j < numClasses) {
////          if (thisLabel == label(j)) {
////            count(j) = count(j) + 1
////            Breaks.break()
////          }
////          j += 1
////        }
////      }
////      dataLabel(i) = j
////      if (j == numClasses) {
////        label += thisLabel
////        count += 1
////        numClasses += 1
////      }
////    }
////
////    // Labels are ordered by their first occurrence in the training set.
////    // However, for two-class sets with -1/+1 labels and -1 appears first,
////    // we swap labels to ensure that internally the binary SVM has positive data corresponding to the +1 instances.
////    //
////    if (numClasses == 2 && label(0) == -1 && label(1) == 1) {
////      val tmpL = label(0)
////      label(0) = label(1)
////      label(1) = tmpL
////      val tmpC = count(0)
////      count(0) = count(1)
////      count(1) = tmpC
////      for (i <- 0 until l) {
////        if (dataLabel(i) == 0)
////          dataLabel(i) = 1
////        else
////          dataLabel(i) = 0
////      }
////    }
////
////    // int *start = Malloc(int,numClasses);
////    val start = new Array[Int](numClasses)
////    start(0) = 0
////    for (i <- 1 until numClasses)
////      start(i) = start(i - 1) + count(i - 1)
////
////    for (i <- 0 until l) {
////      perm(start(dataLabel(i))) = i
////      start(dataLabel(i)) = start(dataLabel(i)) + 1
////    }
////    start(0) = 0
////    for (i <- 1 until numClasses)
////      start(i) = start(i - 1) + count(i - 1)
////
////    //	*numClasses_ret = numClasses;
////    //	*label_ret = label;
////    //	*start_ret = start;
////    //	*count_ret = count;
////    new Grouped(numClasses = numClasses, label = label.toArray, start = start, count = count.toArray, perm = perm)
////  }
////}
//
//private[train] trait ClassificationTrainer extends Trainer {
//  case class Grouped(numClasses: Int, label: Array[Int], start: Array[Int], count: Array[Int])
//
//  def info(what: => String): Unit = println(what)
//
//  def train(param: SVMParameter, problem: Problem): Model = {
//    val l = problem.size
//
//    // group training data of the same class
//    val map     = problem.groupClasses // groupClasses(problem)
//    val keys    = map.keys.toArray.sorted
//    val groups  = keys.map(map)
//
//    // TODO:
//    // Labels are ordered by their first occurrence in the training set.
//    // However, for two-class sets with -1/+1 labels and -1 appears first,
//    // we swap labels to ensure that internally the binary SVM has positive data corresponding to the +1 instances.
//
//    val numClasses = keys.size
//    if(numClasses == 1)
//      info("WARNING: training data in only one class. See README for details.\n")
//
//    val x = new Array[List[Node]](l)
//    //    for (i <- 0 until l)
//    //      x(i) = ??? // problem.x(perm(i))
//
//    // calculate weighted C
//
//    val weightedC  = Array.tabulate[Double](numClasses) { ci =>
//      param.C * param.weights.getOrElse(keys(ci), 1)
//    }
//
//    // train k*(k-1)/2 models
//
//    val nonzero = new Array[Boolean](l)
//    // val f = new Array[DecisionFunction](numClasses * (numClasses - 1) / 2)
//
//    require(!param.probability, "Not yet implemented: probability")
//    //    double *probA=NULL,*probB=NULL;
//    //    if (param->probability)
//    //    {
//    //      probA=Malloc(double,numClasses*(numClasses-1)/2);
//    //      probB=Malloc(double,numClasses*(numClasses-1)/2);
//    //    }
//
//    val f = for {
//      i <- 0   until numClasses
//      j <- i+1 until numClasses
//    } yield {
//      if(param.probability) {
//        ??? // svm_binary_svc_probability(&sub_prob,param,weighted_C[i],weighted_C[j],probA[p],probB[p]);
//      }
//
//      val gi  = groups(i)
//      val gj  = groups(j)
//      val si  = gi.instances.map(_.copy(y = 1)) ++ gj.instances.map(_.copy(y = -1))
//      val sp  = new Problem(si)
//
//      trainOne(param, sp, weightedC(i),weightedC(j))
//    }
//
//    var p = 0
//    for (i <- 0 until numClasses) {
//      for(j <- i + 1 until numClasses) {
//        // svm_problem sub_prob;
//        val si: Int = ??? // start[i],
//        val sj: Int = ??? // start[j];
//        val ci: Int = ??? // = count[i]
//        val cj: Int = ??? // count[j];
//
//        val subInst   = new Array[Instance](ci + cj)
//        val subProb   = new Problem(null) // XXX
//
//        // sub_prob.x = Malloc(svm_node *,sub_prob.l);
//        // sub_prob.y = Malloc(double,sub_prob.l);
//
//        //        for (k <- 0 until ci) {
//        //          sub_prob.x[k] = x[si+k];
//        //          sub_prob.y[k] = +1;
//        //        }
//        //        for(k=0;k<cj;k++) {
//        //          sub_prob.x[ci+k] = x[sj+k];
//        //          sub_prob.y[ci+k] = -1;
//        //        }
//
//        if(param.probability) {
//          ??? // svm_binary_svc_probability(&sub_prob,param,weighted_C[i],weighted_C[j],probA[p],probB[p]);
//        }
//
//        f(p) = trainOne(param, subProb, weightedC(i),weightedC(j))
//        for (k <- 0 until ci) {
//          if(!nonzero(si+k) && math.abs(f(p).alpha(k)) > 0)
//            nonzero(si+k) = true
//        }
//        for (k <- 0 until cj) {
//          if(!nonzero(sj+k) && math.abs(f(p).alpha(ci+k)) > 0)
//            nonzero(sj+k) = true
//        }
//        p += 1
//      }
//    }
//
//    // build output
//
//    // model->numClasses = numClasses
//
//    new Model(numClasses = numClasses, param = param, supportVectors = ???, rho = f.map(_.rho)) {
//      def predictValues(x: List[Node]) = ??? : Double
//    }
//
//    //    case class SupportVector(
//    //      vector      : List[Node],
//    //      coefficient : Double,
//    //      index       : Int)
//
//    ???
//    //    model->label = Malloc(int,numClasses);
//    //    for(i=0;i<numClasses;i++)
//    //      model->label[i] = label[i];
//
//    //    if(param->probability)
//    //    {
//    //      model->probA = Malloc(double,numClasses*(numClasses-1)/2);
//    //      model->probB = Malloc(double,numClasses*(numClasses-1)/2);
//    //      for(i=0;i<numClasses*(numClasses-1)/2;i++)
//    //      {
//    //        model->probA[i] = probA[i];
//    //        model->probB[i] = probB[i];
//    //      }
//    //    }
//    //    else
//    //    {
//    //      model->probA=NULL;
//    //      model->probB=NULL;
//    //    }
//
//    var total_sv = 0
//    val nz_count = new Array[Int](numClasses)
//    val modelNSV = new Array[Int](numClasses)
//    for (i <- 0 until numClasses) {
//      var nSV = 0
//      val countI: Int = ??? // count(i)
//      for (j <- 0 until countI) {
//        if(nonzero(start(i)+j)) {
//          nSV += 1
//          total_sv += 1
//        }
//      }
//      modelNSV(i) = nSV
//      nz_count(i) = nSV
//    }
//
//    info(s"Total nSV = ${total_sv}")
//
//    model->l = total_sv;
//    model->SV = Malloc(svm_node *,total_sv);
//    model->sv_indices = Malloc(int,total_sv);
//    p = 0;
//    for(i=0;i<l;i++) {
//      if(nonzero(i))
//      {
//        model->SV[p] = x[i];
//        model->sv_indices[p++] = perm[i] + 1;
//      }
//    }
//
////    int *nz_start = Malloc(int,numClasses);
////    nz_start[0] = 0;
////    for(i=1;i<numClasses;i++)
////      nz_start[i] = nz_start[i-1]+nz_count[i-1];
////
////    model->sv_coef = Malloc(double *,numClasses-1);
////    for(i=0;i<numClasses-1;i++)
////      model->sv_coef[i] = Malloc(double,total_sv);
////
////    p = 0;
////    for(i=0;i<numClasses;i++)
////      for(int j=i+1;j<numClasses;j++)
////      {
////        // classifier (i,j): coefficients with
////        // i are in sv_coef[j-1][nz_start[i]...],
////        // j are in sv_coef[i][nz_start[j]...]
////
////        int si = start[i];
////        int sj = start[j];
////        int ci = count[i];
////        int cj = count[j];
////
////        int q = nz_start[i];
////        int k;
////        for(k=0;k<ci;k++)
////          if(nonzero[si+k])
////            model->sv_coef[j-1][q++] = f[p].alpha[k];
////        q = nz_start[j];
////        for(k=0;k<cj;k++)
////          if(nonzero[sj+k])
////            model->sv_coef[i][q++] = f[p].alpha[ci+k];
////        ++p;
////      }
//    ???
//  }
//}
