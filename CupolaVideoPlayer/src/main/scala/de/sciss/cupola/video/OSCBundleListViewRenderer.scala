/*
 *  OSCBundleListViewRenderer.scala
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

import de.sciss.osc.{OSCMessage, OSCBundle}
import swing.{Alignment, Label, ListView}
import java.awt.Font

class OSCBundleListViewRenderer extends ListView.AbstractRenderer[ OSCBundle, Label ]( new Label ) {
   component.horizontalAlignment = Alignment.Leading
   component.font = new Font( "Menlo", Font.PLAIN, 10 )

   def configure( list: ListView[ _ ], isSelected: Boolean, hasFocus: Boolean, a: OSCBundle, index: Int ) {
      val millis0 = (OSCBundle.timetagToSecs( a.timetag ) * 1000).toInt
      val secs0   = millis0 / 1000
      val mins0   = secs0 / 60
      val millis  = millis0 % 1000
      val secs    = secs0 % 60
      val mins    = mins0 % 60
      val hours   = mins0 / 60
      val timeStr =
         (hours  +  100).toString.substring( 1 ) + ":" +
         (mins   +  100).toString.substring( 1 ) + ":" +
         (secs   +  100).toString.substring( 1 ) + "." +
         (millis + 1000).toString.substring( 1 ) + "  "
      val text    = timeStr + (a.headOption match {
         case Some( OSCMessage( name, args @ _* )) => args.mkString( name + ", ", ", ", "" )
         case _ => ""
      })

      component.text = text
   }
}