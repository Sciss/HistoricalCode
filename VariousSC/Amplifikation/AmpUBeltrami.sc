/**
 *	AmpUBeltrami
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	Changelog:
 *		28-Aug-09  created
 *
 *	@version	0.13, 28-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpUBeltrami : NuagesUGenAudioSynth {
	classvar verbose 	= false;

	classvar <>ampsH, <>ampsV, <>numFramesH, <>numFramesV;
	classvar <>downDamp = -3;  // dB
	classvar <>domBoost = 12;	// dB

	classvar <>prefNumChannels = 16;
	
	var orientAttr;
	
	*new { arg server;
		^super.new( server ).prInitBeltrami;
	}
	
	*displayName { ^\Beltrami }

	prInitBeltrami {
		orientAttr = this.protMakeAttr( \orient, ControlSpec( 0, 1, \lin, 1, 0 ), shouldFade: true, canMap: false );
	}

	autoPlay { ^false }

	// ----------- protected instance methods -----------

//	protPreferredNumInChannels { arg idx; ^if( buf.isNil, prefNumOutChannels, { buf.numChannels })}
	protPreferredNumOutChannels { ^prefNumChannels }

	protCreateBuffersToBundle { arg bndl, synth, numInChannels, numChannels;
		var	bufBase, bufs, startFrame, numFrames, path, amps, letter, orient;
		
		Assertion({ numChannels == prefNumChannels });
		
		orient    = orientAttr.getValue( this );
		numFrames = if( orient == 0, numFramesH, numFramesV );
		letter    = if( orient == 0, $H, $V );
		amps      = if( orient == 0, ampsH, ampsV );
		bufBase   = server.bufferAllocator.alloc( numChannels );
		bufs      = Array.fill( numChannels, { arg i;
			Buffer.new( server, 32768, 1, i + bufBase );
		});
		bufs.do({ arg buf, ch;
			startFrame	= (numFrames[ ch ] - 44100).max( 0 ).rand;
			path			= Amplifikation.audioDir +/+ "ScanP%%.aif".format( ch + 1, letter );
			bndl.addPrepare( buf.allocMsg( buf.cueSoundFileMsg( path, startFrame )));
		});
		bndl.add( synth.setMsg( \aBuf, bufs.first.bufnum, \domBoost, domBoost.dbamp ));
		bndl.add( synth.setnMsg( \amps, amps * numChannels.collect({ arg ch; if( ch.even, 1, { downDamp.dbamp })})));
		^bufs;
	}
	
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg aBuf, out, /* amp = 1.0, i_fadeIn, gate = 1, x, y, cDomBus, aDomBus, */ domBoost, domLag = 0.05, posFreq = 0.1, emphFreq = 0.09, amtFreq = 0.08;
			var /* env,*/ sig, cDomX, cDomY, cDomEmph, cDomAmt, dist, domSig, selfDom;
			var x, y, emph, amt, cDomSig;
			var outs, subAmp, subDomAmt, domOutSig, subX, subY, amps;

			// dom
			x		= LFNoise1.kr( posFreq / 7, 5, 5 );
			y		= LFNoise1.kr( posFreq, 0.5, 0.5 );
			emph		= LFNoise1.kr( emphFreq, 2, 3 );
			amt		= LFNoise1.kr( amtFreq, 0.5, 0.5 );
			cDomSig	= [ x, y, emph, amt ];

			#cDomX, cDomY, cDomEmph, cDomAmt = cDomSig; // In.kr( cDomBus, 4 );

//			env		= EnvGen.kr( Env.asr( i_fadeIn ), gate, doneAction: 2 ) * amp;
//			domSig	= InFeedback.ar( aDomBus );
			domSig	= LocalIn.ar( 1 );
			domOutSig	= 0.0;
			
			amps		= Control.names([ \amps ]).kr( 1 ! numChannels );
			outs		= numChannels.collect({ arg ch;
				subX		= ch.div( 2 ) + ch.div( 4 );
				subY		= ch & 1;
				dist		= ((subX - cDomX).squared + (subY - cDomY).squared).sqrt.min( 1 );
				selfDom	= 1 - dist;
				subAmp	= (selfDom * cDomEmph) + dist;
				sig		= DiskIn.ar( 1, aBuf + ch, 1 );
	
//				Out.ar( aDomBus, Lag.ar( sig.squared, domLag ) * selfDom * domBoost );
				domOutSig = domOutSig + (Lag.ar( sig.squared, domLag ) * selfDom);
	
				subDomAmt	= cDomAmt * dist;
				sig		= ((sig * domSig) * subDomAmt) + (sig * (1 - subDomAmt));
				sig * subAmp;
			});
			Out.ar( out, outs * amps );
			LocalOut.ar( domOutSig * domBoost );
		});
	}
}