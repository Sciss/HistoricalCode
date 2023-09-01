/*
 *	BosqueAudioRegionStake
 *	(Bosque)
 *
 *	Copyright (c) 2007-2009 Hanns Holger Rutz. All rights reserved.
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
 *	@version	0.20, 28-Jun-09
 */
BosqueAudioRegionStake : BosqueRegionStake {
	var <audioFile;
	var <fileStartFrame;
	var synth;	// for audio player
	
	*new { arg span, name, track, colr, fadeIn, fadeOut, gain = 1.0, fileStartFrame = 0, audioFile;
		^super.new( span, name, track, colr ?? { Color.green( 0.6 )}, fadeIn, fadeOut, gain ).prInitFARS( fileStartFrame, audioFile );
	}

	storeArgs { ^super.storeArgs ++ [ fileStartFrame, audioFile ]}
	
	prInitFARS { arg argFileStartFrame, argAudioFile;
		fileStartFrame	= argFileStartFrame;
		audioFile			= argAudioFile;
		java				= JavaObject( "de.sciss.timebased.bosque.AudioRegionStake", audioFile.bosque.swing,
							span, name, track, colr, fadeIn, fadeOut, gain, fileStartFrame, audioFile.view );
//		java				= JavaObject( "de.sciss.timebased.RegionStake", audioFile.bosque.swing, span, name );
	}

//	*newFrom { arg anotherRegion;
//		^this.new( anotherRegion.getSpan, anotherRegion.name, anotherRegion.colr, anotherRegion.fadeIn, anotherRegion.fadeOut, anotherRegion.gain, anotherRegion.fileStartFrame, anotherRegion.audioFile );
//	}
//
//	duplicate {
//		^this.class.new( this.getSpan, name, colr, fadeIn, fadeOut, gain, fileStartFrame, audioFile );
//	}
//
	replaceFile { arg newFile;
		var args = this.storeArgs;
		args[ 8 ] = newFile;
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceFileStartFrame { arg newStartFrame;
		var args = this.storeArgs;
		args[ 7 ] = newStartFrame;
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceStart { arg newStart;
		var args = this.storeArgs;
		args[ 0 ] = Span( newStart, span.stop );
		args[ 4 ] = fadeIn.replaceFrames( 0 );
		args[ 7 ] = fileStartFrame + (newStart - span.start);      // !
		this.protFixFade( args );
		this.protDupArgs( args );
		^this.class.new( *args );
	}
	
	replaceStop { arg newStop;
		var args = this.storeArgs;
		args[ 0 ] = Span( span.start, newStop );
		args[ 5 ] = fadeOut.replaceFrames( 0 );
		this.protFixFade( args );
		this.protDupArgs( args );
		^this.class.new( *args );
	}

//	shiftVirtual { arg delta;
//		^this.class.new( this.getSpan.shift( delta ), name, colr, fadeIn, fadeOut, gain, fileStartFrame, audioFile );
//	}

//	asString {
//		^this.asCompileString;
//	}
	
//	asCompileString {
//		^this.notYetImplemented;
////		^(this.class.name ++ "( "++span.asCompileString++", "++name.asCompileString++", "++colr.asCompileString++", "++fadeIn.asCompileString++", "++fadeOut.asCompileString++", "++gain.asCompileString )");
//	}
		
	playToBundle { arg bndl, player, frameOffset = 0;
		var buffer, s, durFrames, durSecs, fadeFrames, fadeInSecs, fadeOutSecs;
		
//		SynthDef( \bosqueDiskIn ++ numChannels, { arg out, i_bufNum, i_dur, i_fadeIn, i_fadeOut, gate = 1, amp ... })
		if( track.busConfig.isNil, { ^this });

		s			= player.scsynth;
		buffer 		= Buffer( s, 32768, audioFile.numChannels );
// allocReadMsg doesn't allow leaveOpen !!!
//		bndl.addPrepare( buffer.allocReadMsg( audioFile.path, fileStartFrame + frameOffset ));

		bndl.addPrepare( buffer.allocMsg );
		bndl.addPrepare( buffer.cueSoundFileMsg( audioFile.path, fileStartFrame + frameOffset ));
		synth		= Synth.basicNew( audioFile.synthDefName, player.scsynth );
//		(max( 0, span.length - frameOffset ) / s.sampleRate).postln;
		durFrames		= max( 0, span.length - frameOffset );
		durSecs		= durFrames / s.sampleRate;
		fadeFrames	= if( frameOffset == 0, { min( durFrames, fadeIn.numFrames )}, 0 );  // XXX a little bit cheesy!
		fadeInSecs	= fadeFrames / s.sampleRate;
		fadeOutSecs	= min( durFrames - fadeFrames, fadeOut.numFrames ) / s.sampleRate;
		bndl.add( synth.newMsg( player.diskGroup, [ \i_bufNum, buffer.bufnum, \i_dur, durSecs, \i_fadeIn, fadeInSecs, \i_finTyp, fadeIn.type, \i_fadeOut, fadeOutSecs, \i_foutTyp, fadeOut.type, \amp, gain, \out, track.busConfig.bus.index ]));
		player.nw.register( synth );
		UpdateListener.newFor( synth, { arg upd, obj, what;
			if( what === \n_end, {
				upd.remove;
				buffer.close;
				buffer.free;
//				("n_end : " ++ synth.nodeID).postln;
				synth = nil;
			});
		});
	}
	
	isPlaying {
//		("isPlaying : " ++ if( synth.isNil, "nil", { synth.nodeID })).postln;
		^synth.notNil;
	}
	
	protRemoved {
		if( synth.notNil, {
			synth.server.sendBundle( BosqueAudioPlayer.bufferLatency + BosqueAudioPlayer.transportDelta, synth.freeMsg );
			synth = nil;
		});
	}
}