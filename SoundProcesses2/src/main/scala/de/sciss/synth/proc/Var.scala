package de.sciss.synth.proc

import de.sciss.confluent.VersionPath

trait EVar[ C, T ] {
   def get( implicit c: C ) : T
   def set( v: T )( implicit c: C ) : Unit
}

trait KVar[ C, T ] extends EVar[ C, T ]  {
   def krange( vStart: VersionPath, vStop: VersionPath )( implicit c: ECtx ) : Traversable[ T ]
}

trait PVar[ C, T ] extends EVar[ C, T ]  {
   def prange( r: Interval )( implicit c: ECtx ) : Traversable[ T ]
}

trait BVar[ C, T ] extends KVar[ C, T ] with PVar[ C, T ]
