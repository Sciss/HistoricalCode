package de.sciss.cupola.video

import actors.DaemonActor
import de.sciss.osc.{OSCClient, OSCTransport}
import java.net.SocketAddress
import swing.Swing

object OSCHandle {
   private case object Connect
   private case object Disconnect

   def apply( stream: OSCStream, target: SocketAddress, transport: OSCTransport ) : OSCHandle = {
      val client = OSCClient( transport )
      client.target = target
      new OSCHandle( stream, client )
   }
}
class OSCHandle private( stream: OSCStream, client: OSCClient ) extends TemporalHandle {
   import OSCHandle._

   @volatile private var timeViewVar = (source: AnyRef, secs: Double, playing: Boolean) => ()
   def timeView = timeViewVar
   def timeView_=( fun: (AnyRef, Double, Boolean) => Unit ) { timeViewVar = fun }

   protected val actor = new DaemonActor {
      import TemporalHandle._
      def act() {
         var open = true
         loopWhile( open ) { react {
            case Seek( source, secs ) =>
            case Play =>
            case Stop =>
            case Connect =>
               try {
                  client.start
               } catch {
                  case e => Swing.onEDT( Util.displayError( null, "Connect OSC", e ))
               }
            case Disconnect =>
               try {
                  client.stop
               } catch {
                  case e => Swing.onEDT( Util.displayError( null, "Disconnect OSC", e ))
               }
            case Dispose => open = false
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