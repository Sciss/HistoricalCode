package org.jzy3d.demos

import java.awt.EventQueue

import org.jzy3d.chart.factories.AWTChartComponentFactory
import org.jzy3d.chart.{Chart, ChartLauncher}
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.colors.{Color, ColorMapper}
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.builder.{Builder, Mapper}
import org.jzy3d.plot3d.rendering.canvas.Quality

object WTF extends Runnable {
  def main(args: Array[String]): Unit = EventQueue.invokeLater(this)

  def run(): Unit = {

    // Define a function to plot
    val mapper = new Mapper() {
      def f(x: Double, y: Double): Double = 10 * Math.sin(x / 10) * Math.cos(y / 20) * x
    }

    // Define range and precision for the function to plot
    val range = new Range(-150, 150)
    val steps = 50

    // Create a surface drawing that function
    val surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper)
    surface.setColorMapper(new ColorMapper(new ColorMapRainbow, surface.getBounds.getZmin, surface.getBounds.getZmax, new Color(1, 1, 1, .5f)))
    surface.setFaceDisplayed(true)
    surface.setWireframeDisplayed(false)
    surface.setWireframeColor(Color.BLACK)

    // Create a chart and add the surface
    val f = new AWTChartComponentFactory
    val chart = new Chart(f, Quality.Advanced)
    chart.getScene.getGraph.add(surface)
    ChartLauncher.openChart(chart)
  }
}