/*
 *  ProcImpl.scala
 *  (SoundProcesses)
 *
 *  Copyright (c) 2009-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	 This software is free software; you can redistribute it and/or
 *	 modify it under the terms of the GNU General Public License
 *	 as published by the Free Software Foundation; either
 *	 version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	 This software is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	 General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public
 *	 License (gpl.txt) along with this software; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	 For further information, please contact Hanns Holger Rutz at
 *	 contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.synth.proc
package impl

import collection.immutable.{Queue => IQueue}

object ProcImpl {
   def apply[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( name: String )( implicit sys: System[ C, V ], c: C ) : Proc[ C, V ] = {
      val playing = new Switch[ C, V ] with ModelImpl[ C, Boolean ] {
         val name = "playing"
         val v = sys.userVar( false ) { (c, bool) => fireUpdate( bool )( c )}
         override def toString = "Switch(" + name + ")"
      }
      val amp = new Controller[ C, V ] with ModelImpl[ C, Double ] {
         val name = "amp"
         val v = sys.userVar( 1.0 ) { (c, d) => fireUpdate( d )( c )}
         override def toString = "Controller(" + name + ")"
      }
      val freq = new Controller[ C, V ] with ModelImpl[ C, Double ] {
         val name = "freq"
         val v = sys.userVar( 441.0 ) { (c, d) => fireUpdate( d )( c )}
         override def toString = "Controller(" + name + ")"
      }
      val res = new Impl[ C, V ]( name, playing, amp, freq )
      playing.addListener( new Model.Listener[ C, Boolean ] {
         def updated( v: Boolean )( implicit c: C ) {
            res.fireUpdate( Proc.Update( playing -> v ))
         }
      })
      amp.addListener( new Model.Listener[ C, Double ] {
         def updated( v: Double )( implicit c: C ) {
            res.fireUpdate( Proc.Update( amp -> v ))
         }
      })
      freq.addListener( new Model.Listener[ C, Double ] {
         def updated( v: Double )( implicit c: C ) {
            res.fireUpdate( Proc.Update( freq -> v ))
         }
      })
      res
   }

   private class Impl[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]](
      val name: String,
      val playing: Switch[ C, V ],
      val amp: Controller[ C, V ],
      val freq: Controller[ C, V ]
   )
   extends Proc[ C, V ] with ModelImpl[ C, Proc.Update ] {
      
      override def toString = "Proc(" + name + ")"

//      def playing : sys.Var[ Boolean ] with Model[ sys.Ctx, Boolean ] with Named = _playing
//      val playing = c.Var[ ]

//      val playing = new SwitchImpl[ C, V ]( "playing", false )
//      val amp     = new ControllerImpl[ C, V ]( "amp", 1.0 )
//      val freq    = new ControllerImpl[ C, V ]( "freq", 441.0 )
   }
}