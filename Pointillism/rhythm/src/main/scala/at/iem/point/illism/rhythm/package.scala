/*
 *  package.scala
 *  (Pointillism)
 *
 *  Copyright (c) 2013-2018 IEM Graz / Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package at.iem.point.illism

import spire.math.Rational

package object rhythm {
  implicit val numericCompat    : Numeric   [Rational] = spire.compat.numeric   [Rational]  // Spire to Scala Numeric
  implicit val fractionalCompat : Fractional[Rational] = spire.compat.fractional[Rational]  // Spire to Scala Fractional
}
