/*
 *  PSystemImpl.scala
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

import edu.stanford.ppl.ccstm.{TxnLocal, Txn, Ref, STM}
import collection.immutable.{Set => ISet, SortedMap => ISortedMap}

object PSystemImpl {
   def apply() : PSystem = new Sys

   private class Sys extends PSystem with ModelImpl[ PCtx, PSystem.Update[ PCtx, PSystem.Var ]] {
      sys =>

      override def toString = "PSystem"

      val cursorsRef = Ref( ISet.empty[ PCursor[ PCtx, PSystem.Var ]])

      def pcursors( implicit c: ECtx ) : ISet[ PCursor[ PCtx, PSystem.Var ]] = cursorsRef.get( c.txn )

      def addPCursor( implicit c: PCtx ) : PCursor[ PCtx, PSystem.Var ] = {
//         val csr = new CursorImpl( sys, c.path )
//         cursorsRef.transform( _ + csr )( c.txn )
//         sys.fireUpdate( PSystem.CursorAdded[ PCtx, PSystem.Var ]( csr ))
//         csr
         error( "NOT YET IMPLEMENTED" )
      }

      def removePCursor( cursor: PCursor[ PCtx, PSystem.Var ])( implicit c: PCtx ) {
         cursorsRef.transform( _ - cursor )( c.txn )
         sys.fireUpdate( PSystem.CursorRemoved[ PCtx, PSystem.Var ]( cursor ))
      }

      def in[ R ]( ival: Interval )( fun: PCtx => R ) : R = STM.atomic { tx =>
         fun( new Ctx( sys, tx, ival ))
      }

      def t[ R ]( fun: ECtx => R ) : R = Factory.esystem.t( fun )

      def v[ T ]( init: T )( implicit m: ClassManifest[ T ], c: PCtx ) : PVar[ PCtx, T ] = {
         val (ref, name) = prep( init )
         new Var( ref, name )
      }

      def modelVar[ T ]( init: T )( implicit m: ClassManifest[ T ], c: PCtx ) : PVar[ PCtx, T ] with Model[ PCtx, T ] = {
         val (ref, name) = prep( init )
         new ModelVar( ref, name )
      }

      def userVar[ T ]( init: T )( user: (PCtx, T) => Unit )( implicit m: ClassManifest[ T ], c: PCtx ) : PVar[ PCtx, T ] = {
         val (ref, name) = prep( init )
         new UserVar( ref, name, user )
      }

      private def prep[ T ]( init: T )( implicit m: ClassManifest[ T ], c: PCtx ) =
         Ref( ISortedMap( Period( 0.0 ) -> init )( Ordering.ordered[ Period ])) -> m.toString
   }

   private class Ctx( system: Sys, val txn: Txn, initIVal: Interval )
   extends PCtx {
      ctx =>

      override def toString = "PCtx"

      private val intervalRef = new TxnLocal[ Interval ] {
         override protected def initialValue( txn: Txn ) = initIVal
      }

      def period : Period     = interval.start
      def interval : Interval = intervalRef.get( txn )

      def eph : ECtx = ESystemImpl.join( txn )
   }

   private trait AbstractVar[ T ] // ( ref: Ref[ FatValue[ T ]], typeName: String )
   extends PVar[ PCtx, T ] /* with ModelImpl[ PCtx, T ] */ {
      protected val ref: Ref[ ISortedMap[ Period, T ]]
      protected val typeName : String

      override def toString = "PVar[" + typeName + "]"

      def get( implicit c: PCtx ) : T = {
         val map = ref.get( c.txn )
         // lastOption.getOrElse( error( "No assignment..." )) ?
         map.to( c.period ).last._2  // XXX is .last efficient? we might need to switch to FingerTree.Ranged
      }
      def set( v: T )( implicit c: PCtx ) {
         val ival = c.interval
         ref.transform( map =>
            map.to( ival.start ) +
            (ival.start -> v) +
            (ival.end -> map.to( ival.end ).last._2) ++ // XXX .last efficient?
            map.from( ival.end )
         )( c.txn )
         fireUpdate( v )
      }

      protected def fireUpdate( v: T )( implicit c: PCtx ) : Unit

      def prange( ival: Interval )( implicit c: ECtx ) : Traversable[ T ] =
         error( "NOT YET IMPLEMENTED" )

//      def transform( f: T => T )( implicit c: C ) {
//         ref.transform( _.assign( c.repr.writePath.path, v ))( txn( c ))
//         fireUpdate( v, c )
//      }
   }

   private class Var[ T ]( val ref: Ref[ ISortedMap[ Period, T ]], val typeName: String ) extends AbstractVar[ T ] {
      protected def fireUpdate( v: T )( implicit c: PCtx ) {}
   }

   private class ModelVar[ T ]( val ref: Ref[ ISortedMap[ Period, T ]], val typeName: String )
   extends AbstractVar[ T ] with ModelImpl[ PCtx, T ]

   private class UserVar[ T ]( val ref: Ref[ ISortedMap[ Period, T ]], val typeName: String, user: (PCtx, T) => Unit )
   extends AbstractVar[ T ] {
      protected def fireUpdate( v: T )( implicit c: PCtx ) { user( c, v )}
   }

//   private class CursorImpl( sys: Sys, initialPath: VersionPath )
//   extends KCursor[ PCtx, PSystem.Var ] with ModelImpl[ PCtx, KCursor.Update ] {
//      private val vRef = Ref( initialPath )
//
//      private val txnInitiator = new TxnLocal[ Boolean ] {
//         override protected def initialValue( txn: Txn ) = false
//      }
//
//      def isApplicable( implicit c: PCtx ) = txnInitiator.get( c.txn )
//      def path( implicit c: ECtx ) : VersionPath = vRef.get( c.txn )
//
//      def t[ R ]( fun: PCtx => R ) : R = {
//         // XXX todo: should add t to KTemporalSystemLike and pass txn to in
//         // variant so we don't call atomic twice
//         // (although that is ok and the existing transaction is joined)
//         // ; like BitemporalSystem.inRef( vRef.getTxn( _ )) { ... } ?
//         STM.atomic { t =>
//            val oldPath = vRef.get( t )
//            txnInitiator.set( true )( t )
//            sys.in( oldPath ) { implicit c =>
//               val res     = fun( c )
//               val newPath = c.path
//               if( newPath != oldPath ) {
//                  vRef.set( newPath )( c.txn )
//                  fireUpdate( KCursor.Moved( oldPath, newPath ))
//               }
//               txnInitiator.set( false )( t )
//               res
//            }
//         }
//      }
//   }
}