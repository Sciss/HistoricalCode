package org.jzy3d.demos

import org.jzy3d.chart.{ChartLauncher, Settings}
import org.jzy3d.maths.Rectangle

object Launcher {
  var USE_ACCEL = false

  def openDemo(demo: Demo, rectangle: Rectangle = new Rectangle(0, 0, 600, 600)): Unit = {
    if (USE_ACCEL) Settings.getInstance.setHardwareAccelerated(true)
    val chart = demo.mkChart()
    ChartLauncher.instructions()
    ChartLauncher.openChart(chart, rectangle, demo.name)
  }
}