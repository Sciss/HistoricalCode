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

   def all( implicit c: Ctx[ C ]) : Traversable[ Proc[ C ]] = procs.get( c )
}