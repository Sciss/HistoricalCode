(
~inPath = "/Volumes/Kalligraphie070107_1/Qi-Gong1EQEd.aif";
m = ScissUtil.readMarkersFromAIFF( ~inPath );
m = m.collect(_.pos);
//m = m.slide( 2, 1 ).clump( 2 )
m = m.clump( 2 );
m = m.collect({ arg x; Span( x[ 0 ], x[ 1 ])});
//m[ 1 ]
~maxFrames = m.collect(_.length).maxItem;
~buf = Array.fill( 2, { Signal.newClear( ~maxFrames )});
~outPath = OhrwaldUtil.workDir +/+ "Qi-Gong1Extr.aif";
)

(
Routine({ var sf1, sf2;
	sf1 = SoundFile.openRead( ~inPath );
	sf2 = SoundFile.newUsing( sf1 );
	sf2.openWrite( ~outPath );
	m.do({ arg span, i;
		sf1.seekFrame( span.start );
		sf1.readFrames( ~buf, 0, span.length );
		sf2.writeFrames( ~buf, 0, span.length );
		((i+1)/m.size*100).asInteger.postln;
		0.yield;
	});
	sf1.close; sf2.close;
	"Done.".postln;
}).play( AppClock );
)

e = Eisenkraut.default;
e.addr.connect;
e.sendMsg( '/doc', \open, ~outPath );
// NOW MAKE IT ACTIVE BEFORE CALLING THE NEXT BLOCK!
	
(
	var off = 0;
	m.do({ arg span;
		e.sendMsg( '/doc/active/markers', \add, off, "Mark" );
		off = off + span.length;
	});
)
