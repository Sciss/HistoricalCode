/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 17-Apr-09
 */
NuagesUVOutput : NuagesUOutput {
	classvar perms;
	
	var baseAzi;
	var rotaAmt	= 0;
	var levelMod	= 1;
	var spread;
	
	*initClass {
		perms = IdentityDictionary.new;
//		perms = Dictionary.new;  // required for point!
	}
	
	*new { arg server;
		^super.new( server ).prInitVOutput;
	}
	
	prInitVOutput {
		this.protPrependAttr(
			UnitAttr( \baseAzi, ControlSpec( 0.0, 360.0, \lin ), \normal, \getBaseAzi, \setBaseAzi, nil, false ),
			UnitAttr( \rotaAmt, ControlSpec( 0.0, 1.0, \lin ), \normal, \getRotaAmt, \setRotaAmt, nil, false ),
//			UnitAttr( \levelMod, ControlSpec( 0.0, 1.0, \lin ), \normal, \getLevelMod, \setLevelMod, nil, false ),
			UnitAttr( \spread, ControlSpec( 0.0, 1.0, \lin ), \normal, \getSpread, \setSpread, nil, false ));
			
		baseAzi	= 360.0.rand;
		spread	= 0.25.rand;
	}

	setBaseAzi { arg value;
		this.protMakeBundle({ arg bndl; this.setBaseAziToBundle( bndl, value )});
	}
	
	setBaseAziToBundle { arg bndl, value;
		if( value != baseAzi, {
			baseAzi = value;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \baseAzi, value ));
			});
			this.tryChanged( \attrUpdate, \baseAzi );
		});
	}
	
	getBaseAzi { ^baseAzi }

	setRotaAmt { arg value;
		this.protMakeBundle({ arg bndl; this.setRotaAmtToBundle( bndl, value )});
	}
	
	setRotaAmtToBundle { arg bndl, value;
		if( value != rotaAmt, {
			rotaAmt = value;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \rotaAmt, value ));
			});
			this.tryChanged( \attrUpdate, \rotaAmt );
		});
	}
	
	getRotaAmt { ^rotaAmt }

	setLevelMod { arg value;
		this.protMakeBundle({ arg bndl; this.setLevelModToBundle( bndl, value )});
	}
	
	setLevelModToBundle { arg bndl, value;
		if( value != levelMod, {
			levelMod = value;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \levelMod, value ));
			});
			this.tryChanged( \attrUpdate, \levelMod );
		});
	}
	
	getLevelMod { ^levelMod }

	setSpread { arg value;
		this.protMakeBundle({ arg bndl; this.setSpreadToBundle( bndl, value )});
	}
	
	setSpreadToBundle { arg bndl, value;
		if( value != spread, {
			spread = value;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \spread, value ));
			});
			this.tryChanged( \attrUpdate, \spread );
		});
	}
	
	getSpread { ^spread }
	
	protMakeDef { arg defName, inChannels, outChannels;
		var key;
//		key		= inChannels @ outChannels;
		key		= outChannels;
		if( perms.includesKey( key ).not, {
//[ "Putting", key ].postln;
			perms.put( key, Urn.newUsing( Array.series( outChannels )).autoReset_( true ));
		});
		^SynthDef( defName, { arg in, out, volume = 1, baseAzi = 0, rotaAmt = 0, levelMod = 1, spread = 0.0;
			var pre, post, sig, peak, monoPeak, pos, level, width, orient, rota, pan, w, noise, rotaSpeed;

			rotaSpeed = 0.1;
			pre = In.ar( in, inChannels ).asArray;
			post = (pre * volume).asArray;
			noise = LFDNoise1.kr( rotaSpeed, mul: (rotaAmt * 2) );
			sig = 0 ! outChannels;
//			outChannels.do({ arg ch; post[ ch % inChannels ]});

			inChannels.do({ arg inCh;
				pos   = (baseAzi / 180) + (inCh / inChannels * 2);
				pos   = pos + noise;
				
				// + rota
				w	 = inCh / (inChannels -1);
				level = ((1 - levelMod) * w) + (1 - w);
				width = (spread * (outChannels - 2)) + 2;
				pan = PanAz.ar( outChannels, post[ inCh ], pos, level, width, 0 );
				pan.do({ arg chanSig, i;
					sig[ i ] = sig[ i ] + chanSig;
				});
			});

			Out.ar( out, sig );
		}, [ nil, nil, 0.05, 0.5 /* baseAzi */, 0.1 /* rotaAmt */, 0.5 /* levelMod */, 0.5 /* spread */ ]);
	}
	
	protSetAttrToBundle { arg bndl, synth;
//		var urn, offsets, key;
//		key		= this.getAudioInputBus.numChannels @ this.numChannels;
//		key		= this.numChannels;
//[ "Checking", key ].postln;
//		urn		= perms.at( key );
//		if( urn.notNil, {
//			offsets = Array.fill( this.getAudioInputBus.numChannels, { urn.next });
//			offsets.postln;
			bndl.add( synth.setMsg( \baseAzi, baseAzi, \rotaAmt, rotaAmt, \levelMod, levelMod, \spread, spread ));
//		}, {
//			TypeSafe.methodWarn( thisMethod, "No urn found" );
//		});
	}
}
