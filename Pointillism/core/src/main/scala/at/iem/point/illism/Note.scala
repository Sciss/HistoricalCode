/*
 *  Note.scala
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

import de.sciss.midi
import de.sciss.midi.TickRate

import scala.collection.immutable.{IndexedSeq => Vec}

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

  override def toString =
    s"$productPrefix($pitch, dur = $durationString, vel = $velocity})"

  def withOffset(offset: Double): OffsetNote =
    OffsetNote(offset = offset, /* channel = channel, */ pitch = pitch,
      duration = duration, velocity = velocity /*, release = release */)
}

final case class OffsetNote(offset: Double, /* channel: Int, */ pitch: Pitch, duration: Double, velocity: Int /*, release: Int = 0 */)
  extends NoteLike with ConvertibleToMIDI with ConvertibleToNotes {

  override def toString =
    s"$productPrefix($pitch, off = $offsetString, dur = $durationString, vel = $velocity)"

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