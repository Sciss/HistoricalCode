package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicLabelUI
import javax.swing.plaf.ComponentUI
import sun.swing.SwingUtilities2
import javax.swing.{AbstractButton, LookAndFeel, JLabel, JComponent}
import java.awt._

object LabelUI {
   def createUI( c: JComponent ) : ComponentUI = new LabelUI
}
class LabelUI extends BasicLabelUI {
//   override protected def installDefaults( lb: JLabel ) {
//      super.installDefaults( lb )
////
////
////      LookAndFeel.installProperty( b, "opaque", java.lang.Boolean.FALSE )
////      b.setMargin( new Insets( 30, 30, 30, 30 ))
//      println( "YO CHUCK : " + lb.getClientProperty( "Dorian.mode" ))
//   }

   override def paint( g: Graphics, c: JComponent ) {
      val g2      = g.asInstanceOf[ Graphics2D ]
      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      // XXX DUPLICATE ACTION
      if( c.getClientProperty( "Dorian.mode" ) == "lcd" ) {
         val gc      = g2.getDeviceConfiguration()
         val img     = gc.createCompatibleImage( c.getWidth(), c.getHeight() )
         val imgG    = img.getGraphics()
         val imgG2   = imgG.asInstanceOf[ Graphics2D ]
         imgG2.setColor( new Color( 0x07, 0x10, 0x18 ))
         imgG2.fillRect( 0, 0, c.getWidth(), c.getHeight() )
         imgG2.setFont( c.getFont() )
         super.paint( imgG2, c )
         g2.drawImage( img, ButtonPainter.opLCD, 0, 0 )
         img.flush()
      } else {
         super.paint( g, c )
      }
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }

   override protected def paintEnabledText( lb: JLabel, g: Graphics, s: String, x0: Int, y: Int )
   {
      val g2      = g.asInstanceOf[ Graphics2D ]
      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      val mnemo   = lb.getDisplayedMnemonicIndex()
      val x       = if( lb.getClientProperty( "Dorian.mode" ) == "lcd" ) {
         g2.setPaint( PanelBackgroundPainter.pntLCD )
         x0 + 5
      } else {
         g2.setColor( lb.getForeground() )
         x0
      }
      SwingUtilities2.drawStringUnderlineCharAt( lb, g, s, mnemo, x, y )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }

   override def getPreferredSize( c: JComponent ) : Dimension = {
      val d = super.getPreferredSize( c )
      if( c.getClientProperty( "Dorian.mode" ) == "lcd" ) {
         d.width  += 10
      }
      d
   }
}