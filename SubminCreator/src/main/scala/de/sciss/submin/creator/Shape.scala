package de.sciss.submin.creator

import de.sciss.lucre.{DataOutput, Writable, DataInput}

object Shape {
   def read( in: DataInput ) : Shape = {
      in.readUnsignedByte() match {
         case Rectangle.cookie => Rectangle.readIdentified( in )
      }
   }
}
sealed trait Shape extends Writable {
   def bounds: Rectangle
}

object Rectangle {
   final private[creator] val cookie = 0

   private[creator] def readIdentified( in: DataInput ) : Rectangle = {
      val left    = in.readInt()
      val top     = in.readInt()
      val width   = in.readInt()
      val height  = in.readInt()
      Rectangle( left, top, width, height )
   }

   object Expr {

   }
}
final case class Rectangle( left: Int, top: Int, width: Int, height: Int ) extends Shape {
   def write( out: DataOutput ) {
      out.writeInt( left )
      out.writeInt( top )
      out.writeInt( width )
      out.writeInt( height )
   }

   def bounds: Rectangle = this

   def right  = left + (width  - 1)
   def bottom = top  + (height - 1)
}