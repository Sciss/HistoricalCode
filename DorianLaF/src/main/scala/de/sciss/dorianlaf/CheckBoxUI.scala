package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicCheckBoxUI
import javax.swing.plaf.ComponentUI
import javax.swing._
import java.awt._
import geom._
import com.jhlabs.composite.PinLightComposite

object CheckBoxUI {
   def createUI( c: JComponent ) : ComponentUI = new CheckBoxUI
}
class CheckBoxUI extends BasicCheckBoxUI {
   override protected def installDefaults( b: AbstractButton ) {
      super.installDefaults( b )
      LookAndFeel.installProperty( b, "opaque", java.lang.Boolean.FALSE )
   }

   override def paint( g: Graphics, c: JComponent ) {
      val g2      = g.asInstanceOf[ Graphics2D ]
      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      super.paint( g, c )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }
}

class CheckBoxIcon extends Icon {
   def getIconWidth  = 28 // 22
   def getIconHeight = 22

   private val outline : Shape = {
      val gp = new GeneralPath()
      gp.append( new Rectangle( 3, 3, 1, getIconHeight  - 6 ), false )
      gp.append( new Rectangle( 6, 3, getIconWidth - 12, getIconHeight  - 6 ), false )
      gp.append( new Rectangle( getIconWidth - 4, 3, 1, getIconHeight  - 6 ), false )
      gp
   }

   def paintIcon( c: Component, g: Graphics, x: Int, y: Int ) {
      val b = c.asInstanceOf[ AbstractButton ]
//      g.setColor( if( b.isSelected ) Color.yellow else Color.white )
//      g.fillRect( x, y, 32, 22 )
      paintButton( b, g.asInstanceOf[ Graphics2D ], x, y, outline ) //
   }

   private def paintButton( b: AbstractButton, cg2: Graphics2D, x0: Int, y0: Int, outline: Shape ) {
      val gc      = cg2.getDeviceConfiguration()
      val olb     = outline.getBounds
      val img     = gc.createCompatibleImage( olb.width + 6, olb.height + 6, Transparency.TRANSLUCENT )
      val g2      = img.getGraphics().asInstanceOf[ Graphics2D ]
      g2.setFont( cg2.getFont )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      paintBackground( b, g2, outline )
      g2.setPaint( PanelBackgroundPainter.pntVertical )
      val cmpOrig = g2.getComposite
      val atOrig  = g2.getTransform
//      g2.setComposite( new ApplyCanvas( 128, 3 )) // PanelBackgroundPainter.cmpCanvas
      g2.setComposite( new PinLightComposite( 0.25f ))
      val winOff  = SwingUtilities.convertPoint( b, 0, 0, SwingUtilities.getWindowAncestor( b ))
//      // XXX we could do modulo canvas-size (128). is that better performance-wise?
      g2.translate( -winOff.x, -winOff.y )
//      g2.fill( a2.createTransformedArea( AffineTransform.getTranslateInstance( winOff.x, winOff.y )))
      g2.fill( AffineTransform.getTranslateInstance( winOff.x, winOff.y ).createTransformedShape( outline ))
      g2.setComposite( cmpOrig )
      g2.setTransform( atOrig )

      val bm         = b.getModel
      val pressed    = bm.isArmed && bm.isPressed
      val selected   = bm.isSelected

      if( pressed ) {
         cg2.drawImage( img, x0 /* + 1 */, y0 /* + 1 */, b )
      } else if( selected ) {
         cg2.drawImage( img, x0, y0, b )
      } else {
//         a4.subtract( new Area( new Ellipse2D.Double( x+8, y + (h - (w-24)) * 0.5f, w-16, w-24 )))
//         g2.setColor( new Color( 0x00, 0x00, 0x00, 0x20 ))     // 0x36
//         g2.fill( a4 )
         cg2.drawImage( img, x0, y0, b )
      }
      img.flush()
   }

   private def paintBackground( b: AbstractButton, g2: Graphics2D, outline: Shape ) {
      val bm         = b.getModel
      val pressed    = bm.isArmed && bm.isPressed
      val selected   = bm.isSelected

      val a3 = new Area( outline )
      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x24 ))
      val a0 = new Area( (new BasicStroke( 7f )).createStrokedShape( outline ))
      a0.add( a3 )
      g2.fill( a0 )

      val a1 = new Area( (new BasicStroke( 5f )).createStrokedShape( outline ))
      a1.add( a3 )
      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x45 ))
      g2.fill( a1 )

      val a2 = new Area( (new BasicStroke( 3f )).createStrokedShape( outline ))
      a2.add( a3 )
      g2.setColor( new Color( 0x00, 0x00, 0x00, 0xFF ))
      g2.fill( a2 )

      val colr3 = if( pressed ) {
         new Color( 0xF0, 0xF0, 0xF0 )
      } else if( selected ) {
         new Color( 0xEF, 0xE3, 0xA3 )
      } else {
         new Color( 0xC0, 0xC0, 0xC0 )
      }
      g2.setColor( colr3 )
      g2.fill( outline )

      val a4 = new Area( outline )
      a4.subtract( new Area( (new BasicStroke( 1f )).createStrokedShape( outline ))) 
      val colr4 = if( pressed ) {
         new Color( 0xB6, 0xB6, 0xB6 )
      } else if( selected ) {
         new Color( 0xB4, 0xB3, 0x70 )
     } else {
         new Color( 0x96, 0x96, 0x96 )
      }
      g2.setColor( colr4 )
      g2.fill( a4 )

      val a5 = new Area( outline )
      a5.subtract( new Area( (new BasicStroke( 3f )).createStrokedShape( outline )))
      val colr5 = if( pressed ) {
         new Color( 0xB2, 0xB2, 0xB2 )
      } else if( selected ) {
         new Color( 0xA9, 0xA8, 0x62 )
      } else {
         new Color( 0x92, 0x92, 0x92 )
      }
      g2.setColor( colr5 )
      g2.fill( a5 )

      val a6 = new Area( outline )
      a6.subtract( new Area( (new BasicStroke( 5f )).createStrokedShape( outline ))) 
      val colr6 = if( pressed ) {
         new Color( 0x90, 0x90, 0x90 )
      } else if( selected ) {
         new Color( 0x90, 0x7E, 0x58 )
      } else {
         new Color( 0x70, 0x70, 0x70 )
      }
      g2.setColor( colr6 )
      g2.fill( a6 )
   }
}