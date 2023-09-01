package de.sciss.cupola.video

import actors.Actor

object TemporalHandle {
   case class Seek( source: AnyRef, secs: Double )
   case object Play
   case object Stop
   case object Dispose
}
trait TemporalHandle {
   import TemporalHandle._

   actor.start

   protected def actor : Actor

   def seek( source: AnyRef, secs: Double ) {
      actor ! Seek( source, secs )
   }

   def play() {
      actor ! Play
   }

   def stop() {
      actor ! Stop
   }

   def dispose() {
      actor ! Dispose
   }
}