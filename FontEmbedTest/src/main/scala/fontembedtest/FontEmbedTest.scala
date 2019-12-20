package fontembedtest

import java.awt.font.{FontRenderContext, TextAttribute}
import java.awt.{Color, Font, GraphicsEnvironment, RenderingHints}
import java.text.AttributedString

import scala.swing.{Component, Dimension, Frame, Graphics2D, MainFrame, SimpleSwingApplication}
import scala.util.Try

object FontEmbedTest extends SimpleSwingApplication {
  var fntSize = 24f

  override def startup(args: Array[String]): Unit = {
    if (args.length > 0) Try(args(0).toFloat).foreach(fntSize = _)
    super.startup(args)
  }

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

    val fntRegular  = new Font(family, Font.PLAIN, 1).deriveFont(fntSize)
    val fntBold     = new Font(family, Font.BOLD , 1).deriveFont(fntSize)

    val frc         = new FontRenderContext(null, true, false)
    val sb          = fntRegular.getStringBounds("X", frc)
    val fntAscent   = math.ceil(-sb.getY).toInt
    val fntAdvanceF = sb.getWidth
    val fntAdvance  = fntAdvanceF.toInt
    val lineSpacing = 1.12
    val lineHeight  = math.ceil(sb.getHeight * lineSpacing).toInt

//    if (weight == Weight.Bold   ) result.addAttribute(WEIGHT    , WEIGHT_BOLD     , begin, end)
//    if (style  == Style .Italic ) result.addAttribute(POSTURE   , POSTURE_OBLIQUE , begin, end)

    val scale = fntSize / 24f
    val inset = (8f * scale).toInt

    val c: Component = new Component {
      preferredSize = new Dimension((440 * scale).toInt, (110 * scale).toInt)

      override protected def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING      , RenderingHints.VALUE_ANTIALIAS_ON         )
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS , RenderingHints.VALUE_FRACTIONALMETRICS_OFF)

        val testString = "Bold Font - using AttributedString"
        g.setColor(Color.lightGray)
        for (i <- 0 until testString.length) {
          g.fillRect(inset + i * fntAdvance, inset + 2 * lineHeight, fntAdvance - 2, lineHeight)
        }

        g.setColor(Color.black)
        g.setFont(fntRegular)
        g.drawString("Regular Font" , inset, inset + fntAscent)
        g.setFont(fntBold)
        g.drawString("Bold Font"    , inset, inset + fntAscent + lineHeight)


        val as = new AttributedString(testString)
        as.addAttribute(TextAttribute.FAMILY, family)
        as.addAttribute(TextAttribute.SIZE  , fntSize)
        as.addAttribute(TextAttribute.WEIGHT    , TextAttribute.WEIGHT_BOLD, 0, 9)
        as.addAttribute(TextAttribute.FOREGROUND, Color.red, 0, 9)

        // Inconsolata does not come with italics variant
//        as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, 0, 12)

        g.drawString(as.getIterator, inset, inset + fntAscent + 2 * lineHeight)
      }
    }

    new MainFrame {
      title = s"Font advance = $fntAdvanceF"
      contents = c
    }
  }
}