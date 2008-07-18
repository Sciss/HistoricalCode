package de.sciss.timebased;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;

import javax.swing.JFrame;

import de.sciss.app.Document;
import de.sciss.app.DocumentHandler;
import de.sciss.app.DocumentListener;
import de.sciss.common.BasicApplication;
import de.sciss.common.BasicMenuFactory;
import de.sciss.common.BasicWindowHandler;
import de.sciss.gui.MenuAction;
import de.sciss.timebased.gui.TimelinePanel;

public class Main
extends BasicApplication
{
	private static final double VERSION	= 0.11;
	
	public static void main( final String[] args )
	{
		EventQueue.invokeLater( new Runnable() { public void run() { new Main( args );}});
	}
	
	public Main()
	{
		this( new String[0] );
	}
	
	public Main( String... args )
	{
		super( Main.class, "TimeBased" );
		
		init();
		
		if( (args.length > 0) && args[ 0 ].equals( "--test" )) {
			final JFrame f = new JFrame( "TimeBased Test" );
			f.getContentPane().add( new TimelinePanel(), BorderLayout.CENTER );
			f.setSize( 400, 400 );
			f.setVisible( true );
			f.toFront();
		}
	}
	
	public double getVersion() { return VERSION; }
	public String getMacOSCreator() { return "????"; };
	
	protected BasicWindowHandler createWindowHandler()
	{
		return new BasicWindowHandler( this );
	}
	
	protected BasicMenuFactory createMenuFactory()
	{
		return new BasicMenuFactory( this ) {
			public void addMenuItems() { /* none */ }
			public void showPreferences() { /* none */ }
			public void openDocument( File f ) { /* none */ }
			public MenuAction getOpenAction() { return null; }
		};
	}
	
	protected DocumentHandler createDocumentHandler()
	{
		return new DocumentHandler() {
			public void addDocument( Object source, Document doc ) { /* nothing */ }
			public void removeDocument( Object source, Document doc ) { /* nothing */ }
			public void setActiveDocument( Object source, Document doc ) { /* nothing */ }
			public void addDocumentListener( DocumentListener l ) { /* nothing */ }
			public void removeDocumentListener( DocumentListener l ) { /* nothing */ }
			public Document getActiveDocument() { return null; }
			public Document getDocument( int i ) { return null; }
			public int getDocumentCount() { return 0; }
			public boolean isMultiDocumentApplication() { return false; }
		};
	}
}
