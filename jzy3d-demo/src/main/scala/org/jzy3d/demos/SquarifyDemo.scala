package org.jzy3d.demos

import org.jzy3d.chart.Chart
import org.jzy3d.chart.factories.AWTChartComponentFactory
import org.jzy3d.colors.{Color, ColorMapper}
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.builder.{Builder, Mapper}
import org.jzy3d.plot3d.rendering.canvas.Quality
import org.jzy3d.plot3d.transform.squarifier.XZSquarifier

object SquarifyDemo extends Demo {
  def mkChart(): Chart = {
    // Define a function to plot
    val mapper = new Mapper() {
      def f(x: Double, y: Double): Double = x * Math.sin(x * y) * 10
    }

    // Define range and precision for the function to plot
    val range   = new Range(-2.5f, 2.5f)
    val steps   = 80
    val yRange  = new Range(-5, 5)

    // Create the object to represent the function over the given range.
    val surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, yRange, steps), mapper)
    surface.setColorMapper(new ColorMapper(new ColorMapRainbow, surface.getBounds.getZmin, surface.getBounds.getZmax,
      new Color(1, 1, 1, .5f)))
    surface.setFaceDisplayed(true)
    surface.setWireframeDisplayed(false)

    // Create a chart
    val chart = AWTChartComponentFactory.chart(Quality.Intermediate, "newt")

    //This addition keeps the aspect ratio of the X and Y data
    //but makes X and Z square
    chart.getView.setSquarifier(new XZSquarifier)
    chart.getView.setSquared(true)
    chart.getScene.getGraph.add(surface)
    chart
  }
}
