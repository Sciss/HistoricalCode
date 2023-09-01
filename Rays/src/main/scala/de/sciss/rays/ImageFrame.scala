/*
 * ImageFrame.scala
 * (Rays)
 *
 * Copyright (c) 2016 Hanns Holger Rutz. All rights reserved.
 *
 * This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.rays

import java.awt.image.BufferedImage

import scala.swing.Frame

class ImageFrame(image0: BufferedImage, title0: String = "Rendering", scale0: Double = 1.0)
  extends Frame {

  private[this] var _closed = false

  val viewer: ImageViewer = new ImageViewer(image0)
  viewer.scale = scale0

  contents  = viewer
  title     = title0

  override def closeOperation(): Unit = _closed = true

  def closed: Boolean = _closed

  pack().centerOnScreen()
}
