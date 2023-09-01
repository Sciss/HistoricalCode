/*
 *  ButtonPainters.scala
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

import java.awt._
import geom._
import image.BufferedImageOp
import util.Random
import javax.swing.plaf.synth.{SynthConstants, SynthContext, SynthPainter}
import com.jhlabs.image._
import javax.swing.{SwingUtilities, AbstractButton, UIManager}
import com.jhlabs.composite.{OverlayComposite, PinLightComposite, HardLightComposite}

object ButtonPainter {
   lazy val opSharpen : BufferedImageOp = {
      val res = new UnsharpFilter() // new WaveletSharpenImageOp
      res.setRadius( 3f )
      res.setThreshold( 1 )
//      println( "AMT " + res.getAmount )
      res.setAmount( 0.5f )
      res
   }

   lazy val opShine : BufferedImageOp = {
//      val res = new GlintFilter()
//      res.setThreshold( 0.6f )
//      res.setAmount( 0.07f ) // 7f )
//      res.setLength( 1 )
////      res.setBlur( 0f )
////      res.setColormap( )
//      val res = new RescaleFilter()
//      res.setScale( 1.56f )
//      val res = new GammaFilter()
//      res.setGamma( 1.5f )
      val res = new ContrastFilter()
      res.setBrightness( 1.333f )
      res
   }

   lazy val opLCD : BufferedImageOp = {
      val res = new GlowFilter()
      res.setAmount( 0.15f )
      res.setRadius( 5f )
      res
   }

//   lazy val opSharpenAndShine : BufferedImageOp = new CompoundFilter( opSharpen, opShine )
}

/*
class ButtonPainter extends SynthPainter {
   override def paintButtonBackground( c: SynthContext, g: Graphics, x: Int, y0: Int, w: Int, h0: Int ) {
      val g2 = g.asInstanceOf[ Graphics2D ]
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      val h = h0 - 2
      val y = y0 + 1

//      g.setColor( Color.white )
//      g.drawLine( x, y, x, y + h - 1 )
      
      val a0 = new Area( new Ellipse2D.Double( x+0, y + (h - (w-0)) * 0.5f, w-0, w-0 ))
      a0.intersect( new Area( new Rectangle2D.Double( x+0, y+0, w-0, h-0 )))
      g.setColor( new Color( 0x00, 0x00, 0x00, 0x24 ))
      g2.fill( a0 )

      val a1 = new Area( new Ellipse2D.Double( x+1, y + (h - (w-2)) * 0.5f, w-2, w-2 ))
      a1.intersect( new Area( new Rectangle2D.Double( x+1, y+1, w-2, h-2 )))
      g.setColor( new Color( 0x00, 0x00, 0x00, 0x45 ))
      g2.fill( a1 )

      val a2 = new Area( new Ellipse2D.Double( x+2, y + (h - (w-4)) * 0.5f, w-4, w-4 ))
      a2.intersect( new Area( new Rectangle2D.Double( x+2, y+2, w-4, h-4 )))
      g.setColor( new Color( 0x00, 0x00, 0x00, 0xFF ))
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
      val state = c.getComponentState()
      val pressed = (state & SynthConstants.PRESSED) != 0 
      val colr = if( pressed ) Color.red else new Color( 0x80, 0x80, 0x80 ) 
      g.setColor( colr )
      g2.fill( a6 )

      g2.setPaint( PanelBackgroundPainter.pntCanvas )
      val cmpOrig = g2.getComposite
      g2.setComposite( PanelBackgroundPainter.cmpCanvas )
      g2.fill( a3 )
      g2.setComposite( cmpOrig )
   }

   def paintButtonBackgroundXXX2( context: SynthContext, g: Graphics, x: Int, y0: Int, w: Int, h0: Int ) {
      val g2 = g.asInstanceOf[ Graphics2D ]
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      g2.drawArc( x, y, w, h, 45, -90 )
//      val clpOrig = g2.getClip()
//      g2.clipRect( x, y + 1, w, h - 2 )
      g.setColor( Color.red ) // new Color( 0x00, 0x00, 0x00, 0x24 ))
//      g2.drawOval( x, y - (w - h)/2, w, w )
      val h = h0 - 2
      val y = y0 + 1

      val wm1 = w - 1
      val hm1 = h - 1
      val hh  = (wm1 - hm1) * 0.5f
      val a0 = new Area( new Ellipse2D.Float( x + 0.5f, y - hh, wm1, wm1 ))
      a0.intersect( new Area( new Rectangle( x, y, wm1, hm1 ))) // Rectangle2D.Float( x - 1, y - 1, wm1 + 2, hm1 + 2 )))
//      g2.draw( a0 )
//      g2.clipRect( x, y + 2, w, h - 4 )
      g.setColor( Color.green ) // new Color( 0x00, 0x00, 0x00, 0x59 ))
//      g2.drawOval( x + 1, y - (w - h)/2 + 1, w - 2, w - 2 )
      val xp1 = x + 1
      val yp1 = y + 1
      val wm3 = w - 3
      val hm3 = h - 3
      val a1 = new Area( new Ellipse2D.Float( xp1, yp1 - hh, wm3, wm3 ))
//      a1.intersect( new Area( new Rectangle2D.Float( xp1, yp1, wm3, hm3 )))
//      g2.draw( a1 )
//      g2.clipRect( x, y + 3, w, h - 6 )
      g.setColor( Color.blue ) // new Color( 0x00, 0x00, 0x00, 0xFF ))
//      g2.drawOval( x + 2, y - (w - h)/2 + 2, w - 4, w - 4 )
      val xp2 = x + 2
      val yp2 = y + 2
      val wm5 = w - 5
      val hm5 = h - 5
      val a2 = new Area( new Ellipse2D.Float( xp2, yp2 - hh, wm5, wm5 ))
      a2.intersect( new Area( new Rectangle2D.Float( xp2, yp2, wm5, hm5 )))
//      g2.draw( a2 )
//      g2.clipRect( x, y + 4, w, h - 8 )
//      g2.fillOval( x + 3, y - (w - h)/2 + 3, w - 6, w - 6 )
      val xp3 = x + 3
      val yp3 = y + 3
      val hm6 = h - 6
      val wm6 = w - 6
      val wm7 = w - 7
      val hm7 = h - 7
      val xp6 = x + 6
      val yp6 = y + 6
      val wm12 = w - 12
      val hm12 = h - 12
      val a3 = new Area( new Ellipse2D.Double( xp6, y + (h - wm12) * 0.5f, wm12, wm12 ))
      a3.intersect( new Area( new Rectangle2D.Double( xp6, yp6, wm12, hm12 )))

      g.setColor( new Color( 0x00, 0x00, 0x00, 0x24 ))
      g2.fill( (new BasicStroke( 13f )).createStrokedShape( a3 ))
      g.setColor( new Color( 0x00, 0x00, 0x00, 0x59 ))
      g2.fill( (new BasicStroke( 11f )).createStrokedShape( a3 ))
      g.setColor( new Color( 0x00, 0x00, 0x00, 0xFF ))
      g2.fill( (new BasicStroke( 9f )).createStrokedShape( a3 ))
      g2.setColor( new Color( 0xC0, 0xC0, 0xC0 ))
      g2.fill( (new BasicStroke( 7f )).createStrokedShape( a3 ))
      g2.setColor( new Color( 0xA0, 0xA0, 0xA0 ))
      g2.fill( (new BasicStroke( 5f )).createStrokedShape( a3 ))
      g2.setColor( new Color( 0x90, 0x90, 0x90 ))
      g2.fill( (new BasicStroke( 3f )).createStrokedShape( a3 ))
      g.setColor( new Color( 0x80, 0x80, 0x80 ))
      g2.fill( a3 )
      val gp = new GeneralPath()
//      gp.moveTo( xp3, yp3 )
//      val arc = new Arc2D.Float( xp3, y + (hm1 * 0.5f - wm7 * 0.5f, float w, float h, float start, float extent, int type)

      val atOrig = g2.getTransform()
//      g2.translate( 0.5, 0.5 )

//      g2.setColor( new Color( 0xC0, 0xC0, 0xC0 ))
//      g2.fill( (new BasicStroke( 1f )).createStrokedShape( a3 ))


      //      g2.drawOval( x + 3, y - (w - h)/2 + 3, w - 6, w - 6 )
//      g2.draw( a3 )
//      g2.fill( (new BasicStroke( 1.5f )).createStrokedShape( a3 ))

      g2.setTransform( atOrig )
//      g2.setClip( clpOrig )
   }

   def paintButtonBackgroundXXX( context: SynthContext, g: Graphics, x0: Int, y0: Int, w0: Int, h0: Int ) {
      val start   = UIManager.getColor( "Panel.startBackground" )
      val end     = UIManager.getColor( "Panel.endBackground" )
      val g2      = g.asInstanceOf[ Graphics2D ]
      val x = x0 + 3
      val y = y0 + 3
      val w = w0 - 6
      val h = h0 - 6
//      val grPaint = new GradientPaint( x.toFloat, y.toFloat, Color.gray, w.toFloat, h.toFloat, end )
//      g2.setPaint( grPaint )
//      g2.fillRect( x, y, w, h )
//      g2.setPaint( null )
//      g2.setColor( new Color( 255, 255, 255, 120 ))
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      val arc2d   = new CubicCurve2D.Double( 0, h/4, w/3, h/10, 66 * w, 1.5 * h, w, h/8 )
//      g2.draw( arc2d )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF )
   }

//   override def paintButtonBorder( context: SynthContext, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
//      g.setColor( Color.lightGray )
//      val g2 = g.asInstanceOf[ Graphics2D ]
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
////      g2.drawArc( x, y, w, h, 45, -90 )
//      val clpOrig = g2.getClip()
//      g2.clipRect( x, y + 4, w, h - 8 )
//      g2.drawOval( x, y - (w - h)/2, w - 1, w - 1 )
//      g2.setClip( clpOrig )
//   }

   override def paintButtonBorder( context: SynthContext, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
//      g.setColor( Color.white )
//      g.drawLine( x, y, x, y + h - 1 )
   }

   def paintButtonBorderX( context: SynthContext, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
      g.setColor( Color.lightGray )
//      g.drawLine( x + 3, y, x + 3, y + 8 )
//      g.drawLine( x, y + 3, x + 8, y + 3 )
//      g.drawLine( x + w - 4, y + h - 9, x + w - 4, y + h - 1 )
//      g.drawLine( x + w - 9, y + h - 4, x + w - 1, y + h - 4 )

      val g2 = g.asInstanceOf[ Graphics2D ]

      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

//      val gp = new GeneralPath()
//      gp.moveTo( x, y + h - 4.0f )
//      gp.lineTo( x + 25, y + h - 3 )
//      gp.lineTo( x, y + h - 1.5f )
//      gp.lineTo( x, y + h - 4.0f )
//      gp.moveTo( x + 1.5f, y + h - 1 )
//      gp.lineTo( x + 3, y + h - 12 )
//      gp.lineTo( x + 4.5f, y + h - 1 )
//      gp.closePath()
//      g2.fill( gp )

      val strkOrig = g2.getStroke
      val seed = context.getComponent().hashCode
      g2.setStroke( new StrkSketch( seed, 1.0f, 1.0f, 1f, 0f ))
      g2.drawRect( x + 4, y + 4, w - 8, h - 8 )
      g2.setStroke( new StrkSketch( seed, 1.5f, 0.5f, 2f, 10f ))
//      g2.setColor( Color.white )
      g2.drawRect( x + 4, y + 4, w - 8, h - 8 )
   }

   class StrkSketch( seed: Int, width: Float, fuzzy: Float, overshoot: Float, maxLen: Float ) extends Stroke {
      private val strkBasic   = new BasicStroke( width )
      private val rnd         = new Random( seed )
      private val fuzzMul     = fuzzy * 2
      private val fuzzAdd     = -fuzzy
      private val maxLenSqrM2 = maxLen * maxLen * 4

      private def fuzz = (rnd.nextFloat() * fuzzMul + fuzzAdd)

      def createStrokedShape( sin: Shape ) : Shape = {
         val sout = new GeneralPath()
         val c    = new Array[ Float ]( 6 )
         val iter = sin.getPathIterator( null )
         var lastX= 0f
         var lastY= 0f
         while( !iter.isDone ) {
            iter.currentSegment( c ) match {
               case PathIterator.SEG_MOVETO =>
//                 perturb( c, 2 )
                 sout.moveTo( c(0), c(1) )
               case PathIterator.SEG_LINETO =>
//                 perturb( c, 2 )
                  val dx0     = c(0) - lastX
                  val dy0     = c(1) - lastY
                  val ang     = math.atan2( dy0, dx0 )
                  val cos     = math.cos( ang ).toFloat
                  val sin     = math.sin( ang ).toFloat
                  val dx      = cos * overshoot
                  val dy      = sin * overshoot
//                  val fuzzx   = fuzz
//                  val fuzzy   = fuzz
                  sout.moveTo( lastX - dx + fuzz, lastY - dy + fuzz )
                  if( maxLen > 0f ) {
                     val lenSqr = dx0 * dx0 + dy0 * dy0
                     if( lenSqr <= maxLenSqrM2 ) {
                        sout.lineTo( c(0) + dx + fuzz, c(1) + dy + fuzz )
                     } else {
                        val dx1 = cos * maxLen
                        val dy1 = sin * maxLen
                        sout.lineTo( lastX + dx1, lastY + dy1 )
                        sout.moveTo( c(0) - dx1, c(1) - dy1 )
                        sout.lineTo( c(0) + dx + fuzz, c(1) + dy + fuzz )
                     }
                  }
                  sout.lineTo( c(0) + dx + fuzz, c(1) + dy + fuzz )
               case PathIterator.SEG_QUADTO =>
//                 perturb( c, 4 )
                  sout.quadTo( c(0), c(1), c(2), c(3) )
               case PathIterator.SEG_CUBICTO =>
//                 perturb( c, 6 )
                  sout.curveTo( c(0), c(1), c(2), c(3), c(4), c(5) )
               case PathIterator.SEG_CLOSE =>
                  sout.closePath()
            }
            lastX = c(0)
            lastY = c(1)
            iter.next()
         }

         // Finally, stroke the perturbed shape and return the result
         strkBasic.createStrokedShape( sout )
      }

      // Randomly modify the specified number of coordinates, by an amount
      // specified by the sloppiness field.
      private def perturb( c: Array[ Float ], num: Int ) {
         var i = 0; while( i < num ) {
            c(i) += (rnd.nextFloat * 2 - 1) * fuzzy
            i += 1
         }
      }
   }
}
*/

trait ButtonPainter extends ThumbPainter {
   protected def paintButtonText( b: AbstractButton, g2: Graphics2D, colrBg: Color ) : Unit

   def paintButton( b: AbstractButton, cg2: Graphics2D, x0: Int, y0: Int, pasOutline: Shape, actOutline: Shape ) {
      val outline = new GeneralPath()
      outline.append( pasOutline, false )
      outline.append( actOutline, false )
      val gc      = cg2.getDeviceConfiguration()
      val olb     = outline.getBounds
      val img     = gc.createCompatibleImage( olb.width + 6, olb.height + 6, Transparency.TRANSLUCENT )
      val g2      = img.getGraphics().asInstanceOf[ Graphics2D ]
      g2.setFont( cg2.getFont )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

      val bm         = b.getModel
      val pressed    = bm.isArmed && bm.isPressed
      val selected   = bm.isSelected
      
      paintThumb( b, pressed, g2, outline )
      // XXX DUPLICATE ACTION
      val colrBg = if( pressed ) {
         new Color( 0x90, 0x90, 0x90 )
      } else {
         new Color( 0x70, 0x70, 0x70 )
      }
      paintButtonText( b, g2, colrBg )

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
//      g2.setComposite( cmpOrig )
      g2.setTransform( atOrig )

      if( selected ) {
         // XXX DUPLICATE ACTION
         val a3 = new Area( actOutline )
         g2.setColor( new Color( 0x00, 0x00, 0x00, 0x24 ))
         val a0 = new Area( (new BasicStroke( 2f )).createStrokedShape( actOutline ))
         a0.add( a3 )
         val a0b = a0.getBounds()
         val clpOrig = g2.getClip()
         // XXX FIND GIMP GRAIN MERGE EQUIVALENT!
         g2.setComposite( new OverlayComposite( 1f ))
//         g2.setComposite( new HardLightComposite( 0.85f ))
         g2.clip( a0 )
         g2.drawImage( PanelBackgroundPainter.imgGlow, a0b.x, a0b.y, a0b.width, a0b.height, b )
         g2.setClip( clpOrig )
      }
      g2.setComposite( cmpOrig )

      cg2.drawImage( img, x0, y0, b )
//      if( pressed ) {
//         cg2.drawImage( img, x0 /* + 1 */, y0 /* + 1 */, b )
//      } else if( selected ) {
//         cg2.drawImage( img, x0, y0, b )
//      } else {
////         a4.subtract( new Area( new Ellipse2D.Double( x+8, y + (h - (w-24)) * 0.5f, w-16, w-24 )))
////         g2.setColor( new Color( 0x00, 0x00, 0x00, 0x20 ))     // 0x36
////         g2.fill( a4 )
//         cg2.drawImage( img, x0, y0, b )
//      }
      img.flush()
   }
}