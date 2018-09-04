/*
 *  package.scala
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

package at.iem.point

import java.awt.EventQueue
import java.math.RoundingMode
import java.text.DecimalFormat

import de.sciss.midi
import de.sciss.midi.TickRate

import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.{IndexedSeq => Vec}
import scala.collection.{breakOut, mutable}
import scala.language.higherKinds

package object illism {
  implicit final class IllismInt(val i: Int) extends AnyVal {
    def asPitch: Pitch = new Pitch(i)
  }

  implicit final class IllismIterableLike[A, CC[~] <: Iterable[~]](val it: CC[A]) extends AnyVal {
    def pairDiff[To](implicit num: Numeric[A], cbf: CanBuildFrom[CC[A], A, To]): To = {
      val b     = cbf(it)
      val iter  = it.iterator
      if (iter.hasNext) {
        var pred = iter.next()
        while (iter.hasNext) {
          import num.mkNumericOps
          val succ = iter.next()
          b += succ - pred
          pred = succ
        }
      }
      b.result()
    }
  }

  implicit final class IllismIterable[A](val it: Iterable[A]) extends AnyVal {
    def intervals(implicit ev: A <:< Pitch): Vec[DirectedInterval] =
      it.sliding(2,1).map({ case Seq(low, high) => high to low }).toIndexedSeq

    def play()(implicit ev: A <:< ConvertibleToMIDI): Unit = {
      implicit val rate: TickRate = TickRate.tempo(120, 1024)
      val events0: Vec[midi.Event] = it.flatMap(_.toMIDI)(breakOut)
      val min     = events0.minBy(_.tick).tick
      val events  = events0.map(e => e.copy(tick = e.tick - min))
      val track   = midi.Track(events)
      val seq     = midi.Sequence(Vector(track))
      val player  = midi.Sequencer.open()
      player.play(seq)
    }

    def meanVariance(implicit num: Fractional[A]): (A, A) = {
      var sum   = num.zero
      var size  = num.zero
      val one   = num.one
      import num.mkNumericOps
      it.foreach { e =>
        sum  += e
        size += one
      }
      val mean = sum / size
      var vari = num.zero
      it.foreach { e =>
        val d = e - mean
        vari += d * d
      }

      (mean, vari)
    }

    def isSortedBy[B](fun: A => B)(implicit ord: Ordering[B]): Boolean = {
      it.sliding(2, 1).forall {
        case Seq(a, b) => ord.lteq(fun(a), fun(b))
        case _ => true  // happens when it size == 1
      }
    }

    def histogram: Map[A, Int] = {
      var res = Map.empty[A, Int] withDefaultValue 0
      it.foreach { elem =>
        res += elem -> (res(elem) + 1)
      }
      res
    }
  }

  def defer(thunk: => Unit): Unit =
    if (EventQueue.isDispatchThread) thunk else EventQueue.invokeLater(new Runnable { def run(): Unit = thunk })

  def stopSequencer(): Unit = midi.Sequencer.open().stop()

  private lazy val dfRound3 = {
    val res = new DecimalFormat("#.###")
    res.setRoundingMode(RoundingMode.HALF_UP)
    res
  }

  implicit final class IllismSeconds(val sec: Double) extends AnyVal {
    def roundSecondsToMillis: String = dfRound3.format(sec)
  }

  implicit final class IllismSequence(val sq: midi.Sequence) extends AnyVal {
    def notes: Vec[OffsetNote] = notes(-1)
    def notes(channel: Int): Vec[OffsetNote] = sq.tracks.flatMap(_.notes(channel)).sortBy(_.offset)
  }

  implicit final class IllismTrack(val t: midi.Track) extends AnyVal {
    def notes: Vec[OffsetNote] = notes(-1)
    def notes(channel: Int): Vec[OffsetNote] = {
      val r     = t.rate
      val b     = Vec.newBuilder[OffsetNote]
      val wait  = mutable.Map.empty[(Int, Int), (Double, midi.NoteOn)]
      t.events.foreach {
        case midi.Event(tick, on @ midi.NoteOn(ch, pitch, _)) if channel == -1 || channel == ch =>
          val startTime = tick / r.value
          wait += (ch, pitch) -> (startTime, on)

        case midi.Event(tick, midi.NoteOff(ch, pitch, _)) if channel == -1 || channel == ch =>
          val stopTime  = tick / r.value
          wait.remove(ch -> pitch).foreach { case (startTime, on) =>
            b += OffsetNote(offset = startTime, /* channel = ch, */ pitch = pitch.asPitch, duration = stopTime - startTime,
              velocity = on.velocity /*, release = off.velocity */)
          }

        case _ =>
      }
      if (wait.nonEmpty) {
        println(s"Warning: pending notes ${wait.mkString("(", ",", ")")}")
      }
      b.result()
    }
  }
}
