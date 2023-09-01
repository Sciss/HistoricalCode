/*
 *  MarkerAxis.java
 *  TimeBased
 *
 *  Copyright (c) 2004-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *  Changelog:
 *		24-Jul-05	created
 *		19-Feb-06	doesn't use DynamicAncestorAdapter any more ; doc frame should
 *					call startListening / stopListening !
 *		13-Jul-08	copied back from EisK
 *		17-Jul-08	copied from Cillo
 */

package de.sciss.timebased.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import de.sciss.app.AbstractApplication;
import de.sciss.app.AncestorAdapter;
import de.sciss.app.Application;
import de.sciss.app.BasicEvent;
import de.sciss.app.DynamicAncestorAdapter;
import de.sciss.app.DynamicListening;
import de.sciss.app.EventManager;
import de.sciss.app.GraphicsHandler;
import de.sciss.common.BasicWindowHandler;
import de.sciss.gui.ComponentHost;
import de.sciss.gui.DoClickAction;
import de.sciss.gui.MenuAction;
import de.sciss.gui.ParamField;
import de.sciss.gui.SpringPanel;
import de.sciss.io.Span;
import de.sciss.timebased.MarkerStake;
import de.sciss.timebased.Trail;
import de.sciss.timebased.timeline.Timeline;
import de.sciss.timebased.timeline.TimelineView;
import de.sciss.util.DefaultUnitTranslator;
import de.sciss.util.Disposable;
import de.sciss.util.Param;
import de.sciss.util.ParamSpace;

/**
 *  @author		Hanns Holger Rutz
 *  @version	0.10, 18-Jul-08
 *
 *	@todo		uses TimelineListener to
 *				not miss document changes. should use 
 *				a document change listener!
 *
 *	@todo		marker sortierung sollte zentral von session o.ae. vorgenommen
 *				werden sobald neues file geladen wird!
 *
 *	@todo		had to add 2 pixels to label y coordinate in java 1.5 ; have to check look back in 1.4
 *
 *	@todo		repaintMarkers : have to provide dirtySpan that accounts for flag width, esp. for dnd!
 *
 *	@todo		actionEditPrev/NextClass shortcuts funktionieren nicht
 */
public class MarkerAxis
extends JComponent
implements	TimelineView.Listener,
			DynamicListening, Trail.Listener, Disposable,
			EventManager.Processor
{
//	private final Font			fntLabel; //		= new Font( "Helvetica", Font.ITALIC, 10 );

	private String[]			markLabels		= new String[0];
	private int[]				markFlagPos		= new int[0];
	private int					numMarkers		= 0;
	protected final GeneralPath shpFlags		= new GeneralPath();
	private int					recentWidth		= -1;
	private boolean				doRecalc		= true;
	protected Span				visibleSpan;
//	protected double			scale			= 1.0;

	private static final int[] pntBarGradientPixels = { 0xFFB8B8B8, 0xFFC0C0C0, 0xFFC8C8C8, 0xFFD3D3D3,
														0xFFDBDBDB, 0xFFE4E4E4, 0xFFEBEBEB, 0xFFF1F1F1,
														0xFFF6F6F6, 0xFFFAFAFA, 0xFFFBFBFB, 0xFFFCFCFC,
														0xFFF9F9F9, 0xFFF4F4F4, 0xFFEFEFEF };
	private static final int barExtent = pntBarGradientPixels.length;

	private static final int[] pntMarkGradientPixels ={ 0xFF5B8581, 0xFF618A86, 0xFF5D8682, 0xFF59827E,
														0xFF537D79, 0xFF4F7975, 0xFF4B7470, 0xFF47716D,
														0xFF446E6A, 0xFF426B67, 0xFF406965, 0xFF3F6965,
														0xFF3F6864 };	// , 0xFF5B8581

	private static final int[] pntMarkDragPixels;
	
	private static final Color	colrLabel		= Color.white;
	private static final Color	colrLabelDrag	= new Color( 0xFF, 0xFF, 0xFF, 0xBF );

	private static final Paint	pntMarkStick= new Color( 0x31, 0x50, 0x4D, 0x7F );
	private static final Paint	pntMarkStickDrag = new Color( 0x31, 0x50, 0x4D, 0x5F );
	private static final Stroke	strkStick	= new BasicStroke( 1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,
		1.0f, new float[] { 4.0f, 4.0f }, 0.0f );

	private static final int			markExtent = pntMarkGradientPixels.length;
	private final Paint					pntBackground;
	private final Paint					pntMarkFlag, pntMarkFlagDrag;
	private final BufferedImage			img1, img2, img3;

	private final ComponentHost			host;
	private boolean						isListening	= false;

	// ----- Edit-Marker Dialog -----
	private JPanel						editMarkerPane	= null;
	private Object[]					editOptions		= null;
	private ParamField					ggMarkPos;
	protected JTextField				ggMarkName;
	private JButton						ggEditPrev, ggEditNext;
	protected int						editIdx			= -1;
	private DefaultUnitTranslator		timeTrans;
	
	// ---- dnd ----
	protected MarkerStake				dragMark		= null;
	protected MarkerStake				dragLastMark	= null;
	protected boolean					dragStarted		= false;
	protected int						dragStartX		= 0;
	
	protected boolean					dispatchedStart	= false;
	
	protected final TimelineView		view;
	protected Trail						trail			= null;
	
	private final EventManager			elm				= new EventManager( this );
	protected Trail.Editor				editor			= null;
	
	private final MouseInputListener	mil;
	private final KeyListener			kl;
	
	static {
		pntMarkDragPixels = new int[ pntMarkGradientPixels.length ];
		for( int i = 0; i < pntMarkGradientPixels.length; i++ ) {
			pntMarkDragPixels[ i ] = pntMarkGradientPixels[ i ] & 0xBFFFFFFF;	// = 50% alpha
		}
	}
	
	public MarkerAxis( TimelineView tlv )
	{
		this( tlv, null );
	}

	/**
	 *  Constructs a new object for
	 *  displaying the timeline ruler
	 *
	 *  @param  root	application root
	 *  @param  doc		session Session
	 */
	public MarkerAxis( TimelineView tlv, ComponentHost host )
	{
		super();
        
		this.view	= tlv;
		this.host	= host;
		
		visibleSpan	= tlv.getSpan();
		
//		fntLabel	= AbstractApplication.getApplication().getGraphicsHandler().getFont( GraphicsHandler.FONT_LABEL | GraphicsHandler.FONT_MINI ).deriveFont( Font.ITALIC );
		
		setMaximumSize( new Dimension( getMaximumSize().width, barExtent ));
		setMinimumSize( new Dimension( getMinimumSize().width, barExtent ));
		setPreferredSize( new Dimension( getPreferredSize().width, barExtent ));

		img1		= new BufferedImage( 1, barExtent, BufferedImage.TYPE_INT_ARGB );
		img1.setRGB( 0, 0, 1, barExtent, pntBarGradientPixels, 0, 1 );
		pntBackground = new TexturePaint( img1, new Rectangle( 0, 0, 1, barExtent ));
		img2		= new BufferedImage( 1, markExtent, BufferedImage.TYPE_INT_ARGB );
		img2.setRGB( 0, 0, 1, markExtent, pntMarkGradientPixels, 0, 1 );
		pntMarkFlag	= new TexturePaint( img2, new Rectangle( 0, 0, 1, markExtent ));
		img3		= new BufferedImage( 1, markExtent, BufferedImage.TYPE_INT_ARGB );
		img3.setRGB( 0, 0, 1, markExtent, pntMarkDragPixels, 0, 1 );
		pntMarkFlagDrag = new TexturePaint( img3, new Rectangle( 0, 0, 1, markExtent ));

		setOpaque( true );
 		final Application app = AbstractApplication.getApplication();
 		if( app != null ) setFont( app.getGraphicsHandler().getFont( GraphicsHandler.FONT_SYSTEM | GraphicsHandler.FONT_MINI ));
		
		mil = new MouseInputAdapter() {
			public void mousePressed( MouseEvent e )
		    {
				final double	scale	= (double) visibleSpan.getLength() / Math.max( 1, getWidth() );
				final long		pos		= (long) (e.getX() * scale + visibleSpan.start + 0.5);
				
				if( shpFlags.contains( e.getPoint() )) {
					if( e.isAltDown() ) {					// delete marker
						removeMarkerLeftTo( pos + 1 );
					} else if( e.getClickCount() == 2 ) {	// rename
						editMarkerLeftTo( pos + 1 );
					} else {								// start drag
						dragMark			= getMarkerLeftTo( pos + 1 );
						dragStarted			= false;
						dragStartX			= e.getX();
						if( dragMark != null ) {
							dispatchedStart = true;
							dispatchEvent( Event.DRAGSTARTED, dragMark.getSpan() );
							requestFocus();
						}
					}
					
				} else if( !e.isAltDown() && (e.getClickCount() == 2) ) {		// insert marker
					addMarker( pos );
				}
			}

			public void mouseReleased( MouseEvent e )
			{
				if( dispatchedStart ) {
					dispatchedStart = false;
					dispatchEvent( Event.DRAGSTOPPED, (dragLastMark != null) ?
						dragLastMark.getSpan() : dragMark.getSpan() );
				}
				
				if( (dragLastMark != null) && (editor != null) ) {
					final int id = editor.editBegin( MarkerAxis.this, getResourceString( "editMoveMarker" ));
					try {
						editor.editRemove( id, dragMark );
						editor.editAdd( id, dragLastMark );
						editor.editEnd( id );
					}
					catch( IOException e1 ) {	// should never happen
						System.err.println( e1 );
						editor.editCancel( id );
					}
				}
				dragStarted		= false;
				dragMark		= null;
				dragLastMark	= null;
			}
			
			public void mouseDragged( MouseEvent e )
			{
				if( dragMark == null ) return;

				if( !dragStarted ) {
					if( Math.abs( e.getX() - dragStartX ) < 5 ) return;
					dragStarted = true;
				}

				final Span		dirtySpan;
				final long		oldPos	= dragLastMark != null ? dragLastMark.pos : dragMark.pos;
				final double	scale	= (double) getWidth() / visibleSpan.getLength();
				final long		newPos	= view.getTimeline().getSpan().clip( (long) ((e.getX() - dragStartX) / scale + dragMark.pos + 0.5) );

				if( oldPos == newPos ) return;
				
				dirtySpan		= new Span( Math.min( oldPos, newPos ), Math.max( oldPos, newPos ));
				dragLastMark	= new MarkerStake( newPos, dragMark.name );
				if( dispatchedStart ) dispatchEvent( Event.DRAGADJUSTED, dirtySpan );
			}
		};
		
		kl = new KeyAdapter() {
		    public void keyPressed( KeyEvent e )
			{
				if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
					dragMark		= null;
					dragLastMark	= null;
					if( dragStarted ) {
						dragStarted	= false;
					}
					if( dispatchedStart ) {
						dispatchedStart = false;
						dispatchEvent( Event.DRAGSTOPPED, visibleSpan );
					}
				}
			}
		};
		
        new DynamicAncestorAdapter( this ).addTo( this );
	}
	
	public void setTrail( Trail t )
	{
		if( (trail != null) && isListening ) {
			trail.removeListener( this );
		}
		trail = t;
		if( (trail != null) && isListening ) {
			trail.addListener( this );
		}
		triggerRedisplay();
	}
	
	public void setEditor( Trail.Editor newEditor )
	{
		if( editor != newEditor ) {
			if( (editor != null) && (newEditor == null) ) {
				this.removeMouseListener( mil );
				this.removeMouseMotionListener( mil );
				this.removeKeyListener( kl );
			} else if( (editor == null) && (newEditor != null) ){
				this.addMouseListener( mil );
				this.addMouseMotionListener( mil );
				this.addKeyListener( kl );
			}
			editor = newEditor;
		}
	}

	protected static String getResourceString( String key )
	{
		return( AbstractApplication.getApplication().getResourceString( key ));
	}
	
	private void recalcDisplay( FontMetrics fm )
	{
		final List		markers;
//		final long		start		= visibleSpan.start;
//		final long		stop		= visibleSpan.stop;
		final double	scale		= (double) recentWidth / visibleSpan.getLength();

		MarkerStake		mark;
		
		shpFlags.reset();
		numMarkers	= 0;
		
		if( trail != null ) {
			markers		= trail.getRange( visibleSpan, true );	// XXX plus a bit before
			numMarkers	= markers.size();
		} else {
			markers		= null;
			numMarkers	= 0;
		}
		if( (numMarkers > markLabels.length) || (numMarkers < (markLabels.length >> 1)) ) {
			markLabels		= new String[ numMarkers * 3 / 2 ];		// 'decent growing and shrinking'
			markFlagPos		= new int[ markLabels.length ];
		}
		
		for( int i = 0; i < numMarkers; i++ ) {
			mark				= (MarkerStake) markers.get( i );
			markLabels[ i ]		= mark.name;
			markFlagPos[ i ]	= (int) (((mark.pos - visibleSpan.start) * scale) + 0.5);
			shpFlags.append( new Rectangle( markFlagPos[ i ], 1, fm.stringWidth( mark.name ) + 8, markExtent ), false );
		}
		doRecalc	= false;
	}

	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		
		final Graphics2D	g2	= (Graphics2D) g;
		
//		g2.setFont( fntLabel );
		
		final FontMetrics	fm	= g2.getFontMetrics();
		final int			y	= fm.getAscent() + 2;

		if( doRecalc || (recentWidth != getWidth()) ) {
			recentWidth = getWidth();
			recalcDisplay( fm );
		}

		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );

		g2.setPaint( pntBackground );
		g2.fillRect( 0, 0, recentWidth, barExtent );

		g2.setPaint( pntMarkFlag );
		g2.fill( shpFlags );

		g2.setColor( colrLabel );
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		for( int i = 0; i < numMarkers; i++ ) {
			g2.drawString( markLabels[ i ], markFlagPos[ i ] + 4, y );
		}

		// handle dnd graphics
		if( dragLastMark != null ) {
			final int dragMarkFlagPos = (int) (((dragLastMark.pos - visibleSpan.start) * (double) recentWidth / visibleSpan.getLength()) + 0.5);
			g2.setPaint( pntMarkFlagDrag );
			g2.fillRect( dragMarkFlagPos, 1, fm.stringWidth( dragLastMark.name ) + 8, markExtent );
			g2.setColor( colrLabelDrag );
			g2.drawString( dragLastMark.name, dragMarkFlagPos + 4, y );
		}
	}

	public void paintFlagSticks( Graphics2D g2, Rectangle bounds )
	{
		if( doRecalc ) {
			recalcDisplay( g2.getFontMetrics() );	// XXX nicht ganz sauber (anderer graphics-context!)
		}
	
		final Stroke	strkOrig	= g2.getStroke();
	
		g2.setPaint( pntMarkStick );
		g2.setStroke( strkStick );
		for( int i = 0; i < numMarkers; i++ ) {
			g2.drawLine( markFlagPos[i], bounds.y, markFlagPos[i], bounds.y + bounds.height );
		}
		if( dragLastMark != null ) {
			final int dragMarkFlagPos = (int) (((dragLastMark.pos - visibleSpan.start) * (double) recentWidth / visibleSpan.getLength()) + 0.5);
			g2.setPaint( pntMarkStickDrag );
			g2.drawLine( dragMarkFlagPos, bounds.y, dragMarkFlagPos, bounds.y + bounds.height );
		}
		g2.setStroke( strkOrig );
	}

	private void triggerRedisplay()
	{
		doRecalc = true;
		if( host != null ) {
			host.update( this );
		} else if( isVisible() ) {
			repaint();
		}
	}
  
	public void addMarker( long pos )
	{
		if( editor == null ) throw new IllegalStateException();
		
		pos				= view.getTimeline().getSpan().clip( pos );
		final int id	= editor.editBegin( this, getResourceString( "editAddMarker" ));
		try {
			editor.editAdd( id, new MarkerStake( pos, "Mark" ));
			editor.editEnd( id );
		}
		catch( IOException e1 ) {	// should never happen
			e1.printStackTrace();
			editor.editCancel( id );
		}
	}
	
	protected void removeMarkerLeftTo( long pos )
	{
		if( editor == null ) throw new IllegalStateException();
		
		final MarkerStake mark;
	
		mark	= getMarkerLeftTo( pos );
		pos		= view.getTimeline().getSpan().clip( pos );
		if( mark == null ) return;
		
		final int id = editor.editBegin( this, getResourceString( "editDeleteMarker" ));
		try {
			editor.editRemove( id, mark );
			editor.editEnd( id );
		}
		catch( IOException e1 ) {	// should never happen
			e1.printStackTrace();
			editor.editCancel( id );
			return;
		}
	}

	protected void editMarkerLeftTo( long pos )
	{
		if( trail == null ) throw new IllegalStateException();
		
		final int result;

		editIdx		= trail.indexOf( pos, true );
		if( editIdx < 0 ) {
			editIdx = -(editIdx + 2);
			if( editIdx == -1 ) return;
		}
	
		if( editMarkerPane == null ) {
			final SpringPanel		spring;
			final ActionMap			amap;
			final InputMap			imap;
			JLabel					lb;
			KeyStroke				ks;
			Action					a;

			spring			= new SpringPanel( 4, 2, 4, 2 );
			ggMarkName		= new JTextField( 24 );
			ggMarkName.addAncestorListener( new AncestorAdapter() {
				public void ancestorAdded( AncestorEvent e ) {
					ggMarkName.requestFocusInWindow();
					ggMarkName.selectAll();
				}
			});

			timeTrans		= new DefaultUnitTranslator();
			ggMarkPos		= new ParamField( timeTrans );
			ggMarkPos.addSpace( ParamSpace.spcTimeHHMMSS );
			ggMarkPos.addSpace( ParamSpace.spcTimeSmps );
			ggMarkPos.addSpace( ParamSpace.spcTimeMillis );
			ggMarkPos.addSpace( ParamSpace.spcTimePercentF );

			lb				= new JLabel( getResourceString( "labelName" ));
			spring.gridAdd( lb, 0, 0 );
			spring.gridAdd( ggMarkName, 1, 0 );
			lb				= new JLabel( getResourceString( "labelPosition" ));
			spring.gridAdd( lb, 0, 1 );
			spring.gridAdd( ggMarkPos, 1, 1, -1, 1 );
			spring.makeCompactGrid();
			editMarkerPane	= new JPanel( new BorderLayout() );
			editMarkerPane.add( spring, BorderLayout.NORTH );
			
			amap			= spring.getActionMap();
			imap			= spring.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
			ks				= KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
			imap.put( ks, "prev" );
			a				= new ActionEditPrev();
			ggEditPrev		= new JButton( a );
			amap.put( "prev", new DoClickAction( ggEditPrev ));
			ks				= KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
			imap.put( ks, "next" );
			a				= new ActionEditNext();
			ggEditNext		= new JButton( a );
			amap.put( "next", new DoClickAction( ggEditNext ));
			
			editOptions		= new Object[] { ggEditNext, ggEditPrev, getResourceString( "buttonOk" ), getResourceString( "buttonCancel" )};
		}

		final Timeline tl = view.getTimeline();
		timeTrans.setLengthAndRate( tl.getSpan().getLength(), tl.getRate() );
		
		updateEditMarker();
		
		final JOptionPane op = new JOptionPane( editMarkerPane, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
		                                        null, editOptions, editOptions[ 2 ]);
		result = BasicWindowHandler.showDialog( op, BasicWindowHandler.getWindowAncestor( this ), getResourceString( "inputDlgEditMarker" ));

		if( result == 2 ) {
			commitEditMarker();
		}
	}
	
	protected void updateEditMarker()
	{
		if( trail == null ) throw new IllegalStateException();
		
		final MarkerStake mark = (MarkerStake) trail.get( editIdx, true );
		if( mark == null ) return;

		ggMarkPos.setValue( new Param( mark.pos, ParamSpace.TIME | ParamSpace.SMPS ));
		ggMarkName.setText( mark.name );
		
		ggEditPrev.setEnabled( editIdx > 0 );
		ggEditNext.setEnabled( (editIdx + 1) < trail.getNumStakes() );
		
		ggMarkName.requestFocusInWindow();
		ggMarkName.selectAll();
	}
	
	protected void commitEditMarker()
	{
		if( (editor == null) || (trail == null) ) throw new IllegalStateException();
		
		final MarkerStake mark = (MarkerStake) trail.get( editIdx, true );
		if( mark == null ) return;

		final long positionSmps;

		positionSmps	= (long) timeTrans.translate( ggMarkPos.getValue(), ParamSpace.spcTimeSmps ).val;
		if( (positionSmps == mark.pos) && (ggMarkName.getText().equals( mark.name ))) return; // no change
		
		final int id = editor.editBegin( this, getResourceString( "editEditMarker" ));
		try {
			editor.editRemove( id, mark );
			editor.editAdd( id, new MarkerStake( positionSmps, ggMarkName.getText() ));
			editor.editEnd( id );
		}
		catch( IOException e1 ) {	// should never happen
			e1.printStackTrace();
			editor.editCancel( id );
		}
	}

	protected MarkerStake getMarkerLeftTo( long pos )
	{
		if( trail == null ) throw new IllegalStateException();
		
		int idx;
	
		idx = trail.indexOf( pos, true );
		if( idx < 0 ) {
			idx = -(idx + 2);
			if( idx == -1 ) return null;
		}
		return (MarkerStake) trail.get( idx, true );
	}
	
	protected void dispatchEvent( int id, Span modSpan )
	{
		if( elm != null ) {
			elm.dispatchEvent( new Event( this, id, System.currentTimeMillis(), this, modSpan ));
		}
	}
	
	public void addListener( Listener l )
	{
		elm.addListener( l );
	}

	public void removeListener( Listener l )
	{
		elm.removeListener( l );
	}
	
	// -------------- EventManager.Processor interface --------------
	public void processEvent( BasicEvent e )
	{
		for( int i = 0; i < elm.countListeners(); i++ ) {
			final Listener l = (Listener) elm.getListener( i );
			switch( e.getID() ) {
			case Event.DRAGSTARTED:
				l.markerDragStarted( (Event) e );
				break;
			case Event.DRAGSTOPPED:
				l.markerDragStopped( (Event) e );
				break;
			case Event.DRAGADJUSTED:
				l.markerDragAdjusted( (Event) e );
				break;
			default:
				assert false : e.getID();
			}
		}
	}

	// -------------- Disposable interface --------------

	public void dispose()
	{
		stopListening();
		setEditor( null );
		trail		= null;
		markLabels	= null;
		markFlagPos	= null;
		shpFlags.reset();
		img1.flush();
		img2.flush();
		img3.flush();
	}

// ---------------- DynamicListening interface ---------------- 

    public void startListening()
    {
		if( !isListening ) {
			view.addListener( this );
			if( trail != null ) trail.addListener( this );
			triggerRedisplay();
			isListening = true;
		}
    }

    public void stopListening()
    {
		if( isListening ) {
			if( trail != null ) trail.removeListener( this );
			view.removeListener( this );
			isListening = false;
		}
    }

// ---------------- MarkerManager.Listener interface ---------------- 

	public void trailModified( Trail.Event e )
	{
		if( e.getAffectedSpan().touches( visibleSpan )) {
			triggerRedisplay();
		}
	}
	
// ---------------- TimelineListener interface ---------------- 
  
   	public void timelineSelected( TimelineView.Event e ) { /* ignored */ }
	public void timelinePositioned( TimelineView.Event e ) { /* ignored */ }
	public void timelineChanged( TimelineView.Event e ) { /* ignored */ }

   	public void timelineScrolled( TimelineView.Event e )
    {
   		visibleSpan = view.getSpan();			
   		triggerRedisplay();
    }

// ---------------- internal classes ----------------
   	
   	public interface Listener
   	{
   		public void markerDragStarted( Event e ); 
   		public void markerDragStopped( Event e );
   		public void markerDragAdjusted( Event e ); 
   	}

	public static class Event
	extends BasicEvent
	{
		public static final int DRAGSTARTED		= 1;
		public static final int DRAGSTOPPED		= 2;
		public static final int DRAGADJUSTED	= 3;
		
		private final MarkerAxis	axis;
		private final Span			modSpan;

		public Event( Object source, int id, long when, MarkerAxis axis, Span modSpan )
		{
			super( source, id, when );
			this.axis 		= axis;
			this.modSpan	= modSpan;
		}
		
		public MarkerAxis getAxis()
		{
			return axis;
		}
		
		public Span getModificatioSpan()
		{
			return modSpan;
		}

		public boolean incorporate( BasicEvent oldEvent )
		{
			return( oldEvent instanceof Event &&
				this.getSource() == oldEvent.getSource() &&
				this.getID() == oldEvent.getID() &&
				this.axis == ((Event) oldEvent).axis );
			}
	}

	private class ActionEditPrev
	extends MenuAction
	{
		protected ActionEditPrev()
		{
			super( "\u21E0" );
		}
		
		public void actionPerformed( ActionEvent e )
		{
			commitEditMarker();
			if( editIdx > 0 ) {
				editIdx--;
				updateEditMarker();
			}
		}
	}

	private class ActionEditNext
	extends MenuAction
	{
		protected ActionEditNext()
		{
			super( "\u21E2", KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
		}
		
		public void actionPerformed( ActionEvent e )
		{
			commitEditMarker();
			if( (editIdx + 1) < trail.getNumStakes() ) {
				editIdx++;
				updateEditMarker();
			}
		}
	}
}