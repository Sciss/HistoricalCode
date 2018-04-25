package org.jzy3d.demos

import org.jzy3d.chart.Chart

abstract class AbstractDemo extends IDemo {
  def getName: String =
    getClass.getSimpleName

  def getPitch: String = ""

  def isInitialized: Boolean = chart.isDefined

  def getChart: Chart = chart.get

  protected var chart: Option[Chart] = None
}