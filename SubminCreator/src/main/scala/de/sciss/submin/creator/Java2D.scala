package de.sciss.submin.creator

import java.awt.{BasicStroke, Graphics2D}
import java.awt.geom.Rectangle2D
import annotation.switch

object Java2D {
   implicit def elementToJava2D( elem: Element ) :  Java2DElement = new Java2DElement( elem )
   implicit def paintToJava2D( paint: SomePaint ) : Java2DPaint   = new Java2DPaint( paint )
   implicit def shapeToJava2D( shape: Shape ) :     Java2DShape   = new Java2DShape( shape )
   implicit def strokeToJava2D( stroke: Stroke ) :  Java2DStroke  = new Java2DStroke( stroke )
}

final class Java2DElement( elem: Element ) {
   import Java2D._

   def paint( g2d: Graphics2D ) {
      elem.fill match {
         case NoPaint =>
         case other: SomePaint =>
            other.set( g2d )
            elem.shape.paint( g2d )
      }
      elem.strokePaint match {
         case NoPaint =>
         case other: SomePaint =>
            other.set( g2d )
            elem.strokeStyle.set( g2d )
            elem.shape.draw( g2d )
      }
   }
}

final class Java2DPaint( p: SomePaint ) {
   def set( g2d: Graphics2D ) {
      p match {
         case Color( r, g, b, a ) =>
            val ri = math.max( 0, math.min( 0xFF, (r * 0xFF).toInt ))
            val gi = math.max( 0, math.min( 0xFF, (g * 0xFF).toInt ))
            val bi = math.max( 0, math.min( 0xFF, (b * 0xFF).toInt ))
            val ai = math.max( 0, math.min( 0xFF, (a * 0xFF).toInt ))
            g2d.setColor( new java.awt.Color( ri, gi, bi, ai ))
      }
   }
}

final class Java2DShape( s: Shape ) {
   private def jv: java.awt.Shape = s match {
      case Rectangle( left, top, width, height ) =>
         new Rectangle2D.Double( left, top, width, height )
   }

   def paint( g2d: Graphics2D ) {
      g2d.fill( jv )
   }

   def draw( g2d: Graphics2D ) {
      g2d.draw( jv )
   }
}

final class Java2DStroke( p: Stroke ) {
   def set( g2d: Graphics2D ) {
      val jCap = p.cap match {
         case Stroke.CapButt     => BasicStroke.CAP_BUTT
         case Stroke.CapRound    => BasicStroke.CAP_ROUND
         case Stroke.CapSquare   => BasicStroke.CAP_SQUARE
      }
      val jJoin = p.join match {
         case Stroke.JoinMiter   => BasicStroke.JOIN_MITER
         case Stroke.JoinRound   => BasicStroke.JOIN_ROUND
         case Stroke.JoinBevel   => BasicStroke.JOIN_BEVEL
      }
      val d     = p.dashes
      val dSz   = d.size
      val width = p.width.toFloat
      val miter = p.miterLimit.toFloat
      val jStrk = if( dSz < 2 ) {
         new BasicStroke( width, jCap, jJoin, miter )
      } else {
         val pattern = d.pattern
         val arr     = Array.tabulate( dSz )( i => pattern( i ).toFloat )
         new BasicStroke( width, jCap, jJoin, miter, arr, d.phase.toFloat )
      }
      g2d.setStroke( jStrk )
   }
}
