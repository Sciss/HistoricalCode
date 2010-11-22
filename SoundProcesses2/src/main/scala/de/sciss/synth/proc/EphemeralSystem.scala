///*
// *  EphemeralSystem.scala
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
//
//import edu.stanford.ppl.ccstm.{STM, Txn, Ref}
//import impl.ModelImpl
//
//object ESystem extends ESystem {
//   private type C = Ctx[ Ephemeral, Nothing ]
//
//   override def toString = "EphemeralSystem"
//
//   def t[ T ]( fun: C => T ) : T = STM.atomic( tx => fun( new EphemeralImpl( tx )))
//
//   private class EphemeralImpl private[proc]( val txn: Txn )
//   extends Ephemeral with Ctx[ Ephemeral, Nothing ] {
//      private type C = Ctx[ Ephemeral, Nothing ]
//
//      def repr = this
//      def system = EphemeralSystem
//      def v[ T ]( init: T )( implicit m: ClassManifest[ T ]) : Var[ Ephemeral, Nothing, T ] =
//         new EVar( Ref( init ))
//
//      private class EVar[ T ]( ref: Ref[ T ])
//      extends Var[ Ephemeral, Nothing, T ] with ModelImpl[ Ephemeral, Nothing, T ] {
//         def repr = error( "No repr" )
//         def get( implicit c: C ) : T = ref.get( c.txn )
//         def set( v: T )( implicit c: C ) {
//            ref.set( v )( c.txn )
//            fireUpdate( v )
//         }
//      }
//   }
//}
//
//// trait Ephemeral extends Ctx[ Ephemeral, Nothing ]