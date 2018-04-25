package org.jzy3d.demos

import org.jzy3d.chart.Chart
import org.jzy3d.chart.factories.AWTChartComponentFactory
import org.jzy3d.colors.Color
import org.jzy3d.maths.Coord3d
import org.jzy3d.plot3d.primitives.Scatter
import org.jzy3d.plot3d.rendering.canvas.Quality

import scala.util.Random

object ScatterDemo extends Demo {
  def mkChart(): Chart = {
    val size    = 500000
    val points  = new Array[Coord3d](size)
    val colors  = new Array[Color](size)

    val r = new Random(0)

    var i = 0
    while (i < size) {
      val x = r.nextFloat() - 0.5f
      val y = r.nextFloat() - 0.5f
      val z = r.nextFloat() - 0.5f
      points(i) = new Coord3d(x, y, z)
      val a = 0.25f
      colors(i) = new Color(x, y, z, a)
      i += 1
    }

    val scatter = new Scatter(points, colors)
    val chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt")
    chart.getScene.add(scatter)
    chart
  }
}
