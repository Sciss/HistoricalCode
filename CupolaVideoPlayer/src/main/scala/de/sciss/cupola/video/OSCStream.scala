/*
 *  OSCStream.scala
 *  (CupolaVideoPlayer)
 *
 *  Copyright (c) 2011 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.cupola.video

import io.Source
import de.sciss.osc.{OSCBundle, OSCMessage}
import collection.immutable.{IndexedSeq => IIdxSeq}

object OSCStream {
   val BOOL_TO_INT   = true

   private val intEx    = """(\d+)""".r
   private val floatEx  = """(\d?\.\d+)""".r
   private val boolEx   = """([Tt]rue|[Ff]alse)""".r

   val bundleToTag = (b: OSCBundle) => b.timetag

   def fromSource( source: Source, offset: Double = 0.0 ) : OSCStream = {
//      var tOff = if( beginAtZero ) Double.PositiveInfinity else 0.0
      val bundles = source.getLines().map { l =>
         val idx     = l.indexOf( ' ' )
         val timeStr = l.substring( 0, idx )
         val hours   = timeStr.substring( 0, 2 ).toInt
         val mins    = timeStr.substring( 3, 5 ).toInt
         val secs    = timeStr.substring( 6 ).toDouble
         val time0   = hours * 3600 + mins * 60 + secs
         val msgStr  = l.substring( idx + 1 )
         val msgArgsS= msgStr.split( ", " )
         val msgName = msgArgsS.head
         val msgArgs = msgArgsS.tail.map {
            case intEx( i )   => i.toInt
            case floatEx( f ) => f.toFloat
            case boolEx( b )  =>
               val bv = b.toBoolean
               if( BOOL_TO_INT ) { if( bv ) 1 else 0 } else bv
            case s            => s
         }
//         if( tOff == Double.PositiveInfinity ) {
////println( "tOff = " + -time0 )
//            tOff = -time0
//         }
         val time = time0 + offset // tOff
         OSCBundle.secs( time, OSCMessage( msgName, msgArgs: _* ))
      }
      val bndlSeq = bundles.toIndexedSeq
      source.close()
      new OSCStream( bndlSeq )
   }
}
class OSCStream( val bundles: IIdxSeq[ OSCBundle ])