/*
*  ProcGroupImpl.scala
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

import collection.immutable.{Set => ISet}

object ProcGroupImpl {
   def apply[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( name: String )( implicit sys: System[ C, V ], c: C ) : ProcGroup[ C, V ] = {
      val procs: V[ ISet[ Proc[ C, V ]]] = sys.v( ISet.empty[ Proc[ C, V ]])
      new Group[ C, V ]( name, procs )
   }

   private class Group[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( val name: String, procs: V[ ISet[ Proc[ C, V ]]])
   extends ProcGroup[ C, V ] with ModelImpl[ C, ProcGroup.Update[ C, V ]] {

      override def toString = "ProcGroup(" + name + ")"

      def add( p: Proc[ C, V ])( implicit c: C ) {
         procs.set( procs.get + p )
         fireUpdate( ProcGroup.ProcAdded( p ))
      }

      def remove( p: Proc[ C, V ])( implicit c: C ) {
         procs.set( procs.get - p )
         fireUpdate( ProcGroup.ProcRemoved( p ))
      }

      def all( implicit c: C ) : ISet[ Proc[ C, V ]] = procs.get
   }
}