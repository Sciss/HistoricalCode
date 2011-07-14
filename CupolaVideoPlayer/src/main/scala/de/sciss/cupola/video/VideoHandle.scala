package de.sciss.cupola.video

import java.net.URL
import com.xuggle.xuggler.video.{IConverter, ConverterFactory}
import swing.Label
import actors.{TIMEOUT, DaemonActor}
import com.xuggle.xuggler.{Global, Utils, IVideoPicture, IPacket, IStream, IStreamCoder, ICodec, IVideoResampler, IPixelFormat, IContainer}

object VideoHandle {
   def open( file: URL ) : VideoHandle = {
      val container = IContainer.make()
      require( container.open( file.toString, IContainer.Type.READ, null ) >= 0, "Could not open file: " + file )
      val numStreams = container.getNumStreams
      val (streamIdx, dec) = (0 until numStreams).map( i => i -> container.getStream( i ).getStreamCoder )
         .find( _._2.getCodecType == ICodec.Type.CODEC_TYPE_VIDEO )
         .getOrElse( sys.error( "Could not find video decoder for container: " + file ))

      require( dec.open() >= 0, "Could not open video decoder for container: "  + file )

      val pixType       = dec.getPixelType
      val convName      = "native_to_" + pixType.name
      val width         = dec.getWidth
      val height        = dec.getHeight
      val (conv, resampler: IVideoResampler) = pixType match {
         case IPixelFormat.Type.BGR24 =>
            ConverterFactory.createConverter( ConverterFactory.XUGGLER_BGR_24, pixType, width, height ) -> null
         case IPixelFormat.Type.ARGB  =>
            ConverterFactory.createConverter( ConverterFactory.XUGGLER_ARGB_32, pixType, width, height ) -> null
         case _ =>
            val res = IVideoResampler.make( width, height, IPixelFormat.Type.BGR24, width, height, pixType )
            require( res != null, "Could not create color space resampler for " + file )
            ConverterFactory.createConverter( ConverterFactory.XUGGLER_BGR_24, IPixelFormat.Type.BGR24, width, height ) -> res
      }

      new VideoHandle( container, streamIdx, dec, conv, resampler )
   }
}
class VideoHandle private (container: IContainer, streamIdx: Int, dec: IStreamCoder,
                           conv: IConverter, resampler: IVideoResampler ) {
   @volatile private var videoViewVar : ImageView = null
//   @volatile private var timeViewVar : Label = null
   @volatile private var timeViewVar = (secs: Double, playing: Boolean) => ()

   def videoView = Option( videoViewVar )
   def videoView_=( v: Option[ ImageView ]) { videoViewVar = v.orNull }

//   def timeView = Option( timeViewVar )
//   def timeView_=( v: Option[ Label ]) { timeViewVar = v.orNull }

   def timeView = timeViewVar
   def timeView_=( fun: (Double, Boolean) => Unit ) { timeViewVar = fun }

   def width : Int   = dec.getWidth
   def height : Int  = dec.getHeight

   private val stream    = container.getStream( streamIdx )
   private val timeBase  = stream.getTimeBase.getDouble

//println( " TIME BASE = " + timeBase + " dur = " + stream.getDuration )

   private case class Seek( secs: Double )
   private case object Play
   private case object Stop
   private case object Dispose

   private object VideoActor extends DaemonActor {
      start

      def act() {
         var time    = 0L
         val packet  = IPacket.make()
         val picIn   = IVideoPicture.make( dec.getPixelType, width, height )
         val picOut  = if( resampler == null ) picIn else
            IVideoPicture.make( resampler.getOutputPixelFormat, width, height )

         var vidCurrent = Global.NO_PTS

         def tryRead() : Boolean = {
            (container.readNextPacket( packet ) >= 0 && (packet.getStreamIndex == streamIdx)) && {
               var offset  = 0
               var picDone = false
               val pSize   = packet.getSize
               while( !picDone && offset < pSize ) {
                  val num = dec.decodeVideo( picIn, packet, 0 )
                  if( num >= 0 ) {
                     offset  += num
                     picDone  = picIn.isComplete
                  } else {
                     offset   = pSize // error -- break loop
                  }
               }
               val succ = picDone && (resampler == null) || {
                  resampler.resample( picOut, picIn ) >= 0 && picOut.getPixelType == IPixelFormat.Type.BGR24
               }
               if( succ ) {
                  vidCurrent = picIn.getTimeStamp   // WARNING : this is microseconds, not re timebase !!!
               }
               succ
            }
         }

         def aDisplay( secs: Double, playing: Boolean ) {
            val vv = videoViewVar
            if( vv != null ) vv.image = conv.toImage( picOut )
//            val tv = timeViewVar
//            if( tv != null ) {
////println( Util.formatTimeString( secs ))
//               tv.text = Util.formatTimeString( secs )
//            }
            timeViewVar( secs, playing )
         }

         def aSeek( secs: Double ) : Boolean = {
            val succ = aSeekNoDisplay( secs )
            if( succ ) aDisplay( secs, false )
            succ
         }

         def aSeekNoDisplay( secs: Double ) : Boolean = {
            time = (secs / timeBase).toLong
            container.seekKeyFrame( streamIdx, time, IContainer.SEEK_FLAG_BACKWARDS ) // .SEEK_FLAG_ANY )
            tryRead()
         }

         var open = true

         def aDispose() {
            try {
               dec.close()
               container.close()
            } finally {
               open = false
            }
         }

         loopWhile( open ) { react {
            case Seek( secs ) => aSeek( secs )

            case Play =>
               if( vidCurrent != Global.NO_PTS || aSeekNoDisplay( 0.0 )) {
                  var playing    = true
                  var delay      = 0L
                  val sysStart   = System.currentTimeMillis()
                  val vidStart   = vidCurrent

                  def displayCurrent() {
//                     aDisplay( (vidCurrent - vidStart) * 1.0e-6, true )
                     aDisplay( vidCurrent * 1.0e-6, true )  // XXX assumes stream begins at zero!
                  }

                  loopWhile( playing ) { reactWithin( delay ) {
                     case TIMEOUT =>
                        displayCurrent()
                        playing = tryRead()
                        if( playing ) {
                           val sysCurrent = System.currentTimeMillis()
                           val sysMillis  = sysCurrent - sysStart
//println( "dv " + (vidCurrent - vidStart) )
                           val vidMillis = ((vidCurrent - vidStart) * 1.0e-3).toLong
                           delay = math.max( 0L, vidMillis - sysMillis )
                        }

                     case Stop =>
                        displayCurrent()
                        playing = false
                     case Seek( secs ) =>
                        playing = false
                        aSeek( secs )
                     case Dispose =>
                        playing = false
                        aDispose()
                  }}
               }

            case Dispose => aDispose()
         }}
      }
   }

   def seek( secs: Double ) {
      VideoActor ! Seek( secs )
   }

   def play() {
      VideoActor ! Play
   }

   def stop() {
      VideoActor ! Stop
   }

   def dispose() {
      VideoActor ! Dispose
   }
}