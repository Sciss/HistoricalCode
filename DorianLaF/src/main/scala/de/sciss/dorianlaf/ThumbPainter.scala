package de.sciss.dorianlaf

import javax.swing.JComponent
import java.awt.geom.Area
import java.awt.{BasicStroke, Color, Shape, Graphics2D}

trait ThumbPainter {
   protected def paintThumb( c: JComponent, pressed: Boolean, g2: Graphics2D, outline: Shape ) {
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
      } /* else if( selected ) {
         new Color( 0xEF, 0xE3, 0xA3 )
      } */ else {
         new Color( 0xC0, 0xC0, 0xC0 )
      }
      g2.setColor( colr3 )
      g2.fill( outline )

      val a4 = new Area( outline )
      a4.subtract( new Area( (new BasicStroke( 1f )).createStrokedShape( outline )))
      val colr4 = if( pressed ) {
         new Color( 0xB6, 0xB6, 0xB6 )
      } /* else if( selected ) {
         new Color( 0xB4, 0xB3, 0x70 )
     } */ else {
         new Color( 0x96, 0x96, 0x96 )
      }
      g2.setColor( colr4 )
      g2.fill( a4 )

      val a5 = new Area( outline )
      a5.subtract( new Area( (new BasicStroke( 3f )).createStrokedShape( outline )))
      val colr5 = if( pressed ) {
         new Color( 0xB2, 0xB2, 0xB2 )
      } /* else if( selected ) {
         new Color( 0xA9, 0xA8, 0x62 )
      } */ else {
         new Color( 0x92, 0x92, 0x92 )
      }
      g2.setColor( colr5 )
      g2.fill( a5 )

      val a6 = new Area( outline )
      a6.subtract( new Area( (new BasicStroke( 5f )).createStrokedShape( outline )))
      val colr6 = if( pressed ) {
         new Color( 0x90, 0x90, 0x90 )
      } /* else if( selected ) {
         new Color( 0x90, 0x7E, 0x58 )
      } */ else {
         new Color( 0x70, 0x70, 0x70 )
      }
      g2.setColor( colr6 )
      g2.fill( a6 )
   }
}