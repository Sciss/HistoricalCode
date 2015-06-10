(
r = Routine({
	~dir ="/Volumes/KarminUser/Projekte/Ohrwald/Users/vorwerk/audio_work";
	~files = PathName( ~dir ).files;
	~outFrames = ~files.collect({ arg p; var sf = SoundFile.openRead( p.fullPath ); sf.close; sf.numFrames }).maxItem;
	~outFrames.postln;
	~buf1 = Array.fill( 2, {ÊSignal.newClear( 8192 )});
	~buf2 = Array.fill( 1, {ÊSignal.newClear( 8192 )});
	~buf3 = ~buf2.first;
	21.do({ arg ch;
		"\n\n--------------- PROCESSING % -----------\n".postf( ch + 1 );
		~gaga = (ch + 1).asString;
		~sf = SoundFile.openRead( ~files.detect({ arg p; p.fileName.copyRange( 3, 2 + ~gaga.size ) == ~gaga }).fullPath );
		~sf2 = SoundFile.newUsing( ~sf );
		~sf2.numChannels = 1;
		~sf2.headerFormat = "AIFF";
		~sf2.openWrite( ~dir +/+ "CH-%.aif".format( ch + 1 ));
		~off = 0;
		~lastProg = 0;
		while({ ~off < ~sf.numFrames }, {
			~chunkLen = min( 8192, ~sf.numFrames - ~off );
			~sf.readFrames( ~buf1, 0, ~chunkLen );
			~chunkLen.do({ arg i;
				~buf3[ i ] = (~buf1[ 0 ][ i ] + ~buf1[ 1 ][ i ]) / 2;
			});
			~sf2.writeFrames( ~buf2, 0, ~chunkLen );
			~off = ~off + ~chunkLen;
			~prog = (~off / ~sf.numFrames * 20).asInteger;
			while({ ~lastProg < ~prog }, {
				"#".post;
				~lastProg = ~lastProg + 1;
			});
			0.yield;
		});
		~sf.close;
		~buf3.size.do({ arg i; ~buf3[ i ] = 0.0 });
		while({ ~off < ~outFrames }, {
			~chunkLen = min( 8192, ~outFrames - ~off );
			~sf2.writeFrames( ~buf2, 0, ~chunkLen );
			~off = ~off + ~chunkLen;
		});
		~sf2.close;
	});
	"Done.".postln;
  }).play( AppClock );
 )
 