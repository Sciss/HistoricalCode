/*
 *  ControllerImpl.scala
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

// todo: we could mix in something like a VarProxy?
class ControllerImpl[ C ]( val name: String, init: Double )( implicit c: Ctx[ C ])
extends Controller[ C ] with ModelImpl[ C, Double ] {
   private val vr = c.v( init )

   def get( implicit c: Ctx[ C ]) = vr.get
   def set( v: Double )( implicit c: Ctx[ C ]) {
      vr.set( v )
      fireUpdate( v )
   }
//   def addListener( l: L )( implicit c: Ctx[ C ]) = vr.addListener( l )
//   def removeListener( l: L )( implicit c: Ctx[ C ]) = vr.removeListener( l )
}