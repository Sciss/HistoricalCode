/*
 *  Util.scala
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

import swing.{Dialog, Frame}

object Util {
   def displayError( parent: Frame, title: String, e: Throwable ) {
      Dialog.showMessage( Option( parent ).flatMap( _.contents.headOption ).orNull, "An error occurred:\n" +
         abbreviateStackTrace( e ), title, Dialog.Message.Error )
   }

   def abbreviateStackTrace( e: Throwable, maxLine: Int = 16 ) : String = {
      val sb      = new StringBuffer( e.getClass.getName + " : " + e.getLocalizedMessage + "\n" )
      val trace   = e.getStackTrace
      val num     = math.min( maxLine, trace.length )
      var i = 0; while( i < num ) {
         sb.append( "\tat " + trace( i ) + "\n" )
      i += 1 }
      if( trace.length > num ) {
         sb.append( "\t...\n" )
      }
      sb.toString
   }

   /**
    *    Binary search on an indexed collection with a given mapping function
    *    to Long.
    *
    *    @return  if positive: the position of elem in coll (i.e. elem is
    *             contained in coll view). if negative: (-ins -1) where ins is the
    *             position at which elem should be inserted into the collection
    *             (thus `ins = -(res + 1)`)
    */
   def binarySearch[ T ]( coll: IndexedSeq[ T ], elem: Long )( implicit view: T => Long ) : Int = {
      var index   = 0
      var low	   = 0
      var high	   = coll.size - 1
      while({
         index  = (high + low) >> 1
         low   <= high
      }) {
         val map = view( coll( index ))
         if( map == elem ) return index
         if( map < elem ) {
            low = index + 1
         } else {
            high = index - 1
         }
      }
      -low - 1
   }

   def unifiedLook( f: Frame ) {
      f.peer.getRootPane.putClientProperty( "apple.awt.brushMetalLook", java.lang.Boolean.TRUE )
   }

   def formatSignedTimeString( secs: Double ) : String = {
      val ts = formatTimeString( math.abs( secs ))
      (if( secs < 0 ) "-" else " ") + ts
   }

   def formatTimeString( secs: Double ) : String = {
      val millis0 = (secs * 1000).toInt
      val secs0   = millis0 / 1000
      val mins0   = secs0 / 60
      val millis  = millis0 % 1000
      val secs1   = secs0 % 60
      val mins    = mins0 % 60
      val hours   = mins0 / 60

      (hours  +  100).toString.substring( 1 ) + ":" +
      (mins   +  100).toString.substring( 1 ) + ":" +
      (secs1  +  100).toString.substring( 1 ) + "." +
      (millis + 1000).toString.substring( 1 ) + "  "
   }
}