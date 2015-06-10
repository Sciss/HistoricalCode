/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.11, 02-Aug-09
 */
NuagesUPOutput : NuagesUOutput {
	classvar perms;
	
	*initClass {
		perms = IdentityDictionary.new;
//		perms = Dictionary.new;  // required for point!
	}
	
	protMakeDef { arg defName, inChannels, outChannels;
		var key;
//		key		= inChannels @ outChannels;
		key		= outChannels;
		if( perms.includesKey( key ).not, {
//[ "Putting", key ].postln;
			perms.put( key, Urn.newUsing( Array.series( outChannels )).autoReset_( true ));
		});
		^SynthDef( defName, { arg in, out, volume = 1;
			var pre, post, sig, peak, monoPeak, offsets;
			pre		= In.ar( in, inChannels );
			offsets	= Control.names( \offset ).ir( Array.series( inChannels ));
			post		= (pre * volume).asArray;
			offsets.do({ arg off, ch; Out.ar( out + off, post[ ch ])});
//			sig		= Array.fill( outChannels, { arg ch; post[ ch % inChannels ]});
//			Out.ar( out, sig );
		}, [ nil, nil, 0.05 ]);
	}
	
	protSetAttrToBundle { arg bndl, synth;
		var urn, offsets, key;
//		key		= this.getAudioInputBus.numChannels @ this.numChannels;
		key		= this.numOutChannels;
//[ "Checking", key ].postln;
		urn		= perms.at( key );
		if( urn.notNil, {
			offsets = Array.fill( this.getAudioInputBus.numChannels, { urn.next });
//			offsets.postln;
			bndl.add( synth.setnMsg( \offset, offsets ));
		}, {
			TypeSafe.methodWarn( thisMethod, "No urn found" );
		});
	}
}