package de.sciss.dorianlaf

import javax.swing.plaf.synth.{SynthContext, SynthPainter}
import javax.swing.UIManager
import javax.imageio.ImageIO
import java.awt._

object PanelBackgroundPainter {
   lazy val pntCanvas = new TexturePaint( ImageIO.read(
      PanelBackgroundPainter.getClass.getResourceAsStream( "canvas.png" )), new Rectangle( 0, 0, 128, 128 ))
   lazy val cmpCanvas = new ApplyCanvas
}

class PanelBackgroundPainter extends SynthPainter {
   import PanelBackgroundPainter._
   
   override def paintPanelBackground( context: SynthContext, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
      val start   = UIManager.getColor( "Panel.startBackground" )
      val end     = UIManager.getColor( "Panel.endBackground" )
      val g2      = g.asInstanceOf[ Graphics2D ]
      val pntGrad = new GradientPaint( x, y, start, w, h, end )
      g2.setPaint( pntGrad )
      g2.fillRect( x, y, w, h )

      g2.setPaint( pntCanvas )
      val cmpOrig = g2.getComposite
      g2.setComposite( cmpCanvas )
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