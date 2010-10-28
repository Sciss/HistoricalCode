package de.sciss.synth.proc

import edu.stanford.ppl.ccstm.{STM, Txn, Ref}

object EphemeralSystem extends System[ EphemeralCtx ] {
   private type C = EphemeralCtx
   
   def v[ T ]( v: T )( implicit m: ClassManifest[ T ]) : Var[ C, T ] = new EVar( Ref( v ))
   def t[ T ]( fun: C => T ) : T = STM.atomic( tx => fun( new C( tx )))

   private class EVar[ /* @specialized */ T ]( ref: Ref[ T ]) extends Var[ C, T ] {
      def get( implicit c: C ) : T = ref.get( c.txn )
      def set( v: T )( implicit c: C ) : Unit = ref.set( v )( c.txn )
   }
}

class EphemeralCtx private[proc]( private[proc] val txn: Txn )