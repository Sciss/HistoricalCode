package de.sciss.submin.creator

import de.sciss.lucre.expr.Type
import de.sciss.lucre.{DataInput, DataOutput}
import de.sciss.lucre.event.{Targets, Sys}

object Strings extends Type[ String ] {
   def readValue( in: DataInput ) : String = in.readString()

   def writeValue( value: String, out: DataOutput ) { out.writeString( value )}

   protected def readTuple[ S <: Sys[ S ]]( cookie: Int, in: DataInput, access: S#Acc, targets: Targets[ S ])
                                          ( implicit tx: S#Tx ) : ReprNode[ S ] = ???
}