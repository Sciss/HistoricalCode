package de.sciss.synth.proc

import de.sciss.confluent.VersionPath

trait EVar[ C, T ] {
   def get( implicit c: C ) : T
   def set( v: T )( implicit c: C ) : Unit
}

trait KVar[ C, T ] extends EVar[ C, T ] {
   def range( vStart: VersionPath, vStop: VersionPath )( implicit c: ECtx ) : Traversable[ T ]
}
