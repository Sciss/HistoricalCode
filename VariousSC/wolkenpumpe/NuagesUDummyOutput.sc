/**
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 31-Oct-08
 */
NuagesUDummyOutput : NuagesUOutput {
	protMakeDef { arg defName, inChannels, outChannels;
		^SynthDef( defName, { arg in, out, volume = 1;
			var pre, post, sig, peak, monoPeak, offsets;
//			pre		= In.ar( in, inChannels );
//			offsets	= Control.names( \offset ).ir( Array.series( inChannels ));
//			post		= (pre * volume).asArray;
//			offsets.do({ arg off, ch; Out.ar( out + off, post[ ch ])});
//			sig		= Array.fill( outChannels, { arg ch; post[ ch % inChannels ]});
//			Out.ar( out, sig );
		}, [ nil, nil, 0.05 ]);
	}
}