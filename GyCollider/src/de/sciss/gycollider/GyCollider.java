package de.sciss.gycollider;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

public class GyCollider
{
	private final Binding		binding;
	private final GroovyShell	shell;
	
	private GyCollider()
	{
		binding = new Binding();
		shell	= new GroovyShell( binding );

		final JFrame f = new JFrame();
		final Container cp = f.getContentPane();
		final JTextArea ggText = new JTextArea( 20, 80 );
		final Action actionEval = new AbstractAction( "Eval" ) {
			public void actionPerformed( ActionEvent e )
			{
				try {
					final Caret c = ggText.getCaret();
					int dot = c.getDot();
					int mark = c.getMark();
					if( dot == mark ) {
						final int line = ggText.getLineOfOffset( dot );
						dot = ggText.getLineStartOffset( line );
						mark = ggText.getLineEndOffset( line );
					}
					eval( ggText.getText( Math.min( dot, mark ), Math.abs( mark - dot )));
				}
				catch( BadLocationException ble ) {}
			}
		};
		final JButton ggEval = new JButton( actionEval );
		final InputMap imap = ggText.getInputMap();
		final ActionMap amap = ggText.getActionMap();
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_MASK ), "eval" );
		amap.put( "eval", actionEval );
		ggText.setTabSize( 4 );
		ggText.setFont( new Font( "Monaco", Font.PLAIN, 11 ));
//		ggText.putClientProperty( com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY, Boolean.TRUE );
		ggText.setWrapStyleWord(  true  );
		ggText.setLineWrap( true );
		
		cp.add( new JScrollPane( ggText ), BorderLayout.CENTER );
		cp.add( ggEval, BorderLayout.SOUTH );
		
		f.pack();
		f.setLocationRelativeTo( null );
		f.setVisible( true );
	}
	
	private void eval( String code )
	{
		code = "import de.sciss.jcollider.*;\nimport de.sciss.net.*;\n" + code;
		final Object result = shell.evaluate( code );
		System.out.println( result );
	}
	
	public static void main( String[] args )
	{
		EventQueue.invokeLater( new Runnable() {
			public void run()
			{
				new GyCollider();
			}
		});
	}
}
