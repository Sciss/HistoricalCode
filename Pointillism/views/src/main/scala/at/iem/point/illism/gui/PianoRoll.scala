/*
 *  PianoRoll.scala
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

package at.iem.point.illism
package gui

import java.awt.{Color, Graphics2D}
import javax.swing.JComponent
import collection.immutable.{IndexedSeq => IIdxSeq}
import impl.{PianoRollImpl => Impl}

object PianoRoll {
  def apply(): PianoRoll = new Impl {
    protected def repaint() {}
  }
  def j(): JComponent with PianoRoll = new Impl.JComponent

  final case class NoteDecoration(color: Option[Color] = None)
}
trait PianoRoll {
  var pitchRange: (Int, Int)
  var timeRange: (Double, Double)
  var notes : IIdxSeq[OffsetNote]
  var chords: IIdxSeq[Chord]
  var decoration: Map[OffsetNote, PianoRoll.NoteDecoration]
  var keyWidth: Int
  /** The height of each normalized key in pixels. This will be rounded to an even number! */
  var keyHeight: Int

  // def preferredKeyWidth: Int

  def preferredHeight: Int = keyHeight * pitchRange._2 - pitchRange._1

  def paint        (g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit
  def paintKeyboard(g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit
  def paintRoll    (g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit
}