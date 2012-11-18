package de.sciss.submin.creator

import de.sciss.lucre.{DataOutput, stm, DataInput, expr, event => evt}

object Shapes extends expr.Type[ Shape ] {
   def readValue( in: DataInput ) : Shape = Shape.read( in )

   def writeValue( value: Shape, out: DataOutput ) { value.write( out )}

   protected def readTuple[ S <: evt.Sys[ S ]]( cookie: Int, in: DataInput, access: S#Acc, targets: evt.Targets[ S ])
                                              ( implicit tx: S#Tx ) : ReprNode[ S ] = {
      ???
   }
}