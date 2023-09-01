//package de.sciss.synth.proc.impl
//
//import de.sciss.synth.proc.{Ctx, Var}
//import edu.stanford.ppl.ccstm.{Txn, Ref}
//
//class EphemeralModelVarImpl[ C, T ]( init: T )( implicit c: Ctx[ C, Nothing ], cm: ClassManifest[ T ])
//extends Var[ C, Nothing, T ] with ModelImpl[ C, Nothing, T ] {
//   private val ref = Ref( init )
//
////   def repr = this
//
//   def getTxn( txn: Txn ) : T = ref.get( txn )
//
//   def get( implicit c: Ctx[ C, Nothing ]) : T = ref.get( c.txn )
//
//   def set( v: T )( implicit c: Ctx[ C, Nothing ]) {
//      ref.set( v )( c.txn )
//      fireUpdate( v )
//   }
//}
