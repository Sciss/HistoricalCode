package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicPanelUI
import javax.swing.plaf.ComponentUI
import javax.swing.{UIManager, AbstractButton, JComponent}
import java.awt._

object PanelUI {
   def createUI( c: JComponent ) : ComponentUI = new PanelUI
}
class PanelUI extends BasicPanelUI {
   override def update( g: Graphics, c: JComponent ) {
      val g2   = g.asInstanceOf[ Graphics2D ]
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      paintPanelBackground( c, g2, 0, 0, c.getWidth, c.getHeight )
//      super.paint( g, c )
   }

   private def paintPanelBackground( c: JComponent , g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
      val start   = UIManager.getColor( "Panel.startBackground" )
      val end     = UIManager.getColor( "Panel.endBackground" )
      val g2      = g.asInstanceOf[ Graphics2D ]
      val pntGrad = new GradientPaint( x, y, start, w, h, end )
      g2.setPaint( pntGrad )
      g2.fillRect( x, y, w, h )

      g2.setPaint( PanelBackgroundPainter.pntCanvas )
      val cmpOrig = g2.getComposite
      g2.setComposite( PanelBackgroundPainter.cmpCanvas )
      g2.fillRect( x, y, w, h )
      g2.setComposite( cmpOrig )

//      g2.setPaint( null )
//      g2.setColor( new Color( 255, 255, 255, 120 ))
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      val arc2d   = new CubicCurve2D.Double( 0, h/4, w/3, h/10, 66 * w, 1.5 * h, w, h/8 )
//      g2.draw( arc2d )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF )
   }
}
