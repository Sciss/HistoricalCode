package org.jzy3d.demos

import org.jzy3d.chart.Chart
import org.jzy3d.chart.factories.AWTChartComponentFactory
import org.jzy3d.colors.Color
import org.jzy3d.maths.Coord3d
import org.jzy3d.plot3d.primitives.LineStrip
import org.jzy3d.plot3d.rendering.canvas.Quality

import scala.util.Random

object LineStripDemo extends Demo {
  def mkChart(): Chart = {
    val r     = new Random(0L)
    val ls    = new LineStrip(Color.BLACK)
    val ext   = 50f
    val step  = 1f
    def rnd(): Float = (r.nextFloat() * 2 - 1) * step
    (0 until 10000).foldLeft(new Coord3d(ext, ext, ext)) { (pt, _) =>
      ls.add(pt)
      new Coord3d(pt.x + rnd(), pt.y + rnd(), pt.z + rnd())
    }

    ls.setFaceDisplayed     (false)
    ls.setShowPoints        (true)
    ls.setWireframeDisplayed(false)

    val chart = AWTChartComponentFactory.chart(Quality.Nicest, "newt")

    chart.getScene.getGraph.add(ls)
    chart
  }
}
