package de.sciss.synth.proc

import edu.stanford.ppl.ccstm.{TxnLocal, STM, Txn, Ref}
import collection.immutable.{SortedMap => ISortedMap}

object PTemporalSystem extends System[ PTemporalCtx ] {
   private type C = PTemporalCtx

   def v[ T ]( v: T )( implicit m: ClassManifest[ T ]) : Var[ C, T ] =
      new EVar( Ref( ISortedMap( Time( 0.0 ) -> v )( Ordering.ordered[ Time ])))

   def t[ T ]( fun: C => T ) : T = STM.atomic( tx => fun( new C( tx )))
   def at[ T ]( time: Time )( thunk: => T )( implicit c: C ) : T = {
      val oldTime = c.time
      c.time = time
      try { thunk } finally { c.time = oldTime }
   }

   private class EVar[ /* @specialized */ T ]( ref: Ref[ ISortedMap[ Time, T ]]) extends Var[ C, T ] {
      def get( implicit c: C ) : T = {
         val map = ref.get( c.txn )
         map.to( c.time ).last._2  // XXX is .last efficient? we might need to switch to FingerTree.Ranged
      }
      def set( v: T )( implicit c: C ) {
         val tim = c.time
         // "write" till end of time... eventually we could have an interval in Ctx
         ref.transform( _.to( tim ) + (tim -> v) )( c.txn )
      }
   }
}

class PTemporalCtx private[proc]( private[proc] val txn: Txn ) {
   private val timeRef = new TxnLocal[ Time ] {
      override protected def initialValue( txn: Txn ) = Time( 0.0 )
   }
   def time : Time = timeRef.get( txn )
   private[proc] def time_=( newTime: Time ) = timeRef.set( newTime )( txn )
}