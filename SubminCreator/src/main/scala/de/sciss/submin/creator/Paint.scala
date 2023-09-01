package de.sciss.submin.creator

import de.sciss.lucre.{DataInput, DataOutput, Writable}
import annotation.switch

object Paint {
   def read( in: DataInput ) : Paint = {
      (in.readUnsignedByte(): @switch) match {
         case NoPaint.cookie => NoPaint
         case Color.cookie => Color.readIdentified( in )
      }
   }
}
sealed trait Paint extends Writable

case object NoPaint extends Paint {
   final private[creator] val cookie = 0

   def write( out: DataOutput ) {
      out.writeUnsignedByte( NoPaint.cookie )
   }
}

sealed trait SomePaint extends Paint

object Color {
   final private[creator] val cookie = 1

   private[creator] def readIdentified( in: DataInput ) : Color = {
      val red     = in.readDouble()
      val green   = in.readDouble()
      val blue    = in.readDouble()
      val alpha   = in.readDouble()
      Color( red, green, blue, alpha )
   }
}
final case class Color( red: Double, green: Double, blue: Double, alpha: Double ) extends SomePaint {
   def write( out: DataOutput ) {
      out.writeUnsignedByte( Color.cookie )
      out.writeDouble( red   )
      out.writeDouble( green )
      out.writeDouble( blue  )
      out.writeDouble( alpha )
   }
}