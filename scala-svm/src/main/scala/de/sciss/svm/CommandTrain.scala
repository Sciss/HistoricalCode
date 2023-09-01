/*
 *  CommandTrain.scala
 *  (ScalaSVM)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.svm

import de.sciss.file._

object CommandTrain {
  def apply(args: Vec[String]): Unit = {
    val inputFile   = file(args.head) // .absolute
    val outputFile  = inputFile.replaceExt("model")

    // val param   = new SVMParameter(kernel = ???, nu = ???, eps = ???, gamma = ???)
    val param   = new Parameters(LinearKernel)
    val problem = Problem.read(param, io.Source.fromFile(inputFile, "UTF-8"))
    val param1  = new Parameters(new RBFKernel(param.gamma), nu = 0.5, eps = 1.0e-3, gamma = param.gamma)
    val tpe     = SVM.Classification.C
    val model   = tpe.trainer.train(problem, param1)
    model.save(outputFile)
  }
}
