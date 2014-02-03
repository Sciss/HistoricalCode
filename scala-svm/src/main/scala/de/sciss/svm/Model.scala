/*
 *  Model.scala
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

import java.io.{PrintStream, File}

trait Model {
  def tpe           : Type
  // def classes       : Vec[Int] // labels
  def param         : Parameters
  // val supportVectors: Vec[Vec[SupportVector]],

  // def rho           : Vec[Double]

  // require(supportVectors.size == rho.size)

  // lazy val numVectors: Int = supportVectors.iterator.map(_.size).sum

  // def numClasses = classes.size

  def predict(x       : List[Node]): Double = predictValues(x)._1
  def predict(instance: Instance  ): Double = predict(instance.x)

  def predictProbability(instance: Instance): (Double, Vec[Double]) = (0.0, Vec.empty)

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

  def supportVector     : Vec[SupportVector]
  def coefficientVector : CoefficientVector
  def rho               : Double

  override def predictValues(x: List[Node]): (Double, Vec[Double]) = {
    val p = (0 until supportVector.size).map { i =>
      val coef  = coefficientVector(i)
      val sv    = supportVector(i)
      coef * param.kernel(x, sv.vector)
    } .sum - rho
    (p, Vec(p))
  }

  override def predictProbability(instance: Instance): (Double, Vec[Double]) =
    predictValues(instance.x) // predict(instance)

  override def toString = Array(
    param.toString,
    "total_sv " + supportVector.size,
    "rho " + rho).mkString("\n")
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
