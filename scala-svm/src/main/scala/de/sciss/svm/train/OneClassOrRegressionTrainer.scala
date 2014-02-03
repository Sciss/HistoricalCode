package de.sciss.svm
package train

private[train] trait OneClassOrRegressionTrainer extends Trainer {
  protected def tpe: Type

  def train(problem: Problem, param: Parameters): model.OneClassModel = {
    val decisionFunction = trainOne(problem, param, 0, 0)

    val supportVectors    = Vec.newBuilder[SupportVector]
    val coefficientVector = Vec.newBuilder[Double       ]

    for (i <- 0 until problem.size if math.abs(decisionFunction.alpha(i)) > 0) {
      supportVectors    += new SupportVector(problem.x(i), i + 1)
      coefficientVector += decisionFunction.alpha(i)
    }

    new model.OneClassModel(param = param,
      supportVector     = supportVectors.result(),
      coefficientVector = coefficientVector.result(),
      rho               = decisionFunction.rho)
  }

  // def solver: FormulationSolver = new OneClassSolver
}
