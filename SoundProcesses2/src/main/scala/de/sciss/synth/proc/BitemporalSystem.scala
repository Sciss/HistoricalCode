/*
 *  BitemporalSystem.scala
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

import de.sciss.confluent.{FatValue, VersionPath}
import edu.stanford.ppl.ccstm.{STM, Ref, TxnLocal, Txn}
import collection.immutable.{SortedMap => ISortedMap}
import Double.{PositiveInfinity => dinf}
import impl.ModelImpl

// todo: compose systems, contexts and vars from common parts
object BitemporalSystem extends System[ Bitemporal ] {
   private type C = Ctx[ Bitemporal ]

   def in[ T ]( version: VersionPath )( fun: C => T ) : T = STM.atomic { tx =>
      fun( new Bitemporal( tx, version ))
   }

   def at[ T ]( period: Period )( thunk: => T )( implicit c: C ) : T =
      during( Interval( period, Period( dinf )))( thunk )

   def during[ T ]( interval: Interval )( thunk: => T )( implicit c: C ) : T = {
      val oldIval = c.repr.interval
      c.repr.interval = interval
      try { thunk } finally { c.repr.interval = oldIval }
   }
}

class Bitemporal private[proc]( val txn: Txn, initPath: VersionPath )
extends Ctx[ Bitemporal ] {
   private type C = Ctx[ Bitemporal ]

   private val pathRef = new TxnLocal[ VersionPath ] {
      override protected def initialValue( txn: Txn ) = initPath
   }

   private val intervalRef = new TxnLocal[ Interval ] {
      override protected def initialValue( txn: Txn ) = Interval( Period( 0.0 ), Period( dinf ))
   }
   
   def repr = this
   def system = BitemporalSystem

   def v[ T ]( init: T )( implicit m: ClassManifest[ T ]) : Var[ Bitemporal, T ] = {
      val fat0 = FatValue.empty[ ISortedMap[ Period, T ]]
      val vp   = writePath
      val fat1 = fat0.assign( vp.path, ISortedMap( Period( 0.0 ) -> init )( Ordering.ordered[ Period ]))
      new KVar( Ref( fat1 ))
   }

   def path : VersionPath = pathRef.get( txn )

   private[proc] def writePath : VersionPath = {
      val p = pathRef.get( txn )
      if( p == initPath ) {
         val pw = p.newBranch
         pathRef.set( pw )( txn )
         pw
      } else p
   }

   def period : Period     = interval.start
   def interval : Interval = intervalRef.get( txn )

   private[proc] def interval_=( newInterval: Interval ) = intervalRef.set( newInterval )( txn )

   private class KVar[ /* @specialized */ T ]( ref: Ref[ FatValue[ ISortedMap[ Period, T ]]])
   extends Var[ Bitemporal, T ] with ModelImpl[ Bitemporal, T ] {
      def get( implicit c: C ) : T = {
         val vp   = c.repr.path
         val map  = ref.get( c.txn ).access( vp.path )
           .getOrElse( error( "No assignment for path " + vp ))
         map.to( c.repr.period ).last._2  // XXX is .last efficient? we might need to switch to FingerTree.Ranged
      }

      // todo: collapse access and assign into one transform method in FatValue
      def set( v: T )( implicit c: C ) {
         val rp   = c.repr.path
         val wp   = c.repr.writePath 
         val ival = c.repr.interval
         val t    = c.txn
         val fat  = ref.get( t )
         val map  = fat.access( rp.path )
           .getOrElse( error( "No assignment for path " + rp ))

         ref.set( fat.assign( wp.path,
            map.to( ival.start ) +
            (ival.start -> v) +
            (ival.end -> map.to( ival.end ).last._2) ++ // XXX .last efficient?
            map.from( ival.end )
         ))( t )
         fireUpdate( v, c )
      }
   }
}