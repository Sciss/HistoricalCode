/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 21-Jul-08
 */
BosqueEnvRegionStake : BosqueRegionStake {
//	var <java;
//	var playing	= false;
	var <env;		// a BosqueTrail whose stakes are instances of BosqueEnvSegmentStake
	
	classvar array; // for quick envAt calculations
	
	*initClass {
		array = [ /* 0=startLevel */ 0, 1, -99, -99, /* 4=stopLevel */ 0, /* 5=stopTime */ 1.0, /* 6=shapeNumber */ 0, /* 7=curveValue */ 0 ];
	}
	
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
		
//		("stake #" ++ idx ++ "; startLvl " ++ stake.startLevel ++ "; stopLvl " ++ stake.stopLevel).postln;
		
		array[ 0 ] = stake.startLevel;
		array[ 4 ] = stake.stopLevel;
//		array[ 5 ] = stake.span.length;
		array[ 6 ] = stake.shape;
		array[ 7 ] = stake.curve;
//		^array.envAt( max( frame, stake.span.stop ) - stake.span.start );
		^array.envAt( ((frame - stake.span.start) / stake.span.length).clip( 0, 1 ));
	}

	storeArgs { ^super.storeArgs ++ [ env ]}
	
	prInitFERS { arg argEnv;
		env	= argEnv ?? { this.prCreateDefaultEnv };
		java		= JavaObject( "de.sciss.timebased.bosque.EnvRegionStake", Bosque.default.swing,
							span, name, track, colr, fadeIn, fadeOut, gain, env );
	}
	
	replaceStart { arg newStart;
		var delta, spanNew, envNew, args;

		delta	= newStart - span.start;
		spanNew	= Span( newStart, span.stop );
		envNew	= env.createEmptyCopy;

		envNew.addAll( nil, env.getCuttedRange( Span( delta, env.span.stop ), true, Trail.kTouchSplit, delta.neg ));
		
		args		= this.storeArgs;
		args[ 0 ]	= spanNew;
		args[ 4 ]	= fadeIn.replaceFrames( 0 );
		args[ 7 ]	= envNew;
		this.protFixFade( args );
		^this.class.new( *args );
	}
	
	replaceStop { arg newStop;
		var spanNew, envNew, args;

		spanNew	= Span( span.start, newStop );
		envNew	= env.createEmptyCopy;
		envNew.addAll( nil, env.getCuttedRange( Span( 0, spanNew.length ), true, Trail.kTouchSplit, 0 ));

		args		= this.storeArgs;
		args[ 0 ]	= spanNew;
		args[ 5 ]	= fadeOut.replaceFrames( 0 );
		args[ 7 ]	= envNew;
		this.protFixFade( args );
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