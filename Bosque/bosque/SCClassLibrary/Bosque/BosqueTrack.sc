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
	
	*new { arg trackID, trail;
		var forest, doc, tracks;
		
		forest		= Bosque.default;
		doc			= forest.session;
		tracks		= doc.tracks;
		tracks.do({ arg t; if( t.trackID == trackID, { ^t })});
		
		^super.new.prInit( trackID, trail );
	}

	prInit { arg argID, argTrail;
		trackID	= argID;
		trail	= argTrail;
		forest	= Bosque.default;
//		forest.doWhenSwingBooted({
			java = JavaObject( "de.sciss.timebased.ForestTrack", forest.swing, trackID, trail );
//		});
//		forest.doWhenScSynthBooted({ this.prAudioInit });
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

	name_ { arg str;
		name = str;
		java.setName( name );
		this.changed( \name );
	}
	
	// use with care!!!
	trackID_ { arg id;
		trackID = id;		
	}

	editMute { arg source, bool, ce;
		ce.addPerform( BosqueFunctionEdit({ this.muted = bool }, { this.muted = bool.not }, "Change Track Mute", false ));
	}
	
	editBusConfig { arg source, cfg, ce;
		var oldCfg = busConfig;
		ce.addPerform( BosqueFunctionEdit({ this.busConfig = cfg }, { this.busConfig = oldCfg }, "Change Track Bus", true ));
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
	}
	
	asSwingArg { ^java.asSwingArg }
	
	dispose {
//		java.dispose;
		if( java.notNil, { java.destroy });
//		if( mon.notNil, { mon.stop });
//		if( bus.notNil, { bus.free });
	}
}
