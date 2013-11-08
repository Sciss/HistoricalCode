package de.sciss.svm
package train

private[svm] object EpsilonSVRTrainer extends RegressionTrainer {
  protected def tpe: Type = SVM.Regression.Epsilon

  def solver: FormulationSolver = ???
}
