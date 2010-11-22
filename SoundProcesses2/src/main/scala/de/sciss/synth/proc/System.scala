/*
*  System.scala
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

trait System {
   type Var[ _ ]
   type Ctx
   def t[ R ]( fun: Ctx => R ) : R // any system can initiate an ephemeral transaction
}

object ESystem {
   private type Var[ A ] = EVar[ ECtx, A ]
}
trait ESystem extends System
with Cursor[ ESystem, ECtx, ESystem.Var ] with CursorProvider[ ESystem ] {
   type Var[ T ] = EVar[ Ctx, T ]
   type Ctx = ECtx
}

object KSystem {
   private type Var[ A ] = KVar[ KCtx, A ]
}
trait KSystem extends System with KAccessProvider[ KSystem ] {
   type Var[ T ] = KVar[ KCtx, T ]
   type Ctx = KCtx
   def in( v: Int ) : Cursor[ KSystem, KCtx, KSystem.Var ]
}
