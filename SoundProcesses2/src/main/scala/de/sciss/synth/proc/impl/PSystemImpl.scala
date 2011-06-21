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

import collection.immutable.{Set => ISet, SortedMap => ISortedMap}
import Double.{PositiveInfinity => dinf}
import concurrent.stm.{TxnExecutor, TxnLocal, InTxn, Ref}
import sys.error

object PSystemImpl {
   def apply() : PSystem = new Sys

   private class Sys extends PSystem {
      sys =>

      override def toString = "PSystem"

//      def in[ R ]( ival: Interval )( fun: PCtx => R ) : R = STM.atomic { tx =>
//         fun( new Ctx( sys, tx, ival ))
//      }

      def at[ T ]( period: Period )( thunk: => T )( implicit c: PCtx ) : T =
         during( Interval( period, Period( dinf )))( thunk )

      def during[ T ]( interval: Interval )( thunk: => T )( implicit c: PCtx ) : T = {
         val oldIval = c.interval
         c.interval = interval
         try { thunk } finally { c.interval = oldIval }
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

      def pProjector    = PEProjImpl
      def peProjector   = PEProjImpl

      object PEProjImpl extends PEProjector[ PCtx, PSystem.Var ]
      with ModelImpl[ ECtx, Projector.Update[ PCtx, PSystem.Cursor ]] {
         val cursorsRef = Ref( ISet.empty[ PSystem.Cursor ])

         def cursors( implicit c: CtxLike ) : Iterable[ PSystem.Cursor ] = cursorsRef.get( c.txn )

         def projectAt( p: Period ) : PSystem.Projection = new CursorImpl( sys, Interval( p, p ))

         def cursorAt( p: Period )( implicit c: ECtx ) : PSystem.Cursor = {
            val csr = new CursorImpl( sys, Interval( p, p ))
            cursorsRef.transform( _ + csr )( c.txn )
            fireUpdate( Projector.CursorAdded[ PCtx, PSystem.Cursor ]( csr ))
            csr
         }

         def removePCursor( cursor: PSystem.Cursor )( implicit c: ECtx ) {
            cursorsRef.transform( _ - cursor )( c.txn )
            fireUpdate( Projector.CursorRemoved[ PCtx, PSystem.Cursor ]( cursor ))
         }

         def at[ R ]( p: Period )( fun: PCtx => R ) : R = TxnExecutor.defaultAtomic { tx =>
            fun( new Ctx( sys, tx, Interval( p, p )))
         }

         def range[ T ]( vr: PSystem.Var[ T ], interval: Interval )( implicit c: CtxLike ) : Traversable[ (Period, T) ] =
            error( "NOT YET IMPLEMENTED" )
      }
   }

   private class Ctx( system: Sys, val txn: InTxn, initIVal: Interval )
   extends PCtx {
      ctx =>

      override def toString = "PCtx"

      private val intervalRef = TxnLocal[ Interval ]( init = initIVal )

      def period : Period     = interval.start
      def interval : Interval = intervalRef.get( txn )
      private[proc] def interval_=( i: Interval ) = intervalRef.set( i )( txn )

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
            (ival.stop -> map.to( ival.stop ).last._2) ++ // XXX .last efficient?
            map.from( ival.stop )
         )( c.txn )
         fireUpdate( v )
      }

      protected def fireUpdate( v: T )( implicit c: PCtx ) : Unit

      def pRange( ival: Interval )( implicit c: CtxLike ) : Traversable[ (Period, T) ] =
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

   private class CursorImpl( sys: Sys, initialInterval: Interval )
   extends ECursor[ PCtx ] with PProjection[ PCtx ] with ModelImpl[ ECtx, Cursor.Update ] {
      csr =>

      private val ivalRef = Ref( initialInterval )

//      private val txnInitiator = new TxnLocal[ Boolean ] {
//         override protected def initialValue( txn: Txn ) = false
//      }

//      def isApplicable( implicit c: PCtx ) = txnInitiator.get( c.txn )
      def isApplicable( implicit c: PCtx ) : Boolean = {
         val ivalCtx = c.interval
         val ival    = interval
         ivalCtx.touches( ival )  // XXX overlaps
      }

      def interval( implicit c: CtxLike ) : Interval = ivalRef.get( c.txn )
      def period( implicit c: CtxLike ) : Period = interval.start

      def dispose( implicit c: ECtx ) {
         sys.pProjector.removePCursor( csr )
      }

      def t[ R ]( fun: PCtx => R ) : R = {
         // XXX todo: should add t to KTemporalSystemLike and pass txn to in
         TxnExecutor.defaultAtomic { t =>
            sys.pProjector.at( ivalRef.get( t ).start ) { implicit c =>
               fun( c )
            }
         }
      }
   }
}