/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.13, 06-Jul-08
 */
BosqueFuncRegionStake : BosqueRegionStake {
	var <eventName;
	var <>modTrack;
	var <position	= \pre;
//	var playing	= false;
	var <event, <group;
	
	*new { arg span, name, track, colr, fadeIn, fadeOut, gain = 1.0, eventName, modTrack, position = \pre;
		^super.new( span, name, track, colr ?? { Color.red( 0.6 )}, fadeIn, fadeOut, gain ).prInitFFRS( eventName, modTrack, position );
	}

	storeArgs { ^super.storeArgs ++ [ eventName, modTrack, position ]}
	
	prInitFFRS { arg argEventName, argModTrack, argPosition;
		eventName		= argEventName;
		modTrack		= argModTrack;
		position		= argPosition;
					// not abstract in java!
		java			= JavaObject( "de.sciss.timebased.bosque.BosqueRegionStake", Bosque.default.swing,
				 				span, name, track, colr, fadeIn, fadeOut, gain );
	}

	replaceEventName { arg newName;
		var args = this.storeArgs;
		args[ 7 ] = newName;
		^this.class.new( *args );
	}

	replaceModTrack { arg newTrack;
		var args = this.storeArgs;
		args[ 8 ] = newTrack;
		^this.class.new( *args );
	}

	replacePosition { arg newPosition;
		var args = this.storeArgs;
		args[ 9 ] = newPosition;
		^this.class.new( *args );
	}
	
	playToBundle { arg bndl, player, frameOffset = 0;
		var durFrames, durSecs, busIndex, numChannels;
		
		if( eventName.isNil, { ^this });
		
		try {
//			("try " ++ eventName).postln;
			event		= eventName.interpret;
//			("event = " ++ event).postln;
			if( event.isNil, { ^this });
			event.player	= player;
			durFrames		= max( 0, span.length - frameOffset );
			durSecs		= durFrames / player.scsynth.sampleRate;
			
//			"1".postln;
			if( modTrack.notNil and: { modTrack.busConfig.notNil }, {
//				"2".postln;
				busIndex		= modTrack.busConfig.bus.index;
				numChannels	= if( position === \pre, {
					modTrack.busConfig.numInputs;
				}, {
					modTrack.busConfig.numOutputs;
				});
//				"3".postln;
			});
//			"4".postln;
			group = if( position === \pre, { player.forest.preFilterGroup }, { player.forest.postFilterGroup });
//			"5".postln;
			if( event.playToBundle( this, bndl, player, durSecs, frameOffset, busIndex, numChannels, group, position ), {
//				"HHHHHHHH".postln;
//				playing = true;
				UpdateListener.newFor( event, { arg upd, obj, what, param;
					if( (what === \playing) and: { param.not }, {
						upd.remove;
//						playing = false;
						event = nil;
					});
				});
			}, {
				event = nil;
			});
		} { arg error;
			("FuncRegionStake( " ++ span.asCompileString ++ ", " ++ name.asCompileString ++ ", ... ) play : ").postln; error.reportError; // ( ++ error.what).error;
			event = nil;
		};
	}
	
	isPlaying {
		^event.notNil; // playing;
	}
	
	protRemoved {
		var bndl, player;
		if( event.notNil, {
			bndl = OSCBundle.new;
			try {
				player = event.player;
				event.stopToBundle( this, bndl, player );
				player.freeFuncSynths( this, bndl );
				bndl.send( player.scsynth, BosqueAudioPlayer.bufferLatency + BosqueAudioPlayer.transportDelta );
			} { arg error;
				("FuncRegionStake( " ++ span.asCompileString ++ ", " ++ name.asCompileString ++ ", ... ) stop : " ++ error.what).error;
			};
			event = nil;
		});
	}
}