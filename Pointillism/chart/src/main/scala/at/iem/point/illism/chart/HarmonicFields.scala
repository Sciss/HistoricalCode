/*
 *  HarmonicFields.scala
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
package chart

import java.awt.{Color, Font}

import org.jfree.chart.axis.{NumberAxis, NumberTickUnit}
import org.jfree.chart.title.TextTitle
import scalax.chart.XYChart
import scalax.chart.api._

import scala.collection.immutable.{IndexedSeq => Vec}

object HarmonicFields {
  def contingencyChart(notes: Vec[OffsetNote], allIntervals: Boolean = false,
              intervalClasses: Boolean = false, chordSize: Int = -1, title: String = "Title"): ContingencyChartPanel = {
    // val n   = seq.notes
    val nf0 = ChordUtil.findHarmonicFields(notes)
    val nf  = if (chordSize < 0) nf0 else nf0.filter(_.size == chordSize)

    //    var max = 0.0
    var mp  = Map.empty[Int, Map[Int, Int]] withDefaultValue(Map.empty withDefaultValue 0)

    nf.foreach { ch =>
      val iv = if (allIntervals) ch.allIntervals else ch.layeredIntervals
      for ((i,ii) <- iv.zipWithIndex; (j,jj) <- iv.zipWithIndex if ii < jj) {
        val x = if (intervalClasses) i.`class`.steps else i.semitones % 12
        val y = if (intervalClasses) j.`class`.steps else j.semitones % 12
        val xm = mp(x)
        val ym = mp(y)
        mp += x -> (xm + (y -> (xm(y) + 1)))
        mp += y -> (ym + (x -> (ym(x) + 1)))
      }
    }

    //    println(mp)

    val panel = ContingencyChartPanel(mp, if (intervalClasses) 7 else 12, title)
    panel
  }

  def barChart(notes: Vec[OffsetNote], weighted: Boolean = false, allIntervals: Boolean = false,
            intervalClasses: Boolean = false, chordSize: Int = -1, title: String = "Title"): XYChart = {
    // val n   = seq.notes
    val nf0 = ChordUtil.findHarmonicFields(notes)
    val nf  = if (chordSize < 0) nf0 else nf0.filter(_.size == chordSize)

    val iv  = nf.flatMap { ch =>
      val res = if (allIntervals) ch.allIntervals else ch.layeredIntervals
      res.map { i =>
        val steps   = if (intervalClasses) i.`class`.steps else i.semitones % 12
        val weight  = if (weighted) ch.avgDuration else 1.0
        (steps, weight)
      }
    }

    var ivm = Map.empty[Int, Double] withDefaultValue 0.0
    iv.foreach { case (i, dur) =>
      ivm += i -> (ivm(i) + dur)
    }

    val data  = ivm.map { case (i, dur) => (i, dur) } .toXYSeriesCollection()
    val chart = XYBarChart(data, title = title)

    chart.peer.removeLegend()
    chart.peer.setTitle(new TextTitle(chart.title, new Font("SansSerif", java.awt.Font.BOLD, 12)))
    val plot  = chart.plot
    val rangeX  = plot.getDomainAxis.asInstanceOf[NumberAxis]
    plot.getRenderer.setSeriesPaint(0, Color.darkGray)
    //    plot.getRenderer().setBarPainter(new StandardBarPainter())
    rangeX.setTickUnit(new NumberTickUnit(1))
    rangeX.setRange(-0.5, (if (intervalClasses) 7 else 12) - 0.5)
    if (!weighted) {
      val rangeY  = plot.getRangeAxis.asInstanceOf[NumberAxis]
      rangeY.setTickUnit(new NumberTickUnit(1))
    }

    chart
  }
}