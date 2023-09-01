/*
 *  RegressionModel.scala
 *  (ScalaSVM)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *  Copyright (c) 2013-2014 Shixiong Zhu.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

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
