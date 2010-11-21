package de.sciss.synth.proc.impl

import de.sciss.synth.proc.{Ctx, Var}
import edu.stanford.ppl.ccstm.{Txn, Ref}

class EphemeralModelVarImpl[ C, T ]( init: T )( implicit c: Ctx[ C, _ ], cm: ClassManifest[ T ])
extends Var[ C, Option, T ] with ModelImpl[ C, Option, T ] {
   private val ref = Ref( init )

   def getTxn( txn: Txn ) : T = ref.get( txn )

   def get( implicit c: Ctx[ C, _ ]) : T = ref.get( c.txn )

   def set( v: T )( implicit c: Ctx[ C, _ ]) {
      ref.set( v )( c.txn )
      fireUpdate( v )
   }
}
