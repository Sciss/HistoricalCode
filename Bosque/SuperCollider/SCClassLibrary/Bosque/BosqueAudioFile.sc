/*
 *	BosqueAudioFile
 *	(Bosque)
 *
 *	Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
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
 *	Changelog:
 */

/**
 *	@author	Hanns Holger Rutz
 *	@version	0.14, 23-Jul-08
 */
BosqueAudioFile : Object {
	classvar	<>overviews	= true;

	var <bosque;
	var <path;
	var <view;
	var <name;
	var <numChannels;
	var <numFrames;
//	var <regions;
	var <synthDefName;
	
	*new { arg path;
		var bosque, doc, audioFiles;
		
		bosque		= Bosque.default;
		doc			= bosque.session;
		audioFiles	= doc.audioFiles;
		audioFiles.do({ arg af; if( af.path == path, { ^af })});
		
		^super.new.prInit( path );
	}
	
	prInit { arg argPath;
		var sf;

		path			= argPath;
		bosque		= Bosque.default;
		sf			= SoundFile.openRead( path );
		if( sf.isNil, { Error( "Could not open audio file '" ++ path ++ "'" ).throw });
		sf.close;
		name			= PathName( argPath ).fileNameWithoutExtension;
		numChannels	= sf.numChannels;
		synthDefName	= \bosqueDiskIn ++ numChannels;
		numFrames		= sf.numFrames;
//		regions		= BosqueSessionCollection.new;

		if( overviews, {
			bosque.doWhenSwingBooted({
				view = JavaObject( "de.sciss.swingosc.SoundFileView", bosque.swing );
				view.setGridPainted( false );
				view.setTimeCursorPainted( false );
	//			view.setBackground( Color.new255( 0xFF, 0xFF, 0xFF, 0x7F ));
	//			view.setObjWaveColors( Color.black ! numChannels );
				view.setCacheManager( bosque.cacheManager );
				view.readSndFile( path, 0, numFrames );
			});
		});
	}
	
	dispose {
		if( view.notNil, { view.dispose; view.destroy });
//		if( regions.size > 0, { "BosqueAudioFile.dispose : regions not empty".warn });
//		regions.clear;
	}
	
	// XXX doc.audioFiles sould not have a server instance !!!
	asSwingArg { "BosqueAudioFile:asSwingArg : INVALID".warn; ^nil.asSwingArg }
	
	storeArgs { ^[ path ]}
}