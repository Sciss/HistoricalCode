/*
 *  PrefComboBox.java
 *  de.sciss.gui package
 *
 *  Copyright (c) 2004-2005 Hanns Holger Rutz. All rights reserved.
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
 *		20-May-05	created from de.sciss.meloncillo.gui.PrefComboBox
 */

package de.sciss.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;

import de.sciss.app.DynamicAncestorAdapter;
import de.sciss.app.DynamicListening;
import de.sciss.app.EventManager;
import de.sciss.app.LaterInvocationManager;
import de.sciss.app.PreferenceEntrySync;

/**
 *  Equips a normal JComboBox with
 *  preference storing / recalling capabilities.
 *  To preserve maximum future compatibility,
 *  we decided to not override setSelectedItem()
 *  and the like but to install an internal
 *  ActionListener. Thus, there are two ways
 *  to alter the gadget state, either by invoking
 *  the setSelectedIndex/Item() methods or by
 *  changing the associated preferences.
 *  The whole mechanism would be much simpler
 *  if we reduced listening to the preference
 *  changes, but a) this wouldn't track user
 *  GUI activities, b) the PrefComboBox can
 *  be used with preferences set to null.
 *  When a preference change occurs, the
 *  setSelectedItem() method is called, allowing
 *  clients to add ActionListeners to the
 *  gadget in case they don't want to deal
 *  with preferences. However, when possible
 *  it is recommended to use PreferenceChangeListener
 *  mechanisms.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.27, 25-Sep-05
 *
 *  @see		java.util.prefs.PreferenceChangeListener
 */
public class PrefComboBox
extends JComboBox
implements  DynamicListening, PreferenceChangeListener,
			LaterInvocationManager.Listener, PreferenceEntrySync
{
	private static final long serialVersionUID = 0x050925L;

	private boolean listening				= false;
	private Preferences prefs				= null;
	private String key						= null;
	private final LaterInvocationManager lim= new LaterInvocationManager( this );
	private java.util.List collKeys			= null;
	private ActionListener listener;

	private Object  defaultValue			= null;

	/**
	 *  Creates a new <code>PrefComboBox</code>
	 *  with default data model and no initial preferences set.
	 */
	public PrefComboBox()
	{
		super();
		init();
	}
	
	private void init()
	{
		new DynamicAncestorAdapter( this ).addTo( this );
		listener = new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				if( EventManager.DEBUG_EVENTS ) System.err.println( "@comb actionPerformed : "+key+" --> "+getSelectedItem() );
				updatePrefs( getSelectedItem() );
			}
		};
	}

	/**
	 *  Because the items in the ComboBox
	 *  can be naturally moved, added and replaced,
	 *  it is crucial to have a non-index-based
	 *  value to store in the preferences. Since
	 *  the actual String representation of the
	 *  the items is likely to be locale specific,
	 *  it is required to add items of class
	 *  StringItem !
	 *
	 *  @param  item	the <code>StringItem</code> to add
	 */
	public void addItem( Object item )
	{
		super.addItem( validateItem( item ));
	}

	/*  Add a new item at a specific index position
	 *  to the gadget. See {@link #addItem( Object ) addItem( Object )}
	 *  for an explanation of the <code>StringItem</code>
	 *  usage.
	 *
	 *  @param  item	the <code>StringItem</code> to add
	 *  @see	de.sciss.meloncillo.util.StringItem
	 */
	public void insertItemAt( Object item, int index )
	{
		super.insertItemAt( validateItem( item ), index );
	}

	private Object validateItem( Object item )
	{
		if( !(item instanceof StringItem) ) {
			item = new StringItem( item.toString(), item.toString() );
		}
		return item;
	}
	
//	public void setSelectedIndex( int index )
//	{
//		if( index < -1 || index >= getItemCount() ) throw new IllegalArgumentException( String.valueOf( index ));
//	
//		setSelectedItem( getItemAt( index ));
//	}
//	
//	public void setSelectedItem( Object item )
//	{
//		if( EventManager.DEBUG_EVENTS ) System.err.println( "@comb setSelectedIndex : "+this.key+" --> "+item );
//		super.setSelectedItem( item );	// will notify action listeners
//		updatePrefs( item );
//	}
	
	private void updatePrefs( Object item )
	{
		String value	= null;
		String oldValue;
	
		if( (prefs != null) && (key != null) ) {
			if( item != null && (item instanceof StringItem) ) {
				value = ((StringItem) item).getKey();
			}
			oldValue = prefs.get( key, null );
			if( EventManager.DEBUG_EVENTS ) System.err.println( "@comb updatePrefs : "+this.key+"; old = "+oldValue+" --> "+value );
			if( (value != null && oldValue == null) ||
				(value != null && !value.equals( oldValue ))) {
				
				prefs.put( key, value );

			} else if( value == null && oldValue != null ) {
				prefs.remove( key );
			}
		}
	}

	public void setPreferenceNode( Preferences prefs )
	{
		setPreferences( prefs, this.key );
	}

	public void setPreferenceKey( String key )
	{
		setPreferences( this.prefs, key );
	}

	public void setPreferences( Preferences prefs, String key )
	{
		if( (this.prefs == null) || (this.key == null) ) {
			defaultValue = getSelectedItem();
		}
		if( listening ) {
			stopListening();
			this.prefs  = prefs;
			this.key	= key;
			startListening();
		} else {
			this.prefs  = prefs;
			this.key	= key;
		}
	}

	public Preferences getPreferenceNode() { return prefs; }
	public String getPreferenceKey() { return key; }

	public void startListening()
	{
		if( prefs != null ) {
			prefs.addPreferenceChangeListener( this );
			this.addActionListener( listener );
			listening = true;
			if( key != null ) {
				laterInvocation( new PreferenceChangeEvent( prefs, key, prefs.get( key, null )));
			}
		}
	}

	public void stopListening()
	{
		if( prefs != null ) {
			prefs.removePreferenceChangeListener( this );
			this.removeActionListener( listener );
			listening = false;
		}
	}
	
	// o instanceof PreferenceChangeEvent
	public void laterInvocation( Object o )
	{
		String  prefsValue	= ((PreferenceChangeEvent) o).getNewValue();
		if( prefsValue == null && defaultValue != null ) {
			updatePrefs( defaultValue );
			return;
		}
		Object  guiItem		= getSelectedItem();
		String  guiValue	= null;
		Object  prefsItem   = null;
		int		i;

		if( guiItem != null && (guiItem instanceof StringItem) ) {
			guiValue = ((StringItem) guiItem).getKey();
		}
		if( (prefsValue == null && guiValue != null) ) {
			if( EventManager.DEBUG_EVENTS ) System.err.println( "@comb lim select null (guiValue was "+guiValue+")" );
			// thow we filter out events when preferences effectively
			// remain unchanged, it's more clean and produces less
			// overhead to temporarily remove our ActionListener
			// so we don't produce potential loops
			this.removeActionListener( listener );
			super.setSelectedItem( null );			// will notify action listeners
			this.addActionListener( listener );
		
		} else if( (prefsValue != null && guiValue == null) ||
				   (prefsValue != null && !prefsValue.equals( guiValue ))) {
			
			for( i = 0; i < getItemCount(); i++ ) {
				guiItem = getItemAt( i );
				if( guiItem != null && ((StringItem) guiItem).getKey().equals( prefsValue )) {
					prefsItem = guiItem;
					break;
				}
			}

			if( EventManager.DEBUG_EVENTS ) System.err.println( "@comb lim select "+prefsItem );
			// thow we filter out events when preferences effectively
			// remain unchanged, it's more clean and produces less
			// overhead to temporarily remove our ActionListener
			// so we don't produce potential loops
			this.removeActionListener( listener );
			super.setSelectedItem( prefsItem );	// will notify action listeners
			this.addActionListener( listener );
		}
	}
	
	public void preferenceChange( PreferenceChangeEvent e )
	{
		if( e.getKey().equals( key )) {
			if( EventManager.DEBUG_EVENTS ) System.err.println( "@comb preferenceChange : "+key+" --> "+e.getNewValue() );
			lim.queue( e );
		}
	}
}
