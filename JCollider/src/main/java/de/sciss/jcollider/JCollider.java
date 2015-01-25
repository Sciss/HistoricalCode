/*
 *  JCollider.java
 *  (JCollider)
 *
 *  Copyright (c) 2004-2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.jcollider;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 *  This is a helper class containing utility static functions
 */
public abstract class JCollider
{
	public static final String VERSION	= "1.0.0";
	private static final ResourceBundle resBundle = ResourceBundle.getBundle( "JColliderStrings" );
//	private static final Preferences prefs = Preferences.userNodeForPackage( JCollider.class );

	/**
	 *	<code>true</code> if we're running on Mac OS X.
	 *	This value can be used to check if certain UGens
	 *	are available, or to find the location of scsynth.
	 */
	public static final boolean	isMacOS		= System.getProperty("os.name").contains("Mac OS");
	/**
	 *	<code>true</code> if we're running on a Windows system
	 */
	public static final boolean	isWindows	= System.getProperty("os.name").contains("Windows");

	/**
	 *	This method gets called when one tries
	 *	to start the .jar file directly.
	 *	It prints copyright information and
	 *	quits, unless one of the test methods
	 *	is specified in the arguments
	 *
	 *	@param	args	shell arguments. there may be a single argument
	 *					&quot;--test1&quot; or &quot;--test2&quot; to
	 *					run the demos. &quot;--bindefs&quot; to create
	 *					a binary def file from the xml descriptions
	 */
	public static void main(String args[]) {
		System.err.println("\nJCollider v" + VERSION + "\n" +
				getCopyrightString() + "\n\n" +
				getCreditsString() + "\n\n  " +
				getResourceString("errIsALibrary"));
		System.exit(1);
	}

	/**
	 *	Returns the library's version.
	 *
	 *	@return	the current version of JCollider
	 */
	public static String getVersion()
	{
		return VERSION;
	}

	/**
	 *	Returns a copyright information string
	 *	about the library
	 *
	 *	@return	text string which can be displayed
	 *			in an about box
	 */
	public static String getCopyrightString()
	{
		return JCollider.getResourceString( "copyright" );
	}

	/**
	 *	Returns a license and website information string
	 *	about the library
	 *
	 *	@return	text string which can be displayed
	 *			in an about box
	 */
	public static String getCreditsString()
	{
		return JCollider.getResourceString( "credits" );
	}

	public static String getResourceString( String key )
	{
		try {
			return resBundle.getString( key );
		}
		catch( MissingResourceException e1 ) {
			return( "[Missing Resource: " + key + "]" );
		}
	}
   
	/**
	 *  Set a font for a container
	 *  and all children we can find
	 *  in this container (calling this
	 *  method recursively). This is
	 *  necessary because calling <code>setFont</code>
	 *  on a <code>JPanel</code> does not
	 *  cause the <code>Font</code> of the
	 *  gadgets contained in the panel to
	 *  change their fonts.
	 *
	 *  @param  c		the container to traverse
	 *					for children whose font is to be changed
	 *  @param  fnt		the new font to apply
	 *
	 *  @see	java.awt.Component#setFont( Font )
	 */
	public static void setDeepFont( Container c, Font fnt )
	{
		final Component[] comp = c.getComponents();
		
//		if( fnt == null ) {
//			final Application app = AbstractApplication.getApplication();
//			if( app == null ) return;
//			fnt = app.getWindowHandler().getDefaultFont();
//		}
		
//		if( c.getFont() != null ) return;
		c.setFont( fnt );
		for (Component aComp : comp) {
			if (aComp instanceof Container) {
				setDeepFont((Container) aComp, fnt);
			} else {
				aComp.setFont(fnt);
			}
		}
	}

	/**
	 *  Displays an error message dialog by
	 *  examining a given <code>Exception</code>. Returns
	 *  after the dialog was closed by the user.
	 *
	 *  @param  component   the component in which to open the dialog.
	 *						<code>null</code> is allowed in which case
	 *						the dialog will appear centered on the screen.
	 *  @param  exception   the exception that was thrown. the message's
	 *						text is displayed using the <code>getLocalizedMessage</code>
	 *						method.
	 *  @param  title		name of the action in which the error occurred
	 *
	 *  @see	java.lang.Throwable#getLocalizedMessage()
	 */
	public static void displayError( Component component, Exception exception, String title )
	{
		String							message = exception.getLocalizedMessage();
		StringTokenizer					tok;
		final StringBuilder strBuf  = new StringBuilder( getResourceString( "errException" ));
		int								lineLen = 0;
		String							word;
		String[]						options = { getResourceString( "buttonOk" ),
													getResourceString( "optionDlgStack" )};
	
		if( message == null ) message = exception.getClass().getName();
		tok = new StringTokenizer( message );
		strBuf.append( ":\n" );
		while( tok.hasMoreTokens() ) {
			word = tok.nextToken();
			if( lineLen > 0 && lineLen + word.length() > 40 ) {
				strBuf.append( "\n" );
				lineLen = 0;
			}
			strBuf.append( word );
			strBuf.append( ' ' );
			lineLen += word.length() + 1;
		}
		if( JOptionPane.showOptionDialog( component, strBuf.toString(), title, JOptionPane.YES_NO_OPTION,
									      JOptionPane.ERROR_MESSAGE, null, options, options[0] ) == 1 ) {
			exception.printStackTrace();
		}
	}
}