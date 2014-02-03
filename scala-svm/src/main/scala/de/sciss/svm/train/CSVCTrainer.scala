package de.sciss.svm
package train

import de.sciss.svm.solve.CSVCSolver

private[svm] object CSVCTrainer extends ClassificationTrainer {
  protected def tpe: Type = SVM.Classification.C

  def solver: FormulationSolver = CSVCSolver

  protected def mkModel(classes: Vec[Int],
                        param: Parameters,
                        supportVectors: Vec[Vec[SupportVector]],
                        coefficients: Coefficients,
                        probA: Vec[Double],
                        probB: Vec[Double],
                        rho: Vec[Double]): model.ClassificationModel =
    new model.CSVCModel(
      param           = param,
      supportVectors  = supportVectors,
      coefficients    = coefficients,
      rho             = rho,
      probA           = probA,
      probB           = probB,
      label           = classes /* XXX right? */)
}