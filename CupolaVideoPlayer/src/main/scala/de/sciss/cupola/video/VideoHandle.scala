package de.sciss.cupola.video

import java.net.URL
import com.xuggle.xuggler.{IStreamCoder, ICodec, IVideoResampler, IPixelFormat, IContainer}

object VideoHandle {
   def open( file: URL ) : VideoHandle = {
      val container = IContainer.make()
      require( container.open( file.toString, IContainer.Type.READ, null ) >= 0, "Could not open file: " + file )
      val numStreams = container.getNumStreams
      val dec = (0 until numStreams).map( container.getStream( _ ).getStreamCoder )
         .find( _.getCodecType == ICodec.Type.CODEC_TYPE_VIDEO )
         .getOrElse( sys.error( "Could not find video decoder for container: " + file ))

      require( dec.open() >= 0, "Could not open video decoder for container: "  + file )

      // if this stream is not in BGR24, we're going to need to convert it
      val resampler = if( dec.getPixelType != IPixelFormat.Type.BGR24 ) {
         val res = IVideoResampler.make( dec.getWidth, dec.getHeight, IPixelFormat.Type.BGR24,
            dec.getWidth, dec.getHeight, dec.getPixelType )
         require( res != null, "Could not create color space resampler for " + file )
         res
      } else null

      new VideoHandle( container, dec, resampler )
   }
}
class VideoHandle private (container: IContainer, dec: IStreamCoder, resampler: IVideoResampler ) {
   def width : Int   = dec.getWidth
   def height : Int  = dec.getHeight
}