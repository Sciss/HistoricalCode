/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	@version	0.14, 19-Jul-08
 */
BosqueTrack {
	var <trackID;
	var java;
//	var <>y = 0, <>height = 1;
	var <muted = false;
	var <forest;
	
	var <busConfig;
	var <name;
	var <trail;
	var <ctrlSpec;
	
	*new { arg trackID, trail;
	
		var forest, doc, tracks;
		
		forest		= Bosque.default;
		doc			= forest.session;
		tracks		= doc.tracks;
		tracks.do({ arg t; if( t.trackID == trackID, { ^t })});
		
		^super.new.prInit( trackID, trail );
	}

	prInit { arg argTrackID, argTrail;
		trackID	= argTrackID;
		trail	= argTrail;
		forest	= Bosque.default;
		java		= JavaObject( "de.sciss.timebased.ForestTrack", forest.swing, trail, trackID );
//		java.setID( java.id );
	}
	
	muted_ { arg bool;
		if( bool != muted, {
			muted = bool;
//			if( mon.notNil, {
//				this.prMonRun;
//			});
			this.changed( \muted, muted );
		});
	}
	
	busConfig_ { arg cfg;
		busConfig = cfg;
		this.changed( \busConfig, cfg );
	}
	
	ctrlSpec_ { arg spec;
		ctrlSpec = spec;
		this.changed( \ctrlSpec, spec );
	}

	name_ { arg str;
		name = str;
		java.setName( name );
		this.changed( \name );
	}
	
	// use with care!!!
	trackID_ { arg id;
		trackID = id;
		java.setID( java.id );
	}

	editMute { arg source, bool, ce;
		ce.addPerform( BosqueFunctionEdit({ this.muted = bool }, { this.muted = bool.not }, "Change Track Mute", false ));
	}
	
	editBusConfig { arg source, cfg, ce;
		var oldCfg = busConfig;
		ce.addPerform( BosqueFunctionEdit({ this.busConfig = cfg }, { this.busConfig = oldCfg }, "Change Track Bus", true ));
	}

	editCtrlSpec { arg source, spec, ce;
		var oldSpec = ctrlSpec;
		ce.addPerform( BosqueFunctionEdit({ this.ctrlSpec = spec }, { this.ctrlSpec = oldSpec }, "Change Track Spec", true ));
	}

	editRename { arg source, newName, ce;
		var oldName = name;
		ce.addPerform( BosqueFunctionEdit({ this.name = newName }, { this.name = oldName }, "Change Track Name", true ));
	}
	
	storeArgs { ^[ trackID ]}

	storeModifiersOn { arg stream;
		if( name.notNil, {
			stream << ".name_(";
			name.storeOn( stream );
			stream << ")";
		});
//		if( y != 0, {
//			stream << ".y_(";
//			y.storeOn( stream );
//			stream << ")";
//		});
//		if( height != 1, {
//			stream << ".height_(";
//			height.storeOn( stream );
//			stream << ")";
//		});
		if( muted, {
			stream << ".muted_(";
			muted.storeOn( stream );
			stream << ")";
		});
		if( busConfig.notNil, {
			stream << ".busConfig_(";
			busConfig.storeOn( stream );
			stream << ")";
		});
		if( ctrlSpec.notNil, {
			stream << ".ctrlSpec_(";
			ctrlSpec.storeOn( stream );
			stream << ")";
		});
	}
	
	asSwingArg { ^java.asSwingArg }
	
	dispose {
//		java.dispose;
		if( java.notNil, { java.destroy; java = nil });
//		if( mon.notNil, { mon.stop });
//		if( bus.notNil, { bus.free });
	}
}
