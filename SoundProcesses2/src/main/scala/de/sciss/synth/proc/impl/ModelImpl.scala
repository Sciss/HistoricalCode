package de.sciss.synth.proc.impl

import edu.stanford.ppl.ccstm.Ref
import de.sciss.synth.proc.{Ctx, Model}
import collection.immutable.{Queue => IQueue}
                    
trait ModelImpl[ Repr, U ] extends Model[ Repr, U ] {
   import Model._

   private val listeners = Ref( IQueue.empty[ L ])

   def addListener( l: L )( implicit c: Ctx[ Repr ]) {
      listeners.transform( _ enqueue l )( c.txn )
   }

   def removeListener( l: L )( implicit c: Ctx[ Repr ]) {
      listeners.transform( _.filter( _ != l ))( c.txn )
   }

   protected def fireUpdate( u: U, c: Ctx[ Repr ]) {
      listeners.get( c.txn ).foreach( _.updated( u )( c ))
   }
}
