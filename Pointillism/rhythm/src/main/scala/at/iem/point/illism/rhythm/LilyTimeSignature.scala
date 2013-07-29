/*
 *  LilyTimeSignature.scala
 *  (Pointillism)
 *
 *  Copyright (c) 2013 IEM Graz / Hanns Holger Rutz. All rights reserved.
 *
 *  This software is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either
 *  version 3, june 2007 of the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License (gpl.txt) along with this software; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
