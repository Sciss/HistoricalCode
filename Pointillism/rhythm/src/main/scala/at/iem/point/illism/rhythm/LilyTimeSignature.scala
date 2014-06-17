/*
 *  LilyTimeSignature.scala
 *  (Pointillism)
 *
 *  Copyright (c) 2013-2014 IEM Graz / Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package at.iem.point.illism.rhythm

import scala.annotation.switch
import spire.math.Rational

object LilyTimeSignature {
  def apply(id: Int): LilyTimeSignature = (id: @switch) match {
    case Raw    .id => Raw
    case Rounded.id => Rounded()
    case Decimal.id => Decimal
  }
  case object Raw extends LilyTimeSignature {
    final val id = 0
  }
  object Rounded {
    final val id = 1
  }
  case class Rounded(denom: Set[Int] = Set(1, 2, 4, 8)) extends LilyTimeSignature {
    private val denomRed = denom.filter { i => !denom.exists(j => j > i && j % i == 0) }

    def id = Rounded.id
    def apply(raw: Rational): (Rational, Float) = {
      val dint = raw.denominator.toInt
      if (denom.contains(dint)) return (raw, 0f)

      val nint = raw.numerator.toInt
      val m = denomRed.map { i =>
        val f   = Rational(i, dint)
        val r   = (f * nint).round / i
        val err = (r/raw - 1).toFloat // relative error
        (r, err)
      }

      val res = m.minBy { case (_, err) => math.abs(err) }
      res
    }
  }
  case object Decimal extends LilyTimeSignature {
    final val id = 2
  }
}
sealed trait LilyTimeSignature { def id: Int }
