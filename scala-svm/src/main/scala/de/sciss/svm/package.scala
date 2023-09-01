/*
 *  package.scala
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

package de.sciss

package object svm {
  // ---- types ----
  val Vec         = collection.immutable.IndexedSeq
  type Vec[+A]    = collection.immutable.IndexedSeq[A]

  final val Inf   = Double.PositiveInfinity

  type Label      = Int

  type Classifier             = (Int, Int)
  type CoefficientVector      = Vec[Double]
  type ClassifierCoefficient  = (CoefficientVector, CoefficientVector)

  // ---- logging ----

  // TODO: use proper logging
  def logInfo(what: => String): Unit = Console.out.print(what)
  def logWarn(what: => String): Unit = Console.err.print(s"\nWARNING: $what")

  // cf. https://stackoverflow.com/questions/21557195/c-fprintf-g-correspondence-in-scala-java
  def ¬(x: Double) = {
    val obj = x.toFloat.asInstanceOf[AnyRef]
    val s0  = String.format("%.6g", obj)
    val i0  = s0.lastIndexOf('e')
    val i   = if (i0 < 0) s0.length else i0
    s0.substring(0, i).replaceFirst("\\.?0+($)", "$1") + s0.substring(i)
  }
}