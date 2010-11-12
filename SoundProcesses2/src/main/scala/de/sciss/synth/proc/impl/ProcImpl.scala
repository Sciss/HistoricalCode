package de.sciss.synth.proc
package impl

import collection.immutable.{Queue => IQueue}

class ProcImpl[ C ]( implicit c: Ctx[ C ]) extends Proc[ C ] with ModelImpl[ C, Proc.Update ] {
//   val playing : Switch[ C ]  = new SwitchImpl( false )
//   val amp : Controller[ C ]  = new ControllerImpl( 1.0 )
//   val freq : Controller[ C ] = new ControllerImpl( 441.0 )
   val playing = new SwitchImpl( "playing", false ) // c.v( false ))
   val amp     = new ControllerImpl( "amp", 1.0 )
   val freq    = new ControllerImpl( "freq", 441.0 )

   playing.addListener( new Model.Listener[ C, Boolean ] {
      def updated( v: Boolean )( implicit c: Ctx[ C ]) {
         println( "PROC : playing now " + v )
      }
   })
   amp.addListener( new Model.Listener[ C, Double ] {
      def updated( v: Double )( implicit c: Ctx[ C ]) {

      }
   })
   freq.addListener( new Model.Listener[ C, Double ] {
      def updated( v: Double )( implicit c: Ctx[ C ]) {

      }
   })

//   private val listeners = c.v( IQueue.empty[ L ])
//
//   def addListener( l: L )( implicit c: Ctx[ C ]) {
//      listeners.transform( _ enqueue l )( txn( c ))
//   }
//
//   def removeListener( l: L )( implicit c: Ctx[ C ]) {
//      listeners.transform( _.filter( _ != l ))( txn( c ))
//   }
}