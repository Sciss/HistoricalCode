/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.13, 26-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	could use LocalBuf?
 */
NuagesUConvolve : NuagesUFilterSynth {
	var speedAttr;
	var kernelSize	= 2048;
	
	// ----------- instantiation -----------
	
	init {
		speedAttr = this.protMakeAttr( \speed, ControlSpec( 0, 1, \lin, default: 0.5 ));
	}
	
	// ----------- public instance methods -----------

	numAudioInputs { ^2 }
	getAudioInputName { arg idx; ^if( idx == 1, \resp )}

	protCreateBuffersToBundle { arg bndl, synth, numInChannels, numChannels;
		var	bufBase, bufs;
		bufBase = server.bufferAllocator.alloc( numChannels );
		bufs = Array.fill( numInChannels, { arg i;
			Buffer.new( server, kernelSize, 1, i + bufBase );
		});
		bufs.do({ arg buf; bndl.addPrepare( buf.allocMsg )});
		bndl.add( synth.setMsg( \i_kernel, bufs.first.bufnum ));
		^bufs;
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		var numInChan1, numInChan2;
		#numInChan1, numInChan2 = numInChannels;
		^SynthDef( defName, { arg in, in2, i_kernel, boost = 0.5;
			var ins1, ins2, outs, flt, kernelSize, trigFreq, speed;
			var recTrig, convTrig, convTrig1, convTrig2, irSig, conv1, conv2, conv, sr, high;
				
			speed	= speedAttr.kr;
			sr		= SampleRate.ir;
			kernelSize = BufFrames.ir( i_kernel );
			high		= sr / (kernelSize + (ControlDur.ir * sr));
			trigFreq	= speed.linexp( 0, 1, high / 1000, high );			// periode 48ms bis 48sec bei 44.1 kHz

			ins1		= this.protCreateIn( in,  numInChan1, extend: numChannels );
						
			ins2		= this.protCreateIn( in2, numInChan2, extend: numChannels );
			ins2		= Compander.ar( ins2, ins2, 0.5, 1, 0.05, 0.001, 0.02 );
			recTrig	= Impulse.kr( trigFreq );
			irSig	= ins2 * (1.0 - Sweep.ar( recTrig, SampleRate.ir / kernelSize));
			irSig.do({ arg inp, ch; RecordBuf.ar( inp, i_kernel + ch, recTrig, loop: 0, trigger: recTrig )});
			convTrig	= TDelay.kr( recTrig, BufDur.kr( i_kernel ));
			convTrig1 = PulseDivider.kr( convTrig, 2, 1 );
			convTrig2 = PulseDivider.kr( convTrig, 2, 0 );
		
			conv1	= ins1.collect({ arg inp, ch; Convolution2.ar( inp, i_kernel, convTrig1, kernelSize )});
			conv2	= ins1.collect({ arg inp, ch; Convolution2.ar( inp, i_kernel, convTrig2, kernelSize )});
			conv		= XFade2.ar( conv1, conv2, LFTri.kr( trigFreq * 0.5, 1 ));
						
			flt		= LeakDC.ar( conv * boost );
			
			Assertion({ flt.size == numChannels });
			
			this.protCreateXOut( flt );
		}, [ nil, nil, nil, 0.1 ]);
	}
}