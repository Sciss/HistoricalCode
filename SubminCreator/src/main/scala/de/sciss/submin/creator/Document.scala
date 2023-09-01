package de.sciss.submin.creator

import de.sciss.lucre.{expr, stm}
import stm.Mutable
import impl.{DocumentImpl => Impl}

object Document {
   def apply()( implicit tx: S#Tx ) : Document = Impl()
}
trait Document extends Mutable[ S#ID, S#Tx ] {
   def figures: expr.LinkedList.Modifiable[ S, Figure, Unit ]
}