package de.sciss.timebased.net;

import java.awt.event.MouseEvent;

import de.sciss.timebased.Stake;
import de.sciss.timebased.gui.TrailView;
import de.sciss.timebased.session.Track;
import de.sciss.util.Disposable;

public class NetTrailViewListener
implements TrailView.Listener, Disposable
{
	private final Master		master;
	private final TrailView		view;
	private int					oscID		= -1;
	
	public NetTrailViewListener( Master master, TrailView view )
	{
		this.master	= master;
		this.view	= view;
		view.addListener( this );
	}
	
	public Master getMaster()
	{
		return master;
	}

	public void setID( int oscID )
	{
		this.oscID = oscID;
	}
	
	// ------------ Disposable interface ------------
	
	public void dispose()
	{
		view.removeListener( this );
	}

	// ------------ TrailView.Listener interface ------------
	
	public void mouseAction( MouseEvent e, long frame, float level, float innerLevel,
							 int hitIdx, Track track, Stake stake )
	{
		final String action;
		
		switch( e.getID() ) {
		case MouseEvent.MOUSE_PRESSED:
			action = "pressed";
			break;
		case MouseEvent.MOUSE_RELEASED:
			action = "released";
			break;
		case MouseEvent.MOUSE_MOVED:
		case MouseEvent.MOUSE_DRAGGED:
			action = "moved";
			break;
		default:
			return;	// don't process entered, exited, clicked
		}
		
		final int trackIdx = (track != null) ? view.getTracksTable().indexOf( track ) : -1;
		final int stakeIdx = (stake != null) ? view.getTrail().indexOf( stake, true ) : -1;
		
		master.reply( "/trail", oscID, "mouse", action, frame, level, innerLevel,
		              hitIdx, trackIdx, stakeIdx,
		              e.getX(), e.getY(), e.getModifiers(),
		              e.getButton(), e.getClickCount() );
	}
}