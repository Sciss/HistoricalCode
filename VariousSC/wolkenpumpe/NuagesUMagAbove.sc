/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	@version	0.14, 26-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@warning	there is a really bad bug i couldn't yet find. dynamically
 *			creating and disposing instances of this class is not
 *			a good idea in a concert!!!! is seems there is a race
 *			condition with allocating / freeing buffers and / or
 *			synths that work with those buffers.
 */
NuagesUMagAbove : NuagesUFilterSynth {
	var threshAttr;
	var bufSize = 1024;
	
	init {
		threshAttr = this.protMakeAttr( \thresh, ControlSpec( 1.0e-3, 1.0e-1, \exp, default: 1.0e-2 ));
	}
	
	protCreateBuffersToBundle { arg bndl, synth, numInChannels, numChannels;
		var	bufBase, bufs;
		bufBase = server.bufferAllocator.alloc( numInChannels );
		bufs = Array.fill( numInChannels, { arg i;
			Buffer.new( server, bufSize, 1, i + bufBase );
		});
		bufs.do({ arg buf; bndl.addPrepare( buf.allocMsg )});
		bndl.add( synth.setMsg( \aBuf, bufs.first.bufnum ));
		^bufs;
	}
	
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in, aBuf;
			var inp, chain, volume, ramp, env, flt, thresh, wet, sig;
			
			thresh		= threshAttr.kr( lag: 0.1 );
			env			= Env([ 0.0, 0.0, 1.0 ], [ 0.2, 0.2 ], [ \step, \linear ]);
			ramp			= EnvGen.kr( env, levelScale: 0.999 ); // 0.999 = bug fix !!!
			volume		= LinLin.kr( thresh, 1.0e-3, 1.0e-1, 32, 4 );
			inp			= this.protCreateIn( in, numInChannels, numChannels );
			chain 		= FFT( aBuf + Array.series( numChannels ), HPZ1.ar( inp ));
			chain 		= PV_MagAbove( chain, thresh );
			flt			= LPZ1.ar( volume * IFFT( chain )) * ramp;
			
			// account for initial dly
			wet			= EnvGen.kr( Env([ 0, 0, 1 ], [ BufDur.kr( aBuf ) * 2, 0.2 ]));
			sig			= (inp * (1 - wet).sqrt) + (flt * wet);
			
			this.protCreateXOut( sig, numChannels );
		});
	}
}