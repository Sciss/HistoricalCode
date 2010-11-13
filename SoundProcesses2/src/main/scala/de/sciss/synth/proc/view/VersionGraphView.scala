/*
 *  VersionGraphView.scala
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

import javax.swing.event.{AncestorListener, AncestorEvent}
import de.sciss.synth.proc.{Model, Ctx, Bitemporal, BitemporalSystem, EphemeralSystem => Eph}
import prefuse.action.assignment.ColorAction
import prefuse.action.layout.graph.ForceDirectedLayout
import prefuse.visual.VisualItem
import prefuse.util.ColorLib
import prefuse.activity.Activity
import prefuse.action.{RepaintAction, ActionList}
import prefuse.{Constants, Visualization, Display}
import prefuse.visual.expression.InGroupPredicate
import prefuse.render.{DefaultRendererFactory, EdgeRenderer, LabelRenderer}
import java.awt.{Color, BorderLayout, EventQueue}
import collection.breakOut
import collection.immutable.{IntMap, Stack => IStack}
import prefuse.controls._
import prefuse.data.event.TupleSetListener
import prefuse.data.tuple.TupleSet
import prefuse.data.{Tuple, Node, Graph}
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JComponent, JButton, WindowConstants, JFrame}
import java.beans.{PropertyChangeListener, PropertyChangeEvent}
import de.sciss.confluent._

class VersionGraphView {
   private val grpGraph    = "graph"
   private val grpNodes    = "graph.nodes"
   private val grpEdges    = "graph.edges"
   private val actColor    = "color"
   private val actLayout   = "layout"
   private val colPath     = "path"
   private val vis         = new Visualization()
   private val g           = new Graph()

   private var nodeMap     = IntMap.empty[ Node ]

   private val selListeners   = new JComponent {
      type S      = List[ VersionPath ]
      var oldSel: S = Nil
      val propSel = "selection"
      
      def newSelection( sel: S ) {
         val o    = oldSel
         oldSel   = sel
         firePropertyChange( propSel, o, sel )
      }

      def addL( fun: S => Unit ) {
         val l = new SelectionActionListener( fun )
         addPropertyChangeListener( propSel, l )
      }

      def removeL( fun: S => Unit ) {
         val l = new SelectionActionListener( fun )
         removePropertyChangeListener( propSel, l )
      }

      private case class SelectionActionListener( fun: S => Unit )
      extends PropertyChangeListener {
         def propertyChange( e: PropertyChangeEvent ) {
            fun( e.getNewValue().asInstanceOf[ S ])
         }
      }
   }

   val panel : JComponent = {
//      val f    = new JFrame( "Version Graph" )
//      val cp   = f.getContentPane()
      val sys  = BitemporalSystem   // XXX could as well be KTemporal
      
      val l = new Model.Listener[ Bitemporal, Bitemporal.Update ] {
         def updated( u: Bitemporal.Update )( implicit c: Ctx[ Bitemporal ]) {
            u match {
               case Bitemporal.NewBranch( oldPath, newPath ) => defer( add( oldPath.version, newPath ))   // XXX on txn commit
            }
         }
      }

//      val colLabel      = "label"
      val display       = new Display( vis )
      val vg            = vis.addGraph( grpGraph, g )
      val nodes         = g.getNodeTable()
      g.addColumn( VisualItem.LABEL, classOf[ String ])
      g.addColumn( colPath, classOf[ Vector[ Version ]])

      // colors
      val colrGray   = ColorLib.rgb( 0xC0, 0xC0, 0xC0 )
      val colrFocus  = ColorLib.rgb( 0x60, 0x60, 0xC0 )
      val colrBlack  = ColorLib.rgb( 0x00, 0x00, 0x00 )
      val colrWhite  = ColorLib.rgb( 0xFF, 0xFF, 0xFF )
      val actionNodeStroke = new ColorAction( grpNodes, VisualItem.STROKECOLOR, colrGray )
      val actionNodeFill   = new ColorAction( grpNodes, VisualItem.FILLCOLOR,   colrGray )
      val actionTextColor  = new ColorAction( grpNodes, VisualItem.TEXTCOLOR,   colrBlack )
      val actionEdgeColor  = new ColorAction( grpEdges, VisualItem.STROKECOLOR, colrGray )
      val predFocus        = new InGroupPredicate( Visualization.FOCUS_ITEMS )
      actionNodeFill.add( predFocus, colrFocus )
      actionNodeStroke.add( predFocus, colrFocus )
      actionTextColor.add( predFocus, colrWhite )

      val lay = new ForceDirectedLayout( grpGraph )
      val nodeRenderer = new LabelRenderer( VisualItem.LABEL )
      val edgeRenderer = new EdgeRenderer( Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD )
      nodeRenderer.setRoundedCorner( 4, 4 )
      val rf = new DefaultRendererFactory( nodeRenderer )
      rf.add( new InGroupPredicate( grpEdges), edgeRenderer )
      vis.setRendererFactory( rf )

      // fix selected focus nodes
      val focusGroup = vis.getGroup( Visualization.FOCUS_ITEMS )
      focusGroup.addTupleSetListener( new TupleSetListener {
          def tupleSetChanged( ts: TupleSet, add: Array[ Tuple ], remove: Array[ Tuple ]) {
             selListeners.newSelection( selection )
             vis.run( actColor )
          }
      })

      // quick repaint
      val actionColor = new ActionList()
      actionColor.add( actionTextColor )
      actionColor.add( actionNodeStroke )
      actionColor.add( actionNodeFill )
      actionColor.add( actionEdgeColor )
      vis.putAction( actColor, actionColor )

      val actionLayout = new ActionList( Activity.INFINITY, 50 )
      actionLayout.add( lay )
      actionLayout.add( new RepaintAction() )
      vis.putAction( actLayout, actionLayout )
      vis.alwaysRunAfter( actColor, actLayout )

      display.addControlListener( new ZoomToFitControl() )
      display.addControlListener( new WheelZoomControl() )
      display.addControlListener( new PanControl() )
      display.addControlListener( new DragControl() )
      display.addControlListener( new FocusControl( 1, actColor ))

      edgeRenderer.setHorizontalAlignment1( Constants.CENTER )
      edgeRenderer.setHorizontalAlignment2( Constants.CENTER )
      edgeRenderer.setVerticalAlignment1( Constants.CENTER )
      edgeRenderer.setVerticalAlignment2( Constants.CENTER )

      display.setForeground( Color.WHITE )
      display.setBackground( Color.BLACK )

      display.addAncestorListener( new AncestorListener {
         def ancestorAdded( e: AncestorEvent ) {
            stopAnimation
            try {
               g.clear()
               nodeMap = nodeMap.empty
               Eph.t { implicit c =>
                  addFull( sys.dag )
                  sys.addListener( l )
               }
            } finally {
               startAnimation
            }
         }
         def ancestorRemoved( e: AncestorEvent ) {
            stopAnimation
            Eph.t { implicit c => sys.removeListener( l )}
         }
         def ancestorMoved( e: AncestorEvent ) {}
      })

      display.setSize( 300, 300 )
//      cp.add( display, BorderLayout.CENTER )
//      f.setSize( 300, 300 )
//      f.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE )
//      f.setLocationRelativeTo( null )
//      f.setVisible( true )
//      f
      display
   }

   def addSelectionListener( fun: List[ VersionPath ] => Unit ) {
      selListeners.addL( fun )
   }

   def removeSelectionListener( fun: List[ VersionPath ] => Unit ) {
      selListeners.removeL( fun )
   }

   def selection : List[ VersionPath ] = {
      val tupT = vis.getGroup( Visualization.FOCUS_ITEMS ).tuples.asInstanceOf[ java.util.Iterator[ Tuple ]] 
      val iter = collection.JavaConversions.asIterator( tupT )
      iter.map( t => VersionPath.wrap( t.get( colPath ).asInstanceOf[ Vector[ Version ]])).toList
   }

   private def stopAnimation {
      vis.cancel( actColor )
      vis.cancel( actLayout )
   }

   private def startAnimation {
      vis.run( actColor )
   }

   private def defer( thunk: => Unit ) { EventQueue.invokeLater( new Runnable { def run = thunk })}

   private def addFull( trie: LexiTrie[ OracleMap[ VersionPath ]]) {
//               full.inspect
//               full.sub.keys.toList.sorted
      trie.sub.foreach { tup =>
         val (key, trie2) = tup
         val tree = trie2.value.get.entries.head._1.tree // woops, elegant it's not...
         // to reconstruct the tree from pre- and post-order traversal, the
         // following algorithm can be used:
         // - the root is the first element of the pre-order (this element will be called pred)
         // - let there be an empty stack
         // (1) - advance to the element next to pred in the pre-order (this element will be called succ)
         // (2) - check in the post-order for succ < pred
         //   ; if true, succ is a child of pred. push pred to the stack,
         //     and make succ the new pred, then continue with step 1
         //   ; if false, pop an element from the stack and make it the new pred,
         //     then continue with step 2

         // pre:  F, B, A, D, C, E, G, I, H
         // post: A, C, E, D, B, H, I, G, F
//
//         pred = F
//         stack = []
//         succ = B
//         post.lt( B, F ) = true
//         stack = [F]
//         pred = B
//         succ = A
//         post.lt( A, B ) = true
//         stack = [B, F]
//         pred = A
//         succ = D
//         post.lt( D, A ) = false
//         pred = B; stack = [F]
//         post.lt( D, B ) = true
//         stack = [B, F]
//         pred = D
//         succ = C
//         post.lt( C, D ) = true
//         stack = [D, B, F]
//         pred = C
//         succ = E
//         post.lt( E, C ) = false
//         pred = D; stack = [B, F]
//         post.lt( E, D ) = true
//         etc.

         val pre     = tree.preOrder
         val post    = tree.postOrder
         var preBase = pre.base
         var pred    = preBase.succ
         var stack   = IStack.empty[ PreOrder.Record[ Version ]]
         var parent: Option[ Node ] = None
         val headPath = Vector( pred.elem )
         while( pred != preBase ) {
            val pNode   = g.addNode()
            val predV   = pred.elem
            pNode.setString( VisualItem.LABEL, predV.toString )
            pNode.set( colPath, headPath :+ predV )
            parent.foreach( g.addEdge( _, pNode ))
            nodeMap += predV.id -> pNode
            val succ    = pred.moveRight
            if( succ != preBase ) {
               val succPost = succ.elem.vertex.postRec
               while( post.gteq( succPost, pred.elem.vertex.postRec )) {
                  pred  = stack.top
                  stack = stack pop
               }
               stack = stack push pred
            }
            pred     = succ
            parent   = Some( pNode )
         }

//         trie2.value.foreach { oracle =>
////            oracle.query()
//         }
      }
   }

   private def add( parent: Version, child: VersionPath ) {
      stopAnimation
      try {
         val pNode   = g.addNode()
         val childV  = child.version
         pNode.setString( VisualItem.LABEL, childV.toString )
         pNode.set( colPath, child.path )
//         val vi      = vis.getVisualItem( grpGraph, pNode )
//         vi.setString( VisualItem.LABEL, path.version.toString )
         nodeMap += childV.id -> pNode
         nodeMap.get( parent.id ).foreach( g.addEdge( _, pNode ))
      } finally {
         startAnimation
      }
   }
}