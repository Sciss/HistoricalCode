Ohrwald.boot( launch: false );


x = 21.collect({ arg x; x.asBinaryString( 5 )});
z = x.collect({ arg str; str.collectAs( _.ascii, Array ) - 48 });

//(
//y = { arg bus = 0, amp = 0.01, freq1= 441, freq2 = 661.5, dur = 0.15;
//	var clicks, sig, clickL;
//	clicks = LFPulse.kr( dur.reciprocal, 0, 0.5 ) * LFPulse.kr( (dur * 10).reciprocal, 0, 0.5 );
//	clickL = LagUD.kr( clicks, 0.02, 0.1 );
//	sig = x.collect({ arg str;
//		SinOsc.ar( Demand.kr( clicks, 0, Dseq( str.collectAs( _.ascii, Array ) - 48, 0, inf )) *
//			(freq2 - freq1) + freq1, mul: clickL );
//	});
//	Out.ar( bus, sig * amp );
//}.play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index ]);
//)
//
//y.set( \amp, 0.1 );
//y.free;


(
y = { arg bus = 0, amp = 0.01, freq1= 441, freq2 = 661.5, dur = 0.15, zeroOne;
	var clicks, sig, clickL;
	clicks = LFPulse.kr( dur.reciprocal, 0, 0.5 ) * LFPulse.kr( (dur * 10).reciprocal, 0, 0.5 );
	clickL = LagUD.kr( clicks, 0.02, 0.1 );
	sig = z.collect({ arg seq;
		seq.postln;
		zeroOne = Demand.kr( clicks, 0, Dseq( seq, inf ));
//		zeroOne.poll( clicks );
		SinOsc.ar( zeroOne *
			(freq2 - freq1) + freq1, mul: clickL );
	});
	// C5 lauter
	8.do({ arg i; sig[ i ] = sig[ i ] * 1.5 });
	Out.ar( bus, sig * amp );
}.play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index ]);
)

y.set( \amp, 0.2 );
y.set( \amp, 0.1 );
y.set( \amp, 0.15 );
y.set( \amp, 0.005 );
y.set( \amp, 0.01 );
y.free;


///////////

(
y = { arg bus = 0, amp = 0.1;
	Out.ar( bus, PinkNoise.ar( amp ));
}.play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index + 4 ]);
)

y.set( \amp, 0.5 );
y.free;

/////////// OK. NOW RELATIVE VOLUMES

(
y = SynthDef( \gugu, { arg bus = 0, amp = 0.1, freq = 1;
	var busses, click, pos, gains, noise;
	
	busses = Control.names([ \busses ]).kr( 0 ! 21 );
	click  = Impulse.kr( freq ) - Impulse.kr( 0 );
	pos	  = Stepper.kr( click, 0, 0, 20 );
	gains  = Array.fill( 21, { arg i; 1 - ((i - pos).abs.min( 1 ))});
	gains  = LagUD.kr( gains, 0.02, 0.02 );
	noise  = PinkNoise.ar( amp );
	
	Ohrwald.circChans.do({ arg ch, i;
		Out.ar( ch + bus, noise * gains[ i ]);
	});
}).play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index, \busses, Ohrwald.circChans ]);
)

y.set( \amp, 1 );
y.set( \freq, 10 );
y.free;

///////////

(
y = { arg bus = 0, amp = 0.5;
	Out.ar( bus, PinkNoise.ar( amp ).abs );
}.play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index + 4 ]);
)

y.set( \amp, 4 );
y.set( \amp, 1 );


/////////// OK. NOW RELATIVE VOLUMES

(
y = SynthDef( \gugu, { arg bus = 0, amp = 0.5, freq = 1;
	var busses, click, pos, gains, noise;
	
	busses = Control.names([ \busses ]).kr( 0 ! 21 );
	click  = Impulse.kr( freq ) - Impulse.kr( 0 );
	pos	  = Stepper.kr( click, 0, 0, 20 );
	gains  = Array.fill( 21, { arg i; 1 - ((i - pos).abs.min( 1 ))});
	gains  = LagUD.kr( gains, 0.02, 0.02 );
	noise  = PinkNoise.ar( amp );
	
	Ohrwald.circChans.do({ arg ch, i;
		Out.ar( ch + bus, noise * gains[ i ]);
	});
}).play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index, \busses, Ohrwald.circChans ]);
)

y.free;

//////////// RiCHTIG

(
y = SynthDef( \gugu, { arg bus = 0, amp = 0.5, freq = 1;
	var busses, click, pos, gains, gains2, noise;
	
	busses = Control.names([ \busses ]).kr( 0 ! 21 );
	click  = Impulse.kr( freq ) - Impulse.kr( 0 );
	pos	  = Stepper.kr( click, 0, 0, 20 );
	gains  = Array.fill( 21, { arg i; 1 - ((i - pos).abs.min( 1 ))});
	gains  = LagUD.kr( gains, 0.02, 0.02 );

	gains2  = Array.fill( 21, { arg i; 1 - (((i + 10 % 21)  - pos).abs.min( 1 ))});
	gains2  = LagUD.kr( gains2, 0.02, 0.02 );
	noise  = PinkNoise.ar( amp );
	
	Ohrwald.circChans.do({ arg ch, i;
		Out.ar( ch + bus, noise * (gains[ i ] + gains2[ i ]));
	});
}).play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index, \busses, Ohrwald.circChans ]);
)

y.free;


/////////// LOCAL


(
y = SynthDef( \gugu, { arg bus = 0, amp = 0.5, freq = 1;
	var busses, click, pos, gains, gains2, noise;
	
	busses = Control.names([ \busses ]).kr( 0 ! 21 );
	click  = Impulse.kr( freq ) - Impulse.kr( 0 );
	pos	  = Stepper.kr( click, 0, 0, 20 );
	gains  = Array.fill( 21, { arg i; 1 - ((i - pos).abs.min( 1 ))});
	gains  = LagUD.kr( gains, 0.02, 0.02 );

	gains2  = Array.fill( 21, { arg i; 1 - (((i + 10 % 21)  - pos).abs.min( 1 ))});
	gains2  = LagUD.kr( gains2, 0.02, 0.02 );
	noise  = PinkNoise.ar( amp );
	
	Ohrwald.circChans.do({ arg ch, i;
		if( (i >= 14) && (i <= 19), { Out.ar( ch + bus, noise * (gains[ i ] + gains2[ i ]))});
	});
}).play( Ohrwald.masterGroup, [ \bus, Ohrwald.masterBus.index, \busses, Ohrwald.circChans ]);
)

y.free;
