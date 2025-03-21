/**
 *	(C)opyright 2006 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	@version	0.1, 22-Oct-06
 *	@author	Hanns Holger Rutz
 */
RectCombUnit : GeneratorUnit {
	var defName = nil, speed1 = 0.1, speed2 = 0.1;
	var bufs = nil, synth = nil;
	var bufSize;
	
	classvar <allUnits;			// elem = instances of RectCombUnit
	
	var attributes;

	*initClass {
		allUnits = IdentitySet.new;
	}

	*new { arg server;
		^super.new( server ).prInitRectCombUnit;
	}
	
	cmdPeriod {
		var result;
		
		result		= super.cmdPeriod;
		synth		= nil;
		bufs			= nil;
		
		^result;
	}

	prInitRectCombUnit {
		bufSize			= 1024;

		attributes		= [
			UnitAttr( \speed1, ControlSpec( 0.01, 100, \exp ), \normal, \getSpeed1, \setSpeed1, nil, false ),
			UnitAttr( \speed2, ControlSpec( 0.01, 100, \exp ), \normal, \getSpeed2, \setSpeed2, nil, false )
		];

		allUnits.add( this );
	}
	
	getAttributes {
		^attributes;
	}

	play {
		var bndl;

		bndl = List.new;
		this.playToBundle( bndl );
		server.listSendBundle( nil, bndl );
	}
	
//	/**
//	 *	This method can be called prior to play in order
//	 *	to have the buffer creation ready and decrease latency
//	 */
//	allocBuffer {
//		if( buf.isNil, {
//			if( file.notNil, {
//				buf	= Buffer.new( server, bufSize, file.numChannels );
//				this.protAddGlobalBuffer( buf );
//				buf.allocRead( path, 0, bufSize );
//			}, {
//				TypeSafe.methodWarn( thisMethod, "No path has been specified" );
//			});
//		});
//	}

	freeBuffer {
	 	var bndl;
	 	
	 	bndl = List.new;
		bufs.do({ arg buf;
//			buf.close;
// ZZZ
//			this.protRemoveGlobalBuffer( buf );
			bndl.add( buf.freeMsg );
		});
		if( bndl.notEmpty, {
(this.hash.asString ++ " : free "++bufs).postln;
			bufs = nil;
			server.listSendBundle( nil, bndl );
		});
	}

	playToBundle { arg bndl, position, atk = 0;
		var allocMsg, newMsg, inBus, outBus;

		if( playing, {
			this.stop;
			if( synth.notNil, {�this.protRemoveNode( synth );});
			synth = nil;
		});

		inBus = this.getInputBus;
		if( inBus.notNil, {
			synth	= Synth.basicNew( defName, server );
			outBus	= this.getOutputBus;

			if( bufs.isNil, {
				bufs = this.prAllocConsecutive( numChannels, server, bufSize );

(this.hash.asString ++ " : alloc "++bufs).postln;

// ZZZ
//				this.protAddGlobalBuffer( buf );
				bndl.add( this.prAllocConsecutiveMsg( bufs, synth.newMsg( target, [�\aBuf, bufs.first.bufnum, \speed1, speed1, \speed2, speed2, \in, inBus.index, \out,
					outBus.notNil.if({ outBus.index }, 0 ) ], \addToHead )));
			}, {
				bndl.add( synth.newMsg( target, [�\aBuf, bufs.first.bufnum, \speed1, speed1, \speed2, speed2, \in, inBus.index, \out,
					outBus.notNil.if({ outBus.index }, 0 ) ], \addToHead ));
			});
//bndl.postln;
			this.protAddNode( synth );
			this.protSetPlaying( true );

		}, {
			TypeSafe.methodWarn( thisMethod, "No input bus has been specified" );
		});
	}
	
	prAllocConsecutive { arg numBufs = 1, server, numFrames, numChannels = 1;
		var	bufBase, newBuf;
		bufBase = server.bufferAllocator.alloc( numBufs );
		^Array.fill( numBufs, { arg i;
			Buffer.new( server, numFrames, numChannels, i + bufBase );
		});
	}
	
	prAllocConsecutiveMsg { arg bufs, completionMsg;
		bufs.do({ arg buf;
			completionMsg = buf.allocMsg( completionMsg );
		});
		^completionMsg;
	}
	
	setInputBus { arg bus;
		var outBus;
		
		outBus = this.getOutputBus;
		
		if( outBus.notNil and: {�outBus.numChannels != bus.numChannels }, {
			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
			^this;
		});

		numChannels	= bus.numChannels;
		defName		= ("rectCombUnit" ++ numChannels).asSymbol;
		this.prCacheDef( numChannels );
		^super.setInputBus( bus );
	}

	setOutputBusToBundle { arg bndl, bus;
		var inBus;
		
		inBus = this.getInputBus;
		
		if( inBus.notNil and: {�inBus.numChannels != bus.numChannels }, {
			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
			^this;
		});
		numChannels	= bus.numChannels;
		defName		= ("rectCombUnit" ++ numChannels).asSymbol;
		this.prCacheDef( numChannels );
		^super.setOutputBusToBundle( bndl, bus );
	}

	setOutputBus { arg bus;
		var inBus;
		
		inBus = this.getInputBus;
		
		if( inBus.notNil and: {�inBus.numChannels != bus.numChannels }, {
			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
			^this;
		});
		numChannels	= bus.numChannels;
		defName		= ("rectCombUnit" ++ numChannels).asSymbol;
		this.prCacheDef( numChannels );
		^super.setOutputBus( bus );
	}

	setSpeed1 { arg value;
		speed1 = value;
		synth.set( \speed1, speed1 );
	}

	setSpeed2 { arg value;
		speed2 = value;
		synth.set( \speed2, speed2 );
	}
	
	getSpeed1 {
		^speed1;
	}

	getSpeed2 {
		^speed2;
	}

//	dispose {
//// ZZZ
//		this.freeBuffer;
//		allUnits.remove( this );
//		^super.dispose;	// handles free buf
//	}

	dispose {
		var result;
		allUnits.remove( this );
		result = super.dispose;
		this.freeBuffer;
		^result;
	}
	
	*disposeAll {
		var all;
		
		all = List.newFrom( allUnits );
		all.do({ arg unit; unit.dispose; });
	}

	prCacheDef { arg numChannels;
		var defName, def;
		defName	= ("rectCombUnit" ++ numChannels).asSymbol;
		if( defCache.contains( defName ).not, {
			def = SynthDef( defName, {
				arg in, out, aBuf, speed1 = 0.1, speed2 = 0.24, teeth = 8;

				var inp, chain, volume, ramp, env, width, phase;
				
				env			= Env([ 0.0, 0.0, 1.0 ], [ 0.2, 0.2 ], [ \step, \linear ]);
//				ramp			= Line.kr( dur: 0.2 );
				ramp			= EnvGen.kr( env );

				volume		= 32; // LinLin.kr( thresh, 1.0e-2, 1.0e-0, 4, 1 );
				inp			= In.ar( in, numChannels );

				chain		= FFT( aBuf + Array.series( numChannels ), LPZ1.ar( inp ));
				phase 		= LFTri.kr( speed1, 0, 0.4, 0.5);
				width 		= LFTri.kr( speed2, 0, -0.5, 0.5 ); 
				chain 		= PV_RectComb( chain, teeth, phase, width );
				
				ReplaceOut.ar( out, HPZ1.ar( volume * IFFT( chain )) * ramp );
			}, [ nil, nil, nil, 0.1 ]);
			defCache.add( def );
		});
	}

//	n_go {�arg node;
//		if( node === synth, {
//			currentPosT = SystemClock.seconds;
//			currentPos = startPos;
//		});
//		^super.n_go( node );
//	}
	
	n_end { arg node;
		if( node === synth, {
			synth		= nil;
			this.protSetPlaying( false );
		});
		^super.n_end( node );
	}

	protDuplicate { arg dup;
		dup.setSpeed1( this.getSpeed1 );
		dup.setSpeed2( this.getSpeed2 );
	}
}