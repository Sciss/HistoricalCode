/*
 *  ChordUtil.scala
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

import scala.annotation.tailrec
import scala.collection.immutable.{IndexedSeq => Vec}

object ChordUtil {
  /** Tries to find the chords by clustering a given sequence of notes. */
  def findChords(notes: Vec[OffsetNote], minPoly: Int = 2, offsetTolerance: Double = 0.1,
                 stopTolerance: Double = 10.0): Vec[Chord] = {
    val b = Vec.newBuilder[Chord]
    @tailrec def loop(ns: Vec[OffsetNote]): Unit = {
      if (ns.isEmpty) return
      val h = ns.head
      // takes as many notes that satisfy the offset tolerance. because notes are sorted by
      // offset, this can strictly stop at the first note which doesn't satisfy the criterion.
      // note that we operate on `ns`, not `ns.tail`, therefore the result automatically
      // includes the head note itself.
      val (fit1, rest1) = ns.span(n => math.abs(n.offset - h.offset) <= offsetTolerance && n.offset < h.stop)
      // then from those notes that fit wrt offset, filter those which satisfy the
      // stop tolerance.
      val (fit, rest2)  = fit1.span(n => math.abs(n.stop - h.stop ) <= stopTolerance  )
      // if the minimum polyphony is found, add a new chord, and glue the two rest bins together
      val next = if (fit.size >= minPoly) {
        val sorted = fit.sortBy(_.pitch)
        b += Chord(sorted)
        rest2 ++ rest1
      } else ns.tail  // else drop h
      loop(next)
    }
    loop(notes)
    b.result()
  }

  /** Tries to find harmonic constellations which are vertical structures of a minimum
    * number of voices and duration
    */
  def findHarmonicFields(notes: Vec[OffsetNote], minPoly: Int = 2, minDuration: Double = 0.1): Vec[Chord] = {
    if (notes.isEmpty) return Vector.empty

    /*
    Algorithm:
    - place all notes in an interval tree according to their offsets and durations
    - traverse tree and eliminate intervals which are too short,
      and truncate them to better align with neighbouring notes
    - assume a segmentation of the timeline by all remaining note starts and ends
    - collect the overlapping notes for the segments thus obtains
    - convert to chords and keep those which satisfy the minimum polyphony
     */

    val tFlt  = NoteUtil.clean(notes, minDuration = minDuration)
    val stabs = NoteUtil.stabbings(tFlt)
    val res   = Vector.newBuilder[Chord]
    val pairs = stabs.sliding(2, 1)
    pairs.foreach {
      case Vec(start, stop) if stop - start >= minDuration =>
        val segm = tFlt.filter(n => n.offset <= start && n.stop >= stop).map { n =>
          n.replaceStart(start).replaceStop(stop)
        }
        if (segm.size >= minPoly) {
          res += Chord(segm.sortBy(_.pitch))
        }

      case _ =>
    }

    res.result()
  }
}