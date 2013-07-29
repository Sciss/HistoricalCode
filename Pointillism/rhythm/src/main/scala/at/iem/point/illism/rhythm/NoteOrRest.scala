/*
 *  NoteOrRest.scala
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

import language.implicitConversions
import spire.math.Rational

object NoteOrRest {
  implicit def fromInt(i: Int): NoteOrRest =
    if (i < 0) Rest(-i) else if (i > 0) Note(i) else sys.error("dur must not be zero")
}
sealed trait NoteOrRest {
  def dur: Rational
  def toNumber: Rational
  def *(factor: Rational): NoteOrRest
  def isRest: Boolean
}
final case class Note(dur: Rational) extends NoteOrRest {
  def toNumber =  dur
  def *(factor: Rational): Note = copy(dur * factor)
  def isRest = false
}
final case class Rest(dur: Rational) extends NoteOrRest {
  def toNumber = -dur
  def *(factor: Rational): Rest = copy(dur * factor)
  def isRest = true
}