package de.sciss.svm

object CommandTrain {
  def apply(args: Vec[String]): Unit = {
    //    param.svm_type = C_SVC;
    //   	param.kernel_type = RBF;
    //   	param.degree = 3;
    //   	param.gamma = 0;	// 1/num_features
    //   	param.coef0 = 0;
    //   	param.nu = 0.5;
    //   	param.cache_size = 100;
    //   	param.C = 1;
    //   	param.eps = 1e-3;
    //   	param.p = 0.1;
    //   	param.shrinking = 1;
    //   	param.probability = 0;
    //   	param.nr_weight = 0;
    //   	param.weight_label = NULL;
    //   	param.weight = NULL;

    // new SVMParameter()

    val inputPath = args.head

    // val param   = new SVMParameter(kernel = ???, nu = ???, eps = ???, gamma = ???)
    val param   = new SVMParameter(new LinearKernel)
    val problem = Problem.read(param, io.Source.fromFile(inputPath, "UTF-8"))
    val param1  = new SVMParameter(new RBFKernel(param.gamma), nu = 0.5, eps = 1.0e-3, gamma = param.gamma)
    val model   = ???
  }
}
