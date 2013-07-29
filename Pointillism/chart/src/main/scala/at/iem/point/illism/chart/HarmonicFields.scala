/*
 *  HarmonicFields.scala
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
package chart

import de.sciss.midi
import scalax.chart.XYChart
import org.jfree.chart.title.TextTitle
import java.awt.{Color, Font}
import org.jfree.chart.axis.{NumberTickUnit, NumberAxis}
import scalax.chart.Charting._

object HarmonicFields {
  def contingencyChart(seq: midi.Sequence, allIntervals: Boolean = false,
              intervalClasses: Boolean = false, chordSize: Int = -1, title: String = "Title"): ContingencyChartPanel = {
    val n   = seq.notes
    val nf0 = ChordUtil.findHarmonicFields(n)
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

  def barChart(seq: midi.Sequence, weighted: Boolean = false, allIntervals: Boolean = false,
            intervalClasses: Boolean = false, chordSize: Int = -1, title: String = "Title"): XYChart = {
    val n   = seq.notes
    val nf0 = ChordUtil.findHarmonicFields(n)
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