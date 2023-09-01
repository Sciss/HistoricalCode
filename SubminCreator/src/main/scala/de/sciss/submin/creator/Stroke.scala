package de.sciss.submin.creator

import de.sciss.lucre.{DataInput, DataOutput, Writable}
import collection.immutable.{IndexedSeq => IIdxSeq}
import annotation.switch

object Stroke {
   sealed trait Cap { private[creator] def cookie: Int }
   case object CapButt   extends Cap { final private[creator] val cookie = 0 }
   case object CapRound  extends Cap { final private[creator] val cookie = 1 }
   case object CapSquare extends Cap { final private[creator] val cookie = 2 }

   sealed trait Join { private[creator] def cookie: Int }
   case object JoinMiter extends Join { final private[creator] val cookie = 0 }
   case object JoinRound extends Join { final private[creator] val cookie = 1 }
   case object JoinBevel extends Join { final private[creator] val cookie = 2 }

   def read( in: DataInput ) : Stroke = {
      val width   = in.readDouble()
      val cap     = (in.readUnsignedByte(): @switch) match {
         case CapButt.cookie   => CapButt
         case CapRound.cookie  => CapRound
         case CapSquare.cookie => CapSquare
      }
      val join    = (in.readUnsignedByte(): @switch) match {
         case JoinBevel.cookie => JoinBevel
         case JoinMiter.cookie => JoinMiter
         case JoinRound.cookie => JoinRound
      }
      val miterLimit = in.readDouble()
      val dashes     = Dashes.read( in )
      Stroke( width, cap, join, miterLimit, dashes )
   }

   object Dashes {
      def read( in: DataInput ) : Dashes = {
         val sz      = in.readInt()
         val pattern = IIdxSeq.fill( sz )( in.readDouble() )
         val phase   = in.readDouble()
         Dashes( pattern, phase )
      }
   }
   final case class Dashes( pattern: IIdxSeq[ Double ], phase: Double ) extends Writable {
      def write( out: DataOutput ) {
         out.writeInt( pattern.size )
         pattern.foreach( out.writeDouble )
         out.writeDouble( phase )
      }

      def size : Int = pattern.size
   }
}
final case class Stroke( width: Double, cap: Stroke.Cap, join: Stroke.Join, miterLimit: Double, dashes: Stroke.Dashes )
extends Writable {
   def write( out: DataOutput ) {
      out.writeDouble( width )
      out.writeUnsignedByte( cap.cookie )
      out.writeUnsignedByte( join.cookie )
      out.writeDouble( miterLimit )
      dashes.write( out )
   }
}