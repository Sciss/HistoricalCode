/*
 *  ContingencyChartPanel.scala
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

import java.awt.{Color, Font, Graphics2D}

import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.GrayPaintScale
import org.jfree.chart.renderer.xy.XYBlockRenderer
import org.jfree.chart.{ChartPanel, JFreeChart}
import org.jfree.data.xy.{MatrixSeries, MatrixSeriesCollection}
import scalax.chart.XYChart

import scala.swing.Panel

object ContingencyChartPanel {
  private val DEBUG = false

  def apply(mp: Map[Int, Map[Int, Int]], size: Int, title: String): ContingencyChartPanel = {
    val _size = size
    val m   = new MatrixSeries("Continguency", size, size)

    // normalize
    for(i <- 0 until size) {
      val maxi = (0 until size).map(jj => mp(i)(jj)).max
      for(j <- 0 until size) {
        val maxj = (0 until size).map(ii => mp(ii)(j)).max
        val max = math.max(maxi, maxj)
        val n0 = if (max > 0) 1.0 - (mp(i)(j).toDouble / max) else 1.0
        val n = n0 // math.pow(n0, 2)
        m.update(i, j, n)
      }
    }

    val coll      = new MatrixSeriesCollection(m)
    val renderer  = new XYBlockRenderer()
    val scale     = new GrayPaintScale(0.0, 1.0)
    renderer.setPaintScale(scale)
    val xAxis     = new NumberAxis("Semitones")
    xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
    xAxis.setLowerMargin(0.0)
    xAxis.setUpperMargin(0.0)
    val yAxis     = new NumberAxis("Semitones")
    yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
    yAxis.setLowerMargin(0.0)
    yAxis.setUpperMargin(0.0)
    val plot      = new XYPlot(coll, xAxis, yAxis, renderer)
    plot.setBackgroundPaint(Color.lightGray)
    plot.setDomainGridlinesVisible(false)
    plot.setRangeGridlinePaint(Color.white)
    val jchart     = new JFreeChart(title, plot)
    jchart.removeLegend()
    jchart.setBackgroundPaint(Color.white)
    val _chart: XYChart = new XYChart {
      lazy val peer: JFreeChart = jchart
    }

    val fnt = new Font("SansSerif", Font.BOLD, 18)
    val panel: ContingencyChartPanel = new ContingencyChartPanel {
      lazy val chart: XYChart = _chart
      override lazy val peer = new ChartPanel(jchart, false) with SuperMixin
      override protected def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)
        // g.drawString("Schoko", 10, 30)
        val xoff    = 49
        val right   = 7
        val yoff    = 28
        val bottom  = 39
        val w       = peer.getWidth - (xoff + right)
        val h       = peer.getHeight - (yoff + bottom)

        if (DEBUG) {
          g.setColor(Color.red)
          g.drawLine(xoff, yoff, xoff + 100, yoff)
          g.drawLine(xoff, yoff, xoff, yoff + 100)
          g.drawLine(xoff + w - 1, yoff + h - 1, xoff + w - 100, yoff + h - 1)
          g.drawLine(xoff + w - 1, yoff + h - 1, xoff + w - 1, yoff + h - 100)
        }

        g.setFont(fnt)
        val fm = g.getFontMetrics
        for (i <- 0 until _size) {
          for (j <- 0 until _size) {
            g.setColor(if (m.get(i, j) > 0.5) Color.black else Color.white)
            val cnt: Int = mp(i)(j)
            if (cnt > 0) {
              val str = cnt.toString
              val x   = (i + 0.5) / _size * w - (fm.stringWidth(str) * 0.5) + xoff
              val y   = (1.0 - ((j + 0.5) / _size)) * h + 6 /* + (fm.getAscent * 0.5) */ + yoff
              g.drawString(str, x.toFloat, y.toFloat)
            }
          }
        }
      }
    }
    panel
  }
}
trait ContingencyChartPanel extends Panel {
  def chart: XYChart
}