/*
 *  GroupView.scala
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

import javax.swing._
import event.{AncestorEvent, AncestorListener, ListSelectionListener, ListSelectionEvent}
import java.awt.{EventQueue, BorderLayout}
import java.awt.event.{ActionListener, ActionEvent, WindowEvent, WindowAdapter}
import GUIUtils._
import de.sciss.synth.proc.{Cursor, Model, Factory, Proc, Ctx, EphemeralSystem => Eph, ProcGroup}

class GroupView[ C ]( g: ProcGroup[ C ], csr: Cursor[ C ]) {
   private val listModel  = new DefaultListModel()
   private val list       = new JList( listModel )

   val frame = {
//      val l = g.listener { implicit c => {
//         case ProcGroup.ProcAdded( p )   /* XXX if( nav.isApplicable( c )) */ => defer( add( p ))    // XXX on txn commit
//         case ProcGroup.ProcRemoved( p ) /* XXX if( nav.isApplicable( c )) */ => defer( remove( p )) // XXX on txn commit
//      }}

      val l = Model.filterOnCommit[ C, ProcGroup.Update[ C ]]( (_, c) => csr.isApplicable( c ))( tr =>
         defer( tr.foreach {
            case ProcGroup.ProcAdded( p )   => add( p )
            case ProcGroup.ProcRemoved( p ) => remove( p )
         }))

      val f          = new JFrame( "Group : " + g.name )
      val cp         = f.getContentPane()
      list.setVisibleRowCount( 5 )
      list.setPrototypeCellValue( "Halochila" )
      val butAdd     = new JButton( "Add" )
      butAdd.addActionListener( new ActionListener {
         def actionPerformed( e: ActionEvent ) = userAddProc
      })
      val butRemove  = new JButton( "Remove" )
      butRemove.addActionListener( new ActionListener {
         def actionPerformed( e: ActionEvent ) = userRemoveProc
      })
      butRemove.setEnabled( false )
      list.addListSelectionListener( new ListSelectionListener {
         def valueChanged( e: ListSelectionEvent ) {
            butRemove.setEnabled( list.getSelectedIndex() >= 0 )
         }
      })
      val butPane    = Box.createHorizontalBox()
//      butPane.add( Box.createHorizontalStrut( 4 ))
      butPane.add( butAdd )
//      butPane.add( Box.createHorizontalStrut( 4 ))
      butPane.add( butRemove )
//      butPane.add( Box.createHorizontalStrut( 4 ))
      butPane.add( Box.createHorizontalGlue() )

//      cp.add( nav.view, BorderLayout.NORTH )
      cp.add( new JScrollPane( list ), BorderLayout.CENTER )
      cp.add( butPane, BorderLayout.SOUTH )

      ancestorAction( list ) {
         csr.t { implicit c =>
            addFull( g.all )
            g.addListener( l )
         }
      } {
         Eph.t { implicit c => g.removeListener( l )}
      }

      f.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE )
//      f.addWindowListener( new WindowAdapter {
//         override def windowClosed( w: WindowEvent ) {
//            Eph.t( implicit c => g.removeListener( l ))
//         }
//      })
      f.pack()
      f.setLocationRelativeTo( null )
      f.setVisible( true )
      f
   }

   private def addFull( ps: Traversable[ Proc[ C ]]) {
      listModel.removeAllElements()
      ps.foreach( listModel.addElement( _ ))
   }

   private def add( p: Proc[ C ]) {
      listModel.addElement( p )
   }

   private def remove( p: Proc[ C ]) {
      listModel.removeElement( p )
   }

   private def userAddProc {
      val name = JOptionPane.showInputDialog( "Enter name of new proc" )
      if( name == null ) return
      
      csr.t { implicit c =>
         g.add( Factory.proc( name ))
      }
   }

   private def userRemoveProc {
      val procs = list.getSelectedValues().collect { case p: Proc[ _ ] => p.asInstanceOf[ Proc[ C ]]}
      csr.t { implicit c =>
         procs.foreach( g.remove( _ ))
      }
   }
}