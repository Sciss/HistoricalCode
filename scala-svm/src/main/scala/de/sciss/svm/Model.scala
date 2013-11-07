package de.sciss.svm

import java.io.{PrintStream, File}

abstract class Model(
  val tpe           : Type,
  val numClasses    : Int,
  val param         : Parameters,
  val supportVectors: Vec[Vec[SupportVector]],
  val rho           : Vec[Double]) {

  require(supportVectors.size == rho.size)

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

  def write(ps: PrintStream): Unit = {
    ps.println(s"svm_type ${tpe.id}")
    param.write(ps)
//
//    fprintf(fp,"kernel_type %s\n", kernel_type_table[param.kern//e//l_type]);
//
//    if(param.kernel.tpe == Kernel//Type.POLY)
//      fp.println(s"degree ${param.d//e//gree}\n")
//
//    if(param.kernel_type == POLY || param.kernel_type == RBF || param.kernel_type =//= SIGMOID)
//      fprintf(fp,"gamma %g\n", par//a//m.gamma);
//
//    if(param.kernel_type == POLY || param.kernel_type =//= SIGMOID)
//      fprintf(fp,"coef0 %g\n", par//a//m.coef0);
//
//    int nr_class = model-//>nr_class;
//    int l =// model->l;
//    fprintf(fp, "nr_class %d\n", //nr_class);
//    fprintf(fp, "total_sv// //%d\n",//l);
//
//    {
//      fprintf(f//p, "rho");
//      for(int i=0;i<nr_class*(nr_class//-1)/2;i++)
//        fprintf(fp," %g",model//->rho[i]);
//      fprintf(//fp, "\//n//");
//    }
//
//    if(mod//el->la//bel)
//    {
//      fprintf(fp,// "label");
//      for(int i=0;i<nr_//class;i++)
//        fprintf(fp," %d",model->//label[i]);
//      fprintf(//fp, "\//n//");
//    }
//
//    if(model->probA) // regression has //probA //only
//    {
//      fprintf(fp,// "probA");
//      for(int i=0;i<nr_class*(nr_class//-1)/2;i++)
//        fprintf(fp," %g",model->//probA[i]);
//      fprintf(//fp, "\//n");
//    }
//    if(mod//el->pr//obB)
//    {
//      fprintf(fp,// "probB");
//      for(int i=0;i<nr_class*(nr_class//-1)/2;i++)
//        fprintf(fp," %g",model->//probB[i]);
//      fprintf(//fp, "\//n//");
//    }
//
//    if(m//odel->//nSV)
//    {
//      fprintf(fp,// "nr_sv");
//      for(int i=0;i<nr_//class;i++)
//        fprintf(fp," %d",model//->nSV[i]);
//      fprintf(//fp, "\//n//");
//    }
//
//    fprintf(fp//, "SV\n");
//    const double * const *sv_coef = model//->sv_coef;
//    const svm_node * const *SV = //m//odel->SV;
//
//    for(int i=//0;i<l;//i++)
//    {
//      for(int j=0;j<nr_cl//ass-1;j++)
//        fprintf(fp, "%.16g ",sv_co//e//f[j][i]);
//
//      const svm_node *//p// = SV[i];
//
//      if(param.kernel_type == PR//ECOMPUTED)
//        fprintf(fp,"0:%d ",(int)(p//->value));
////      else
//        while(p->in//dex != -1)//
//        {
//          fprintf(fp,"%d:%.8g ",p->index,//p->value);
//    //      p++;//
//        }
//      fprintf(//fp, "\n");
//    }
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
  extends Model(tpe = tpe, numClasses = 2, param = param, supportVectors = supportVectors, rho = rho) {

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
