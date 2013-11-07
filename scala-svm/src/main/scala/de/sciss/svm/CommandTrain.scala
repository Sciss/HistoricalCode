/*
 *  CommandTrain.scala
 *  (ScalaSVM)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	Below is a copy of the GNU Lesser General Public License
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
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
    val model   = tpe.trainer.train(param1, problem)
    model.save(outputFile)
  }
}
