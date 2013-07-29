package at.iem.point.illism

import de.sciss.midi
import midi.TickRate
import collection.immutable.{IndexedSeq => Vec}

object ConvertibleToMIDI {
  def play(events: Vec[midi.Event])(implicit rate: TickRate) {
    val track   = midi.Track(events.toIndexedSeq)
    val seq     = midi.Sequence(Vector(track))
    val player  = midi.Sequencer.open()
    player.play(seq)
  }
}
trait ConvertibleToMIDI {
  def toMIDI(implicit tickRate: TickRate): List[midi.Event] = toMIDI(0)

  def toMIDI(channel: Int)(implicit tickRate: TickRate): List[midi.Event]

  def play() {
    implicit val rate = TickRate.tempo(120, 1024)
    val events  = toMIDI
    ConvertibleToMIDI.play(events.toIndexedSeq)
  }
}