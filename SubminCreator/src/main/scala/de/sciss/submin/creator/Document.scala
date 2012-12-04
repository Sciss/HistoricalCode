package de.sciss.submin.creator

import de.sciss.lucre.expr

object Document {
   def apply() : Document = {
      ???
   }
}
trait Document {
   def figures: expr.LinkedList.Modifiable[ S, Figure, Unit ]
}