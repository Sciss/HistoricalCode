///*
// *  ProcImpl.scala
// *  (SoundProcesses)
// *
// *  Copyright (c) 2009-2010 Hanns Holger Rutz. All rights reserved.
// *
// *	 This software is free software; you can redistribute it and/or
// *	 modify it under the terms of the GNU General Public License
// *	 as published by the Free Software Foundation; either
// *	 version 2, june 1991 of the License, or (at your option) any later version.
// *
// *	 This software is distributed in the hope that it will be useful,
// *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
// *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// *	 General Public License for more details.
// *
// *	 You should have received a copy of the GNU General Public
// *	 License (gpl.txt) along with this software; if not, write to the Free Software
// *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// *
// *
// *	 For further information, please contact Hanns Holger Rutz at
// *	 contact@sciss.de
// *
// *
// *  Changelog:
// */
//
//package de.sciss.synth.proc
//package impl
//
//import collection.immutable.{Queue => IQueue}
//
//class ProcFactory[ S <: System ]( val sys: S ) {
//   factory =>
//
//   type Switch       = sys.Var[ Boolean ] with Model[ sys.Ctx, Boolean ] with Named
//   type Controller   = sys.Var[ Double ]  with Model[ sys.Ctx, Boolean ] with Named
//
//   def create( name: String ) : Proc[ S ] = {
//      error( "NOT YET IMPLEMENTED" )
//   }
//
//   private class ProcImpl(
//      val name: String,
//      _playing: Switch,
//      _amp: Controller,
//      _freq: Controller
//   )( implicit c: sys.C )
//   extends Proc[ S ] /* with ModelImpl[ C, V, Proc.Update ] */ {
//      override def toString = "Proc(" + name + ")"
//
//      val sys     = factory.sys
//      val model   = new ModelImpl[ sys.Ctx, Proc.Update ] {}
//
////      def playing : sys.Var[ Boolean ] with Model[ sys.Ctx, Boolean ] with Named = _playing
//      private type Switch = sys.Var[ Boolean ] with Model[ sys.Ctx, Boolean ] with Named
////      val playing = c.Var[ ]
//
////      val playing = new SwitchImpl[ C, V ]( "playing", false )
////      val amp     = new ControllerImpl[ C, V ]( "amp", 1.0 )
////      val freq    = new ControllerImpl[ C, V ]( "freq", 441.0 )
//
////      playing.addListener( new Model.Listener[ sys.Ctx, Boolean ] {
////         def updated( v: Boolean )( implicit c: sys.Ctx ) {
////            model.fireUpdate( Proc.Update( playing -> v ))
////         }
////      })
////      amp.addListener( new Model.Listener[ sys.Ctx, Double ] {
////         def updated( v: Double )( implicit c: sys.Ctx ) {
////            model.fireUpdate( Proc.Update( amp -> v ))
////         }
////      })
////      freq.addListener( new Model.Listener[ sys.Ctx, Double ] {
////         def updated( v: Double )( implicit c: sys.Ctx ) {
////            model.fireUpdate( Proc.Update( freq -> v ))
////         }
////      })
//   }
//}