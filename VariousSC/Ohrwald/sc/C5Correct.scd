(
~c5Correct = SynthDef( \c5Correct, { arg amp = 0.4;
	ReplaceOut.ar( 6, In.ar( 6 ) * amp );
}).play( s, nil, \addToTail );
)

(
~eqs = SynthDef( \eqs, { arg db = 3;
	ReplaceOut.ar( 0, BHiShelf.ar( In.ar( 0, 25 ), 1000, 10, db ));
}).play( s, nil, \addToTail );
)

// bosque master vol +2

Ohrwald.boot( launch: false );