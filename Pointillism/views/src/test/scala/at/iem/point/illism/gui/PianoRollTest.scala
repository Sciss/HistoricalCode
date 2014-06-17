package at.iem.point.illism
package gui

import java.awt.{Color, EventQueue}
import javax.swing.{WindowConstants, JFrame}

object PianoRollTest extends App with Runnable {
  EventQueue.invokeLater(this)

  def run(): Unit = {
    val f = new JFrame("Piano Roll")
    val comp = PianoRoll.j()
    comp.showLines    = false
    comp.showKeyboard = false
    //    val sq = de.sciss.midi.Sequence.read("/home/hhrutz/IEM/POINT/composers/mattias_skoeld/blind/Study_#134C.mid")
    //    val n = sq.notes

    val n = Vector.tabulate(30) { i =>
      val off = i * 2
      val dur = math.random * 1.75 + 0.25
      val pch = (math.random * 88).toInt + 21
      OffsetNote(off, pch.asPitch, dur, 80)
    }
    comp.notes    = n
    val c = Vector.tabulate(10) { i =>
      val off = i * 6
      val dur = math.random * 1.75 + 0.25
      val pch = (math.random * 80).toInt + 21
      val cn  = Vector.tabulate(3)(i => OffsetNote(off + math.random * 2 - 1,
        (pch + (math.random * i * 4).toInt + 1).asPitch, dur + math.random, velocity = 80))
      Chord(cn.sortBy(_.pitch))
    }
    comp.chords   = c
    val deco      = PianoRoll.NoteDecoration(Some(Color.red))
    // comp.decoration  = n.filter(_ => math.random > 0.75).map(n => n -> deco)(breakOut)
    f.getContentPane.add(comp)
    f.pack()
    f.setLocationRelativeTo(null)
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    f.setVisible(true)
  }
}