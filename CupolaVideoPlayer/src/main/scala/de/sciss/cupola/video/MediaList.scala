package de.sciss.cupola.video

import collection.breakOut
import collection.immutable.{IndexedSeq => IIdxSeq}
import xml.XML

object MediaList {
   def read( file: String ) : IIdxSeq[ Entry ] = {
      val xml = XML.loadFile( file )
      require( xml.label == "list" )
      (xml \ "entry").map( e => {
         val name       = (e \ "@name").text
         val vidFile    = ((e \ "video").head \ "file").text
         val oscNode    = (e \ "osc").head
         val oscFile    = (oscNode \ "file").text
         val oscOffset  = {
            val txt = (oscNode \ "offset").text
            if( txt == "" ) 0.0 else txt.toDouble
         }
         Entry( name, vidFile, oscFile, oscOffset )
      })( breakOut )
   }

   case class Entry( name: String, videoPath: String, oscPath: String, oscOffset: Double )
}