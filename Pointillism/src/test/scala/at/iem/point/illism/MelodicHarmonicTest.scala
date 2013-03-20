package at.iem.point.illism

import de.sciss.midi

object MelodicHarmonicTest extends App {
  val sq = midi.Sequence.read(
    "/Users/hhrutz/Desktop/IEM/POINT/composers/elisabeth_harnik/in/2013-01-24_172125/snippet 48.mid")
  val n = sq.notes
  val (m, h) = NoteUtil.splitMelodicHarmonic(n)
}