package fontembedtest

import java.awt.{Font, GraphicsEnvironment, RenderingHints}

import scala.swing.{Component, Dimension, Frame, Graphics2D, MainFrame, SimpleSwingApplication}

object FontEmbedTest extends SimpleSwingApplication {
  lazy val top: Frame = {

    val ge      = GraphicsEnvironment.getLocalGraphicsEnvironment
    val family  = "Inconsolata"

    def register(variant: String): Unit = {
      val fnt = Font.createFont(Font.TRUETYPE_FONT, getClass.getResourceAsStream(s"/$family-$variant.ttf"))
      val ok  = ge.registerFont(fnt)
      require (ok)
    }

    register("Regular")
    register("Bold"   ) // if you remove this, you can witness that the bold rendering looks bad (synthetic)


    val fntRegular  = new Font(family, Font.PLAIN, 24)
    val fntBold     = new Font(family, Font.BOLD , 24)

    val c: Component = new Component {
      preferredSize = new Dimension(256, 128)

      override protected def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
        g.setFont(fntRegular)
        g.drawString("Regular Font" , 8f, 24f)
        g.setFont(fntBold)
        g.drawString("Bold Font"    , 8f, 52f)
      }
    }

    new MainFrame {
      title = "FontEmbedTest"
      contents = c
    }
  }
}