package de.sciss.svm
package model


class NuSVRModel(val param: Parameters,
                 val supportVector: Vec[SupportVector],
                 val coefficientVector: CoefficientVector,
                 val rho: Double)
  extends BaseModel {

  def tpe = SVM.Regression.Nu
}

class EpsilonSVRModel(val param: Parameters,
                      val supportVector: Vec[SupportVector],
                      val coefficientVector: CoefficientVector,
                      val rho: Double)
  extends BaseModel {

  def tpe = SVM.Regression.Epsilon
}
