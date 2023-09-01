/*
 *  DorianLookAndFeel.scala
 *  (DorianLaF)
 *
 *  Copyright (c) 2010 Hanns Holger Rutz. All rights reserved.
 *
 *	 This software is free software; you can redistribute it and/or
 *	 modify it under the terms of the GNU General Public License
 *	 as published by the Free Software Foundation; either
 *	 version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	 This software is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	 General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public
 *	 License (gpl.txt) along with this software; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	 For further information, please contact Hanns Holger Rutz at
 *	 contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicLookAndFeel
import java.awt.{Color, Font}
import javax.swing.plaf._
import javax.swing.{BorderFactory, UIDefaults}

object DorianLookAndFeel {
   private val packageName = "de.sciss.dorianlaf.";
}

class DorianLookAndFeel extends BasicLookAndFeel /* MetalLookAndFeel */ {
   import DorianLookAndFeel._
   
   def getName                = "Dorian"
   def getID                  = "Dorian"
   def getDescription         = "Dorian Look and Feel"
   def isNativeLookAndFeel    = false
   def isSupportedLookAndFeel = true

   override protected def initClassDefaults( table: UIDefaults ) {
      super.initClassDefaults( table )

      val uiDefaults = Array[ AnyRef ](
         "LabelUI",        packageName + "LabelUI",
         "PanelUI",        packageName + "PanelUI",
         "ButtonUI",       packageName + "ButtonUI",
         "ToggleButtonUI", packageName + "ButtonUI",
         "CheckBoxUI",     packageName + "CheckBoxUI",
         "SliderUI",       packageName + "SliderUI",
         "ListUI",         packageName + "ListUI",
         "FileChooserUI",  packageName + "FileChooserUI", // laffy needs it...
         "ProgressBarUI",  packageName + "ProgressBarUI"
      )
      table.putDefaults( uiDefaults )
   }

//   private def createFontFromInputStream( name: String ) = {
//      val is   = DorianLookAndFeel.getClass.getResourceAsStream( name )
//      val f    = File.createTempFile( "dorian", ".tmp" )
//      val raf  = new RandomAccessFile( f, "rw" )
//      val buf  = new Array[ Byte ]( 8192 )
//      var succ = true
//      while( succ ) {
//         val num  = is.read( buf )
//         succ = num > 0
//         if( succ ) raf.write( buf, 0, num )
//      }
//      raf.close()
//      is.close()
//      Font.createFont( Font.TRUETYPE_FONT, f )
//   }

   override protected def initComponentDefaults( table: UIDefaults ) {
      super.initComponentDefaults( table )

//      println( "JO1 " + PanelBackgroundPainter.getClass.getClassLoader.getResource( "cafeta.ttf" ))
//      println( "JO2 " + PanelBackgroundPainter.getClass.getResourceAsStream( "glow2.png "))
//
//      // XXX the InputStream retrieved by getResourceAsStream causes
//      // a loading problem for some reason...
//      val fontBase = createFontFromInputStream( "cafeta.ttf" )

      val fontBase = Font.createFont( Font.TRUETYPE_FONT, DorianLookAndFeel.getClass.getResourceAsStream( "cafeta.ttf" ))

//      val fontBase = Font.createFont( Font.TRUETYPE_FONT,
//         new File( "/Users/rutz/Documents/devel/DorianLaF/src/main/resources/de/sciss/dorianlaf/CAFETA__.ttf" ))
      val dialogPlain15 = new FontUIResource( fontBase.deriveFont( 15f ))
      val dialogPlain16 = new FontUIResource( fontBase.deriveFont( 16f ))
      val colrLCDBg     = new ColorUIResource( 0x07, 0x10, 0x18 )

      val listCellRenderer = new UIDefaults.ActiveValue() {
         def createValue( table: UIDefaults ) : AnyRef = new ListCellRenderer
      }
//      val lcdBorder = new BorderUIResource( BorderFactory.createMatteBorder( 1, 1, 1, 1, colrLCDBg ))
      val emptyBorder = new BorderUIResource( BorderFactory.createEmptyBorder() )

      val uiDefaults = Array[ AnyRef ](
         "Button.border", new BorderUIResource( new ButtonBorder ), // packageName + "ButtonBorder"
         "Button.font", dialogPlain15,
         "Button.textForeground", new ColorUIResource( 30, 30, 30 ),
         "CheckBox.font", dialogPlain16,
         "CheckBox.foreground", new ColorUIResource( 0xFF, 0xFF, 0xFF ),
         "CheckBox.icon", new IconUIResource( new CheckBoxIcon ),
         "Label.font", dialogPlain16,
         "Label.foreground", new ColorUIResource( 0xFF, 0xFF, 0xFF ),
         "List.background", colrLCDBg,
         "List.font", dialogPlain16,
         "List.cellRenderer", listCellRenderer,
         "List.cellNoFocusBorder", emptyBorder,
         "List.focusSelectedCellHighlightBorder", null,
         "List.focusCellHighlightBorder", emptyBorder,
         "Dorian.lcdBackground", colrLCDBg,
         "Panel.startBackground", new ColorUIResource( 70, 70, 70 ),
         "Panel.endBackground", new ColorUIResource( 50, 50, 50 ),
         "ProgressBar.font", dialogPlain15,
         "ProgressBar.border", new BorderUIResource( BorderFactory.createLineBorder( Color.black ))
// XXX why this has no effect??
//         "Button.margin", new InsetsUIResource( 20, 30, 20, 30 )
      )
      table.putDefaults( uiDefaults )
   }
}