/*
 *  NoteUtil.scala
 *  (Pointillism)
 *
 *  Copyright (c) 2013-2014 IEM Graz / Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package at.iem.point.illism

import de.sciss.midi
import midi.{Track, TickRate}
import collection.immutable.{IndexedSeq => Vec}
import annotation.tailrec

object NoteUtil {
  //  def findMonophones(in: Vec[OffsetNote], overlapTolerance: Double = 0.1): Vec[Vec[OffsetNote]] = {
  //    ???
  //  }

  def toTrack(notes: Vec[OffsetNote], channel: Int = 0)(implicit tickRate: TickRate): Track = {
    val events = notes.flatMap(_.toMIDI(channel))
    Track(events)
  }

  /** Cleans a list of notes by throwing away those under a given minimum duration,
    * and adjusting the others to match note beginnings and endings within this
    * minimum duration window.
    */
  def clean(notes: Vec[OffsetNote], minDuration: Double = 0.1): Vec[OffsetNote] = {
    if (notes.isEmpty) return Vector.empty

    val tAll = notes.filter(_.duration >= minDuration)
    var tFlt = Vector.empty[OffsetNote]
    tAll.iterator.foreach { n =>
      val start   = n.offset
      val minStop = start + minDuration
      tFlt      = tFlt.collect {
        // no overlap, keep as is
        case n1 if n1.stop <= start => n1

        // overlaps, but begins early enough
        case n1 if start - n1.offset >= minDuration =>
          if (n1.stop >= minStop) n1 else {
            n1.replaceStop(start) // truncate end to align with current note's offset
          }

        // overlaps, but ends late enough
        case n1 if n1.stop >= minStop =>
          if (start - n1.offset >= minDuration) n1 else {
            n1.replaceStart(start) // truncate beginning to align with current note's offset
          }
      }
      tFlt :+= n
    }
    tFlt
  }

  def stabbings(notes: Vec[OffsetNote]): Vec[Double] = {
    notes.flatMap(n => n.offset :: n.stop :: Nil).toSet.toIndexedSeq.sorted
  }

  def splitMelodicHarmonic(notes: Vec[OffsetNote], minChordDuration: Double = 0.1, minSeq: Int = 2):
    (Vec[((Double, Double), Vector[OffsetNote])], Vec[((Double, Double), Vector[Chord])]) = {

    var resSeq      = Vector.empty[((Double, Double), Vector[OffsetNote])]
    var resChords   = Vector.empty[((Double, Double), Vector[Chord])]
    if (notes.isEmpty) return (resSeq, resChords)

    // the cleaning is only used for generating the stabbing points
    val tFlt    = NoteUtil.clean(notes, minDuration = minChordDuration)
    val stabs   = NoteUtil.stabbings(tFlt)
    var chords  = Vector.empty[Chord]
    val pairs   = stabs.sliding(2, 1)

    //println(s"First ten raw     notes ${notes.take(10).mkString(", ")}")
    //println(s"First ten cleaned notes ${notes.take(10).mkString(", ")}")
    //println(s"First ten stabs         ${stabs.take(10).mkString(", ")}")

    // initially put all notes into the sequential bin, then
    // as chords are detected remove them from this set
    var seqSet  = Set(notes:_ *)

    //var i = 0

    pairs.foreach {
      case Vec(start, stop) =>
        // first condition: given a stabbing span, a note must begin no later than the span start
        // plus the tolerence, and it must end no earlier than the span stop minus the tolerance.
        val par0 = notes.filter { n =>
          n.offset - minChordDuration <= start && n.stop + minChordDuration >= stop
        }
        // second condition: for each note candidate, there must be a different note
        // which overlaps more than 50% with that note.
        val par = par0.filter { n1 =>
          n1.duration >= minChordDuration && par0.exists(n2 => n1 != n2 && {
            val start = math.max(n1.offset, n2.offset)
            val stop  = math.min(n1.stop,   n2.stop  )
            val over  = math.max(0.0, stop - start)
            val rel   = over / n1.duration
            rel > 0.5

//            (n1.offset < n2.stop && (n1.stop - n2.offset > minChordDuration)) ||
//            (n2.offset < n1.stop && (n2.stop - n1.offset > minChordDuration))
          })
        }
        if (par.size >= 2) {
          val segm = par
//          par.map { n0 =>
//            val n1 = if (start - n0.offset > minChordDuration) n0.replaceStart(start) else n0
//            val n2 = if (n1.stop - stop    > minChordDuration) n1.replaceStop (stop ) else n1
//            n2
//          }
          chords :+= Chord(segm.sortBy(_.pitch))
          seqSet --= par
        }
    }

    val seq   = seqSet.to[Vector].sortBy(_.offset)
    chords    = chords.sortBy(_.minOffset)
//println(s"Now we've got ${seq.size} sequential and ${chords.size} parallel entries")

    var seqIdx      = 0
    var chordIdx    = 0
    var seqSpan     = Vector.empty[OffsetNote]
    var chordSpan   = Vector.empty[Chord]

    def isExhausted = seqIdx == seq.size && chordIdx == chords.size
    def isHoriz     = seqIdx < seq.size && {
      chordIdx == chords.size || seq(seqIdx).offset < chords(chordIdx).minOffset
    }

    def flush(): Unit = {
      if (seqSpan.nonEmpty) {
        resSeq :+= ((seqSpan.head.offset, seqSpan.map(_.stop).max), seqSpan)
        seqSpan  = Vector.empty
      }
      if (chordSpan.nonEmpty) {
        resChords :+= ((chordSpan.head.minOffset, chordSpan.map(_.maxStop).max), chordSpan)
        chordSpan   = Vector.empty
      }
    }

    @tailrec def horizontal(): Unit = {
      val n     = seq(seqIdx)
      seqIdx   += 1
      seqSpan :+= n
      if (!isExhausted) {
        if (isHoriz) horizontal()
        else {
          flush()
          vertical()
        }
      }
    }

    @tailrec def vertical(): Unit = {
      val n       = chords(chordIdx)
      chordIdx   += 1
      chordSpan :+= n
      if (!isExhausted) {
        if (!isHoriz) vertical()
        else {
          flush()
          horizontal()
        }
      }
    }

    if (isHoriz) horizontal() else if (!isExhausted) vertical()
    flush()

    (resSeq, resChords)
  }
}