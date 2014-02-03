package de.sciss.svm
package train

import de.sciss.svm.solve.CSVCSolver

private[svm] object CSVCTrainer extends ClassificationTrainer {
  protected def tpe: Type = SVM.Classification.C

  def solver: FormulationSolver = CSVCSolver

  protected def mkModel(classes: Vec[Int], param: Parameters, supportVectors: Vec[Vec[SupportVector]],
                        rho: Vec[Double]): ClassificationModel =
    new CSVCModel(param = param, supportVectors = supportVectors, rho = rho, label = classes /* XXX right? */)
}