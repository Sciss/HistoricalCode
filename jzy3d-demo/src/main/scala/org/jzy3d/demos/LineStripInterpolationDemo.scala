package org.jzy3d.demos

import org.jzy3d.chart.{AWTChart, Chart}
import org.jzy3d.colors.Color
import org.jzy3d.maths.Coord3d
import org.jzy3d.maths.algorithms.interpolation.algorithms.BernsteinInterpolator
import org.jzy3d.plot3d.primitives.{LineStrip, Point}
import org.jzy3d.plot3d.rendering.canvas.Quality

import scala.collection.JavaConverters._

/** From here: https://stackoverflow.com/questions/50008796/jzy3d-3d-interpolation-plots-not-rendering */
object LineStripInterpolationDemo extends Demo {
  def mkChart(): Chart = {
    val ctlCoords = Seq(
      new Coord3d(0.0f, 0.0f,  0.0f),
      new Coord3d(1.0f, 0.0f,  1.0f),
      new Coord3d(1.0f, 0.0f,  2.0f),
      new Coord3d(1.0f, 1.0f,  2.0f),
      new Coord3d(0.0f, 1.0f,  2.0f),
      new Coord3d(3.0f, 2.0f, -1.0f)
    )

    val intp        = new BernsteinInterpolator
    val intpCoords  = intp.interpolate(ctlCoords.asJava, 30)
    val ctlPts      = ctlCoords         .map(new Point(_, Color.RED , 5.0f))
    val intpPts     = intpCoords.asScala.map(new Point(_, Color.BLUE, 3.0f))

    val line = new LineStrip(intpCoords)
    line.setWireframeColor(Color.BLACK)

    val chart = new AWTChart(Quality.Intermediate)
    chart.add(line)
    chart.add(ctlPts .asJava)
    chart.add(intpPts.asJava)
    chart
  }
}
