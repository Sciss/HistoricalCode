/*
 *  Cell.scala
 *  (Pointillism)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
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

import spire.math.Rational
import collection.immutable.{IndexedSeq => IIdxSeq}

final case class Cell(id: Int, elements: IIdxSeq[NoteOrRest], dur: Rational) {
  override def toString = s"Cell#$id($prettyElements}, dur = $dur)"

  /** Number of elements in the cell */
  def size = elements.size

  /** Pretty formatted string representation of the cell's elements */
  def prettyElements: String = elements.map(_.toNumber).mkString("[", ", ", "]")

  /** Pretty formatted string representation of the cell */
  def pretty: String = {
    val s = f"#$id%2s: $prettyElements, ${dur.toString}" // "${dur.toString}%3s"
    s + (" " * math.max(0, 40 - s.length))
  }

  /** Multiplies the elements by a factor so that their sum will become the nominal total duration. */
  def normalized: Cell = {
    val factor = dur / elements.map(_.dur).sum
    copy(elements = elements.map(_ * factor) /* , dur = 1 */)
  }

  /** Scales the total duration of the cell by a given factor */
  def * (factor: Rational): Cell = copy(dur = dur * factor)
}