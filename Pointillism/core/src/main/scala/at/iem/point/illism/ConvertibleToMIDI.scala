/*
 *  ConvertibleToMIDI.scala
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

object ConvertibleToMIDI {
  def play(events: Vec[midi.Event])(implicit rate: TickRate): Unit = {
    val track   = midi.Track(events)
    val seq     = midi.Sequence(Vector(track))
    val player  = midi.Sequencer.open()
    player.play(seq)
  }
}
trait ConvertibleToMIDI {
  def toMIDI(implicit tickRate: TickRate): List[midi.Event] = toMIDI(0)

  def toMIDI(channel: Int)(implicit tickRate: TickRate): List[midi.Event]

  def play(): Unit = {
    implicit val rate: TickRate = TickRate.tempo(120, 1024)
    val events  = toMIDI
    ConvertibleToMIDI.play(events.toIndexedSeq)
  }
}