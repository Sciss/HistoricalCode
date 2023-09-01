/*
 *  TrackRowHeader.java
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
 *		15-Jan-06	created from de.sciss.eisenkraut.timeline.ChannelRowHeader
 *		13-Jul-08	copied back from EisK
 *		18-Jul-08	copied from Cillo
 */

package de.sciss.timebased.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import de.sciss.gui.GradientPanel;

import de.sciss.app.AbstractApplication;
import de.sciss.app.Application;
import de.sciss.app.DynamicAncestorAdapter;
import de.sciss.app.DynamicListening;
import de.sciss.app.GraphicsHandler;
import de.sciss.timebased.session.MutableSessionCollection;
import de.sciss.timebased.session.SessionCollection;
import de.sciss.timebased.session.SessionObject;
import de.sciss.timebased.session.Track;
import de.sciss.util.Disposable;
import de.sciss.util.MapManager;

/**
 *	A row header in Swing's table 'ideology'
 *	is a component left to the leftmost
 *	column of each row in a table. It serves
 *	as a kind of label for that specific row.
 *	This class shows a header left to each
 *	sound file's waveform display, with information
 *	about the channel index, possible selections
 *	and soloing/muting. In the future it could
 *	carry insert effects and the like.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.75, 22-Jul-08
 */
public class TrackRowHeader
extends JPanel
implements DynamicListening, Disposable
{
	private final JLabel			lbTrackName;

	protected boolean				selected		= false;

    private static final Color		colrSelected	= new Color( 0x00, 0x00, 0xFF, 0x2F ); // GraphicsUtil.colrSelection;
    private static final Color		colrUnselected	= new Color( 0x00, 0x00, 0x00, 0x20 );
    private static final Color		colrDarken		= new Color( 0x00, 0x00, 0x00, 0x18 );
	private static final Paint		pntSelected		= new GradientPaint(  0, 0, colrSelected,
																		 36, 0, new Color( colrSelected.getRGB() & 0xFFFFFF, true ));
	private static final Paint		pntUnselected	= new GradientPaint(  0, 0, colrUnselected,
																		 36, 0, new Color( colrUnselected.getRGB() & 0xFFFFFF, true ));
	private static final Paint		pntDarken		= new GradientPaint(  0, 0, colrDarken,
																		 36, 0, new Color( colrDarken.getRGB() & 0xFFFFFF, true ));
	
	private final MapManager.Listener			trackListener;
	private final SessionCollection.Listener	selectedTracksListener;
	
	private final MouseListener					ml;
	protected MutableSessionCollection.Editor	editor			= null;
		
	protected Track								t				= null;
	protected SessionCollection					tracks			= null;
	protected MutableSessionCollection			selectedTracks	= null;
	
	private boolean	isListening	= false;
	
	/**
	 */
	public TrackRowHeader()
	{
		super();
		
//		this.t				= t;
//		this.tracks			= tracks;
//		this.selectedTracks	= selectedTracks;
		
		final SpringLayout	lay	= new SpringLayout();
		SpringLayout.Constraints cons;
		setLayout( lay );
		
 		lbTrackName = new JLabel();
 		final Application app = AbstractApplication.getApplication();
 		if( app != null ) lbTrackName.setFont( app.getGraphicsHandler().getFont( GraphicsHandler.FONT_SYSTEM | GraphicsHandler.FONT_SMALL ));
		cons		= lay.getConstraints( lbTrackName );
		cons.setX( Spring.constant( 7 ));
// doesnt' work (why???)
//		cons.setY( Spring.minus( Spring.max(	// min( X, Y ) = -max( -X, -Y )
//			Spring.constant( -4 ), Spring.minus( Spring.sum(
//				lay.getConstraints( this ).getHeight(), Spring.constant( -15 )))
//		)));
		cons.setY( Spring.minus( Spring.max(	// min( X, Y ) = -max( -X, -Y )
				Spring.constant( -4 ),
				Spring.minus( Spring.sum( Spring.sum( lay.getConstraint( SpringLayout.SOUTH, this ), Spring.minus( lay.getConstraint( SpringLayout.NORTH, this ))), Spring.constant( -15 ))))));
		add( lbTrackName );
		setBorder( BorderFactory.createMatteBorder( 0, 0, 0, 2, Color.white ));   // top left bottom right

		// --- Listener ---
        new DynamicAncestorAdapter( this ).addTo( this );
		
		ml = new MouseAdapter() {
			/**
			 *	Handle mouse presses.
			 *	<pre>
			 *  Keyboard shortcuts as in ProTools:
			 *  Alt+Click   = Toggle item & set all others to same new state
			 *  Meta+Click  = Toggle item & set all others to opposite state
			 *	</pre>
			 *
			 *	@synchronization	attempts exclusive on TRNS + GRP
			 */
			public void mousePressed( MouseEvent e )
		    {
				if( (editor == null) || (t == null) ) return;
				
				final List<Track>	collToAdd;
				final List<Track>	collToRemove;
				final boolean		newSelected;

				final int id = editor.editBegin( this, getResourceString( "editTrackSelection" ));
				
				if( e.isAltDown() ) {
					newSelected = !selected;   // toggle item
					if( newSelected ) {		// select all
//						collToRemove	= Collections.EMPTY_LIST;
						collToAdd		= tracks.getAll();
						collToAdd.removeAll( selectedTracks.getAll() );
						editor.editAdd( id, collToAdd.toArray( new SessionObject[ collToAdd.size() ]));
					} else {				// deselect all
						collToRemove	= selectedTracks.getAll();
						editor.editRemove( id, collToRemove.toArray( new SessionObject[ collToRemove.size() ]));
//						collToAdd		= Collections.EMPTY_LIST; 
					}
				} else if( e.isMetaDown() ) {
					newSelected = !selected;   // toggle item
					if( newSelected ) {		// deselect all except uns
						collToRemove	= selectedTracks.getAll();
						editor.editRemove( id, collToRemove.toArray( new SessionObject[ collToRemove.size() ]));
						editor.editAdd( id, t );
					} else {				// select all except us
						editor.editRemove( id, t );
						collToAdd		= tracks.getAll();
						collToAdd.removeAll( selectedTracks.getAll() );
						editor.editAdd( id, collToAdd.toArray( new SessionObject[ collToAdd.size() ]));
					}
				} else {
					if( e.isShiftDown() ) {
						newSelected = !selected;
						if( newSelected ) {
							editor.editAdd( id, t );		// add us to selection
						} else {
							editor.editRemove( id, t );		// remove us from selection
						}
					} else {
						if( selected ) return;						// no action
						collToRemove	= selectedTracks.getAll();
						editor.editRemove( id, collToRemove.toArray( new SessionObject[ collToRemove.size() ]));
						editor.editAdd( id, t );	// deselect all except uns
					}
				}
				editor.editEnd( id );
//				repaint();
		    }
		};

//		this.addMouseListener( ml );
		
		trackListener = new MapManager.Listener() {
			public void mapChanged( MapManager.Event e ) {
				trackMapChanged( e );
			}
		
			public void mapOwnerModified( MapManager.Event e )
			{
				if( e.getOwnerModType() == SessionObject.OWNER_RENAMED ) {
					checkTrackName();
				}
				trackChanged( e );
			}
		};

		selectedTracksListener = new SessionCollection.Listener() {
			public void sessionCollectionChanged( SessionCollection.Event e )
			{
				if( e.collectionContains( t )) {
					if( selected != selectedTracks.contains( t )) {
						selected = !selected;
						repaint();
					}
				}
			}
			
			public void sessionObjectChanged( SessionCollection.Event e ) { /* ignore */ }
			public void sessionObjectMapChanged( SessionCollection.Event e ) { /* ignore */ }
		};
	}
	
	/**
	 *	@param e
	 */
	protected void trackChanged( MapManager.Event e )
	{
		// nada
	}

	/**
	 *	@param e
	 */
	protected void trackMapChanged( MapManager.Event e )
	{
		// nada
	}
	
	protected static String getResourceString( String key )
	{
		return( AbstractApplication.getApplication().getResourceString( key ));
	}
	
	public void setTrack( final Track t, final SessionCollection tracks,
						  final MutableSessionCollection selectedTracks )
	{
		final boolean wasListening = isListening;
		stopListening();
		this.t				= t;
		this.tracks			= tracks;
		this.selectedTracks	= selectedTracks;
		if( wasListening ) startListening();
	}
	   
	public void setEditor( MutableSessionCollection.Editor editor )
	{
		if( this.editor != editor ) {
			this.editor = editor;
			if( editor != null ) {
				this.addMouseListener( ml );
			} else {
				this.removeMouseListener( ml );
			}
		}
	}

	public void dispose()
	{
		stopListening();
//		pan.dispose();
	}
	
	/**
	 *  Determines if this row is selected
	 *  i.e. is part of the selected transmitters
	 *
	 *	@return	<code>true</code> if the row (and thus the transmitter) is selected
	 */
	public boolean isSelected()
	{
		return selected;
	}

	public Track getTrack()
	{
		return t;
	}

	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );
//System.err.println(" - ");		
		final Graphics2D	g2	= (Graphics2D) g;
		final int			h	= getHeight();
		final int			w	= getWidth();
		final int			x	= Math.min( w - 36, lbTrackName.getX() + lbTrackName.getWidth() );
		
//		g2.setColor( colrDarken );
//		g2.fillRect( 0, 19, w, 2 );
	g2.translate( x, 0 );
		g2.setPaint( pntDarken );
		g2.fillRect( -x, 19, x + 36, 2 );
//		g2.setColor( selected ? colrSelected : colrUnselected );
//		g2.fillRect( 0, 0, 5, h );
//		g2.fillRect( 5, 0, w, 20 );
		g2.setPaint( selected ? pntSelected : pntUnselected );
		g2.fillRect( -x, 0, x + 36, 20 );
	g2.translate( -x, 0 );

//		g2.setPaint( pntTopBorder );
//		g2.fillRect( 0, 0, w, 8 );
	g2.translate( 0, h - 8 );
		g2.setPaint( GradientPanel.pntBottomBorder );
//		g2.fillRect( 0, h - 9, w, 8 );
		g2.fillRect( 0, 0, w, 8 );
	g2.translate( 0, 8 - h );

	}

	public void paintChildren( Graphics g )
	{
		super.paintChildren( g );
		final Graphics2D	g2	= (Graphics2D) g;
		final int			w	= getWidth();
		g2.setPaint( GradientPanel.pntTopBorder );
		g2.fillRect( 0, 0, w, 8 );
	}

	protected void checkTrackName()
	{
		final String name = (t != null) ? t.getName() : null;
		if( !lbTrackName.getText().equals( name )) {
			lbTrackName.setText( name );
		}
	}
	
// ---------------- DynamicListening interface ---------------- 

    public void startListening()
    {
    	if( !isListening ) {
    		isListening = true;
        	if( t != null ) {
        		t.getMap().addListener( trackListener );
        		selectedTracks.addListener( selectedTracksListener );
        	}
    		checkTrackName();
    		if( selected != (selectedTracks != null ? selectedTracks.contains( t ) : false) ) {
    			selected = !selected;
    			repaint();
        	}
    	}
    }

    public void stopListening()
    {
    	if( isListening ) {
    		isListening = false;
    		if( t != null ) {
    			t.getMap().removeListener( trackListener );
    			selectedTracks.removeListener( selectedTracksListener );
    		}
    	}
    }
}