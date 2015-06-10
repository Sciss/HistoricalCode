/**
 *	AmpProcess
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 21-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpProcess {
	classvar <>procDurScale = 1.0;  // relative to playing duration

	var <amp, defWatcher, <player, <isPlaying = false;
	
	var routMap, rout;
	
	*new {
		^super.new.prInitProc;
	}
	
	newBundle { ^amp.newBundle }
	
	prInitProc {
		amp			= Amplifikation;
		defWatcher	= amp.defWatcher;
		player		= AmpPlayer;
	}

//	// asynchronous, must be called in routine
//	start { arg fadeTime = 1.0;
//		^this.subclassResponsibility( thisMethod );
//	}

	name { ^this.class.name.asString.copyToEnd( 3 )}

	waitForEnd {
		var cond;
		cond = Condition.new;
		UpdateListener.newFor( this, { arg upd, proc;
			upd.remove;
			cond.test = true; cond.signal;
		}, \stopped );
		cond.wait;
	}
	
	// asynchronous, must be called in routine
	start { arg rmap, fadeTime = 1.0;
//		var rmap;
		this.stop( fadeTime );
//		routMap = AnyMap.new;
		rmap.fadeTime				= fadeTime;
		rmap.keepRunning			= true;
		rmap.cond					= Condition.new;
		rmap.stopQuickly			= false;
		routMap					= rmap;
		isPlaying					= true;
		rout						= Routine({
			this.tryChanged( \started );
			protect {
				this.prTaskBody( rmap );
			} {
				isPlaying = false;
				this.tryChanged( \stopped );
			};
		}).play( SystemClock );
	}
	
//	// asynchronous, must be called in routine
//	stop { arg fadeTime = 1.0;
//		^this.subclassResponsibility( thisMethod );
//	}

	// asynchronous, must be called in routine
	stop { arg fadeTime = 1, quickly = false;
		if( routMap.notNil, {
			routMap.fadeTime		= fadeTime;
			routMap.keepRunning	= false;
			routMap.stopQuickly	= quickly;
			routMap.cond.test		= true;
			routMap.cond.signal;
			routMap				= nil;
		});
		rout						= nil;
	}

	prTaskBody { arg rmap;
		^this.subclassResponsibility( thisMethod );
	}
}