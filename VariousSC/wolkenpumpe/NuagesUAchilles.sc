/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	@version	0.16, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUAchilles : NuagesUFilterSynth {
	var speedAttr;
	
	init {
		speedAttr = this.protMakeAttr( \speed, ControlSpec( 0.125, 2.3511, \exp, default: 0.5 ));
	}
	
	protCreateBuffersToBundle { arg bndl, synth, numInChannels, numChannels;
		var	buf, bufSize;
		
		bufSize	= synth.server.sampleRate.asInteger;
		buf		= Buffer.new( server, bufSize, numInChannels );
		bndl.addPrepare( buf.allocMsg );
		bndl.add( synth.setMsg( \aBuf, buf.bufnum ));
		^buf;
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in, aBuf;

			var inp, writeRate, readRate, readPhasor, read;
			var numFrames, writePhasor, old, wet, dry, speed;

			speed		= speedAttr.kr( lag: 0.1 );
			inp			= this.protCreateIn( in, numInChannels, numChannels );
			numFrames		= BufFrames.kr( aBuf );
			writeRate 	= BufRateScale.kr( aBuf );
			readRate	 	= writeRate * speed;
			readPhasor	= Phasor.ar( 0, readRate, 0, numFrames );
			read			= BufRd.ar( numChannels, aBuf, readPhasor, 0, 4 );
			writePhasor	= Phasor.ar( 0, writeRate, 0, numFrames );
			old			= BufRd.ar( numChannels, aBuf, writePhasor, 0, 1 );
			wet			= SinOsc.ar( 0, ((readPhasor - writePhasor).abs / numFrames * pi) );
			dry			= 1 - wet.squared;
			wet			= 1 - (1 - wet).squared;
			BufWr.ar( (old * dry) + (inp * wet), aBuf, writePhasor );
			
//[ speed, inp, numFrames, writeRate, readRate, readPhasor, read, writePhasor, old, wet, dry ].postln;
			
			this.protCreateXOut( read, numChannels );
		});
	}
}