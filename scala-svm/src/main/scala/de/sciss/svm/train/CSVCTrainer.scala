package de.sciss.svm
package train

import de.sciss.svm.solve.CSVCSolver

private[svm] object CSVCTrainer extends ClassificationTrainer {
  protected def tpe: Type = SVM.Classification.C

  def solver: FormulationSolver = CSVCSolver
}