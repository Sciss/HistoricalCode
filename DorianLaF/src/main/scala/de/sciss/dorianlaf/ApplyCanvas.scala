package de.sciss.dorianlaf

import java.awt.{Composite, RenderingHints, CompositeContext}
import java.awt.image.{WritableRaster, ColorModel, Raster}

class ApplyCanvas extends Composite with CompositeContext {
   def createContext( srcColorModel: ColorModel, dstColorModel: ColorModel, hints: RenderingHints ) : CompositeContext = this 

   def compose( srcR: Raster, dstInR: Raster, dstOutR: WritableRaster ) {
      val srcBuf		= srcR.getDataBuffer()
      val dstInBuf	= dstInR.getDataBuffer()
      val dstOutBuf	= dstOutR.getDataBuffer()
      val num			= dstOutBuf.getSize()

      var i = 0
      while( i < num ) {
         val src = srcBuf.getElem( i )
         val dst = dstInBuf.getElem( i )
// multiply:
//         dstOutBuf.setElem( i,
//            (((src & 0xFF) * (dst & 0xFF) >> 8)) |
//            ((((src >> 8) & 0xFF) * ((dst >> 8) & 0xFF)) & 0xFF00) |
//            (((((src >> 16) & 0xFF) * ((dst >> 16) & 0xFF)) << 8) & 0xFF0000) |
//            (((((src >> 24) & 0xFF) * ((dst >> 24) & 0xFF)) << 16) & 0xFF000000)
//         )

         // overlay with added and dimmed
         val sar = src >> 24
         val srr = src >> 16
         val sgr = src >> 8
         val sa = sar.toByte // & 0xFF
         val sr = srr.toByte // & 0xFF
         val sg = sgr.toByte // & 0xFF
         val sb = src.toByte // & 0xFF
val shift = 3
         val sai = (sar & 0xFF) // >> shift
         val sri = (srr & 0xFF) // >> shift
         val sgi = (sgr & 0xFF) // >> shift
         val sbi = (src & 0xFF) // >> shift


         val dar = dst >> 24
         val drr = dst >> 16
         val dgr = dst >> 8
         val da = dar.toByte // & 0xFF
         val dr = drr.toByte // & 0xFF
         val dg = dgr.toByte // & 0xFF
         val db = dst.toByte // & 0xFF
         val dai = dar & 0xFF
         val dri = drr & 0xFF
         val dgi = dgr & 0xFF
         val dbi = dst & 0xFF

         val dai2 = da / 3
         val dri2 = dr / 3
         val dgi2 = dg / 3
         val dbi2 = db / 3

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

         val mr = math.max( 0, math.min( 0xFF, dri + (sr + 64) )) // & 0xFF
         val mg = math.max( 0, math.min( 0xFF, dgi + (sg + 64) )) // & 0xFF
         val mb = math.max( 0, math.min( 0xFF, dbi + (sb + 64) )) // & 0xFF

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

         i += 1
      }
   }

   /*

    */
   def dispose {}
}