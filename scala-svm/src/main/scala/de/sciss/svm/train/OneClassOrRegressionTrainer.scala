package de.sciss.svm
package train

private[train] trait OneClassOrRegressionTrainer extends Trainer {
  protected def tpe: Type

  def train(param: Parameters, problem: Problem): OneClassModel = {
    val decisionFunction = trainOne(param, problem, 0, 0)

    val supportVectors = Vec.newBuilder[SupportVector]
    for (i <- 0 until problem.size if math.abs(decisionFunction.alpha(i)) > 0) {
      supportVectors += new SupportVector(problem.x(i), decisionFunction.alpha(i), i + 1)
    }

    new OneClassModel(param = param, supportVector = supportVectors.result(), rho = Vec(decisionFunction.rho))
  }

  // def solver: FormulationSolver = new OneClassSolver
}
