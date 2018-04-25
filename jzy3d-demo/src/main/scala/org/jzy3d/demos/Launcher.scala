package org.jzy3d.demos

import org.jzy3d.chart.{ChartLauncher, Settings}
import org.jzy3d.maths.Rectangle

object Launcher {
  var USE_ACCEL = false

  def openDemo(demo: IDemo, rectangle: Rectangle = DEFAULT_WINDOW): Unit = {
    if (USE_ACCEL) Settings.getInstance.setHardwareAccelerated(true)
    demo.init()
    val chart = demo.getChart
    ChartLauncher.instructions()
    ChartLauncher.openChart(chart, rectangle, demo.getName)
  }

  def openStaticDemo(demo: IDemo): Unit = {
    openStaticDemo(demo, DEFAULT_WINDOW)
  }

  def openStaticDemo(demo: IDemo, rectangle: Rectangle): Unit = {
    if (USE_ACCEL) Settings.getInstance.setHardwareAccelerated(true)
    val chart = demo.getChart
    ChartLauncher.openStaticChart(chart, rectangle, demo.getName)
    ChartLauncher.screenshot(demo.getChart, "./data/screenshots/" + demo.getName + ".png")
  }

  protected var DEFAULT_CANVAS_TYPE : String    = "awt"
  protected var DEFAULT_WINDOW      : Rectangle = new Rectangle(0, 0, 600, 600)
}