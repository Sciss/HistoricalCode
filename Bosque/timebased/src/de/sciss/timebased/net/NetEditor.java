package de.sciss.timebased.net;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.sciss.net.OSCBundle;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacket;
import de.sciss.net.OSCPacketCodec;

public abstract class NetEditor
implements de.sciss.timebased.Editor
{
	private final Master				master;
	private final String				cmd;
	// this is the OSC identification, NOT the edit client identifier:
	private int							oscID		= -1;
	private static int					uniqueID	= 0;
	private final Map<Integer,Client>	map			= new HashMap<Integer,Client>();
	
	protected NetEditor( Master master, String cmd )
	{
		this.master = master;
		this.cmd	= cmd;
	}

	public Master getMaster()
	{
		return master;
	}

	public void setID( int oscID )
	{
		this.oscID = oscID;
	}

	public int editBegin( Object source, String name )
	{		
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final int		editID	= uniqueID++;
		final Client	c		= new Client( cmd, master, oscID, editID );
		map.put( editID, c );
		c.add( "beg", name );
		return editID;
	}
	
	public void editEnd( int editID )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = map.remove( editID );
		if( c == null ) throw new IllegalStateException( String.valueOf( editID ));
		c.add( "end" );
		c.flush();
	}

	public void editCancel( int editID )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = map.remove( editID );
		if( c == null ) throw new IllegalStateException( String.valueOf( editID ));
	}
	
	protected Client getClient( int editID )
	{
		final Client c = map.get( editID );
		if( c == null ) throw new IllegalStateException( String.valueOf( editID ));
		
		return c;
	}

	protected static class Client
	{
		private final int				oscID;
		private final int				editID;
		private final Master			master;
		private final String			cmd;
		private OSCBundle				bndl	= null;
		private final List<OSCBundle>	coll	= new ArrayList<OSCBundle>();
		private int						total	= 0x7FFFFFF;
		
		protected Client( String cmd, Master master, int oscID, int editID )
		{
			this.cmd	= cmd;
			this.master	= master;
			this.oscID	= oscID;
			this.editID	= editID;
		}
		
		protected void add( Object... args )
		{
			final Object[] allArgs = new Object[ args.length + 2 ];
			allArgs[ 0 ] = oscID;
			allArgs[ 1 ] = editID;
			System.arraycopy( args, 0, allArgs, 2, args.length );
			
			try {
				final OSCPacket			p		= new OSCMessage( cmd, allArgs );
				final OSCPacketCodec	codec	= OSCPacketCodec.getDefaultCodec();
				final int				pSize	= codec.getSize( p );
				
				if( total + pSize > 0x10000 ) {
					bndl	= new OSCBundle();
					total	= codec.getSize( bndl );
					coll.add( bndl );
				}
				bndl.addPacket( p );
				total += pSize;
			}
			catch( IOException e1 ) {
				e1.printStackTrace();	// XXX what else could we do?
			}
		}
		
		protected void flush()
		{
			for( OSCPacket p : coll ) master.reply( p );
		}
	}
}
