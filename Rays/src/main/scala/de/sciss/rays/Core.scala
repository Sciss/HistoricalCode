/*
 * Core.scala
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
  * @author Jon Hanson
  */
object MathUtil {
  /**
    * Tent function
    */
  def tent(x: Double): Double = {
    val x2 = 2.0 * x
    if (x2 < 1.0)
      math.sqrt(x2) - 1.0
    else
      1.0 - math.sqrt(2.0 - x2)
  }

  /**
    * Clamp a value to a range.
    */
  def clamp(x: Double, low: Double = 0.0, high: Double = 1.0): Double = {
    if (x > high)
      high
    else if (x < low)
      low
    else
      x
  }

  /**
    * Square function.
    */
  def sqr(x: Double): Double = x * x

  // Standard gamma correction value.
  final val GAMMA = 2.2

  /**
    * Standard gamma correction.
    */
  def gammaCorr(x: Double): Double = math.pow(clamp(x), 1.0 / GAMMA)
}

/**
  * Ray
  *
  * @author Jon Hanson
  */
object Ray {
  def apply(origin: Point3, dir: Vector3): Ray = Ray(dir.normalize, origin)
}
final case class Ray private(dir: Vector3, origin: Point3) {
  def apply(t: Double): Point3 = origin + dir * t
}

/**
  * Material
  *
  * @author Jon Hanson
  * @author Hanns Holger Rutz
  */
object Material {
  def diffuse   (r: Double, g: Double, b: Double): Material = new Diffuse(RGB(r, g, b), RGB.black)
  def diffuse   (color: RGB)                     : Material = new Diffuse(color, RGB.black)

  def emissive  (r: Double, g: Double, b: Double): Material = new Diffuse(RGB.black, RGB(r, g, b), true)
  def emissive  (color: RGB)                     : Material = new Diffuse(RGB.black, color, true)

  def refractive(r: Double, g: Double, b: Double): Material = new Refractive(RGB(r, g, b), RGB.black)
  def refractive(color: RGB)                     : Material = new Refractive(color, RGB.black)

  def reflective(r: Double, g: Double, b: Double): Material = new Reflective(RGB(r, g, b), RGB.black)
  def reflective(color: RGB)                     : Material = new Reflective(color, RGB.black)
}

trait Material {
  def color   : RGB
  def emColor : RGB
  def emission: RGB

  def radiance(rdr: Renderer, ray: Ray, depth: Int, p: Point3, n: Vector3, nl: Vector3): RNG.Type[RGB]
}

/**
  * Camera
  *
  * @author Jon Hanson
  */
final case class Camera(ray: Ray, fov: Double)

/**
  * Shape
  *
  * @author Jon Hanson
  */
object Shape {
  /**
    * Epsilon value to avoid object self-intersections.
    */
  final val T_EPS: Double = 1e-4
}

trait Shape {
  // Only required for debugging.
  def name: String

  def material: Material

  def intersect(ray: Ray, eps: Double): Option[Double]

  def intersect(ray: Ray): Option[Double] = intersect(ray, Shape.T_EPS)

  def normal(p: Point3): Vector3
}

/**
  * Scene
  */

object Scene {
  val distance: Ordering[(Shape, Double)] = Ordering.by(_._2)
}
final case class Scene(camera: Camera, shapes: List[Shape]) {
  /**
    * Finds the closest shape intersected by the ray.
    */
  def intersect(ray: Ray): Option[(Shape, Point3)] = {
    shapes
      .flatMap(obj => obj.intersect(ray).map(t => (obj, t)))
      .reduceOption(Scene.distance.min)
      .map({ case (obj, t) => (obj, ray(t)) })
  }
}

/**
  * Renderer
  */
object Renderer {
  // Need a maximum to avoid stack overflow.
  final val MaxDepth = 200
}

trait Renderer {
  // ---- abstract ----

  def width : Int
  def height: Int
  def scene : Scene

  def radiance(ray: Ray, depth: Int): RNG.Type[RGB]

  // ---- impl ----

  private[this] val cx: Vector3 = Vector3(width * scene.camera.fov / height, 0.0, 0.0)
  private[this] val cy: Vector3 = cx.cross(scene.camera.ray.dir).normalize * scene.camera.fov

  final def camRay(xs: Double, ys: Double): Vector3 =
    cx * (xs / width  - 0.5) +
    cy * (ys / height - 0.5)

  def render(x: Int, y: Int): RNG.Type[SuperSampling] = {
    def subPixelRad(cx: Double, cy: Double): RNG.Type[RGB] = {
      RNG.nextDouble.flatMap(d1 => {
        RNG.nextDouble.flatMap(d2 => {
          val dx      = MathUtil.tent(d1)
          val dy      = MathUtil.tent(d2)
          val sx      = x + (0.5 + cx + dx) * 0.5
          val sy      = y + (0.5 + cy + dy) * 0.5
          val dir     = scene.camera.ray.dir + camRay(sx, sy)
          val origin  = scene.camera.ray.origin
          val ray     = Ray(origin, dir)
          radiance(ray, 0)
        })
      })
    }

    for {
      aa <- subPixelRad(0, 0)
      ba <- subPixelRad(1, 0)
      ab <- subPixelRad(0, 1)
      bb <- subPixelRad(1, 1)
    } yield SuperSampling(aa, ba, ab, bb)
  }
}