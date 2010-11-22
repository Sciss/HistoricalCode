///*
// *  ProcGroupImpl.scala
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
//package de.sciss.synth.proc.impl
//
//import collection.immutable.{Set => ISet}
//import de.sciss.synth.proc.{Proc, ProcGroup}
//
//class ProcGroupFactory[ S <: System ]( val sys: S ) {
//   factory =>
//
//   def create( name: String ) : ProcGroup[ S ] = {
////      new ProcGroupImpl( name )
//      error( "NOT YET IMPLEMENTED" )
//   }
//
//   private class ProcGroupImpl(
//      val name: String,
//      procs: sys.Var[ ISet[ Proc[ S ]]]
//   ) // ( implicit c: Ctx[ C, V ])
//   extends ProcGroup[ S ] /* with ModelImpl[ C, V, ProcGroup.Update[ C, V ]] */ {
////      val sys = factory.sys
//
//      // pero por-que??
////      private val procs = c.v( ISet.empty[ Proc[ S ]])
//
//      val model   = new ModelImpl[ sys.Ctx, ProcGroup.Update[ S ]] {}
//
//      def add( p: Proc[ S ])( implicit c: sys.Ctx ) {
//         procs.set( procs.get + p )
//         model.fireUpdate( ProcGroup.ProcAdded( p ))
//      }
//
//      def remove( p: Proc[ S ])( implicit c: sys. C ) {
//         procs.set( procs.get - p )
//         model.fireUpdate( ProcGroup.ProcRemoved( p ))
//      }
//
//      def all( implicit c: sys.Ctx ) : Traversable[ Proc[ S ]] = procs.get( c )
//   }
//}