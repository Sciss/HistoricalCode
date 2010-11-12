package de.sciss.synth.proc
package impl

class SwitchImpl[ C ]( val name: String, init: Boolean )( implicit c: Ctx[ C ]) extends Switch[ C ] {
   private val vr = c.v( init )

   def get( implicit c: Ctx[ C ]) = vr.get
   def set( v: Boolean )( implicit c: Ctx[ C ]) = vr.set( v )
}