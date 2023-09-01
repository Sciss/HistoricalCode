package de.sciss.submin.creator

import de.sciss.lucre.{DataOutput, Writable, DataInput}

object Point {
   def read( in: DataInput ) : Point = {
      val x = in.readInt()
      val y = in.readInt()
      Point( x, y )
   }
}
final case class Point( x: Int, y: Int ) extends Writable {
   def write( out: DataOutput ) {
      out.writeInt( x )
      out.writeInt( y )
   }
}