/*
 *  ImageView.scala
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

import javax.swing.JComponent
import swing.Component
import java.awt.{Color, Dimension, Graphics, Image}

class ImageView extends Component {
   @volatile private var imageVar: Image = null

   var scale   = false
   var aspect  = true

//   cursor = null

   override lazy val peer : JComponent = new JComponent {
      override def paintComponent( g: Graphics ) {
         val i = imageVar
         val w = math.max( 1, getWidth )
         val h = math.max( 1, getHeight )
         g.setColor( Color.black )
         g.fillRect( 0, 0, w, h )
         if( i != null ) {
            if( scale ) {
               val iw   = math.max( 1, i.getWidth( this ))
               val ih   = math.max( 1, i.getHeight( this ))
               if( w == iw && h == ih ) {
                  var tx = 0
                  var ty = 0
                  var tw = w
                  var th = h
                  if( aspect ) {
                     val sh = w.toDouble / iw
                     val sv = h.toDouble / ih
                     if( sh < sv ) {
                        th = (ih * sh + 0.5).toInt
                        ty = (h - th) >> 1
                     } else {
                        tw = (iw * sv + 0.5).toInt
                        tx = (w - tw) >> 1
                     }
                  }
                  g.drawImage( i, tx, ty, tw, th, this )
               } else {
                  g.drawImage( i, 0, 0, this )
               }
            } else {
               g.drawImage( i, 0, 0, this )
            }
         }
      }

      override def getPreferredSize = {
         val i = image
         if( i == null ) super.getPreferredSize /* new Dimension( 320, 240 ) */ else new Dimension( i.getWidth( this ), i.getHeight( this ))
      }
   }

   def image: Image = imageVar

   /**
    * This method is thread-safe.
    */
   def image_=( i: Image ) {
      imageVar = i
      repaint()
   }
}
