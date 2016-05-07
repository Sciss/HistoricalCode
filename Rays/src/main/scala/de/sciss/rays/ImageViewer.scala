/*
 * ImageViewer.scala
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
import java.awt.RenderingHints
import java.beans.{PropertyChangeEvent, PropertyChangeListener}

import scala.swing.{Component, Dimension, Graphics2D, Insets}

class ImageViewer(image0: BufferedImage)
  extends Component {

  private var _scale = 1.0
  private var _image = image0

  def insets: Insets = peer.getInsets

  def scale: Double = _scale
  def scale_=(value: Double): Unit = if (_scale != value) {
    _scale = value
    updatePreferredSize()
    repaint()
  }

  def image: BufferedImage = _image
  def image_=(value: BufferedImage): Unit = {
    _image = value
    updatePreferredSize()
    repaint()
  }

  private var _highQuality = true
  def highQuality: Boolean = _highQuality
  def highQuality_=(value: Boolean): Unit = if (_highQuality != value) {
    _highQuality = value
    repaint()
  }

  private def updatePreferredSize(): Unit = {
    val ins = peer.getInsets(in)
    val w   = math.ceil(_image.getWidth  * _scale).toInt + ins.left + ins.right
    val h   = math.ceil(_image.getHeight * _scale).toInt + ins.top  + ins.bottom
    preferredSize = new Dimension(w, h)
  }

  peer.addPropertyChangeListener("insets", new PropertyChangeListener {
    def propertyChange(e: PropertyChangeEvent): Unit = updatePreferredSize()
  })

  private[this] val in = new Insets(0, 0, 0, 0)

  override def paint(g: Graphics2D): Unit = {
    val ins = peer.getInsets(in)
    if (_highQuality) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING , RenderingHints.VALUE_ANTIALIAS_OFF        )
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    }
    val atOrig = g.getTransform
    g.translate(ins.left, ins.top)
    g.scale(scale, scale)
    g.drawImage(image, 0, 0, null)
    g.setTransform(atOrig)
  }
}