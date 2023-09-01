/*
 * Color.scala
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

import scala.annotation.switch

/**
  * A triple of color coefficients.
  *
  * @author Jon Hanson
  */
object RGB {
  final val black = RGB(0.0, 0.0, 0.0)
  final val white = RGB(1.0, 1.0, 1.0)

  final val red   = RGB(1.0, 0.0, 0.0)
  final val green = RGB(0.0, 1.0, 0.0)
  final val blue  = RGB(0.0, 0.0, 1.0)

  /** Give a black (minimum values) color. */
  def apply(): RGB = black
}

final case class RGB(red: Double, green: Double, blue: Double) {

  override def toString: String = s"{R : $red, G : $green, B : $blue}"

  /** Queries component.
    *
    * @param i  index, where 0 yields red, 1 yields green, 2 yields blue
    */
  def apply(i: Int): Double =
    (i: @switch) match {
      case 0 => red
      case 1 => green
      case 2 => blue
    }

  def unary_+ : RGB     = this
  def unary_- : Vector3 = Vector3(-red, -green, -blue)

  /** Adds r, g, b components element-wise. */
  def +(rhs: RGB): RGB = RGB(red + rhs.red, green + rhs.green, blue + rhs.blue)

  /** Subtracts r, g, b components element-wise. */
  def -(rhs: RGB): RGB = RGB(red - rhs.red, green - rhs.green, blue - rhs.blue)

  /** Scales r, g, b components. */
  def *(s: Double): RGB = RGB(red * s, green * s, blue * s)

  /** Scales r, g, b components. */
  def /(d: Double): RGB = {
    val s = 1.0 / d
    this * s
  }

  /** Scales r, g, b components element-wise. */
  def *(rhs: RGB): RGB = RGB(red * rhs.red, green * rhs.green, blue * rhs.blue)

  def length       : Double = math.sqrt(lengthSquared)
  def lengthSquared: Double = red * red + green * green + blue * blue

  def normalize: RGB = {
    val s = 1.0 / length
    RGB(red * s, green * s, blue * s)
  }

  def hasNaNs: Boolean = red.isNaN || green.isNaN || blue.isNaN

  /** Clips components to (0, 1) */
  def clamp: RGB = RGB(MathUtil.clamp(red), MathUtil.clamp(green), MathUtil.clamp(blue))

  /** Maximum r, g, b component. */
  def max: Double = math.max(math.max(red, green), blue)
}

/**
  * SuperSampling is a 2x2 grid of colors, used for super-sampling
  * in order improve anti-aliasing.
  *
  * @author Jon Hanson
  */
object SuperSampling {
  final val black = SuperSampling(RGB.black, RGB.black, RGB.black, RGB.black)
}

final case class SuperSampling(c00: RGB, c10: RGB, c01: RGB, c11: RGB) {
  def apply(x: Int, y: Int): RGB =
    (x, y) match {
      case (0, 0) => c00
      case (0, 1) => c01
      case (1, 0) => c10
      case (1, 1) => c11
    }

  def merge(rhs: SuperSampling, n: Int): SuperSampling =
    SuperSampling(
      (c00 * n + rhs.c00) / (n + 1),
      (c10 * n + rhs.c10) / (n + 1),
      (c01 * n + rhs.c01) / (n + 1),
      (c11 * n + rhs.c11) / (n + 1)
    )

  def clamp: RGB =
    (c00.clamp + c10.clamp + c01.clamp + c11.clamp) * 0.25

  def +(rhs: SuperSampling): SuperSampling =
    SuperSampling(c00 + rhs.c00, c10 + rhs.c10, c01 + rhs.c01, c11 + rhs.c11)
}
