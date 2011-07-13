package de.sciss.cupola.video

import swing.{Dialog, Frame}

object Util {
   def displayError( parent: Frame, title: String, e: Throwable ) {
      Dialog.showMessage( Option( parent ).flatMap( _.contents.headOption ).orNull, "An error occurred:\n" +
         abbreviateStackTrace( e ), title, Dialog.Message.Error )
   }

   def abbreviateStackTrace( e: Throwable, maxLine: Int = 16 ) : String = {
      val sb      = new StringBuffer( e.getClass.getName + " : " + e.getLocalizedMessage + "\n" )
      val trace   = e.getStackTrace
      val num     = math.min( maxLine, trace.length )
      var i = 0; while( i < num ) {
         sb.append( "\tat " + trace( i ) + "\n" )
      i += 1 }
      if( trace.length > num ) {
         sb.append( "\t...\n" )
      }
      sb.toString
   }

   def unifiedLook( f: Frame ) {
      f.peer.getRootPane.putClientProperty( "apple.awt.brushMetalLook", java.lang.Boolean.TRUE )
   }

   def formatTimeString( secs: Double ) : String = {
      val millis0 = (secs * 1000).toInt
      val secs0   = millis0 / 1000
      val mins0   = secs0 / 60
      val millis  = millis0 % 1000
      val secs1   = secs0 % 60
      val mins    = mins0 % 60
      val hours   = mins0 / 60

      (hours  +  100).toString.substring( 1 ) + ":" +
      (mins   +  100).toString.substring( 1 ) + ":" +
      (secs1  +  100).toString.substring( 1 ) + "." +
      (millis + 1000).toString.substring( 1 ) + "  "
   }
}