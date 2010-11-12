package de.sciss.synth.proc.impl

import de.sciss.synth.proc.{Proc, ProcGroup, Ctx}
import collection.immutable.{Set => ISet}

class ProcGroupImpl[ C ]( val name: String )( implicit c: Ctx[ C ])
extends ProcGroup[ C ] with ModelImpl[ C, ProcGroup.Update[ C ]] {
   private val procs = c.v( ISet.empty[ Proc[ C ]])

   def add( p: Proc[ C ])( implicit c: Ctx[ C ]) {
      procs.set( procs.get + p )
      fireUpdate( ProcGroup.ProcAdded( p ))
   }

   def remove( p: Proc[ C ])( implicit c: Ctx[ C ]) {
      procs.set( procs.get - p )
      fireUpdate( ProcGroup.ProcRemoved( p ))
   }
}