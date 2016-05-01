package scalapt

import java.awt.event.{MouseAdapter, MouseEvent, WindowAdapter, WindowEvent}
import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Frame, Graphics, Graphics2D, RenderingHints}

class MyFrame(title: String, image: BufferedImage, scale: Double)
  extends Frame(title) {

  var closing : Boolean = false

  pack()

  val ins = getInsets
  val w   = math.ceil(image.getWidth () * scale).toInt
  val h   = math.ceil(image.getHeight() * scale).toInt
  val dim = new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom)
  setSize(dim)
  setResizable(false)
  addWindowListener(new WindowAdapter() {
    override def windowClosing(we : WindowEvent) = {
      closing = true
      dispose()
    }
  })

  addMouseListener(new MouseAdapter() {
    override def mouseClicked(me : MouseEvent) = {
      val sx = me.getX
      val sy = me.getY
      val x = sx - ins.left + 1
      val y = h - (sy - ins.top) - 3

      println(s"$x : $y")
    }
  })

  setLocationRelativeTo(null)
  setBackground(Color.BLACK)
  setVisible(true)

  override def paint(graphics : Graphics) = {
    val g2d = graphics.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
    g2d.scale(scale, scale)
    g2d.drawImage(image, ins.left, ins.top, null)
  }
}