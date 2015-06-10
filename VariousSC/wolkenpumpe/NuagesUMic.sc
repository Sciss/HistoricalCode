/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.14, 16-Apr-09
 *	@author	Hanns Holger Rutz
 */
NuagesUMic : NuagesUBasicMic {
	classvar <>channel		= 0;
	classvar <>numChannels	= 2;
	
	var feedback	= 0.0;

	*new { arg server;
		^super.new( server ).prInitMic;
	}
	
	prInitMic {
		this.protPrependAttr( UnitAttr( \feedback,  ControlSpec( 0, 1, \lin ), \normal, \getFeedback, \setFeedback, nil, false ));

		this.addMic( Bus( \audio, server.options.numOutputBusChannels + channel, numChannels ));
		this.setMicIndex( 0 );
	}
	
	setFeedback { arg amount;
		this.protMakeBundle({ arg bndl; this.setFeedbackToBundle( bndl, amount )});
	}
	
	setFeedbackToBundle { arg bndl, amount;
		if( amount != feedback, {
			feedback = amount;
			if( synth.notNil, {
				synth.set( \feedback, feedback );
			});
			this.tryChanged( \attrUpdate, \feedback );
		});
	}
	
	getFeedback {
		^feedback;
	}
	
	// ----------- protected instance methods -----------
	
	protSetAttrToBundle { arg bndl, synth;
//		\channel, micBus.index
		bndl.add( synth.setMsg( \boost, boost, \feedback, feedback ));
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, {
			arg out, in, boost = 1.0, feedback = 0.0;
			var ins, outs, dly, amp, slope, comp, bandFreqs, flt, band, pureIn;
			
//			pureIn	= SoundIn.ar( Array.fill( numChannels, { arg i; channel + i }), boost );
//			pureIn	= In.ar( channel, numChannels ) * boost;
			pureIn	= this.protCreateIn( in, numChannels, numInChannels ) * boost;
			bandFreqs	= [ 150, 800, 3000 ];
			ins		= HPZ1.ar( pureIn ).asArray;
			outs		= 0;
			flt		= ins;
			bandFreqs.do({ arg maxFreq, idx;
				if( maxFreq != bandFreqs.last, {
					band	= LPF.ar( flt, maxFreq );
					flt	= HPF.ar( flt, maxFreq );
				}, {
					band	= flt;
				});
				amp		= Amplitude.kr( band, 2, 2 );
				slope	= Slope.kr( amp );
				comp		= Compander.ar( band, band, 0.1, 1, slope.max( 1 ).reciprocal, 0.01, 0.01 );
				outs		= outs + comp;
			});
			dly = DelayC.ar( outs, 0.0125, LFDNoise1.kr( 5, 0.006, 0.00625 ));
//			outs	= Array.fill( numChannels, { arg ch;
//				dly.at( ch );
//			});
			outs = XFade2.ar( pureIn, dly, feedback.linlin( 0, 1, -1, 1 ));

			Out.ar( out, outs.wrapExtend( numChannels ));
		});
	}

	protDuplicate { arg dup;
		dup.setFeedback( this.getFeedback );
		^super.protDuplicate( dup );
	}
}