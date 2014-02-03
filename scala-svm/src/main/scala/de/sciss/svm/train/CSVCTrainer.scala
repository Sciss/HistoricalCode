/*
 *  CSVCTrainer.scala
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