/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 06-Jul-08
 */
BosqueEnvRegionStake : BosqueRegionStake {
	var <java;
//	var playing	= false;
	var <env;		// a BosqueTrail whose stakes are instances of BosqueEnvSegmentStake
	
	*new { arg span, name, track, colr, fadeIn, fadeOut, gain = 1.0, env;
		^super.new( span, name, track, colr ?? { Color.blue( 0.6 )}, fadeIn, fadeOut, gain ).prInitFERS( env );
	}

	storeArgs { ^super.storeArgs ++ [ env ]}
	
	prInitFERS { arg argEnv;
		env	= argEnv ?? { this.prCreateDefaultEnv };
		java		= JavaObject( "de.sciss.timebased.ForestEnvRegionStake", Bosque.default.swing,
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
		var env, numSegm, segm, start, stop = 0, startLevel, stopLevel = 0.0;
		numSegm = span.length.div( 4410 ) + 1;
		segm = Array.fill( numSegm, { arg i;
			start		= stop;
			startLevel	= stopLevel;
			stopLevel		= 1.0.rand;
			stop			= (((i + 1) / numSegm) * span.length).asInteger;
			BosqueEnvSegmentStake( Span( start, stop ), startLevel, stopLevel );
		});
		env = BosqueTrail.new;
		env.addAll( nil, segm );
		^env;
	}
}