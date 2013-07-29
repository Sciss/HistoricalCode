/*
 *  Note.scala
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

import de.sciss.midi
import midi.TickRate
import collection.immutable.{IndexedSeq => Vec}

sealed trait NoteLike {
  //  /**
  //   * MIDI channel (0 to 15)
  //   */
  //  def channel: Int

  /** MIDI pitch number */
  def pitch: Pitch

  /** Duration __in seconds__ */
  def duration: Double

  /** MIDI attack velocity */
  def velocity: Int

  //  /**
  //   * MIDI release velocity (typically zero)
  //   */
  //  def release: Int

  /** Returns the duration rounded to milliseconds, as a String. */
  final def durationString: String = s"${duration.roundSecondsToMillis}s"

  final def noteOn(channel: Int):  midi.NoteOn   = midi.NoteOn (channel, pitch.midi, velocity)
  final def noteOff(channel: Int): midi.NoteOff  = midi.NoteOff(channel, pitch.midi, 0)
}

/** A logical grouping of a note on and note off.
  *
  * @param pitch     the MIDI key number (pitch)
  * @param duration  the duration __in seconds__
  * @param velocity    the attack velocity
  */
final case class Note(/* channel: Int, */ pitch: Pitch, duration: Double, velocity: Int /*, release: Int = 0 */)
  extends NoteLike {

  override def toString = {
    s"$productPrefix($pitch, dur = $durationString, vel = $velocity})"
  }

  def withOffset(offset: Double): OffsetNote =
    OffsetNote(offset = offset, /* channel = channel, */ pitch = pitch,
      duration = duration, velocity = velocity /*, release = release */)
}

final case class OffsetNote(offset: Double, /* channel: Int, */ pitch: Pitch, duration: Double, velocity: Int /*, release: Int = 0 */)
  extends NoteLike with ConvertibleToMIDI with ConvertibleToNotes {

  override def toString = {
    s"$productPrefix($pitch, off = $offsetString, dur = $durationString, vel = $velocity)"
  }

  def replaceStart(newOffset: Double): OffsetNote = {
    val newDuration = duration - (newOffset - offset)
    copy(offset = newOffset, duration = newDuration)
  }

  def replaceStop(newStop: Double): OffsetNote = {
    val newDuration = newStop - offset
    copy(duration = newDuration)
  }

  def offsetString: String = s"${offset.roundSecondsToMillis}s"

  def stop: Double = offset + duration

  def dropOffset: Note = Note(/* channel = channel, */ pitch = pitch, duration = duration, velocity = velocity)

  def toMIDI(channel: Int)(implicit tickRate: TickRate): List[midi.Event] = {
    val tps       = tickRate.value
    val startTick = (offset * tps + 0.5).toLong
    val stopTick  = (stop   * tps + 0.5).toLong
    midi.Event(startTick, noteOn(channel)) :: midi.Event(stopTick, noteOff(channel)) :: Nil
  }

  def toNotes = Vec(this)
}