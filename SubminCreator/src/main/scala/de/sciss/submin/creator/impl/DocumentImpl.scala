package de.sciss.submin
package creator
package impl

import de.sciss.lucre.{expr, DataOutput}

object DocumentImpl {
   def apply()( implicit tx: S#Tx ) : Document = {
//      val id = tx.
      ???
   }

   private final class Impl( val id: S#ID, val figures: expr.LinkedList.Modifiable[ S, Figure, Unit ])
   extends Document {
      def write( out: DataOutput ) {
//         sys.error( "TODO" )
         figures.write( out )
      }

      def dispose()( implicit tx: S#Tx ) {
         figures.dispose()
      }
   }
}