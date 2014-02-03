package de.sciss.svm

import java.io.{PrintStream, File}

trait Model {
  def tpe           : Type
  // def classes       : Vec[Int] // labels
  def param         : Parameters
  // val supportVectors: Vec[Vec[SupportVector]],
  def rho           : Vec[Double]

  // require(supportVectors.size == rho.size)

  // lazy val numVectors: Int = supportVectors.iterator.map(_.size).sum

  // def numClasses = classes.size

  def predict(x       : List[Node]): Double = predictValues(x)._1
  def predict(instance: Instance  ): Double = predict(instance.x)

  protected def predictValues(x: List[Node]): (Double, Vec[Double]) // = 0.0

  def save(file: File): Unit = {
    val fp = new PrintStream(file)
    try {
      write(fp)
    } finally {
      fp.close()
    }
  }

  /** Writes a string representation of the model data to a stream.
    *
    * The entries are separated by new lines. The preamble contains pairs of
    * keys and values which are separated by a space character. E.g.
    * `svm_type c_svc` or `gamma 0.25`. The line `SV` concludes the preamble
    * after which the support vectors are printed line by line.
    *
    * @param ps  the output stream to which the string is appended
    */
  def write(ps: PrintStream): Unit = {
    ???
//    ps.println(s"svm_type ${tpe.id}")
//    param.write(ps)
//
//    ps.println(s"nr_class $numClasses")
//    ps.println(s"total_sv $numVectors")
//
//    ps.print("rho")
//    rho.foreach(x => ps.print(s" $x"))  // %g in the C code
//    ps.println()
//
//    // if (classes.nonEmpty) {
//      ps.print("label")
//      classes.foreach(lb => ps.print(s" $lb"))
//      ps.println()
//    // }
//
//    //   	if(model->probA) // regression has probA only
//    //   	{
//    //   		fprintf(fp, "probA");
//    //   		for(int i=0;i<nr_class*(nr_class-1)/2;i++)
//    //   			fprintf(fp," %g",model->probA[i]);
//    //   		fprintf(fp, "\n");
//    //   	}
//    //   	if(model->probB)
//    //   	{
//    //   		fprintf(fp, "probB");
//    //   		for(int i=0;i<nr_class*(nr_class-1)/2;i++)
//    //   			fprintf(fp," %g",model->probB[i]);
//    //   		fprintf(fp, "\n");
//    //   	}
//
//    // if (supportVectors.nonEmpty) {
//      ps.print("nr_sv")
//      supportVectors.foreach(sv => ps.print(s" ${sv.size}"))
//      ps.println()
//   	// }
//
//    ps.println("SV")
//    for (i <- 0 until numVectors) {
//      supportVectors.foreach { sv =>
//        ps.print(f"${sv(i).coefficient}%.16f")
//      }
//      // ???
//      ps.println()
//    }
//
//    //   	const double * const *sv_coef = model->sv_coef;
//    //   	const svm_node * const *SV = model->SV;
//    //
//    //   	for(int i=0;i<l;i++)
//    //   	{
//    //   		for(int j=0;j<nr_class-1;j++)
//    //   			fprintf(fp, "%.16g ",sv_coef[j][i]);
//    //
//    //   		const svm_node *p = SV[i];
//    //
//    //   		if(param.kernel_type == PRECOMPUTED)
//    //   			fprintf(fp,"0:%d ",(int)(p->value));
//    //   		else
//    //   			while(p->index != -1)
//    //   			{
//    //   				fprintf(fp,"%d:%.8g ",p->index,p->value);
//    //   				p++;
//    //   			}
//    //   		fprintf(fp, "\n");
//    //   	}
  }
}

trait BaseModel extends Model {
  // val classes = Vec(0, 1)
  def supportVector: Vec[SupportVector]

  override def predictValues(x: List[Node]): (Double, Vec[Double]) = {
    val p = supportVector.map(sv => sv.coefficient * param.kernel(x, sv.vector)).sum - rho(0)
    (p, Vec(p))
  }

  override def toString = Array(
    param.toString,
    "total_sv " + supportVector.size,
    "rho " + rho.mkString(" ")).mkString("\n")
}

class OneClassModel(val param: Parameters, val supportVector: Vec[SupportVector], val rho: Vec[Double])
  extends BaseModel {

  def tpe = SVM.OneClass

  override def predictValues(x: List[Node]): (Double, Vec[Double]) = {
    val (p, decisionValues) = super.predictValues(x)
    if (p > 0) (1.0, decisionValues) else (-1.0, decisionValues)
  }
}

class NuSVRModel(val param: Parameters,
                 val supportVector: Vec[SupportVector],
                 val rho: Vec[Double])
  extends BaseModel {

  def tpe = SVM.Regression.Nu
}

class EpsilonSVRModel(val param: Parameters,
                      val supportVector: Vec[SupportVector],
                      val rho: Vec[Double]) extends BaseModel {

  def tpe = SVM.Regression.Epsilon
}

trait ClassificationModel extends Model {

  def supportVectors: Vec[Vec[SupportVector]]

  def label: Vec[Int]

  def numClasses: Int = supportVectors.size

  override def predictValues(x: List[Node]): (Double, Vec[Double]) = {
    val kValue          = supportVectors.map(_.map(sv => sv.coefficient * param.kernel(x, sv.vector)))
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
      val si            = (for (k <- 1 until svi.size) yield svi(k).coefficient * ki(k)).sum
      val sj            = (for (k <- 1 until svj.size) yield svj(k).coefficient * kj(k)).sum
      val sum           = si + sj - rho(p)
      val sel           = if (sum > 0) i else j
      votes(sel)        = votes(sel) + 1
      decisionValues(p) = sum
      p += 1
    }
    val li = votes.maxBy(votes(_))
    (label(li), decisionValues.toIndexedSeq)
  }

  override def toString = Array(
    param.toString,
    "total_sv " + supportVectors.size,
    "rho " + rho.mkString(" ")).mkString("\n")
}

class CSVCModel(/* val numClasses: Int, */
                val param: Parameters,
                val supportVectors: Vec[Vec[SupportVector]],
                val rho: Vec[Double],
                val label: Vec[Int])
  extends ClassificationModel {

  def tpe = SVM.Classification.C
}

class NuSVCModel(/* val numClasses: Int, */
                 val param: Parameters,
                 val supportVectors: Vec[Vec[SupportVector]],
                 val rho: Vec[Double],
                 val label: Vec[Int])
  extends ClassificationModel {

  def tpe = SVM.Classification.Nu
}


//object SVMModel {
//    def load(file: String): SVMModel = {
//      for (line <- Source.fromFile(file).getLines()) {
//        val splits = line.split(" ")
//        splits(0) match {
//          case "svm_type" => ""
//          case "kernel_type" => ""
//          case "degree" => ""
//          case "gamma" => ""
//          case "coef0" => ""
//          case "numClasses" => ""
//          case "total_sv" => ""
//          case "rho" => ""
//          case "label" => ""
//          case "probA" => ""
//          case "probB" => ""
//          case "nr_sv" => ""
//          case "SV" => ""
//        }
//      }
//      null
//    }
//
//    def save(file: String): Unit = {
//      val output = new PrintWriter(new FileWriter(file))
//      output.close()
//      //val output = Resource.fromFile(file)(Codec.UTF8)
//    }
//}

//class SolutionInfo {
//  val upper_bound_p : Double = 0
//  val upper_bound_n : Double = 0
//  val rho           : Double = 0
//}
