/*
 *  PianoRoll.scala
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
package gui

import java.awt.{Color, Graphics2D}

import at.iem.point.illism.gui.impl.{PianoRollImpl => Impl}
import javax.swing.JComponent

import scala.collection.immutable.{IndexedSeq => Vec}

object PianoRoll {
  def apply(): PianoRoll = new Impl {
    protected def repaint(): Unit = ()
  }
  def j(): JComponent with PianoRoll = new Impl.JComponent

  final case class NoteDecoration(color: Option[Color] = None)
}
trait PianoRoll {
  var pitchRange: (Int, Int)
  var timeRange: (Double, Double)
  var notes : Vec[OffsetNote]
  var chords: Vec[Chord]
  var decoration: Map[OffsetNote, PianoRoll.NoteDecoration]
  var keyWidth: Int
  /** The height of each normalized key in pixels. This will be rounded to an even number! */
  var keyHeight: Int

  var showLines   : Boolean
  var showKeyboard: Boolean

  // def preferredKeyWidth: Int

  def preferredHeight: Int = keyHeight * pitchRange._2 - pitchRange._1

  def paint        (g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit
  def paintKeyboard(g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit
  def paintRoll    (g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit
}