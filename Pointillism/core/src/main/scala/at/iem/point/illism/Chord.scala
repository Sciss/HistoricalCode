/*
 *  Chord.scala
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

import scala.annotation.tailrec
import scala.collection.breakOut
import scala.collection.immutable.{IndexedSeq => Vec}

/** A chord made of a sequence of notes. Notes must be in ascending order with respect to their
  * pitches.
  */
final case class Chord(notes: Vec[OffsetNote]) extends ConvertibleToMIDI with ConvertibleToNotes {
  require(notes.isSortedBy(_.pitch))

  def minOffset: Double = notes.minBy(_.offset).offset
  def maxStop:   Double = notes.maxBy(_.stop).stop

  /** The number of notes in the chord. */
  def size: Int = notes.size

  /** Calculates the arithmetic mean of all offset times. */
  def avgOffset: Double = notes.map(_.offset).sum / notes.size

  /** Calculates the arithmetic mean of all stop times. */
  def avgStop: Double = notes.map(_.stop).sum / notes.size

  /** Calculates the __geometric__ mean of all durations. */
  def avgDuration: Double = Math.pow(notes.map(_.duration).product, 1.0 / notes.size)

  /** Collects the pitches of the chord.
    *
    * @return  the pitches in ascending order
    */
  def pitches: Vec[Pitch] = notes.map(_.pitch)

  /** Returns the framing interval which is the interval between lowest and highest pitch in the chord. */
  def frameInterval: UndirectedInterval = (notes.last.pitch to notes.head.pitch).undirected

  /** Returns a sequence of subsequent intervals. */
  def layeredIntervals: Vec[UndirectedInterval] = pitches.intervals.map(_.undirected)

  /** Returns a sequence of all intervals between all pairs of pitches. */
  def allIntervals: Vec[UndirectedInterval] = {
    val b = Vec.newBuilder[UndirectedInterval]
    @tailrec def loop(sq: Vec[Pitch]): Unit = {
      sq match {
        case head +: tail =>
          tail.foreach(t => b += (head to t).undirected)
          loop(tail)
        case _ =>
      }
    }
    loop(pitches)
    b.result().sorted
  }

  def toMIDI(channel: Int)(implicit tickRate: TickRate): List[midi.Event] =
    notes.flatMap(_.toMIDI(channel))(breakOut)

  def toNotes: Vec[OffsetNote] = notes
}