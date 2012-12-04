package de.sciss.submin.creator

import de.sciss.lucre.{DataInput, stm}
import impl.{FigureImpl => Impl}
import collection.immutable.{IndexedSeq => IIdxSeq}

object Figure {
   def empty( implicit tx: S#Tx ) : Figure = Impl.empty

   def read( in: DataInput, access: S#Acc )( implicit tx: S#Tx ) : Figure = Impl.read( in, access )
}
trait Figure extends stm.Mutable[ S#ID, S#Tx ] {
   def name( implicit tx: S#Tx ): Ex[ String ]
   def name_=( value: Ex[ String ])( implicit tx: S#Tx ) : Unit

//   def elements: expr.LinkedList.Modifiable[ S, Element, Unit ]

   def elements( overlapping: Rectangle )( implicit tx: S#Tx ) : IIdxSeq[ Element ]
}