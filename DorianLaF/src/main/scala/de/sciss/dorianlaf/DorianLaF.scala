/*
 *  DorianLaF.scala
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

import javax.swing.plaf.synth.SynthLookAndFeel
import javax.swing._
import java.awt.{Font, BorderLayout, EventQueue}
import java.io.{File, BufferedInputStream}
import org.jdesktop.laffy.Laffy

object DorianLaF {
   def main( args: Array[ String ]) {
      EventQueue.invokeLater( new Runnable { def run = args.headOption match {
         case Some( "--laffy" ) => laffyDemo
         case _ => demo2
      }})
   }

   def demo {
      val syn = new SynthLookAndFeel()
      val clz = DorianLaF.getClass
      syn.load( clz.getResourceAsStream( "DorianLaF.xml" ), clz )
      UIManager.setLookAndFeel( syn )
      createSomeWidgets
   }

   def laffyDemo {
      UIManager.installLookAndFeel( "Dorian", "de.sciss.dorianlaf.DorianLookAndFeel" )
//      Laffy.getInstance().load( Array.empty[ AnyRef ])

      // "invokeAndWait cannot be called from event thread"... sucky bastards
      new Thread( new Runnable { def run = Laffy.main( Array.empty[ String ])}).start
   }

   def demo2 {
      val laf = new DorianLookAndFeel
      UIManager.setLookAndFeel( laf )
      createSomeWidgets
   }

   def createSomeWidgets {
      val f       = new JFrame( "Frame" )
      val cp      = f.getContentPane
      cp.setLayout( new BoxLayout( cp, BoxLayout.Y_AXIS ))
      cp.add( Box.createVerticalGlue )
      val lb1     = new JLabel( "Label:" )
      val lb2     = new JLabel( "Booting..." )
      lb2.putClientProperty( "Dorian.mode", "lcd" )
      val butL    = Box.createHorizontalBox()
      butL.add( lb1 )
      butL.add( Box.createHorizontalStrut( 8 ))
      butL.add( lb2 )
      cp.add( butL )
      cp.add( Box.createVerticalStrut( 4 ))
//      val is   = new BufferedInputStream( clz.getResourceAsStream( "DejaVuSans.ttf" ))
//      val fnt  = Font.createFont( Font.TRUETYPE_FONT, is ).deriveFont( 14 )
//      val fnt0    = Font.createFont( Font.TRUETYPE_FONT, new File( "/Users/rutz/Documents/devel/DorianLaF/src/main/resources/de/sciss/dorianlaf/CAFETA__.ttf" ))
//      val fnt     = fnt0.deriveFont( 15f )
      val but1    = new JButton( "Button1" )
//      but1.setFont( fnt )
      val but2    = new JButton( "Button2" )
//      but2.setFont( fnt )
      but2.setEnabled( false )
      val but3    = new JButton( "Button3" )
//      but3.setFont( fnt )
      val butP    = Box.createHorizontalBox()
      butP.add( but1 )
      butP.add( Box.createHorizontalStrut( 4 ))
      butP.add( but2 )
      butP.add( Box.createHorizontalStrut( 4 ))
      butP.add( but3 )
      cp.add( butP )
      cp.add( Box.createVerticalStrut( 4 ))
      val progDet1 = new JProgressBar()
      progDet1.setValue( 33 )
      cp.add( progDet1 )
      cp.add( Box.createVerticalStrut( 4 ))
      val progDet2 = new JProgressBar()
      progDet2.setValue( 70 )
      progDet2.setString( "Progress..." )
      progDet2.setStringPainted( true )
      cp.add( progDet2 )
      cp.add( Box.createVerticalStrut( 4 ))
      val progIndet = new JProgressBar()
      progIndet.setIndeterminate( true )
      cp.add( progIndet )
      cp.add( Box.createVerticalStrut( 4 ))
      val list = new JList( Array[ AnyRef ]( "List", "Item 2", "Item 3" ))
      cp.add( list )
      cp.add( Box.createVerticalStrut( 4 ))
      val toggle = new JToggleButton( "Toggle" )
      cp.add( toggle )
      cp.add( Box.createVerticalStrut( 4 ))
      val check   = new JCheckBox( "CheckBox" )
      cp.add( check )
      cp.add( Box.createVerticalStrut( 4 ))
      val slider  = new JSlider()
      cp.add( slider )
      cp.add( Box.createVerticalStrut( 4 ))
      val combo   = new JComboBox( Array[ AnyRef ]( "Combo", "Box" ))
      cp.add( combo )
      cp.add( Box.createVerticalGlue )
      f.setSize( 400, 400 )
      f.setLocationRelativeTo( null )
      f.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE )
      f.setVisible( true )
   }
}