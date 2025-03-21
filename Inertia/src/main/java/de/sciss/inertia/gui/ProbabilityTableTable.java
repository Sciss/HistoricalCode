/*
 *  ProbabilityTableTable.java
 *  Inertia
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
 *		07-Aug-05	created
 */

package de.sciss.inertia.gui;

import de.sciss.gui.ModificationButton;
import de.sciss.inertia.session.ProbabilityTable;
import de.sciss.inertia.session.Session;
import de.sciss.inertia.session.SessionCollection;
import de.sciss.inertia.session.SessionObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProbabilityTableTable
extends de.sciss.inertia.gui.SessionCollectionTable
{
	public ProbabilityTableTable( final Session doc )
	{
		super( 0 );
		setCollection( doc.tables, doc.selectedTables, doc.bird, Session.DOOR_TABLES, doc.getUndoManager() );

		final JButton ggInfo;

		ggInfo	= new ModificationButton( ModificationButton.SHAPE_INFO );
		ggInfo.setEnabled( false );
		ggInfo.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				if( !doc.bird.attemptShared( Session.DOOR_TABLES, 250 )) return;
				try {
					if( doc.selectedTables.size() != 1 ) return;
					
					ProbabilityTable t = (ProbabilityTable) doc.selectedTables.get( 0 );
					new ProbabilityTableEditor( t );
				}
				finally {
					doc.bird.releaseShared( Session.DOOR_TABLES );
				}
			}
		});
		
		addButton( ggInfo );

		doc.selectedTables.addListener( new SessionCollection.Listener() {
			public void sessionCollectionChanged( SessionCollection.Event e )
			{
				if( !doc.bird.attemptShared( Session.DOOR_TABLES, 250 )) return;
				try {
					ggInfo.setEnabled( doc.selectedTables.size() == 1 );
				}
				finally {
					doc.bird.releaseShared( Session.DOOR_TABLES );
				}
			}
			
			public void sessionObjectMapChanged( SessionCollection.Event e ) {}
			public void sessionObjectChanged( SessionCollection.Event e ) {}
		});
	}
	
	protected SessionObject createNewSessionObject( SessionCollection scAll )
	{
		final String name = SessionCollection.createUniqueName( Session.SO_NAME_PTRN,
			new Object[] { new Integer( 1 ), Session.PROB_NAME_PREFIX, "" }, scAll.getAll() );
		final SessionObject so = new ProbabilityTable();
		so.setName( name );
		return so;
	}
}