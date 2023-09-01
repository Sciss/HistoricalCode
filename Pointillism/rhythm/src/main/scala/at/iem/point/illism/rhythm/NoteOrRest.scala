/*
 *  NoteOrRest.scala
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

package at.iem.point.illism.rhythm

import spire.math.Rational

import scala.language.implicitConversions

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
  def toNumber: Rational =  dur
  def *(factor: Rational): Note = copy(dur * factor)
  def isRest = false
}
final case class Rest(dur: Rational) extends NoteOrRest {
  def toNumber: Rational = -dur
  def *(factor: Rational): Rest = copy(dur * factor)
  def isRest = true
}