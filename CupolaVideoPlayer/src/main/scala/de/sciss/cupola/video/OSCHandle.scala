package de.sciss.cupola.video

import java.net.SocketAddress
import swing.Swing
import actors.{TIMEOUT, DaemonActor}
import de.sciss.osc.{OSCBundle, OSCClient, OSCTransport}

object OSCHandle {
   private case object Connect
   private case object Disconnect

   def apply( stream: OSCStream, target: SocketAddress, transport: OSCTransport ) : OSCHandle = {
      val client = OSCClient( transport )
      client.target = target
      new OSCHandle( stream, client )
   }
}
class OSCHandle private( val stream: OSCStream, client: OSCClient ) extends TemporalHandle {
   handle =>

   import OSCHandle._

   @volatile private var bundleHitViewVar = (idx: Int) => ()
   def bundleHitView = bundleHitViewVar
   def bundleHitView_=( fun: (Int) => Unit ) { bundleHitViewVar = fun }

   protected lazy val actor = new DaemonActor {
      import TemporalHandle._
      def act() {
         var open    = true
         var oscIdx  = -2
         var oscStart = 0.0

         def aConnect() {
            try {
               client.start
//println( "CLIENT CONNECTED" )
            } catch {
               case e => Swing.onEDT( Util.displayError( null, "Connect OSC", e ))
            }
         }

         def aDisconnect() {
            try {
               client.stop
//println( "CLIENT DISCONNECTED" )
            } catch {
               case e => Swing.onEDT( Util.displayError( null, "Disconnect OSC", e ))
            }
         }

         def aSeek( source: AnyRef, secs: Double ) {
            oscStart    = secs
            val tag     = OSCBundle.secsToTimetag( secs )
            val pos0    = Util.binarySearch( stream.bundles, tag )( OSCStream.bundleToTag )
            oscIdx      = if( pos0 >= 0 ) pos0 else -(pos0 + 1)   // the _next_ one
         }

         def aDispose() {
            open = false
         }

         loopWhile( open ) { react {
            case Seek( source, secs ) => aSeek( source, secs )
            case Play =>
               if( oscIdx == -2 ) aSeek( handle, 0.0 )

               var playing    = true
               var delay      = 0L
               val sysStart   = System.currentTimeMillis()
//               val oscStart   = oscCurrent

               def calcDelay() {
                  val p          = stream.bundles( oscIdx )
                  val sysCurrent = System.currentTimeMillis()
                  val sysMillis  = sysCurrent - sysStart
                  val oscCurrent = OSCBundle.timetagToSecs( p.timetag )
                  val oscMillis  = ((oscCurrent - oscStart) * 1000).toLong
                  delay = math.max( 0L, oscMillis - sysMillis )
               }

               calcDelay()
               loopWhile( playing ) { reactWithin( delay ) {
                  case TIMEOUT =>
                     if( client.isActive ) try {
                        client ! stream.bundles( oscIdx )
                     } catch { case e => println( e )}

                     bundleHitViewVar( oscIdx )

                     oscIdx += 1
                     if( oscIdx < stream.bundles.size ) {
                        calcDelay()
                     } else {
                        playing = false
                     }

                  case Stop => playing = false
                  case Connect => aConnect()
                  case Disconnect => aDisconnect()
                  case Seek( source, secs ) =>
                     playing = false
                     aSeek( source, secs )
                  case Dispose =>
                     playing = false
                     aDispose()
               }}

            case Stop =>
            case Connect => aConnect()
            case Disconnect => aDisconnect()
            case Dispose => aDispose()
         }}
      }
   }

   def connect() {
      actor ! Connect
   }

   def disconnect() {
      actor ! Disconnect
   }
}