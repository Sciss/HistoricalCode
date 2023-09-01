/*
 *  NuSVRTrainer.scala
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

private[svm] object NuSVRTrainer extends RegressionTrainer {
  protected def tpe: Type = SVM.Regression.Nu

  def solver: FormulationSolver = ???
}
