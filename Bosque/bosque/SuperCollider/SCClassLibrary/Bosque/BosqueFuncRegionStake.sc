/*
 *	BosqueFuncRegionStake
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
 *	@version	0.29, 14-Sep-08
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
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceModTrack { arg newTrack;
		var args = this.storeArgs;
		args[ 8 ] = newTrack;
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replacePosition { arg newPosition;
		var args = this.storeArgs;
		args[ 9 ] = newPosition;
		this.protDupArgs( args );
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
//			event.player	= player;
			player.initEvent( event );
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
			group = if( position === \pre, { player.bosque.preFilterGroup }, { player.bosque.postFilterGroup });
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
			bndl = MixedBundle.new;
			try {
				player = event.player;
				if( event.respondsTo( \stopToBundle ), {
					event.stopToBundle( this, bndl, player );
				});
				player.freeFuncSynths( this, bndl );
				bndl.send( player.scsynth, BosqueAudioPlayer.bufferLatency + BosqueAudioPlayer.transportDelta );
			} { arg error;
				("FuncRegionStake( " ++ span.asCompileString ++ ", " ++ name.asCompileString ++ ", ... ) stop : " ++ error.what).error;
			};
			event = nil;
		});
	}
}