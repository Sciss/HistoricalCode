/*
 *  OneClassModel.scala
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

class OneClassModel(val param: Parameters,
                    val supportVector: Vec[SupportVector],
                    val coefficientVector: CoefficientVector,
                    val rho: Double)
  extends BaseModel {

  def tpe = SVM.OneClass

  override def predictValues(x: List[Node]): (Double, Vec[Double]) = {
    val (p, decisionValues) = super.predictValues(x)
    if (p > 0) (1.0, decisionValues) else (-1.0, decisionValues)
  }
}
