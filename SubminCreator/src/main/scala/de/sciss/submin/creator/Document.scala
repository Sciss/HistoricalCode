package de.sciss.submin.creator

import de.sciss.lucre.expr

trait Document {
   def figures: expr.LinkedList.Modifiable[ S, Figure, Unit ]
}