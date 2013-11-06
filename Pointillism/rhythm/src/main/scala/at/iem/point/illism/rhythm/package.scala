package at.iem.point.illism

import spire.math.Rational

package object rhythm {
  implicit val numericCompat    = spire.compat.numeric   [Rational]  // Spire to Scala Numeric
  implicit val fractionalCompat = spire.compat.fractional[Rational]  // Spire to Scala Fractional
}
