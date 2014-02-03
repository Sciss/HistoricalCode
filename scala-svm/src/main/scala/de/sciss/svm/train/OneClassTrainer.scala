/*
 *  OneClassTrainer.scala
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

import de.sciss.svm.solve.OneClassSolver

private[svm] object OneClassTrainer extends OneClassOrRegressionTrainer {
  protected def tpe: Type = SVM.OneClass

  def solver: FormulationSolver = OneClassSolver
}
