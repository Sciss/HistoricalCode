package de.sciss.svm
package train

import de.sciss.svm.solve.OneClassSolver

private[svm] object OneClassTrainer extends OneClassOrRegressionTrainer {
  protected def tpe: Type = SVM.OneClass

  def solver: FormulationSolver = OneClassSolver
}
