package de.sciss.svm

import java.io.{PrintStream, File}

abstract class Model(
  val tpe           : Type,
  val classes       : Vec[Int], // labels
  val param         : Parameters,
  val supportVectors: Vec[Vec[SupportVector]],
  val rho           : Vec[Double]) {

  require(supportVectors.size == rho.size)

  lazy val numVectors: Int = supportVectors.iterator.map(_.size).sum

  def numClasses = classes.size

  def predict(x: List[Node]): Double = predictValues(x)

  def predict(instance: Instance): Double = predict(instance.x)

  def predictValues(x: List[Node]): Double // = 0.0

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
    ps.println(s"svm_type ${tpe.id}")
    param.write(ps)

    ps.println(s"nr_class $numClasses")
    ps.println(s"total_sv $numVectors")

    ps.print("rho")
    rho.foreach(x => ps.print(s" $x"))  // %g in the C code
    ps.println()

    // if (classes.nonEmpty) {
      ps.print("label")
      classes.foreach(lb => ps.print(s" $lb"))
      ps.println()
    // }

    //   	if(model->probA) // regression has probA only
    //   	{
    //   		fprintf(fp, "probA");
    //   		for(int i=0;i<nr_class*(nr_class-1)/2;i++)
    //   			fprintf(fp," %g",model->probA[i]);
    //   		fprintf(fp, "\n");
    //   	}
    //   	if(model->probB)
    //   	{
    //   		fprintf(fp, "probB");
    //   		for(int i=0;i<nr_class*(nr_class-1)/2;i++)
    //   			fprintf(fp," %g",model->probB[i]);
    //   		fprintf(fp, "\n");
    //   	}

    // if (supportVectors.nonEmpty) {
      ps.print("nr_sv")
      supportVectors.foreach(sv => ps.print(s" ${sv.size}"))
      ps.println()
   	// }

    ps.println("SV")
    for (i <- 0 until numVectors) {
      supportVectors.foreach { sv =>
        ps.print(f"${sv(i).coefficient}%.16f")
      }
      // ???
      ps.println()
    }

    //   	const double * const *sv_coef = model->sv_coef;
    //   	const svm_node * const *SV = model->SV;
    //
    //   	for(int i=0;i<l;i++)
    //   	{
    //   		for(int j=0;j<nr_class-1;j++)
    //   			fprintf(fp, "%.16g ",sv_coef[j][i]);
    //
    //   		const svm_node *p = SV[i];
    //
    //   		if(param.kernel_type == PRECOMPUTED)
    //   			fprintf(fp,"0:%d ",(int)(p->value));
    //   		else
    //   			while(p->index != -1)
    //   			{
    //   				fprintf(fp,"%d:%.8g ",p->index,p->value);
    //   				p++;
    //   			}
    //   		fprintf(fp, "\n");
    //   	}
  }

  override def toString = Array(
    param.toString,
    "total_sv " + supportVectors.size,
    "rho " + rho.mkString(" ")).mkString("\n")
}

class BaseModel(
    tpe           : Type,
    param         : Parameters,
    supportVectors: Vec[Vec[SupportVector]],
    rho           : Vec[Double])
  extends Model(tpe = tpe, classes = Vec(0, 1), param = param, supportVectors = supportVectors, rho = rho) {

  override def predictValues(x: List[Node]): Double =
    supportVectors(0).map(supportVector => param.kernel(x, supportVector.vector)).sum - rho(0)
}

//class CSVCModel
//
//class NUSVCModel

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
