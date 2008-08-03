/*
 *	BosqueEnvRegionStake
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
 *	@version	0.11, 25-Jul-08
 */
BosqueEnvRegionStake : BosqueRegionStake {
//	var <java;
//	var playing	= false;
	var <env;		// a BosqueTrail whose stakes are instances of BosqueEnvSegmentStake
	
	*new { arg span, name, track, colr, fadeIn, fadeOut, gain = 1.0, env;
		^super.new( span, name, track, colr ?? { Color.blue( 0.6 )}, fadeIn, fadeOut, gain ).prInitFERS( env );
	}
	
	level {Êarg frame;
		var idx, stake;
		frame = (frame ?? { Bosque.default.session.transport.currentFrame }) - span.start;
		idx   = env.indexOfPos( frame );
		if( idx < 0, { idx = (idx + 2).neg });
		stake = env.get( idx );
		if( stake.isNil, { ^nil });
		^stake.level( frame );
	}

	storeArgs { ^super.storeArgs ++ [ env ]}
	
	prInitFERS { arg argEnv;
		env	= argEnv ?? { this.prCreateDefaultEnv };
		java		= JavaObject( "de.sciss.timebased.bosque.EnvRegionStake", Bosque.default.swing,
							span, name, track, colr, fadeIn, fadeOut, gain, env );
	}
	
//	replaceTrack { arg newTrack;
//		var args, envNew;
//		
//		args 	= this.storeArgs;
//		envNew	= env.createEmptyCopy;
//		envNew.addAll( nil, env.getAll );
//		args[ 2 ] = newTrack;
//		args[ 7 ]	= envNew;
//		^this.class.new( *args );
//	}

	protDupArgs { arg args;
		super.protDupArgs;
		if( args[ 7 ] == env, { args[ 7 ] = env.duplicate });
	}

	replaceStart { arg newStart;
		var delta, spanNew, envNew, stake, args;

		delta	= newStart - span.start;
		spanNew	= Span( newStart, span.stop );

		envNew	= env.createEmptyCopy;
		envNew.addAll( nil, env.getCuttedRange( Span( delta, env.span.stop ), true, Trail.kTouchSplit, delta.neg ));
		if( newStart < span.start, {
			stake = envNew.get( 0 );
//			stake.postcs;
			stake = BosqueEnvSegmentStake( Span( 0, stake.span.start ), stake.startLevel, stake.startLevel );
//			stake.postcs;
			envNew.add( nil, stake );
		});
		
		args		= this.storeArgs;
		args[ 0 ]	= spanNew;
		args[ 4 ]	= fadeIn.replaceFrames( 0 );
		args[ 7 ]	= envNew;
		this.protFixFade( args );
		this.protDupArgs( args );
		^this.class.new( *args );
	}
	
	replaceStop { arg newStop;
		var delta, spanNew, envNew, stake, args;

		delta	= newStop - span.stop;
		spanNew	= Span( span.start, newStop );

		envNew	= env.createEmptyCopy;
		envNew.addAll( nil, env.getCuttedRange( Span( 0, spanNew.length ), true, Trail.kTouchSplit, 0 ));
		if( newStop > span.stop, {
			stake = envNew.get( envNew.numStakes - 1 );
			stake = BosqueEnvSegmentStake( Span( stake.span.stop, stake.span.stop + delta ), stake.stopLevel, stake.stopLevel );
			envNew.add( nil, stake );
		});

		args		= this.storeArgs;
		args[ 0 ]	= spanNew;
		args[ 5 ]	= fadeOut.replaceFrames( 0 );
		args[ 7 ]	= envNew;
		this.protFixFade( args );
		this.protDupArgs( args );
		^this.class.new( *args );
	}
	
//	dirtyDirtyRegionStop { arg newStop;
//		span = Span( span.start, newStop );
//	}

	playToBundle { arg bndl, player, frameOffset = 0;
		// XXX
	}
	
	isPlaying {
		^false; // XXX
	}
	
	protRemoved {
		// XXX
	}
	
	prCreateDefaultEnv {
		var env, segm;
//		var numSegm, start, stop = 0, startLevel, stopLevel = 0.0;
//		numSegm = span.length.div( 4410 ) + 1;
//		segm = Array.fill( numSegm, { arg i;
//			start		= stop;
//			startLevel	= stopLevel;
//			stopLevel		= 1.0.rand;
//			stop			= (((i + 1) / numSegm) * span.length).asInteger;
//			BosqueEnvSegmentStake( Span( start, stop ), startLevel, stopLevel );
//		});
		segm = BosqueEnvSegmentStake( Span( 0, span.length ), 0.0, 0.0 );
		env = BosqueTrail.new;
//		env.addAll( nil, segm );
		env.add( nil, segm );
		^env;
	}

	dispose {
		env.dispose;
		^super.dispose;
	}
}