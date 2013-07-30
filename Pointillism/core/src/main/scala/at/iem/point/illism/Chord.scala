/*
 *  Chord.scala
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

import annotation.tailrec
import collection.breakOut
import collection.immutable.{IndexedSeq => Vec}
import de.sciss.midi
import midi.TickRate

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

  def toNotes = notes
}