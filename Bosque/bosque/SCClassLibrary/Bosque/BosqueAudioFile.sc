/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.13, 21-Aug-07
 */
BosqueAudioFile : Object {
	classvar	<>overviews	= true;

	var <forest;
	var <path;
	var <view;
	var <name;
	var <numChannels;
	var <numFrames;
//	var <regions;
	var <synthDefName;
	
	*new { arg path;
		var forest, doc, audioFiles;
		
		forest		= Bosque.default;
		doc			= forest.session;
		audioFiles	= doc.audioFiles;
		audioFiles.do({ arg af; if( af.path == path, { ^af })});
		
		^super.new.prInit( path );
	}
	
	prInit { arg argPath;
		var sf;

		path			= argPath;
		forest		= Bosque.default;
		sf			= SoundFile.openRead( path );
		if( sf.isNil, { Error( "Could not open audio file '" ++ path ++ "'" ).throw });
		sf.close;
		name			= PathName( argPath ).fileNameWithoutExtension;
		numChannels	= sf.numChannels;
		synthDefName	= \forestDiskIn ++ numChannels;
		numFrames		= sf.numFrames;
//		regions		= BosqueSessionCollection.new;

		if( overviews, {
			forest.doWhenSwingBooted({
				view = JavaObject( "de.sciss.swingosc.SoundFileView", forest.swing );
				view.setGridPainted( false );
				view.setTimeCursorPainted( false );
	//			view.setBackground( Color.new255( 0xFF, 0xFF, 0xFF, 0x7F ));
	//			view.setObjWaveColors( Color.black ! numChannels );
				view.setCacheManager( forest.cacheManager );
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