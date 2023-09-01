/*
 *  ButtonUI.scala
 *  (DorianLaF)
 *
 *  Copyright (c) 2010 Hanns Holger Rutz. All rights reserved.
 *
 *	 This software is free software; you can redistribute it and/or
 *	 modify it under the terms of the GNU General Public License
 *	 as published by the Free Software Foundation; either
 *	 version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	 This software is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	 General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public
 *	 License (gpl.txt) along with this software; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	 For further information, please contact Hanns Holger Rutz at
 *	 contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.dorianlaf

import javax.swing.plaf.ComponentUI
import javax.swing.border.Border
import java.awt._
import geom._
import image.{BufferedImage, ConvolveOp, Kernel}
import javax.swing.text.View
import javax.swing.plaf.basic.{BasicHTML, BasicButtonUI}
import sun.swing.SwingUtilities2
import com.jhlabs.composite._
import javax.swing._

object ButtonUI {
   def createUI( c: JComponent ) : ComponentUI = new ButtonUI
}
class ButtonUI extends BasicButtonUI with ButtonPainter {
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

   private val pasOutline : Shape = new GeneralPath()

   override def paint( g: Graphics, c: JComponent ) {
      val b    = c.asInstanceOf[ AbstractButton ]
      val g2   = g.asInstanceOf[ Graphics2D ]
      val w    = b.getWidth()
      val h    = b.getHeight()

      val paint1  = new LinearGradientPaint( 0, 0, 0, h - 1,
         Array[ Float ]( 0.0f, 0.15f, 0.46f, 0.5f, 0.501f, 0.64f, 0.85f, 1.0f ),
//         Array[ Color ]( new Color(   0,   0,   0, 255), new Color(  30,  30,  30, 255 ), new Color(  20,  20,  20, 255 ),
//                         new Color(   0,   0,   0, 255), new Color(  44,  44,  44, 255 ), new Color(  26,  26,  26, 255 ),
//                         new Color(  26,  26,  26, 255), new Color( 110, 110, 110, 255 )))
         Array[ Color ]( new Color( 110, 110, 110, 255 ), new Color(  26,  26,  26, 255),
                         new Color(  26,  26,  26, 255 ), new Color(  44,  44,  44, 255 ),
                         new Color(   0,   0,   0, 255 ), new Color(  20,  20,  20, 255 ),
                         new Color(  30,  30,  30, 255 ), new Color(   0,   0,   0, 255 )))
//      val shape   = new RoundRectangle2D.Float( 0, 0, w, h, 11.5f, 11.5f )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      g2.setPaint( paint1 )
//      g2.fill( shape )
      g2.fillRoundRect( 0, 0, w, h, 11, 11 )
      val stroke  = new BasicStroke( 0.5f ) // , BasicStroke.CAP_BUTT, 0, 4.0f, null, 0.0f )
      g2.setColor( Color.white )
      val strkOrig = g2.getStroke
      g2.setStroke( stroke )
//      g2.draw( shape )
      g2.drawRoundRect( 0, 0, w, h, 11, 11 )
      g2.setStroke( strkOrig )

      paintButtonText( b, g2, Color.black ) // colrBg )
   }

   def paintX( g: Graphics, c: JComponent ) {
      val b       = c.asInstanceOf[ AbstractButton ]
      val g2      = g.asInstanceOf[ Graphics2D ]
//      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      paintButton( b, g2, 0, 0, c.getWidth, c.getHeight )
      val w = b.getWidth()
      val h = b.getHeight()
      val actOutline = new Area( new Ellipse2D.Double( 3, (h - (w-6)) * 0.5f, w-6, w-6 ))
      actOutline.intersect( new Area( new Rectangle2D.Double( 3, 3, w-6, h-6 )))
      paintButton( b, g2, 0, 0, pasOutline, actOutline )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
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

   private def paintText2( g2: Graphics2D, b: AbstractButton, fm: FontMetrics, textRect: Rectangle, text: String, colrBG: Color ) {
      val gc      = g2.getDeviceConfiguration()
//      val img     = b.createImage( textRect.width, textRect.height )
      val img     = gc.createCompatibleImage( textRect.width, textRect.height )
      val imgG    = img.getGraphics()
      val imgG2   = imgG.asInstanceOf[ Graphics2D ]
//      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      val bm      = b.getModel
      val pressed = bm.isArmed && bm.isPressed
//      val colr = /* if( pressed ) Color.red else */ new Color( 0x80, 0x80, 0x80 )
      imgG2.setColor( colrBG )
      imgG2.fillRect( 0, 0, textRect.width, textRect.height )
      imgG2.setFont( g2.getFont )
      imgG2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

//      super.paintText( imgG, b, new Rectangle( 0, 0, textRect.width, textRect.height ), text )
      val mnemo = b.getDisplayedMnemonicIndex()
      if( bm.isEnabled() ) {
          imgG2.setColor( UIManager.getColor( "Button.textForeground" ))
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
      val op = if( pressed ) ButtonPainter.opShine else ButtonPainter.opSharpen
      g2.drawImage( img, op, textRect.x, textRect.y )
      img.flush()

//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }

   // XXX TODO : remove references to sun class SwingUtilities2 
   private def paintText2XX( g2: Graphics2D, b: AbstractButton, fm: FontMetrics, textRect: Rectangle, text: String, colrBG: Color ) {
      val gc      = g2.getDeviceConfiguration()
//      val img     = b.createImage( textRect.width, textRect.height )
      val img     = gc.createCompatibleImage( textRect.width, textRect.height )
      val imgG    = img.getGraphics()
      val imgG2   = imgG.asInstanceOf[ Graphics2D ]
//      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      val bm      = b.getModel
      val pressed = bm.isArmed && bm.isPressed
//      val colr = /* if( pressed ) Color.red else */ new Color( 0x80, 0x80, 0x80 )
      imgG2.setColor( colrBG )
      imgG2.fillRect( 0, 0, textRect.width, textRect.height )
      imgG2.setFont( g2.getFont )
      imgG2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

//      super.paintText( imgG, b, new Rectangle( 0, 0, textRect.width, textRect.height ), text )
      val mnemo = b.getDisplayedMnemonicIndex()
      if( bm.isEnabled() ) {
          imgG2.setColor( UIManager.getColor( "Button.textForeground" ))
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
      val op = if( pressed ) ButtonPainter.opShine else ButtonPainter.opSharpen
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

   protected def paintButtonText( b: AbstractButton, g2: Graphics2D, colrBg: Color ) {
      val fm   = b.getFontMetrics( b.getFont() )
      val text = layout2( b, fm, b.getWidth(), b.getHeight() )

      if( b.getIcon() != null ) {
         paintIcon( g2, b, iconRect )
      }

      if( text != null && text != "" ) {
         val v = b.getClientProperty( BasicHTML.propertyKey ).asInstanceOf[ View ]
         if( v != null ) {
            // XXX currently causes a NullPointerException in BoxView.paint???
            // v.paint( g2, textRect )
         } else {
            paintText2( g2, b, fm, textRect, text, colrBg )
         }
      }
   }

//   private def paintButton( b: AbstractButton, cg2: Graphics2D, x0: Int, y0: Int, w: Int, h: Int ) {
////      val h = h0 - 2
////      val y = y0 + 1
//      val gc      = cg2.getDeviceConfiguration()
////      val img     = b.createImage( textRect.width, textRect.height )
////      val img     = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB )
//
//      val img     = gc.createCompatibleImage( w, h, Transparency.TRANSLUCENT ) // textRect.width, textRect.height
//      val g2      = img.getGraphics().asInstanceOf[ Graphics2D ]
////val g2 = cg2
////      val atOrig2  = g2.getTransform
//
////g2.setColor( Color.black )
////g2.fillRect( 0, 0, w, h )
//
////      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
////if( pressed ) g2.translate( 1, 1 )
////      val colr = /* if( pressed ) Color.red else */ new Color( 0x80, 0x80, 0x80 )
////      imgG2.setColor( colr )
////      imgG2.fillRect( 0, 0, textRect.width, textRect.height )
//      g2.setFont( cg2.getFont )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//val x = 0
//val y = 0
//
//      val bm         = b.getModel
//      val pressed    = bm.isArmed && bm.isPressed
//      val selected   = bm.isSelected
//
//      val a0 = new Area( new Ellipse2D.Double( x+0, y + (h - (w-0)) * 0.5f, w-0, w-0 ))
//      a0.intersect( new Area( new Rectangle2D.Double( x+0, y+0, w-0, h-0 )))
//      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x24 ))
//      g2.fill( a0 )
//
////      if( b.hasFocus ) {
////         val strkOrig = g2.getStroke()
////         val af = new Area( new Ellipse2D.Double( x+0, y + (h - (w-1)) * 0.5f, w-1, w-1 ))
////         af.intersect( new Area( new Rectangle2D.Double( x+0, y+0, w-1, h-1 )))
////         g2.setColor( new Color( 0xF0, 0xF0, 0xF0, 0xC0 ))
////         g2.setStroke( new BasicStroke( 1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, Array( 2f, 3f ), 0f ))
////         g2.draw( af )
////         g2.setStroke( strkOrig )
////      }
//
//      val a1 = new Area( new Ellipse2D.Double( x+1, y + (h - (w-2)) * 0.5f, w-2, w-2 ))
//      a1.intersect( new Area( new Rectangle2D.Double( x+1, y+1, w-2, h-2 )))
//      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x45 ))
//      g2.fill( a1 )
//
//      val a2 = new Area( new Ellipse2D.Double( x+2, y + (h - (w-4)) * 0.5f, w-4, w-4 ))
//      a2.intersect( new Area( new Rectangle2D.Double( x+2, y+2, w-4, h-4 )))
//      g2.setColor( new Color( 0x00, 0x00, 0x00, 0xFF ))
//      g2.fill( a2 )
//
//      val a3 = new Area( new Ellipse2D.Double( x+3, y + (h - (w-6)) * 0.5f, w-6, w-6 ))
//      a3.intersect( new Area( new Rectangle2D.Double( x+3, y+3, w-6, h-6 )))
//      val colr3 = if( pressed ) {
//         new Color( 0xF0, 0xF0, 0xF0 )
//      } else if( selected ) {
////         new Color( 0xFF, 0xFF, 0xCC )
//         new Color( 0xEF, 0xE3, 0xA3 )
////         new Color( 0xF7, 0xF1, 0xB7 )
//      } else {
//         new Color( 0xC0, 0xC0, 0xC0 )
//      }
//      g2.setColor( colr3 )
//      g2.fill( a3 )
//
//      val a4 = new Area( new Ellipse2D.Double( x+4, y + (h - (w-8)) * 0.5f, w-8, w-8 ))
//      a4.intersect( new Area( new Rectangle2D.Double( x+4, y+4, w-8, h-8 )))
//      val colr4 = if( pressed ) {
//         new Color( 0xB6, 0xB6, 0xB6 )
//      } else if( selected ) {
////         new Color( 0xFF, 0xF1, 0xAA )
////         new Color( 0xD4, 0xC3, 0x70 )
////         new Color( 0xE9, 0xDA, 0x8D )
//         new Color( 0xB4, 0xB3, 0x70 )
//     } else {
//         new Color( 0x96, 0x96, 0x96 )
//      }
//      g2.setColor( colr4 )
//      g2.fill( a4 )
//
//      val a5 = new Area( new Ellipse2D.Double( x+5, y + (h - (w-10)) * 0.5f, w-10, w-10 ))
//      a5.intersect( new Area( new Rectangle2D.Double( x+5, y+5, w-10, h-10 )))
////      val colr5 = if( pressed ) new Color( 0xAA, 0xAA, 0xAA ) else new Color( 0x8A, 0x8A, 0x8A )
//      val colr5 = if( pressed ) {
//         new Color( 0xB2, 0xB2, 0xB2 )
//      } else if( selected ) {
////         new Color( 0xFF, 0xEF, 0xA0 )
////         new Color( 0xC9, 0xB8, 0x62 )
////         new Color( 0xE4, 0xD3, 0x81 )
//         new Color( 0xA9, 0xA8, 0x62 )
//      } else {
//         new Color( 0x92, 0x92, 0x92 )
//      }
//      g2.setColor( colr5 )
//      g2.fill( a5 )
//
//      val a6 = new Area( new Ellipse2D.Double( x+6, y + (h - (w-12)) * 0.5f, w-12, w-12 ))
//      a6.intersect( new Area( new Rectangle2D.Double( x+6, y+6, w-12, h-12)))
////      val state = c.getComponentState()
////      val pressed = (state & SynthConstants.PRESSED) != 0
////      val bm = b.getModel
////      val pressed = bm.isArmed && bm.isPressed
////      val colr = /* if( pressed ) Color.red else */ new Color( 0x80, 0x80, 0x80 )
//
////      val colr6 = if( pressed ) new Color( 0xA0, 0xA0, 0xA0 ) else new Color( 0x80, 0x80, 0x80 )
//      val colr6 = if( pressed ) {
//         new Color( 0x90, 0x90, 0x90 )
//      } else if( selected ) {
////         new Color( 0xFF, 0xEE, 0x98 )
////         new Color( 0xC0, 0xAE, 0x58 )
////         new Color( 0xDF, 0xCE, 0x78 )
//         new Color( 0x90, 0x7E, 0x58 )
//      } else {
//         new Color( 0x70, 0x70, 0x70 )
//      }
//      g2.setColor( colr6 )
////      g2.setPaint( new GradientPaint( x+6, y+6, new Color( 0x80, 0x80, 0x80 ), x+40, y+h-12, new Color( 0x60, 0x60, 0x60 )))
//      g2.fill( a6 )
//
//      val fm   = b.getFontMetrics( b.getFont() )
//      val text = layout2( b, fm, w, h )
//
////      clearTextShiftOffset()
//
//      if( b.getIcon() != null ) {
//         paintIcon( g2, b, iconRect ) // new Rectangle( 0, 0, iconRect.width, iconRect.height ))
//      }
//
//      if( text != null && text != "" ) {
//         val v = b.getClientProperty( BasicHTML.propertyKey ).asInstanceOf[ View ]
//         if( v != null ) {
//            // XXX currently causes a NullPointerException in BoxView.paint???
//            // v.paint( g2, textRect )
//         } else {
//            paintText2( g2, b, fm, textRect, text, colr6 )
//         }
////         val mnemo = b.getDisplayedMnemonicIndex()
////         if( bm.isEnabled() ) {
////             g2.setColor( b.getForeground() )
////             SwingUtilities2.drawStringUnderlineCharAt( b, g2, text, mnemo,
////                0 /* textRect.x */ /* + getTextShiftOffset() */,
////                /* textRect.y + */ fm.getAscent() /* + getTextShiftOffset() */)
////         } else {
////             g2.setColor( b.getBackground().brighter() )
////             SwingUtilities2.drawStringUnderlineCharAt( b, g2, text, mnemo,
////                /* textRect.x */ 0, /*textRect.y + */ fm.getAscent() )
////             g2.setColor( b.getBackground().darker() )
////             SwingUtilities2.drawStringUnderlineCharAt( b, g2, text, mnemo,
////                /* textRect.x */ - 1, /* textRect.y + */ fm.getAscent() - 1 )
////         }
////
//////      g2.drawImage( img, textRect.x, textRect.y, b )
//////      val kernel = new Kernel( 3, 3, Array( 0f, -0.5f, 0f, -0.5f, 2f, -0.5f, 0f, -0.5f, 0f ))
//////      val op = new ConvolveOp( kernel )
//////         val op = if( pressed ) ButtonPainter.opShine else ButtonPainter.opSharpen
////         g2.drawImage( img, ButtonPainter.opSharpen, textRect.x, textRect.y )
//////         img.flush()
//      }
//
//      g2.setPaint( PanelBackgroundPainter.pntVertical ) // pntCanvas
//      val cmpOrig = g2.getComposite
//      val atOrig  = g2.getTransform
////      g2.setComposite( new ApplyCanvas( 128, 3 )) // PanelBackgroundPainter.cmpCanvas
////      g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.5f ))
//      g2.setComposite( new PinLightComposite( 0.25f ))
//      val winOff  = SwingUtilities.convertPoint( b, 0, 0, SwingUtilities.getWindowAncestor( b ))
////      // XXX we could do modulo canvas-size (128). is that better performance-wise?
//      g2.translate( -winOff.x, -winOff.y )
//      g2.fill( a2.createTransformedArea( AffineTransform.getTranslateInstance( winOff.x, winOff.y )))
////g2.setColor( Color.green )
////g2.fillRect( 0, 0, w, h )
//      g2.setComposite( cmpOrig )
//      g2.setTransform( atOrig )
//
//      // new Rectangle2D.Double( x+4, y+4, w-8, h-8 )
////      a4.intersect( new Area( new Ellipse2D.Float( x+4, y+4, (w-8) * 3, (h-8) * 3 )))
////      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x50 ))
////      g2.fill( a4 )
////      g2.setPaint( new GradientPaint( x+w*2/5, y+6, new Color( 0x00, 0x00, 0x00, 0x00 ), x+w*3/5, y+h-12, new Color( 0x00, 0x00, 0x00, 0x60 )))
//
////      g2.setComposite( new PinLightComposite( 1f ))
////      g2.setPaint( new GradientPaint( x + w*0.45f, y+6, new Color( 0x00, 0x00, 0x00, 0x00 ), x+w*0.55f, y+h-12, new Color( 0x00, 0x00, 0x00, 0x70 )))
////      g2.setComposite( cmpOrig )
////      val a3b = new Area( new Ellipse2D.Double( x+3.5f, y + (h - (w-6.5f)) * 0.5f, w-7, w-7 ))
////      a3b.intersect( new Area( new Rectangle2D.Double( x+3.5f, y+4, w-7, h-7.5f )))
////      g2.fill( a3b )
//
//      if( pressed ) {
////         ButtonPainter.opShine.setDimensions( w, h )
////         cg2.drawImage( img, ButtonPainter.opShine, x0 + 1, y0 + 1 )
//         cg2.drawImage( img, x0 + 1, y0 + 1, b )
//      } else if( selected ) {
//         cg2.drawImage( img, x0, y0, b )
//      } else {
//         a4.subtract( new Area( new Ellipse2D.Double( x+8, y + (h - (w-24)) * 0.5f, w-16, w-24 )))
//         g2.setColor( new Color( 0x00, 0x00, 0x00, 0x20 ))     // 0x36
//         g2.fill( a4 )
//         cg2.drawImage( img, x0, y0, b )
//      }
////      g2.setTransform( atOrig2 )
//      img.flush()
//   }

   // XXX why is the Button.margin ignored??
   override def getPreferredSize( c: JComponent ) : Dimension = {
      val d = super.getPreferredSize( c )
      d.width  += 20 //  24 // 32
      d.height += 10 // 12 // 16
d.width = (d.width + 3) & ~3   // XXX this is an issue of the jhlabs composites!
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