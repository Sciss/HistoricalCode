package de.sciss.synth.proc

import collection.immutable.{Queue => IQueue}
import edu.stanford.ppl.ccstm.{Txn, Ref}

trait Var[ Repr, T ] {
   type L = Listener[ Repr, T ]

   def set( v: T )( implicit c: Ctx[ Repr ]) : Unit
   def get( implicit c: Ctx[ Repr ]) : T

   def addListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
   def removeListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
}

trait TxnVar[ Repr, T ] extends Var[ Repr, T ] {
   vr =>

   private val listeners = Ref( IQueue.empty[ L ])

   def addListener( l: L )( implicit c: Ctx[ Repr ]) {
      listeners.transform( _ enqueue l )( txn( c ))
   }

   def removeListener( l: L )( implicit c: Ctx[ Repr ]) {
      listeners.transform( _.filter( _ != l ))( txn( c ))
   }

   protected def txn( c: Ctx[ Repr ]) : Txn
   protected def fireUpdate( v: T, c: Ctx[ Repr ]) {
      listeners.get( txn( c )).foreach( _.update( vr, v )( c ))
   }
}

trait Listener[ Repr, T ] {
   // todo: koennte ein implizierter read-only context sein?
   def update( h: Var[ Repr, T ], v: T )( implicit c: Ctx[ Repr ]) : Unit
}