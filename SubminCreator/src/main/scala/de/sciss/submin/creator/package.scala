package de.sciss.submin

import de.sciss.lucre.{event => evt, expr}

package object creator {
   type S = evt.InMemory

   type Ex[ A ]      = expr.Expr[ S, A ]
   type ExVar[ A ]   = expr.Expr.Var[ S, A ]

   def ??? : Nothing = sys.error( "TODO" )
}
