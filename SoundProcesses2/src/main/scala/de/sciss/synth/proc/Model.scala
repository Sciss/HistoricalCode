package de.sciss.synth.proc

import edu.stanford.ppl.ccstm.{Ref, Txn}
import collection.immutable.{Queue => IQueue}

object Model {
   trait Listener[ Repr, U ] {
      def updated( update: U )( implicit c: Ctx[ Repr ])
   }
}

trait Model[ Repr, U ] {
   import Model._

   type L = Listener[ Repr, U ]

//   def addListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
//   def removeListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
   def addListener( l: L )( implicit c: Ctx[ _ ]) : Unit
   def removeListener( l: L )( implicit c: Ctx[ _ ]) : Unit
}
