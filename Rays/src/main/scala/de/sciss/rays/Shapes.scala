/*
 * Shapes.scala
 * (Rays)
 *
 * Copyright (c) 2016 Hanns Holger Rutz.
 * Copyright (c) 2016 Jon Hanson.
 * All rights reserved.
 *
 * This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.rays

/**
  * Sphere
  *
  * @author Jon Hanson
  */
final case class Sphere(name : String, material : Material, centre : Point3, radius : Double)
  extends Shape {

  def intersect(ray: Ray, eps: Double): Option[Double] = {
    val e = ray.origin - centre
    val f = ray.dir dot e
    val g = f * f - (e dot e) + radius * radius
    if (g > 0.0) {
      val det = math.sqrt(g)
      val t = -f - det
      if (t > eps) {
        Some(t)
      } else {
        val t = -f + det
        if (t > eps)
          Some(t)
        else
          None
      }
    } else {
      None
    }
  }

  def normal(p: Point3): Vector3 = (p - centre).normalize
}

/**
  * An axis-aligned infinite plane.
  * Only allows light through in one direction (controlled by posFacing)
  *
  * @author Jon Hanson
  */
final case class Plane(name: String, material: Material, side: Axis.Type, posFacing: Boolean, v: Double)
  extends Shape {

  def intersect(ray: Ray, eps: Double): Option[Double] = {
    if ((math.abs(ray.dir(side)) > Double.MinPositiveValue) &&
      ((ray.origin(side) > v) == posFacing)) {
      val t = (v - ray.origin(side)) / ray.dir(side)
      if (t > eps)
        Some(t)
      else
        None
    } else {
      None
    }
  }

  def normal(p: Point3) =
    side match {
      case Axis.X => Vector3.XUnit
      case Axis.Y => Vector3.YUnit
      case Axis.Z => Vector3.ZUnit
    }
}
