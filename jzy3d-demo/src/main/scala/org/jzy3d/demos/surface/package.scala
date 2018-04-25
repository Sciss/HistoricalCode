package org.jzy3d.demos

import org.jzy3d.plot3d.builder.Mapper

import scala.language.implicitConversions

package object surface {
  implicit def functionToMapper(func: (Double, Double) => Double): Mapper =
    (p1: Double, p2: Double) => func(p1, p2)
}