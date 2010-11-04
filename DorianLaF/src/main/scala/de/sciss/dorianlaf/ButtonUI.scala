package de.sciss.dorianlaf

import javax.swing.plaf.ComponentUI
import javax.swing.border.Border
import java.awt._
import geom.{AffineTransform, Rectangle2D, Ellipse2D, Area}
import image.{ConvolveOp, Kernel}
import javax.swing.{SwingUtilities, LookAndFeel, AbstractButton, JComponent}
import javax.swing.text.View
import javax.swing.plaf.basic.{BasicHTML, BasicButtonUI}
import sun.swing.SwingUtilities2

object ButtonUI {
   def createUI( c: JComponent ) : ComponentUI = new ButtonUI
}
class ButtonUI extends BasicButtonUI {
   private val viewRect = new Rectangle()
   private val textRect = new Rectangle()
   private val iconRect = new Rectangle()

//   override def paint( g: Graphics, c: JComponent ) {
////println( "PAINT!" )
//      super.paint( g, c )
//      g.setColor( Color.red )
//      g.drawLine( 0, 0, 0, c.getHeight )
//   }

   override protected def installDefaults( b: AbstractButton ) {
      super.installDefaults( b )

      LookAndFeel.installProperty( b, "opaque", java.lang.Boolean.FALSE )
//      b.setMargin( new Insets( 30, 30, 30, 30 ))
   }

   override def paint( g: Graphics, c: JComponent ) {
      val b       = c.asInstanceOf[ AbstractButton ]
      val g2      = g.asInstanceOf[ Graphics2D ]
      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      paintButton( b, g2, 0, 0, c.getWidth, c.getHeight )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )

//      val bm      = b.getModel()
//
//      val text    = layout( b, c.getFontMetrics( c.getFont() ), b.getWidth(), b.getHeight() )
//
//      clearTextShiftOffset()

//      // perform UI specific press action, e.g. Windows L&F shifts text
//      if (model.isArmed() && model.isPressed()) {
//         paintButtonPressed(g,b);
//      }

//      // Paint the Icon
//      if(b.getIcon() != null) {
//         paintIcon(g,c,iconRect);
//      }

//        if (text != null && !text.equals("")){
////            val v = c.getClientProperty( BasicHTML.propertyKey ).asInstanceOf[ View ]
////            if( v != null ) {
////                v.paint( g, textRect )
////            } else {
//                paintText(g, b, textRect, text);
////            }
//        }

//        if (b.isFocusPainted() && b.hasFocus()) {
//            // paint UI specific focus
//            paintFocus(g,b,viewRect,textRect,iconRect);
//        }
    }

//   override def update( g: Graphics, c: JComponent ) {
//      val b       = c.asInstanceOf[ AbstractButton ]
//      val g2      = g.asInstanceOf[ Graphics2D ]
//      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      paintButtonBackground( b, g2, 0, 0, b.getWidth, b.getHeight )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
//      super.paint( g, c )
////      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
//   }

//   override protected def paintText( g: Graphics, b: AbstractButton, textRect: Rectangle, text: String ) {
//      val g2 = g.asInstanceOf[ Graphics2D ]
////      val gc      = g2.getDeviceConfiguration()
//      val img     = b.createImage( textRect.width, textRect.height )
//      val imgG    = img.getGraphics()
//      val imgG2   = imgG.asInstanceOf[ Graphics2D ]
////      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
//      val bm      = b.getModel
//      val pressed = bm.isArmed && bm.isPressed
//      val colr = if( pressed ) Color.red else new Color( 0x80, 0x80, 0x80 )
//      imgG2.setColor( colr )
//      imgG2.fillRect( 0, 0, textRect.width, textRect.height )
//      imgG2.setFont( g.getFont )
//      imgG2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      super.paintText( imgG, b, new Rectangle( 0, 0, textRect.width, textRect.height ), text )
//
//      g2.drawImage( )
//      g.drawImage( img, textRect.x, textRect.y, b )
//      img.flush()
//
////      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
//   }

   // XXX TODO : remove references to sun class SwingUtilities2 
   private def paintText2( g2: Graphics2D, b: AbstractButton, fm: FontMetrics, textRect: Rectangle, text: String ) {
      val gc      = g2.getDeviceConfiguration()
//      val img     = b.createImage( textRect.width, textRect.height )
      val img     = gc.createCompatibleImage( textRect.width, textRect.height )
      val imgG    = img.getGraphics()
      val imgG2   = imgG.asInstanceOf[ Graphics2D ]
//      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      val bm      = b.getModel
      val pressed = bm.isArmed && bm.isPressed
      val colr = if( pressed ) Color.red else new Color( 0x80, 0x80, 0x80 )
      imgG2.setColor( colr )
      imgG2.fillRect( 0, 0, textRect.width, textRect.height )
      imgG2.setFont( g2.getFont )
      imgG2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

//      super.paintText( imgG, b, new Rectangle( 0, 0, textRect.width, textRect.height ), text )
      val mnemo = b.getDisplayedMnemonicIndex()
      if( bm.isEnabled() ) {
          imgG2.setColor( b.getForeground() )
          SwingUtilities2.drawStringUnderlineCharAt( b, imgG2, text, mnemo,
             0 /* textRect.x */ /* + getTextShiftOffset() */,
             /* textRect.y + */ fm.getAscent() /* + getTextShiftOffset() */)
      } else {
          imgG2.setColor( b.getBackground().brighter() )
          SwingUtilities2.drawStringUnderlineCharAt( b, imgG2, text, mnemo,
             /* textRect.x */ 0, /*textRect.y + */ fm.getAscent() )
          imgG2.setColor( b.getBackground().darker() )
          SwingUtilities2.drawStringUnderlineCharAt( b, imgG2, text, mnemo,
             /* textRect.x */ - 1, /* textRect.y + */ fm.getAscent() - 1 )
      }

//      g2.drawImage( img, textRect.x, textRect.y, b )
//      val kernel = new Kernel( 3, 3, Array( 0f, -0.5f, 0f, -0.5f, 2f, -0.5f, 0f, -0.5f, 0f ))
//      val op = new ConvolveOp( kernel )
      val op = ButtonPainter.opSharpen
      g2.drawImage( img, op, textRect.x, textRect.y )
      img.flush()

//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }

   // stupidly this is private in BasicButtonUI
   private def layout2( b: AbstractButton, fm: FontMetrics, w: Int, h: Int ) : String = {
      val i = b.getInsets()
      viewRect.x = i.left
      viewRect.y = i.top
      viewRect.width = w - (i.right + viewRect.x)
      viewRect.height = h - (i.bottom + viewRect.y)

      textRect.x = 0
      textRect.y = 0
      textRect.width = 0
      textRect.height = 0
      iconRect.x = 0
      iconRect.y = 0
      iconRect.width = 0
      iconRect.height = 0

      SwingUtilities.layoutCompoundLabel(
         b, fm, b.getText(), b.getIcon(),
         b.getVerticalAlignment(), b.getHorizontalAlignment(),
         b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
         viewRect, iconRect, textRect,
         if( b.getText() == null ) 0 else b.getIconTextGap() )
   }

   private def paintButton( b: AbstractButton, g2: Graphics2D, x: Int, y: Int, w: Int, h: Int ) {
//      val h = h0 - 2
//      val y = y0 + 1

      val a0 = new Area( new Ellipse2D.Double( x+0, y + (h - (w-0)) * 0.5f, w-0, w-0 ))
      a0.intersect( new Area( new Rectangle2D.Double( x+0, y+0, w-0, h-0 )))
      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x24 ))
      g2.fill( a0 )

      val a1 = new Area( new Ellipse2D.Double( x+1, y + (h - (w-2)) * 0.5f, w-2, w-2 ))
      a1.intersect( new Area( new Rectangle2D.Double( x+1, y+1, w-2, h-2 )))
      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x45 ))
      g2.fill( a1 )

      val a2 = new Area( new Ellipse2D.Double( x+2, y + (h - (w-4)) * 0.5f, w-4, w-4 ))
      a2.intersect( new Area( new Rectangle2D.Double( x+2, y+2, w-4, h-4 )))
      g2.setColor( new Color( 0x00, 0x00, 0x00, 0xFF ))
      g2.fill( a2 )

      val a3 = new Area( new Ellipse2D.Double( x+3, y + (h - (w-6)) * 0.5f, w-6, w-6 ))
      a3.intersect( new Area( new Rectangle2D.Double( x+3, y+3, w-6, h-6 )))
      g2.setColor( new Color( 0xC0, 0xC0, 0xC0 ))
      g2.fill( a3 )

      val a4 = new Area( new Ellipse2D.Double( x+4, y + (h - (w-8)) * 0.5f, w-8, w-8 ))
      a4.intersect( new Area( new Rectangle2D.Double( x+4, y+4, w-8, h-8 )))
      g2.setColor( new Color( 0x96, 0x96, 0x96 ))
      g2.fill( a4 )

      val a5 = new Area( new Ellipse2D.Double( x+5, y + (h - (w-10)) * 0.5f, w-10, w-10 ))
      a5.intersect( new Area( new Rectangle2D.Double( x+5, y+5, w-10, h-10 )))
      g2.setColor( new Color( 0x8A, 0x8A, 0x8A ))
      g2.fill( a5 )

      val a6 = new Area( new Ellipse2D.Double( x+6, y + (h - (w-12)) * 0.5f, w-12, w-12 ))
      a6.intersect( new Area( new Rectangle2D.Double( x+6, y+6, w-12, h-12)))
//      val state = c.getComponentState()
//      val pressed = (state & SynthConstants.PRESSED) != 0
      val bm = b.getModel
      val pressed = bm.isArmed && bm.isPressed 
      val colr = if( pressed ) Color.red else new Color( 0x80, 0x80, 0x80 )
      g2.setColor( colr )
      g2.fill( a6 )

      val fm   = b.getFontMetrics( b.getFont() )
      val text = layout2( b, fm, w, h )

//      clearTextShiftOffset()
      
      if( text != null && text != "" ) {
//            val v = c.getClientProperty( BasicHTML.propertyKey ).asInstanceOf[ View ]
//            if( v != null ) {
//                v.paint( g, textRect )
//            } else {
                paintText2( g2, b, fm, textRect, text )
//            }
      }

      g2.setPaint( PanelBackgroundPainter.pntCanvas )
      val cmpOrig = g2.getComposite
      val atOrig  = g2.getTransform
      g2.setComposite( PanelBackgroundPainter.cmpCanvas )
      val winOff  = SwingUtilities.convertPoint( b, 0, 0, SwingUtilities.getWindowAncestor( b ))
      // XXX we could do modulo canvas-size (128). is that better performance-wise?
      g2.translate( -winOff.x, -winOff.y )
      g2.fill( a3.createTransformedArea( AffineTransform.getTranslateInstance( winOff.x, winOff.y )))
      g2.setComposite( cmpOrig )
      g2.setTransform( atOrig )
   }

   // XXX why is the Button.margin ignored??
   override def getPreferredSize( c: JComponent ) : Dimension = {
      val d = super.getPreferredSize( c )
      d.width  += 24 // 32
      d.height += 12 // 16
      d
   }
}

//object ButtonBorder {
//   def getBorder : Border = new ButtonBorder
//}
class ButtonBorder extends Border {
   def isBorderOpaque = true // false
   def getBorderInsets( c: Component ) = new Insets( 0, 0, 0, 0 ) // new Insets( 3, 3, 3, 3 ) // ( 1, 1, 1, 1 )
   def paintBorder( c: Component, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
////      println( "PAINT BORDER" )
////      g.setColor( Color.red )
////      g.drawRect( x, y, w - 1, h - 1 )
//      val g2 = g.asInstanceOf[ Graphics2D ]
//
//      val a3 = new Area( new Ellipse2D.Double( x+3, y + (h - (w-6)) * 0.5f, w-6, w-6 ))
//      a3.intersect( new Area( new Rectangle2D.Double( x+3, y+3, w-6, h-6 )))
//
//      val a0 = new Area( new Ellipse2D.Double( x+0, y + (h - (w-0)) * 0.5f, w-0, w-0 ))
//      a0.intersect( new Area( new Rectangle2D.Double( x+0, y+0, w-0, h-0 )))
//      a0.subtract( a3 )
//      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x24 ))
//      g2.fill( a0 )
//
//      val a1 = new Area( new Ellipse2D.Double( x+1, y + (h - (w-2)) * 0.5f, w-2, w-2 ))
//      a1.intersect( new Area( new Rectangle2D.Double( x+1, y+1, w-2, h-2 )))
//      a1.subtract( a3 )
//      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x45 ))
//      g2.fill( a1 )
//
//      val a2 = new Area( new Ellipse2D.Double( x+2, y + (h - (w-4)) * 0.5f, w-4, w-4 ))
//      a2.intersect( new Area( new Rectangle2D.Double( x+2, y+2, w-4, h-4 )))
//      a2.subtract( a3 )
//      g2.setColor( new Color( 0x00, 0x00, 0x00, 0xFF ))
//      g2.fill( a2 )
   }
}