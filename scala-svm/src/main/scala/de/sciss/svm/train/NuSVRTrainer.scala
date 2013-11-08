package de.sciss.svm
package train

private[svm] object NuSVRTrainer extends RegressionTrainer {
  protected def tpe: Type = SVM.Regression.Nu

  def solver: FormulationSolver = ???
}
