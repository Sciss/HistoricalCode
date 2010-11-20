package de.sciss.synth.proc.impl

import de.sciss.synth.proc.{Ctx, Var}
import edu.stanford.ppl.ccstm.{Txn, Ref}

class EphemeralModelVarImpl[ K, V ]( init: V )( implicit c: Ctx[ K ], cm: ClassManifest[ V ])
extends Var[ K, AnyRef, V ] with ModelImpl[ K, V ] {
   private val ref = Ref( init )

   def getTxn( txn: Txn ) : V = ref.get( txn )

   def get( implicit c: Ctx[ K ]) : V = ref.get( c.txn )

   def set( v: V )( implicit c: Ctx[ K ]) {
      ref.set( v )( c.txn )
      fireUpdate( v )
   }
}