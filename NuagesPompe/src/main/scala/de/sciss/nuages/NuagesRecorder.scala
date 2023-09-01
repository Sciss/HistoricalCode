/*
 *  NuagesRecorder.scala
 *  (NuagesPompe)
 *
 *  Copyright (c) 2010-2012 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either
 *  version 2, june 1991 of the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License (gpl.txt) along with this software; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.nuages

import java.text.SimpleDateFormat
import java.io.File
import de.sciss.synth.io.{SampleFormat, AudioFileType}
import de.sciss.synth.ugen.{Silent, DiskOut, In}
import java.util.{Date, Locale}
import de.sciss.synth.proc.{DSL, ProcDemiurg, RichGroup, ProcTxn, Ref, Proc}
import de.sciss.synth.{AudioBus, Server, addAfter, Group}

object NuagesRecorder {
   private val df = new SimpleDateFormat( "'rec'yyMMdd'_'HHmmss'.irc'", Locale.US )

   val DefaultFilenameGenerator = (s: Settings) => {
      new File( s.folder, df.format( new Date() ))
   }

   sealed trait SettingsLike {
      def folder : File
      def filenameGenerator : Settings => File
      def fileType : AudioFileType
      def sampleFormat : SampleFormat
      def bus : AudioBus // RichAudioBus
   }

   final case class SettingsBuilder() extends SettingsLike {
      var folder : File = {
         val home    = new File( sys.props( "user.home" ))
         val desktop = new File( home, "Desktop" )
         if( desktop.isDirectory ) desktop else home
      }
      var filenameGenerator : Settings => File = DefaultFilenameGenerator
      var fileType : AudioFileType = AudioFileType.IRCAM
      var sampleFormat : SampleFormat = SampleFormat.Float
      var bus : AudioBus = AudioBus( Server.default, 0, 2 ) // RichAudioBus = RichBus.soundOut( Server.default, 2 )

      def build : Settings = SettingsImpl( folder, filenameGenerator, fileType, sampleFormat, bus )
   }

   private final case class SettingsImpl( folder: File, filenameGenerator: Settings => File,
                                          fileType: AudioFileType, sampleFormat: SampleFormat,
                                          bus: /* Rich */ AudioBus )
   extends Settings

   object Settings {
      implicit def fromBuilder( b: SettingsBuilder ) : Settings = b.build
   }
   sealed trait Settings extends SettingsLike

   def apply( settings: Settings = SettingsBuilder().build ) : NuagesRecorder = new NuagesRecorder( settings )
}
class NuagesRecorder private ( val settings: NuagesRecorder.Settings ) {
   private val mitRef = Ref( Option.empty[ Proc ])

   def start( implicit tx: ProcTxn ) : Boolean = {
      if( mitRef().isDefined ) return false
      val fact = ProcDemiurg.factories.find( _.name == "$mitschnitt" ).getOrElse {
         import DSL._
//         val res = diff( "$mitschnitt" ) {
//            pAudioIn( "in" ) // , Some( settings.bus )
//            graph { in: In =>
//               val recPath = settings.filenameGenerator( settings )
//               val buf     = bufRecord( recPath.getAbsolutePath, in.numChannels, settings.fileType, settings.sampleFormat )
//               DiskOut.ar( buf.id, in )
//               Silent.ar   // ouch
//            }
//         }
         val res = gen( "$mitschnitt" ) {
            graph {
               val in      = In.ar( settings.bus.index, settings.bus.numChannels )
               val recPath = settings.filenameGenerator( settings )
               val buf     = bufRecord( recPath.getAbsolutePath, in.numChannels, settings.fileType, settings.sampleFormat )
               DiskOut.ar( buf.id, in )
               Silent.ar   // ouch
            }
         }
         ProcDemiurg.addFactory( res )
         res
      }
      val p = fact.make
      val g = RichGroup( Group( settings.bus.server ))
      g.play( RichGroup.default( settings.bus.server ), addAfter )
      p.group = g
      p.play
//      p.audioInput( "in" ).bus = Some( settings.bus )
      mitRef.set( Some( p ))
      true
   }

   def stop( implicit tx: ProcTxn ) : Boolean = {
      mitRef.swap( None ) match {
         case Some( p ) =>
            p.stop
            p.dispose
            p.group.free( false )
            true
         case None => false
      }
   }
}