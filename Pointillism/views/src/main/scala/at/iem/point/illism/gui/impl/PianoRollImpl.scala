/*
 *  PianoRollImpl.scala
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
package impl

import java.awt.{Dimension, Graphics, RenderingHints, Color, Graphics2D}
import collection.immutable.{IndexedSeq => IIdxSeq}

object PianoRollImpl {
  class JComponent extends javax.swing.JComponent with PianoRollImpl{
    override def paintComponent(g: Graphics): Unit =
      paint(g.asInstanceOf[Graphics2D], 0, 0, getWidth, getHeight)

    override def getPreferredSize: Dimension = {
      if (isPreferredSizeSet) return super.getPreferredSize
      val pw = keyWidth + ((timeRange._2 - timeRange._1) * 24).toInt
      new Dimension(pw, preferredHeight)
    }
  }

  private implicit val noteSpace        = (n: OffsetNote) => (n.offset, n.stop)
  private final val defaultTimeRange    = (0.0, 60.0)
  private final val defaultPitchRange   = (21, 109)
  private final val defaultKeyWidth     = 48
  private final val defaultKeyHeight    = 8 // 12 // 24

  private def isBlack(pch: Int) = {
    val c = pch % 12
    c == 1 || c == 3 || c == 6 || c == 8 || c == 10
  }

  private final val colrGridLines     = new Color(0xD0, 0xD0, 0xD0)
  private final val colrGridWhite     = Color.white
  private final val colrGridBlack     = new Color(0xE8, 0xE8, 0xE8)
  private final val colrNotes         = new Color(0x40, 0x80, 0xFF)
  private final val colrChords        = new Color(0xFF, 0x20, 0x40)
  private final val colrChordOutline  = new Color(0xC0, 0x20, 0x40)
}
/* private[gui] */ trait PianoRollImpl extends PianoRoll {
  import PianoRollImpl._
  import PianoRoll.NoteDecoration

  protected def repaint(): Unit

  private var _notes      = IIdxSeq.empty[OffsetNote]
  // private var _notesTree  = RangedSeq.empty[OffsetNote, Double]
  private var _chords     = IIdxSeq.empty[Chord]
  private var _decoration = Map.empty[OffsetNote, NoteDecoration]
  private var _pitchRange = defaultPitchRange
  private var _timeRange  = defaultTimeRange
  private var _keyWidth   = defaultKeyWidth
  private var _keyHeight  = defaultKeyHeight
  private var _keySize1   = 0
  private var _keySize2   = 0
  private var _autoRange  = true

  recalcKeySize()

  final def notes = _notes
  def notes_=(value: IIdxSeq[OffsetNote]): Unit = {
    _notes = value
    // _notesTree    = RangedSeq[OffsetNote, Double](value: _*)
    if (_autoRange && !value.isEmpty) {
      // val floor   = math.floor(_notesTree.head.offset)
      // val ceil    = math.ceil(_notesTree.filterOverlaps((_notesTree.last.offset, Double.PositiveInfinity)).map(_.stop).max)
      val floor   = math.floor(_notes.map(_.offset).min)
      val ceil    = math.ceil (_notes.map(_.stop  ).max)
      _timeRange  = (floor, ceil)
    }
    repaint()
  }

  final def chords = _chords
  def chords_=(value: IIdxSeq[Chord]): Unit = {
    _chords = value
    if (_autoRange && !value.isEmpty) {
      val floor   = math.floor(_chords.map(_.minOffset).min)
      val ceil    = math.ceil (_chords.map(_.maxStop  ).max)
      _timeRange  = (floor, ceil)
    }
    repaint()
  }

  final def pitchRange = _pitchRange
  def pitchRange_=(value: (Int, Int)): Unit = {
    if (_pitchRange != value) {
      _pitchRange = value
      repaint()
    }
  }

  final def timeRange = _timeRange
  def timeRange_=(value: (Double, Double)): Unit = {
    _autoRange = false
    if (_timeRange != value) {
      _timeRange = value
      repaint()
    }
  }

  final def decoration = _decoration
  def decoration_=(value: Map[OffsetNote, NoteDecoration]): Unit = {
    _decoration = value
    if (_notes.nonEmpty) repaint()
  }

  final def keyWidth = _keyWidth
  def keyWidth_=(value: Int): Unit =
    if (_keyWidth != value) {
      _keyWidth = value
      repaint()
    }

  final def keyHeight = _keyHeight
  def keyHeight_=(value: Int): Unit = {
    val even = value & ~1
    if (_keyHeight != even) {
      _keyHeight = even
      recalcKeySize()
      repaint()
    }
  }

  private def recalcKeySize(): Unit = {
    _keySize1 = _keyHeight + (_keyHeight >> 1)
    _keySize2 = _keyHeight << 1
  }

  def paint(g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit = {
    val kw = math.min(keyWidth, w)
    if (kw > 0) paintKeyboard(g, x, y, kw, h)
    val rw = w - kw
    if (rw > 0) paintRoll(g, x + kw, y, rw, h)
  }

  def paintKeyboard(g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit = {
    val clipOrig = g.getClip
    try {
      g.clipRect(x, y, w, h)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      val bw = _keyWidth * 2 / 3
      g.setColor(Color.black)
      g.fillRect(x, y, w, h)
      val (start, stop) = _pitchRange

      var iw    = start
      var y0 = y + h - _keyHeight
      while (iw < stop) {
        if (!isBlack(iw)) {
          val c       = iw % 12
          val isWide  = c == 2 || c == 7 || c == 9
          val sz      = if (isWide) _keySize2 else _keySize1
          val shift   = if (c == 4 || c == 11) 0 else if (c == 0 || c == 5) sz - _keyHeight else (sz - _keyHeight) >> 1
          g.setColor(if (c == 0) Color.lightGray else Color.white)
          g.fillRoundRect(x, y0 - shift, w - 1, sz - 1, 4, 4)
        }
        y0 -= _keyHeight
        iw += 1
      }

      y0      = y + h - _keyHeight
      var ib  = start
      g.setColor(Color.black)
      // val _ks1h = _keyHeight >> 1
      while (ib < stop) {
        if (isBlack(ib)) {
          g.fillRoundRect(x - 1, y0, bw, _keyHeight, 4, 4)
        }
        y0 -= _keyHeight
        ib += 1
      }

    } finally {
      g.setClip(clipOrig)
    }
  }

  def paintRoll(g: Graphics2D, x: Int, y: Int, w: Int, h: Int): Unit = {
    val clipOrig = g.getClip
    try {
      g.clipRect(x, y, w, h)
      g.setColor(colrGridLines)
      g.fillRect(x, y, w, h)
      var y0 = y + h - _keyHeight // + 1
      val (pstart, pstop) = _pitchRange
      val toff    = -_timeRange._1
      val tscale  = w / (_timeRange._2 - _timeRange._1)

      var i = pstart
      while (i < pstop) {
        g.setColor(if (isBlack(i)) colrGridBlack else colrGridWhite)
        g.fillRect(x, y0, w, _keyHeight - 1)
        y0 -= _keyHeight
        i += 1
      }

      def paintNote(n: OffsetNote, colrDefault: Color) {
        val xn = ((n.offset + toff) * tscale + x).toInt
        val wn = math.max(1, ((n.stop   + toff) * tscale + x).toInt - xn)
        val yn = y0 - (n.pitch.midi - pstart) * _keyHeight
        g.setColor(_decoration.get(n).flatMap(_.color).getOrElse(colrDefault))
        g.fillRect(xn, yn, wn, _keyHeight - 1)
      }

      y0 = y + h - _keyHeight
      _notes.foreach  { n => paintNote(n, colrNotes) }
      _chords.foreach { c =>
        val cn = c.notes
        cn.foreach(n => paintNote(n, colrChords))
        val minPch  = cn.head.pitch.midi
        val maxPch  = cn.last.pitch.midi

        val xn = ((c.minOffset + toff) * tscale + x).toInt
        val wn = math.max(1, ((c.maxStop + toff) * tscale + x).toInt - xn)
        val yn = y0 - (maxPch - pstart) * _keyHeight
        g.setColor(colrChordOutline)
        g.drawRect(xn - 2, yn - 2, wn + 3, (_keyHeight * (maxPch - minPch + 1)) + 2)
      }

    } finally {
      g.setClip(clipOrig)
    }
  }
}