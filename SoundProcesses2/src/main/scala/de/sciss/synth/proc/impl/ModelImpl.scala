package de.sciss.synth.proc.impl

import edu.stanford.ppl.ccstm.Ref
import de.sciss.synth.proc.{Ctx, Model}
import collection.immutable.{Queue => IQueue}
                    
trait ModelImpl[ C, V[ _ ], U ] extends Model[ C, V, U ] {
   import Model._

   private val listeners = Ref( IQueue.empty[ L ])

   def addListener( l: L )( implicit c: Ctx[ _, _ ]) {
      listeners.transform( _ enqueue l )( c.txn )
   }

   def removeListener( l: L )( implicit c: Ctx[ _, _ ]) {
      listeners.transform( _.filter( _ != l ))( c.txn )
   }

   protected def fireUpdate( u: U )( implicit c: Ctx[ C, V ]) {
      listeners.get( c.txn ).foreach( _.updated( u )( c ))
   }
}
