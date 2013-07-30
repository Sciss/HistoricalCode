/*
 *  Interval.scala
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

import annotation.switch
import language.implicitConversions

object Interval {
  // implicit val ordering = Ordering.by[Interval, Int](_.semitones)

  implicit def fromPitchTuple(tup: (Pitch, Pitch)): DirectedInterval = new DirectedInterval(tup._2.midi - tup._1.midi)

  // dim5 or aug4 or tt?
  private val names_en  = Array("per1", "min2", "maj2", "min3", "maj3", "per4", "dim5", "per5", "min6", "maj6", "min7", "maj7", "per8")
  private val names_de  = Array("r1",   "kl2",  "gr2",  "kl3",  "gr3",  "r4",   "v5",   "r5",   "kl6",  "gr6",  "kl7",  "gr7",  "r8"  )

  def toString(semitones: Int): String = {
    val arr = if (Language.default == Language.English) names_en else names_de
    val i   = if (semitones == 12) 12 else semitones % 12
    s"$semitones [${arr(i)}]"
  }
}
sealed trait Interval extends Any {
  def semitones: Int
  // http://www.mta.ca/faculty/arts-letters/music/pc-set_project/pc-set_new/pages/page02/page02.html
  implicit final def `class`: IntervalClass = {
    val i = semitones % 12
    val j = if (i <= 6) i else 12 - i
    new IntervalClass(j)
  }
//  def map(fun: Int => Int): this.type
}
object UndirectedInterval {
  implicit val ordering = Ordering.by[UndirectedInterval, Int](_.semitones)
}
final class UndirectedInterval(val semitones: Int) extends AnyVal with Interval {
  def modOctave: Interval = if (semitones < 12) this else new UndirectedInterval(semitones % 12)

  def map(fun: Int => Int): Interval = new UndirectedInterval(fun(semitones))

  override def toString = Interval.toString(semitones)
}
object DirectedInterval {
//  implicit def fromPitchTuple(tup: (Pitch, Pitch)): DirectedInterval = new DirectedInterval(tup._2.midi - tup._1.midi)
}
final class DirectedInterval(val steps: Int) extends AnyVal with Interval {
  def semitones: Int = math.abs(steps)
  def direction: Int = math.signum(steps)

  override def toString = {
    val und = Interval.toString(semitones)
    (direction: @switch) match {
      case  1 => "+" + und
      case -1 => "-" + und
      case  0 => und
    }
  }

  implicit def undirected: UndirectedInterval = new UndirectedInterval(semitones)
//  def map(fun: Int => Int): Interval = new DirectedInterval(fun(semitones), direction)
}

final class IntervalClass(val steps: Int) extends AnyVal {
  override def toString = s"ic$steps"
}
