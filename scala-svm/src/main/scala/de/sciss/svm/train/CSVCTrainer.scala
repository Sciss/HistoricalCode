package de.sciss.svm
package train

private[svm] object CSVCTrainer extends ClassificationTrainer {
  protected[this] def solver: FormulationSolver = ???

  protected def tpe: Type = SVM.Classification.C
}
