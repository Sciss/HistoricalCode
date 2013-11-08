package de.sciss.svm
package solve

private[svm] object CSVCSolver extends FormulationSolver {
  //  const svm_problem *prob, const svm_parameter* param,
  // 	double *alpha, Solver::SolutionInfo* si, double Cp, double Cn)
  def solve(problem: Problem, param: Parameters): Solution = {
    val l         = problem.size
    val minusOnes = new Array[Double](l)
    val alpha     = new Array[Double](l)

    //    int l = prob->l;
    //   	double *minus_ones = new double[l];
    //   	schar *y = new schar[l];
    //
    //   	int i;
    //
    //   	for(i=0;i<l;i++)
    //   	{
    //   		alpha[i] = 0;
    //   		minus_ones[i] = -1;
    //   		if(prob->y[i] > 0) y[i] = +1; else y[i] = -1;
    //   	}
    //
    //   	Solver s;
    //   	s.Solve(l, SVC_Q(*prob,*param,y), minus_ones, y,
    //   		alpha, Cp, Cn, param->eps, si, param->shrinking);
    //
    //   	double sum_alpha=0;
    //   	for(i=0;i<l;i++)
    //   		sum_alpha += alpha[i];
    //
    //   	if (Cp==Cn)
    //   		info("nu = %f\n", sum_alpha/(Cp*prob->l));
    //
    //   	for(i=0;i<l;i++)
    //   		alpha[i] *= y[i];

    ???
  }
}
