package org.jzy3d.demos

import org.jzy3d.chart.Chart
import org.jzy3d.chart.controllers.keyboard.camera.AWTCameraKeyController
import org.jzy3d.chart.factories.AWTChartComponentFactory
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.colors.{Color, ColorMapper}
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.builder.{Builder, Mapper}
import org.jzy3d.plot3d.primitives.Shape
import org.jzy3d.plot3d.rendering.canvas.Quality

object ColorWaveDemo extends Demo {
  def mkChart(): Chart = {
    val mapper: Mapper = (x: Double, y: Double) => x * math.sin(x * y)
    val range = new Range(-3, 3)
    val steps = 80
    val surface: Shape = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper)
    import surface._
    setColorMapper(new ColorMapper(new ColorMapRainbow, getBounds.getZmin, getBounds.getZmax, new Color(1, 1, 1, .5f)))
    setFaceDisplayed(true)
    setWireframeDisplayed(false)
    val f = new AWTChartComponentFactory
    val chart = new Chart(f, Quality.Advanced)
    chart.getScene.getGraph.add(surface)
    chart.addController(new AWTCameraKeyController)
    chart
  }
}