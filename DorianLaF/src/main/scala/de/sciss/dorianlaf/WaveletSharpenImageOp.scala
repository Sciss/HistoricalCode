package de.sciss.dorianlaf

import java.awt.{Rectangle, RenderingHints}
import java.awt.geom.{Point2D, Rectangle2D}
import java.awt.image._

class WaveletSharpenImageOp extends BufferedImageOp {
   op =>

   def getRenderingHints : RenderingHints = null
   def getBounds2D( src: BufferedImage ) : Rectangle2D = new Rectangle( 0, 0, src.getWidth(), src.getHeight() )

   def getPoint2D( srcPt: Point2D, dstPt: Point2D ) : Point2D = {
      val p = if( dstPt != null ) dstPt else new Point2D.Float()
      p.setLocation( srcPt )
      p
   }

   def createCompatibleDestImage( src: BufferedImage, dstCM: ColorModel ) : BufferedImage = {
       val cm = if( dstCM != null ) dstCM else src.getColorModel()
       new BufferedImage( cm, cm.createCompatibleWritableRaster( src.getWidth(), src.getHeight() ),
          cm.isAlphaPremultiplied(), null )
   }

//    def createCompatibleDestImage( src: BufferedImage, destCM: ColorModel ) : BufferedImage = {
//      val w = src.getWidth()
//      val h = src.getHeight()
//
//      val (wr, cm) = if( destCM == null ) {
//         src.getColorModel() match {
//            case icm: IndexColorModel => {
//               val dcm = ColorModel.getRGBdefault()
//               dcm.createCompatibleWritableRaster( w, h ) -> dcm
//            }
//            case scm => src.getData().createCompatibleWritableRaster( w, h ) -> scm
//         }
//      } else destCM.createCompatibleWritableRaster( w, h ) -> destCM
//
//      new BufferedImage( cm, wr, cm.isAlphaPremultiplied(), null )
//   }

//   def filter( src: BufferedImage, dst: BufferedImage ) : BufferedImage = {
//      require( src != null, "Source image cannot be null" )
//      require( src != dst, "Source and destination image cannot be the same" )
//
//      val (srcC, srcCM) = src.getColorModel() match {
//         case icm: IndexColorModel => {
//            val conv = icm.convertToIntDiscrete( src.getRaster(), false )
//            conv -> conv.getColorModel()
//         }
//         case x => src -> x
//      }
//
//      val (res, dstC, dstCM, convert) = if( dst == null ) {
//         val i = createCompatibleDestImage( srcC, null )
//         (i, i, srcC, false)
//      } else {
//         val dstCM   = dst.getColorModel()
//         val convert = srcCM.getColorSpace().getType() != destCM.getColorSpace().getType()
//         val i = dstCM match {
//            case _: IndexColorModel | convert => createCompatibleDestImage( srcC, null )
//            case _ => dst
//         }
//         (dst, i, i.getColorModel(), convert)
//      }
//
//      wavelet( srcC, dstC )
//
//       if( convert ) {
//           val ccop = new ColorConvertOp( hints )
//           ccop.filter( dstC, res )
//       } else if( res != dstC ) {
//           val g2 = res.createGraphics()
//           g2.drawImage( dstC, 0, 0, null )
//           g2.dispose()
//       }
//
//       res
//   }

   def filter( src: BufferedImage, dst: BufferedImage ) : BufferedImage = {
      val w    = src.getWidth()
      val h    = src.getHeight()
      val num  = w * h

      val dstC = if( dst != null ) dst else createCompatibleDestImage( src, null )

//      val srcPix  = new Array[ Int ]( num )
//      val dstPix  = new Array[ Int ]( num )
//      getRGB( src, 0, 0, width, height, inPixels )
//
////      wavelet(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
//
//      setRGB( dstC, 0, 0, w, h, dstPix )
      dstC
   }
}