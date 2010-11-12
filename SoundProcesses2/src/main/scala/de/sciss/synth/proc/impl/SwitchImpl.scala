package de.sciss.synth.proc
package impl

// todo: we could mix in something like a VarProxy?
class SwitchImpl[ C ]( val name: String, init: Boolean )( implicit c: Ctx[ C ])
extends Switch[ C ] with ModelImpl[ C, Boolean ] {
   private val vr = c.v( init )

   def get( implicit c: Ctx[ C ]) = vr.get
   def set( v: Boolean )( implicit c: Ctx[ C ]) {
      vr.set( v )
      fireUpdate( v )
   }
//   def addListener( l: L )( implicit c: Ctx[ C ]) = vr.addListener( l )
//   def removeListener( l: L )( implicit c: Ctx[ C ]) = vr.removeListener( l )
}