//~path = OhrwaldUtil.workDir +/+ "Ghrn1T1SonaRMosLck22'sRsmp.aif"
~paths = PathName( OhrwaldUtil.workDir ).files.select({ arg p; "Ghrn[1-9][0-9]?T1S[o]?naRMos".matchRegexp( p.fileName )}).collect(_.fileName).sort({ arg a, b; var i1, i2; i1 = a.indexOf( $T ); i2 = b.indexOf( $T ); a.copyRange( 4, i1 - 1 ).asInteger < b.copyRange( 4, i2 - 1 ).asInteger });

//~paths = [ ~paths[ 2 ]];

// (
Routine({
  ~spans = nil;
  ~paths.do({ arg name, k;
  	var list, prog, lastProg, start, stop, sf, buf, off, chunkLen, numFrames;
	"File % ...\n".postf( name );
	sf  = SoundFile.openRead( OhrwaldUtil.workDir +/+ name );
	buf = Array.fill( sf.numChannels, { Signal.newClear( 8192 )});
	off = 0;
	numFrames = sf.numFrames;
	lastProg = 0;
	while({ off < numFrames }, {
		chunkLen = min( 8192, numFrames - off );
		sf.readFrames( buf, 0, chunkLen );
		chunkLen.do({ arg i;
			if( start.isNil, {
				if( buf.any({ arg ch; ch[ i ] != 0 }), {
					start = off + i;
					stop  = nil;
				});
			}, {
				if( buf.every({ arg ch; ch[ i ] == 0 }), {
					if( stop.isNil, {
						stop = off + i;
					}, {
						if( (off + i - stop) >= 4410, {
							list = list.add( Span( start, stop ));
							start = nil;
						});
					});
				}, {
					stop = nil;
				});
			});
		});
		off = off + chunkLen;
		prog = (off / numFrames * 20).asInteger;
		while({ lastProg < prog }, {
			"#".post;
			lastProg = lastProg + 1;
		});
		0.yield;
	});
	if( start.notNil, { // close final span
		list = list.add( Span( start, numFrames ));
	});
	sf.close;
	"\n   found % spans.\n\n".postf( list.size );
	~spans = ~spans.add( list );
  });
  "Done.".postln;
}).play( AppClock );
// )

e = Eisenkraut.default;
e.addr.connect;

//n = 0;

e.sendMsg( '/doc', \open, OhrwaldUtil.workDir +/+ ~paths[ n ]);

// MAKE THAT DOC ACTIVE! THEN:
~spans[ n ].do({ arg span; e.sendMsg( '/doc/active/markers', \add, span.start, "Beg", span.stop, "End" )});
