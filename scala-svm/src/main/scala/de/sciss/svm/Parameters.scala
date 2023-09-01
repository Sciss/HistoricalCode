/*
 *  Parameters.scala
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
  val kernel      : Kernel,
  val nu          : Double = 0.5,
  val eps         : Double = 0.001,
  val C           : Double = 1,                     // for C_SVC, EPSILON_SVR and NU_SVR
  val weights     : Map[Int, Double] = Map.empty,   // for C_SVC
  val shrinking   : Boolean = true,                 // use the shrinking heuristics
  val probability : Boolean = false,
  var gamma       : Double = 0.0) {

  override def toString = Array(
    kernel.writeString).mkString("\n")

  def write(ps: PrintStream): Unit = {
    kernel.write(ps)
  }
}

// class SVMTrainParameter

object Parameters {
  def poly(gamma: Double, degree: Double, coef0: Double) = new PolyParameter(gamma, degree, coef0)
}

class EpsilonSVRSVMParameter(
    kernel: Kernel,
    nu    : Double,
    eps   : Double,
    C     : Double,
    val p : Double)
  extends Parameters(kernel, nu, eps, C = C)
