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

package de.sciss.synth.proc
package view

import prefuse.action.layout.graph.ForceDirectedLayout
import prefuse.visual.VisualItem
import prefuse.util.ColorLib
import prefuse.activity.Activity
import prefuse.action.{RepaintAction, ActionList}
import prefuse.{Constants, Visualization, Display}
import prefuse.visual.expression.InGroupPredicate
import prefuse.render.{DefaultRendererFactory, EdgeRenderer, LabelRenderer}
import collection.breakOut
import collection.immutable.{IntMap, Set => ISet, Stack => IStack}
import prefuse.controls._
import prefuse.data.event.TupleSetListener
import prefuse.data.tuple.TupleSet
import prefuse.data.{Tuple, Node, Graph}
import javax.swing.{JComponent}
import java.beans.{PropertyChangeListener, PropertyChangeEvent}
import de.sciss.confluent._
import GUIUtils._
import prefuse.action.assignment.{StrokeAction, ColorAction}
import java.awt.{BasicStroke, Color}
import prefuse.data.expression.{AbstractPredicate}
import sys.error

class VersionGraphView[ C <: Ct, V[ ~ ] <: KVar[ C, ~ ], Csr <: KProjection[ C ] with Cursor[ C ]]( sys: KSystemLike[ C, V, _, Csr ]) {
   private val grpGraph    = "graph"
   private val grpNodes    = "graph.nodes"
   private val grpEdges    = "graph.edges"
   private val actColor    = "color"
   private val actLayout   = "layout"
   private val colPath     = "path"
   private val colCursor   = "cursor"
   private val vis         = new Visualization()
   private val g           = new Graph()

   private var nodeMap     = IntMap.empty[ Node ]

   type Selection = (VersionPath, List[ Csr ])

   private var mapCsr   = Map.empty[ Csr, CursorListener ]

   private val selListeners   = new JComponent {
      var oldSel: List[ Selection ] = Nil
      val propSel = "selection"

      def newSelection( sel: List[ Selection ]) {
         val o    = oldSel
         oldSel   = sel
//println( "BOOM " + sel )
         firePropertyChange( propSel, o, sel )
      }

      def addL( fun: List[ Selection ] => Unit ) {
         val l = new SelectionActionListener( fun )
         addPropertyChangeListener( propSel, l )
      }

      def removeL( fun: List[ Selection ] => Unit ) {
         val l = new SelectionActionListener( fun )
         removePropertyChangeListener( propSel, l )
      }

      private case class SelectionActionListener( fun: List[ Selection ] => Unit )
      extends PropertyChangeListener {
         def propertyChange( e: PropertyChangeEvent ) {
            fun( e.getNewValue().asInstanceOf[ List[ Selection ]])
         }
      }
   }

   val panel : JComponent = {
      val lSys = Model.onCommit[ CtxLike, KSystemLike.Update ]( tr => defer( tr.foreach {
         case KSystemLike.NewBranch( oldPath, newPath ) => addVertex( oldPath.version, newPath )
      }))

      val lCsr = Model.onCommit[ CtxLike, Projector.Update[ C, Csr ]]( tr => defer( tr.foreach {
         case Projector.CursorAdded( csr )            => addCursor( csr )
         case Projector.CursorRemoved( csr )          => removeCursor( csr )
      }))

//      val colLabel      = "label"
      val display       = new Display( vis )
      val vg            = vis.addGraph( grpGraph, g )
      val nodes         = g.getNodeTable()
      g.addColumn( VisualItem.LABEL, classOf[ String ])
      g.addColumn( colPath, classOf[ Vector[ Version ]])
      g.addColumn( colCursor, classOf[ List[ Csr ]]) // XXX maybe not the smartest way to occupy space in each vertex?

      // colors
      val colrGray   = ColorLib.rgb( 0xC0, 0xC0, 0xC0 )
      val colrFocus  = ColorLib.rgb( 0x60, 0x60, 0xC0 )
      val colrBlack  = ColorLib.rgb( 0x00, 0x00, 0x00 )
      val colrWhite  = ColorLib.rgb( 0xFF, 0xFF, 0xFF )
      val colrHighlight = ColorLib.rgb( 0xFF, 0x00, 0x60 )
      val predFocus        = new InGroupPredicate( Visualization.FOCUS_ITEMS )
//      val predCursor       = new ComparisonPredicate( ComparisonPredicate.NEQ,
//         new ColumnExpression( colCursor ), new ObjectLiteral( Nil ))
      val predCursor       = new AbstractPredicate() {
         override def getBoolean( t: Tuple ) : Boolean = t.get( colCursor ) != Nil
      }
      val actionNodeDraw   = new ColorAction( grpNodes, VisualItem.STROKECOLOR, colrGray )
      val actionNodeFill   = new ColorAction( grpNodes, VisualItem.FILLCOLOR,   colrGray )
      val actionTextColor  = new ColorAction( grpNodes, VisualItem.TEXTCOLOR,   colrBlack )
      val actionEdgeColor  = new ColorAction( grpEdges, VisualItem.STROKECOLOR, colrGray )
      val actionNodeStroke = new StrokeAction( grpNodes )
      actionNodeStroke.add( predCursor, new BasicStroke( 2f ))
      actionNodeFill.add( predFocus, colrFocus )
      actionNodeDraw.add( predCursor, colrHighlight )
      actionNodeDraw.add( predFocus, colrFocus )
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
      actionColor.add( actionNodeDraw )
      actionColor.add( actionNodeFill )
      actionColor.add( actionNodeStroke )
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

      ancestorAction( display ) {
         stopAnimation
         try {
            sys.t { implicit c =>
               addFullVertices( sys.dag )
               addFullCursors( sys.kProjector.cursors )
               sys.addListener( lSys )
               sys.kProjector.addListener( lCsr)
            }
         } finally {
            startAnimation
         }
      } {
         stopAnimation
         sys.t { implicit c =>
            sys.removeListener( lSys )
            sys.kProjector.removeListener( lCsr )
         }
      }

      display.setSize( 300, 300 )
//      cp.add( display, BorderLayout.CENTER )
//      f.setSize( 300, 300 )
//      f.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE )
//      f.setLocationRelativeTo( null )
//      f.setVisible( true )
//      f
      display
   }

   def addSelectionListener( fun: List[ Selection ] => Unit ) {
      selListeners.addL( fun )
   }

   def removeSelectionListener( fun: List[ Selection ] => Unit ) {
      selListeners.removeL( fun )
   }

   def selection : List[ Selection ] = {
      val tupT = vis.getGroup( Visualization.FOCUS_ITEMS ).tuples.asInstanceOf[ java.util.Iterator[ Tuple ]]
      val iter = collection.JavaConversions.asScalaIterator( tupT )
      iter.map( t => {
         val vp   = VersionPath.wrap( t.get( colPath ).asInstanceOf[ Vector[ Version ]])
         val csr  = t.get( colCursor ).asInstanceOf[ List[ Csr ]]
         (vp, csr)
      }).toList
   }

   private def stopAnimation {
      vis.cancel( actColor )
      vis.cancel( actLayout )
   }

   private def startAnimation {
      vis.run( actColor )
   }

   private def addFullVertices( trie: LexiTrie[ OracleMap[ VersionPath ]]) {
      g.clear()
      nodeMap = nodeMap.empty
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
            pNode.set( colCursor, Nil )
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

   private def addFullCursors( csr: Traversable[ Csr ]) {
      csr.foreach( addCursor( _ ))
   }

   private def addCursor( csr: Csr ) {
      sys.t { implicit c =>
         val initPath = csr.path
         nodeMap.get( initPath.version.id ).foreach { pNode =>
            val csrs = pNode.get( colCursor ).asInstanceOf[ List[ Csr ]]
            pNode.set( colCursor, csr :: csrs )
            checkKarlheinz( pNode )
         }
         val csrL = CursorListener( csr )
         csrL.path = initPath
         mapCsr += csr -> csrL
         csr.addListener( csrL.list )
      }
      startAnimation
   }

   private def moveCursor( csr: Csr, oldPath: VersionPath, newPath: VersionPath ) {
//println( "CURSOR DORFER " + oldPath + " -> " + newPath )
      nodeMap.get( oldPath.version.id ).foreach { pNode =>
//println( "CURSOR DORFER REM" )
         val csrs = pNode.get( colCursor ).asInstanceOf[ List[ Csr ]]
         pNode.set( colCursor, csrs.filterNot( _ == csr ))
         checkKarlheinz( pNode )
      }
      nodeMap.get( newPath.version.id ).foreach { pNode =>
//println( "CURSOR DORFER ADD" )
         val csrs = pNode.get( colCursor ).asInstanceOf[ List[ Csr ]]
         pNode.set( colCursor, csr :: csrs )
         checkKarlheinz( pNode ) // XXX should collapse karlheinzes
      }
      startAnimation
   }

   private def removeCursor( csr: Csr ) {
      sys.t { implicit c =>
         mapCsr.get( csr ).foreach { csrL =>
            csr.removeListener( csrL.list )
            mapCsr -= csr
         }
         val id = csr.path.version.id
         nodeMap.get( id ).foreach { pNode =>
            val csrs = pNode.get( colCursor ).asInstanceOf[ List[ Csr ]]
            pNode.set( colCursor, csrs.filterNot( _ == csr ))
            checkKarlheinz( pNode )
         }
         nodeMap -= id
      }
   }

   private def checkKarlheinz( pNode: Node ) {
      val lulu = vis.getVisualItem( grpGraph, pNode )
//      val tupT = vis.getGroup( Visualization.FOCUS_ITEMS ).tuples.asInstanceOf[ java.util.Iterator[ Tuple ]]
//println( "KARLHEINZ? " + collection.JavaConversions.asIterator( tupT ).toList + " :: " + pNode )
      if( vis.getGroup( Visualization.FOCUS_ITEMS ).containsTuple( lulu )) {
//println( "...........SI" )
         selListeners.newSelection( selection )
      }
   }

   private def addVertex( parent: Version, child: VersionPath ) {
      stopAnimation
      try {
         val pNode   = g.addNode()
         val childV  = child.version
         pNode.setString( VisualItem.LABEL, childV.toString )
         pNode.set( colPath, child.path )
         pNode.set( colCursor, Nil )
//         val vi      = vis.getVisualItem( grpGraph, pNode )
//         vi.setString( VisualItem.LABEL, path.version.toString )
         nodeMap += childV.id -> pNode
         nodeMap.get( parent.id ).foreach( g.addEdge( _, pNode ))
      } finally {
         startAnimation
      }
   }

   case class CursorListener( csr: Csr ) {
      var path: VersionPath = _
//      val list = Model.onCommit[ C, Cursor.Update ]( tr => defer( tr.foreach {
//         case KCursor.Moved( oldPath, newPath ) => moveCursor( csr, oldPath, newPath )
//         case _ =>
//      }))
      val list = Model.reduceOnCommit[ CtxLike, Cursor.Update, VersionPath ] {
         case (Cursor.Moved, ctx) => csr.path( ctx.eph )
      } { newPath => defer {
         val oldPath = path
         path = newPath
         moveCursor( csr, oldPath, newPath )
      }}
   }
}