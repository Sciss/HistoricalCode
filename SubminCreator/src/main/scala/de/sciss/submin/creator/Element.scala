package de.sciss.submin.creator

import de.sciss.lucre.{DataInput, DataOutput, Writable}
import de.sciss.lucre.stm.ImmutableSerializer

object Element {
   implicit object serializer extends ImmutableSerializer[ Element ] {
      def write( v: Element, out: DataOutput ) { v.write( out )}
      def read( in: DataInput ) : Element = Element.read( in )
   }

   def read( in: DataInput ) : Element = {
      val shape         = Shape.read( in )
      val fill          = Paint.read( in )
      val strokePaint   = Paint.read( in )
      val strokeStyle   = Stroke.read( in )
      Element( shape, fill, strokePaint, strokeStyle )
   }
}
final case class Element( shape: Shape, fill: Paint, strokePaint: Paint, strokeStyle: Stroke )
extends Writable {
   def write( out: DataOutput ) {
      shape.write( out )
      fill.write( out )
      strokePaint.write( out )
      strokeStyle.write( out )
   }

   def bounds: Rectangle = shape.bounds
}