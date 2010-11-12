package de.sciss.synth.proc.view

import javax.swing.{JViewport, Box, JPanel, ScrollPaneConstants => SPC, JScrollPane, JFrame}
import java.awt.{Dimension, BorderLayout, Color}
import de.sciss.synth.proc.{Period, Interval}

class OfflineVisualView {
   private var viewSpan = Interval( Period( 0.0 ), Period( 60.0 ))

   val (frame, axis) = {
      val f                = new JFrame( "Offline View" )
      val cp               = f.getContentPane()
      val timelineAxis     = new Axis( Axis.HORIZONTAL, Axis.TIMEFORMAT )
      timelineAxis.minimum = viewSpan.start.v
      timelineAxis.maximum = viewSpan.end.v
//      timelineAxis.setPreferredSize( new Dimension( 40, 40 ))
      val columnHeaderView = Box.createVerticalBox()
      val rowHeaderView    = Box.createVerticalBox()
      val tracksPanel      = new JScrollPane( SPC.VERTICAL_SCROLLBAR_ALWAYS, SPC.HORIZONTAL_SCROLLBAR_ALWAYS )
      tracksPanel.setBorder( null )
val viewPort = new JViewport()
      viewPort.setBackground( new Color( 0x28, 0x28, 0x28 ))
      tracksPanel.setViewport( viewPort )
//      viewPort.setBorder( null )
      tracksPanel.setCorner( SPC.UPPER_LEFT_CORNER, new JPanel() ) // fixes white background problem
      tracksPanel.setCorner( SPC.LOWER_LEFT_CORNER, new JPanel() ) // fixes white background problem
      tracksPanel.setCorner( SPC.UPPER_RIGHT_CORNER, new JPanel() ) // fixes white background problem
val timelinePanel = new JPanel()
      tracksPanel.setViewportView( timelinePanel )
      tracksPanel.setRowHeaderView( rowHeaderView )
      columnHeaderView.add( timelineAxis )
      tracksPanel.setColumnHeaderView( columnHeaderView )
      timelineAxis.viewPort = Some( tracksPanel.getColumnHeader )
      cp.add( tracksPanel, BorderLayout.CENTER )
      f.setSize( 600, 300 )
      f.setLocationRelativeTo( null )
      f.setVisible( true )
      (f, timelineAxis)
   }

   
}
