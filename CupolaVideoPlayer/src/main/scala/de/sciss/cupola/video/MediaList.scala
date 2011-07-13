package de.sciss.cupola.video

import collection.breakOut
import collection.immutable.{IndexedSeq => IIdxSeq}
import xml.XML

object MediaList {
   def read( file: String ) : IIdxSeq[ Entry ] = {
      val xml = XML.loadFile( file )
      require( xml.label == "list" )
      (xml \ "entry").map( e => {
         val name = (e \ "@name").text
         val vid  = ((e \ "video").head \ "file").text
         val osc  = ((e \ "osc").head \ "file").text
         Entry( name, vid, osc )
      })( breakOut )
   }

   case class Entry( name: String, videoPath: String, oscPath: String )
}