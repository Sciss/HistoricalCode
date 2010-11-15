/*
 *  OfflineVisualView.scala
 *  (SoundProcesses)
 *
 *  Copyright (c) 2009-2010 Hanns Holger Rutz. All rights reserved.
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

package de.sciss.synth.proc.view

import javax.swing.{JViewport, Box, JPanel, ScrollPaneConstants => SPC, JScrollPane, JFrame}
import java.awt.{Dimension, BorderLayout, Color}
import de.sciss.synth.proc._

class OfflineVisualView[ C <: PTemporalLike ]( g: ProcGroup[ C ], nav: ContextNavigator[ C ]) {
   private var viewSpan = Interval( Period( 0.0 ), Period( 60.0 ))

   val (frame, axis) = {
//      val l = Model.filterOnCommit[ C, ProcGroup.Update[ C ]]( (_, c) => nav.isApplicable( c ))( tr =>
//         defer( tr.foreach {
//            case ProcGroup.ProcAdded( p )   => add( p )
//            case ProcGroup.ProcRemoved( p ) => remove( p )
//         }))

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
