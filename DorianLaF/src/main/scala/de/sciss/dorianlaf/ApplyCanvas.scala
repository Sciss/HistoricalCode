/*
*  ApplyCanvas.scala
*  (DorianLaF)
*
*  Copyright (c) 2010 Hanns Holger Rutz. All rights reserved.
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

package de.sciss.dorianlaf

import java.awt.{Composite, RenderingHints, CompositeContext}
import java.awt.image._

object ApplyCanvas {
   val TURN_OFF = false // true
}

/**
* @todo the old version with setElem getElem on the data buffer was much faster.
*       it didn't work when src and dst raster had different sizes though. need
*       to fix that issue and then use the faster algorithm
*/
class ApplyCanvas( add: Int, div: Int ) extends Composite with CompositeContext {
   import ApplyCanvas._

   def createContext( srcCM: ColorModel, dstCM: ColorModel, hints: RenderingHints ) : CompositeContext = {
      val srcNum = srcCM.getNumComponents()
      val dstNum = dstCM.getNumComponents()
//println( "srcCM = " + srcCM.getClass + " / " + srcNum + " ; dstCM = " + dstCM.getClass + " / " + dstNum )
      val srcAccess = srcCM match {
         case d: DirectColorModel      => srcNum match {
            case 3 => DirectAccess3
            case 4 => DirectAccess4
         }
         case c: ComponentColorModel   => srcNum match {
            case 1 => ComponentAccess1 // grayscale
         }
      }
      val dstAccess = dstCM match {
         case d: DirectColorModel      => dstNum match {
            case 3 => DirectAccess3
            case 4 => DirectAccess4
         }
         case c: ComponentColorModel   => dstNum match {
            case 1 => ComponentAccess1
         }
      }
      new Context( srcAccess, dstAccess )
//      this
   }

   private trait Access {
      def read( rr: Raster, x: Int, y: Int ) : (Int, Int, Int, Int)
      def write( wr: WritableRaster, x: Int, y: Int, a: Int, r: Int, g: Int, b: Int ) : Unit
   }

   private object ComponentAccess1 extends Access {
      def read( rr: Raster, x: Int, y: Int ) : (Int, Int, Int, Int) = {
         val gray = rr.getSample( x, y, 0 )
//         ((argb >> 24) & 0xFF, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF)
         (0xFF, gray, gray, gray)
      }

      def write( wr: WritableRaster, x: Int, y: Int, a: Int, r: Int, g: Int, b: Int ) {
//         println( "HUHU" )
         error( "Unsupported" )
      }
   }

   private object DirectAccess3 extends Access {
      def read( rr: Raster, x: Int, y: Int ) : (Int, Int, Int, Int) = {
//         val argb = rr.getSample( x, y, 0 )
//         (0xFF, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF)
         (0xFF, rr.getSample( x, y, 0 ), rr.getSample( x, y, 1 ), rr.getSample( x, y, 2 ))
      }

      def write( wr: WritableRaster, x: Int, y: Int, a: Int, r: Int, g: Int, b: Int ) {
         wr.setSample( x, y, 0, r )
         wr.setSample( x, y, 1, g )
         wr.setSample( x, y, 2, b )
      }
   }

   private object DirectAccess4 extends Access {
      def read( rr: Raster, x: Int, y: Int ) : (Int, Int, Int, Int) = {
//         val argb = rr.getSample( x, y, 0 )
//         ((argb >> 24) & 0xFF, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF)
         (rr.getSample( x, y, 3 ), rr.getSample( x, y, 0 ), rr.getSample( x, y, 1 ), rr.getSample( x, y, 2 ))
      }

      def write( wr: WritableRaster, x: Int, y: Int, a: Int, r: Int, g: Int, b: Int ) {
         wr.setSample( x, y, 0, r )
         wr.setSample( x, y, 1, g )
         wr.setSample( x, y, 2, b )
         wr.setSample( x, y, 3, a )
      }
   }

   private class Context( srcAccess: Access, dstAccess: Access ) extends CompositeContext {
      def compose( srcR: Raster, dstInR: Raster, dstOutR: WritableRaster ) {
         if( TURN_OFF ) return
   //if( true ) return
//         val srcBuf		= srcR.getDataBuffer()
         val dstInBuf	= dstInR.getDataBuffer()
         val dstOutBuf	= dstOutR.getDataBuffer()
//         val num			= dstOutBuf.getSize()
         val w = dstOutR.getWidth
         val h = dstOutR.getHeight

//println( "compose : " + w + ", " + h + " :: " + dstInR.getWidth + ", " + dstInR.getHeight + " :: " + dstOutR.getWidth + ", " + dstOutR.getHeight )

         var y = 0
         while( y < h ) {
            var x = 0
            while( x < w ) {
               val i = y * w + x
//               val src = srcBuf.getElem( i )
//               val dst = dstInBuf.getElem( i )
//               val dar = dst >> 24
//               val drr = dst >> 16
//               val dgr = dst >> 8
//               val dai = dar & 0xFF
//               val dri = drr & 0xFF
//               val dgi = dgr & 0xFF
//               val dbi = dst & 0xFF

      // multiply:
      //         dstOutBuf.setElem( i,
      //            (((src & 0xFF) * (dst & 0xFF) >> 8)) |
      //            ((((src >> 8) & 0xFF) * ((dst >> 8) & 0xFF)) & 0xFF00) |
      //            (((((src >> 16) & 0xFF) * ((dst >> 16) & 0xFF)) << 8) & 0xFF0000) |
      //            (((((src >> 24) & 0xFF) * ((dst >> 24) & 0xFF)) << 16) & 0xFF000000)
      //         )

               // overlay with added and dimmed
//               val sar = src >> 24
//               val srr = src >> 16
//               val sgr = src >> 8
//               val sa = sar.toByte // & 0xFF
//               val sr = srr.toByte // & 0xFF
//               val sg = sgr.toByte // & 0xFF
//               val sb = src.toByte // & 0xFF
////      val shift = 3
//               val sai = (sar & 0xFF) // >> shift
//               val sri = (srr & 0xFF) // >> shift
//               val sgi = (sgr & 0xFF) // >> shift
//               val sbi = (src & 0xFF) // >> shift

               val (sai, sri, sgi, sbi) = srcAccess.read( srcR, x, y )
               val (dai, dri, dgi, dbi) = dstAccess.read( dstInR, x, y )
               val sa = sai.toByte // & 0xFF
               val sr = sri.toByte // & 0xFF
               val sg = sgi.toByte // & 0xFF
               val sb = sbi.toByte // & 0xFF

//               val da = dar.toByte // & 0xFF
//               val dr = drr.toByte // & 0xFF
//               val dg = dgr.toByte // & 0xFF
//               val db = dst.toByte // & 0xFF

//               val dai2 = da / 3
//               val dri2 = dr / 3
//               val dgi2 = dg / 3
//               val dbi2 = db / 3

      //         val ma = math.max( -128, math.min( 127, sa + da )) & 0xFF
      //         val mr = math.max( -128, math.min( 127, sr + dr )) & 0xFF
      //         val mg = math.max( -128, math.min( 127, sg + dg )) & 0xFF
      //         val mb = math.max( -128, math.min( 127, sb + db )) & 0xFF

      //         val ma = math.max( 0, math.min( 0xFF, sai + dai )) & 0xFF
      //         val mr = math.max( 0, math.min( 0xFF, sri + dri )) & 0xFF
      //         val mg = math.max( 0, math.min( 0xFF, sgi + dgi )) & 0xFF
      //         val mb = math.max( 0, math.min( 0xFF, sbi + dbi )) & 0xFF

      //         val ma = dai // math.max( 0, math.min( 0xFF, sai + da )) & 0xFF
      //         val mr = dri // math.max( 0, math.min( 0xFF, sri + dr )) & 0xFF
      //         val mg = dgi // math.max( 0, math.min( 0xFF, sgi + dg )) & 0xFF
      //         val mb = dbi // math.max( 0, math.min( 0xFF, sbi + db )) & 0xFF

      //         val ma = 0xFF // math.max( 0, math.min( 0xFF, sra + dai2 )) & 0xFF
      //         val mr = math.max( 0, math.min( 0xFF, sr + dri2 )) & 0xFF
      //         val mg = math.max( 0, math.min( 0xFF, sg + dgi2 )) & 0xFF
      //         val mb = math.max( 0, math.min( 0xFF, sb + dbi2 )) & 0xFF
               val ma = 0xFF; // math.max( -0x80, math.min( 0x7F, sa + dai2 )) & 0xFF
      //         val mr = (math.max( -0x80, math.min( 0x7F, sr + dri2 )) + 0x80) & 0xFF
      //         val mg = (math.max( -0x80, math.min( 0x7F, sg + dgi2 )) + 0x80) & 0xFF
      //         val mb = (math.max( -0x80, math.min( 0x7F, sb + dbi2 )) + 0x80) & 0xFF

      //         val mr = math.max( 0, math.min( 0xFF, sri + (dri - 0x80)/111 )) & 0xFF
      //         val mg = math.max( 0, math.min( 0xFF, sgi + (dgi - 0x80)/111 )) & 0xFF
      //         val mb = math.max( 0, math.min( 0xFF, sbi + (dbi - 0x80)/111 )) & 0xFF

      //         val mr = math.max( 0, math.min( 0xFF, dri + sr )) // & 0xFF
      //         val mg = math.max( 0, math.min( 0xFF, dgi + sg )) // & 0xFF
      //         val mb = math.max( 0, math.min( 0xFF, dbi + sb )) // & 0xFF

      //         val mr = math.max( 0, math.min( 0xFF, dri + sr * 2 )) // & 0xFF
      //         val mg = math.max( 0, math.min( 0xFF, dgi + sg * 2 )) // & 0xFF
      //         val mb = math.max( 0, math.min( 0xFF, dbi + sb * 2 )) // & 0xFF

               val mr = math.max( 0, math.min( 0xFF, dri + (sr + add) / div )) // & 0xFF
               val mg = math.max( 0, math.min( 0xFF, dgi + (sg + add) / div )) // & 0xFF
               val mb = math.max( 0, math.min( 0xFF, dbi + (sb + add) / div )) // & 0xFF

      //         dstOutBuf.setElem( i, (ma << 24) | (mr << 16) | (mg << 8) | mb )
      //         dstOutBuf.setElem( i, (ma << 24) | (mr << 16) | (mg << 8) | mb )

      //         val oa = if( ma < 128 ) ma * sai >> 7 else 255 - ((255 - ma) * (255 - sai) >> 7)
      //         val or = if( mr < 128 ) mr * sri >> 7 else 255 - ((255 - mr) * (255 - sri) >> 7)
      //         val og = if( mg < 128 ) mg * sgi >> 7 else 255 - ((255 - mg) * (255 - sgi) >> 7)
      //         val ob = if( mb < 128 ) mb * sbi >> 7 else 255 - ((255 - mb) * (255 - sbi) >> 7)

               // overlay
               val oa = math.min( 0xFF, ma + dai )
      //val mr = 0xFF
      //val mg = 0x00
      //val mb = 0x00
               // WRONG ; cf. http://dev.w3.org/SVG/modules/compositing/master/SVGCompositingPrimer.html
      //         val or = if( dri < 0x80 ) (dri * mr) >> 7 else 0xFF - (((0xFF - dri) * (0xFF - mr)) >> 7)
      //         val og = if( dgi < 0x80 ) (dgi * mg) >> 7 else 0xFF - (((0xFF - dgi) * (0xFF - mg)) >> 7)
      //         val ob = if( dbi < 0x80 ) (dbi * mb) >> 7 else 0xFF - (((0xFF - dbi) * (0xFF - mb)) >> 7)

               val or = if( dri < 0x80 ) math.min( 0xFF, (dri * mr) >> 7) else 0xFF - math.min( 0xFF, ((0xFF - dri) * (0xFF - mr)) >> 7)
               val og = if( dgi < 0x80 ) math.min( 0xFF, (dgi * mg) >> 7) else 0xFF - math.min( 0xFF, ((0xFF - dgi) * (0xFF - mg)) >> 7)
               val ob = if( dbi < 0x80 ) math.min( 0xFF, (dbi * mb) >> 7) else 0xFF - math.min( 0xFF, ((0xFF - dbi) * (0xFF - mb)) >> 7)

               // 1 - 2.(1 - Dc).(1 - Sc)

               // hard light
      //         val oa = math.min( 0xFF, ma + dai )
      //         val or = if( mr < 0x80 ) (dri * mr) >> 7 else 0xFF - (((0xFF - dri) * (0xFF - mr)) >> 7)
      //         val og = if( mg < 0x80 ) (dgi * mg) >> 7 else 0xFF - (((0xFF - dgi) * (0xFF - mg)) >> 7)
      //         val ob = if( mb < 0x80 ) (dbi * mb) >> 7 else 0xFF - (((0xFF - dbi) * (0xFF - mb)) >> 7)

               // screen
      //         val oa = math.min( 0xFF, ma + dai )
      //         val or = 0xFF - (((0xFF - dri) * (0xFF - mr)) >> 8)
      //         val og = 0xFF - (((0xFF - dgi) * (0xFF - mg)) >> 8)
      //         val ob = 0xFF - (((0xFF - dbi) * (0xFF - mb)) >> 8)

      // alpha:         math.min( 0xFF, ma + sai )

               dstOutBuf.setElem( i, (oa << 24) | (or << 16) | (og << 8) | ob )
//               dstAccess.write( dstOutR, x, y, oa, or, og, ob )
//               dstAccess.write( dstOutR, x, y, sai, sri, sgi, sbi )
//               dstAccess.write( dstOutR, x, y, dai, dri, dgi, dbi )

               x += 1
            }
            y += 1
         }
      }

      def dispose {}
   }

   //////////////////////

   def compose( srcR: Raster, dstInR: Raster, dstOutR: WritableRaster ) {
      val srcBuf		= srcR.getDataBuffer()
      val dstInBuf	= dstInR.getDataBuffer()
      val dstOutBuf	= dstOutR.getDataBuffer()

      val sw         = srcR.getWidth
      val sh         = srcR.getHeight
      val dw         = dstOutR.getWidth
      val dh         = dstOutR.getHeight
//      val num			= dstOutBuf.getSize()

println( "compose : " + srcR.getWidth + ", " + srcR.getHeight + " :: " + dstOutR.getWidth + ", " + dstOutR.getHeight )
      var y = 0
      while( y < dh ) {
         var x = 0
         while( x < dw ) {
//            val i   = y * dw + x
//            val src = srcBuf.getElem( y * sw + x ) // i
            val i   = x * dh + y
            val src = srcBuf.getElem( x * sh + y )
            val dst = dstInBuf.getElem( i )

            // overlay with added and dimmed
            val sar = src >> 24
            val srr = src >> 16
            val sgr = src >> 8
            val sa = sar.toByte // & 0xFF
            val sr = srr.toByte // & 0xFF
            val sg = sgr.toByte // & 0xFF
            val sb = src.toByte // & 0xFF

            val dar = dst >> 24
            val drr = dst >> 16
            val dgr = dst >> 8
            val da = dar.toByte // & 0xFF

            val dai = dar & 0xFF
            val dri = drr & 0xFF
            val dgi = dgr & 0xFF
            val dbi = dst & 0xFF

            val ma = dai // 0xFF; // math.max( -0x80, math.min( 0x7F, sa + dai2 )) & 0xFF
            val mr = math.max( 0, math.min( 0xFF, dri + (sr + 64) )) // & 0xFF
            val mg = math.max( 0, math.min( 0xFF, dgi + (sg + 64) )) // & 0xFF
            val mb = math.max( 0, math.min( 0xFF, dbi + (sb + 64) )) // & 0xFF

            // overlay
            val oa = math.min( 0xFF, ma + dai )
            val or = if( dri < 0x80 ) math.min( 0xFF, (dri * mr) >> 7) else 0xFF - math.min( 0xFF, ((0xFF - dri) * (0xFF - mr)) >> 7)
            val og = if( dgi < 0x80 ) math.min( 0xFF, (dgi * mg) >> 7) else 0xFF - math.min( 0xFF, ((0xFF - dgi) * (0xFF - mg)) >> 7)
            val ob = if( dbi < 0x80 ) math.min( 0xFF, (dbi * mb) >> 7) else 0xFF - math.min( 0xFF, ((0xFF - dbi) * (0xFF - mb)) >> 7)

            dstOutBuf.setElem( i, (oa << 24) | (or << 16) | (og << 8) | ob )
            x += 1 // ; i += 1
         }
         y += 1
      }
   }

   def dispose {}
}