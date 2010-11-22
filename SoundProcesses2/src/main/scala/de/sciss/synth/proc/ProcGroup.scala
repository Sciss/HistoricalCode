/*
 *  ProcGroup.scala
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

object ProcGroup {
   sealed trait Update[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]
   case class ProcAdded[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( /* idx: Int, */ p: Proc[ C, V ]) extends Update[ C, V ]
   case class ProcRemoved[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( /* idx: Int, */ p: Proc[ C, V ]) extends Update[ C, V ]
}

trait ProcGroup[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]] extends Model[ C, ProcGroup.Update[ C, V ]] with Named {
//   val sys: S
//   type Listener = Model.Listener[ C, ProcGroup.Update[ C ]]
//   def listener( f: Function1[ Ctx[ C ], PartialFunction[ ProcGroup.Update[ C ], Unit ]]) =
//      new Model.Listener[ C, ProcGroup.Update[ C ]] {
//         def updated( u: ProcGroup.Update[ C ])( implicit c: Ctx[ C ]) = f( c )( u )
//      }

//   def model : Model[ sys.Ctx, ProcGroup.Update[ S ]]

   def add( p: Proc[ C, V ])( implicit c: C ) : Unit
   def remove( p: Proc[ C, V ])( implicit c: C ) : Unit
   def all( implicit c: C ) : Traversable[ Proc[ C, V ]]
}