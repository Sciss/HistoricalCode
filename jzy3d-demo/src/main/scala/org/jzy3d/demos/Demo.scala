package org.jzy3d.demos

import org.jzy3d.chart.Chart

trait Demo {
  def main(args: Array[String]): Unit =
    Launcher.openDemo(this)

  def name: String = {
    val s = getClass.getSimpleName
    if (s.endsWith("$")) s.substring(0, s.length - 1) else s
  }

  def mkChart(): Chart
}