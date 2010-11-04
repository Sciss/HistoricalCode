package de.sciss.dorianlaf

import javax.swing.plaf.synth.SynthLookAndFeel
import javax.swing._
import java.awt.{Font, BorderLayout, EventQueue}
import java.io.{File, BufferedInputStream}

object DorianLaF {
   def main( args: Array[ String ]) {
      EventQueue.invokeLater( new Runnable { def run = demo })
   }

   def demo {
      val syn = new SynthLookAndFeel()
      val clz = DorianLaF.getClass
      syn.load( clz.getResourceAsStream( "DorianLaF.xml" ), clz )
      UIManager.setLookAndFeel( syn )

      val f       = new JFrame( "Frame" )
      val cp      = f.getContentPane
      cp.setLayout( new BoxLayout( cp, BoxLayout.Y_AXIS ))
      cp.add( Box.createVerticalGlue )
      val lb      = new JLabel( "Label" )
      cp.add( lb )
      cp.add( Box.createVerticalStrut( 4 ))
//      val is   = new BufferedInputStream( clz.getResourceAsStream( "DejaVuSans.ttf" ))
//      val fnt  = Font.createFont( Font.TRUETYPE_FONT, is ).deriveFont( 14 )
      val fnt0    = Font.createFont( Font.TRUETYPE_FONT, new File( "/Users/rutz/Documents/devel/DorianLaF/src/main/resources/de/sciss/dorianlaf/CAFETA__.ttf" ))
      val fnt     = fnt0.deriveFont( 15f )
      val but1    = new JButton( "Button1" )
      but1.setFont( fnt )
      val but2    = new JButton( "Button2" )
      but2.setFont( fnt )
      val but3    = new JButton( "Button3" )
      but3.setFont( fnt )
      val butP    = Box.createHorizontalBox()
      butP.add( but1 )
      butP.add( Box.createHorizontalStrut( 4 ))
      butP.add( but2 )
      butP.add( Box.createHorizontalStrut( 4 ))
      butP.add( but3 )
      cp.add( butP )
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