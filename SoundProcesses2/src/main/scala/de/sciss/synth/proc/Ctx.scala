package de.sciss.synth.proc

import edu.stanford.ppl.ccstm.Txn

trait Ctx[ Repr ] {
   def repr : Repr
   def system : System[ Repr ]
   def v[ T ]( init : T )( implicit m: ClassManifest[ T ]) : Var[ Repr, T ]
   def txn: Txn
}

//trait TxnCtx[ Repr ] extends Ctx[ Repr ] {
//   def txn: Txn
//}