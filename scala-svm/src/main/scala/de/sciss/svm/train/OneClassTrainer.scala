package de.sciss.svm
package train

private[svm] object OneClassTrainer extends OneClassOrRegressionTrainer {
  protected def tpe: Type = SVM.OneClass
}
