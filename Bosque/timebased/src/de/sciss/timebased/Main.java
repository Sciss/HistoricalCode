package de.sciss.timebased;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;

import de.sciss.timebased.gui.TimelinePanel;

public class Main
{
	public static void main( String[] args )
	{
		EventQueue.invokeLater( new Runnable() { public void run() { new Main(); }});
	}
	
	public Main()
	{
		final JFrame f = new JFrame( "TimeBased Test" );
		f.getContentPane().add( new TimelinePanel(), BorderLayout.CENTER );
		f.setSize( 400, 400 );
		f.setVisible( true );
		f.toFront();
	}
}
