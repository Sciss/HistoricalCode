package de.sciss.gui

import j.{PeakMeter => JPeakMeter, PeakMeterLike}
import swing.{Orientable, Orientation, Component}

class PeakMeter extends Component with PeakMeterLike {
   override lazy val peer: JPeakMeter = new JPeakMeter with SuperMixin

   def clearHold() { peer.clearHold() }
   def clearMeter() { peer.clearMeter() }
   def dispose() { peer.dispose() }

   def holdDecibels = peer.holdDecibels

   def holdDuration_=( millis: Int ) { peer.holdDuration = millis }
   def holdDuration = peer.holdDuration

   def holdPainted_=( b: Boolean ) { peer.holdPainted = b }
   def holdPainted = peer.holdPainted

   def orientation_=( value: Orientation.Value ) { peer.orientation = value.id }
   def orientation: Orientation.Value = Orientation( peer.orientation )

//   def orientation_=( orient: Int ) { peer.orientation = orient }
//   def orientation = 0

   def peak_=( value: Float ) { peer.peak = value }
   def peak = peer.peak

   def peakDecibels = peer.peakDecibels

   def refreshParent_=( b: Boolean ) { peer.refreshParent = b }
   def refreshParent = peer.refreshParent

   def rms_=( value: Float ) { peer.rms = value }
   def rms = peer.rms

   def rmsPainted_=( b: Boolean ) { peer.rmsPainted = b }
   def rmsPainted = peer.rmsPainted

   def ticks_=( num: Int ) { peer.ticks = num }
   def ticks = peer.ticks

   def update( peak: Float, rms: Float, time: Long ) = peer.update( peak, rms, time )
}