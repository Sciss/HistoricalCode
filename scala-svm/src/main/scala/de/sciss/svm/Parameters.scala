/*
 *  Parameters.scala
 *  (ScalaSVM)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *  Copyright (c) 2013 Shixiong Zhu.
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

import java.io.PrintStream

trait Gamma {
  def gamma: Double
}

trait Degree {
  def degree: Double
}

trait Coef0 {
  def coef0: Double
}

class PolyParameter(
  val gamma : Double,
  val degree: Double,
  val coef0 : Double) extends Gamma with Degree with Coef0

class SigmoidParameter(
  val gamma: Double,
  val coef0: Double) extends Gamma with Coef0

class Parameters(
  val kernel: Kernel,
  val nu    : Double = 0.5,
  val eps   : Double = 0.001,
  val C     : Double = 0,                       // for C_SVC, EPSILON_SVR and NU_SVR
  val weights: Map[Int, Double] = Map.empty,    // for C_SVC
  val probability: Boolean = false,
  var gamma : Double = 0.0) {

  override def toString = Array(
    kernel.toString).mkString("\n")

  def write(ps: PrintStream): Unit = {
    kernel.write(ps)
  }
}

// class SVMTrainParameter

object SVMParameter {
  def poly(gamma: Double, degree: Double, coef0: Double) = new PolyParameter(gamma, degree, coef0)
}

class EpsilonSVRSVMParamter(
  kernel: Kernel,
  nu    : Double,
  eps   : Double,
  C     : Double,
  val p : Double) extends Parameters(kernel, nu, eps, C = C)
