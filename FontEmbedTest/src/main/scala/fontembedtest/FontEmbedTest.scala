package fontembedtest

import scala.swing.{Component, Dimension, Frame, Graphics2D, MainFrame, SimpleSwingApplication}

object FontEmbedTest extends SimpleSwingApplication {
  lazy val top: Frame = {

    val c: Component = new Component {
      preferredSize = new Dimension(256, 128)

      override protected def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)
      }
    }

    new MainFrame {
      title = "FontEmbedTest"
      contents = c
    }
  }
}