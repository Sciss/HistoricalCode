package de.sciss.synth.proc.impl

import de.sciss.synth.proc.{Ctx, Var}
import edu.stanford.ppl.ccstm.{Txn, Ref}

class EphemeralModelVarImpl[ Repr, T ]( init: T )( implicit c: Ctx[ Repr ], cm: ClassManifest[ T ])
extends Var[ Repr, T ] with ModelImpl[ Repr, T ] {
   private val ref = Ref( init )
   def getTxn( txn: Txn ) : T = ref.get( txn )
   def get( implicit c: Ctx[ Repr ]) : T = ref.get( c.txn )
//   def setTxn( txn: Txn, v: T ) {
//      ref.set( v )( txn )
//      fireUpdate( v )
//   }
   def set( v: T )( implicit c: Ctx[ Repr ]) {
      ref.set( v )( c.txn )
      fireUpdate( v )
   }
}