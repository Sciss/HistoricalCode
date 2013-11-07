package de.sciss.svm
package train

private[train] trait OneClassOrRegressionTrainer extends Trainer {
  protected def tpe: Type

  def train(param: Parameters, problem: Problem): Model = {
    val numClasses = 2

    val decisionFunction = trainOne(param, problem, 0, 0)

    val supportVectors = Vec.newBuilder[SupportVector]
    for (i <- 0 until problem.size if math.abs(decisionFunction.alpha(i)) > 0) {
      supportVectors += new SupportVector(problem.x(i), decisionFunction.alpha(i), i + 1)
    }

    assert(numClasses == 2)
    new BaseModel /* SVMModel */(
      // numClasses,
      tpe,
      param,
      Vec(supportVectors.result()),
      Vec(decisionFunction.rho))
  }

  def solver: FormulationSolver = new OneClassSolver
}
