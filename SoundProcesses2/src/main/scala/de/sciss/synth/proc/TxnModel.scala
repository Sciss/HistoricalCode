package de.sciss.synth.proc

import edu.stanford.ppl.ccstm.{Ref, Txn}
import collection.immutable.{Queue => IQueue}

object Model {
   trait Listener[ Repr, U ] {
      def updated( update: U )( implicit c: Ctx[ Repr ])
   }
}

trait Model[ Repr, U ] {
   import Model._

   type L = Listener[ Repr, U ]

   private val listeners = Ref( IQueue.empty[ L ])

//   def addListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
//   def removeListener( l: L )( implicit c: Ctx[ Repr ]) : Unit

   def addListener( l: L )( implicit c: Ctx[ Repr ]) {
      listeners.transform( _ enqueue l )( c.txn )
   }

   def removeListener( l: L )( implicit c: Ctx[ Repr ]) {
      listeners.transform( _.filter( _ != l ))( c.txn )
   }

//   protected def txn( c: Ctx[ Repr ]) : Txn
   protected def fireUpdate( u: U, c: Ctx[ Repr ]) {
      listeners.get( c.txn ).foreach( _.updated( u )( c ))
   }
}

//trait TxnModel[ Repr, U ] extends Model[ Repr, U ] {
//   private val listeners = Ref( IQueue.empty[ L ])
//
//   def addListener( l: L )( implicit c: Ctx[ Repr ]) {
//      listeners.transform( _ enqueue l )( txn( c ))
//   }
//
//   def removeListener( l: L )( implicit c: Ctx[ Repr ]) {
//      listeners.transform( _.filter( _ != l ))( txn( c ))
//   }
//
//   protected def txn( c: Ctx[ Repr ]) : Txn
//   protected def fireUpdate( u: U, c: Ctx[ Repr ]) {
//      listeners.get( txn( c )).foreach( _.updated( u )( c ))
//   }
//}