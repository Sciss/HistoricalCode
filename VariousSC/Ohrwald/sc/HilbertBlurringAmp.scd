//////////////////////////// CORE /////////////////////////////////////////////////////////
(
~iters = 4;

Routine({

// (
"================= AMP =================".postln;
{
//	group = ~groups3; // 1;
forkIfNeeded { var sf, off, sf2, buf, buf2, win, winLen, lastProg, prog, chunkLen, mi;
	sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone6CutConvLHPFCCHlbCC.aif" );
	sf2 = SoundFile.newUsing( sf );
	sf2.sampleFormat = "float";
	sf2.openWrite( OhrwaldUtil.workDir +/+ "BigStone6CutConvLHPFCCHlbCCAmp.aif" );
	winLen = 44100;
	buf2 = Signal.newClear( winLen );
	buf  = [Êbuf2 ];
//	win  = Signal.welchWindow( winLen ).collect({ arg x; x.pow( 1/4 )}).plot
	win  = Signal.welchWindow( winLen ).collect({ arg x; x.max( -60.dbamp ).log });
	mi   = win.minItem;
	winLen.do({ arg i; win[ i ] = win[ i ].linlin( mi, 0, 0, 1 )});
	off  = 0;
	lastProg = 0;
	while({ off < sf.numFrames }, {
		chunkLen = min( sf.numFrames - off, winLen );
		sf.readFrames( buf, 0, chunkLen );
		~iters.do({
			winLen.do({ arg i; buf2[ i ] = buf2[ i ] * win[ i ]});
		});
		sf2.writeFrames( buf, 0, chunkLen );
		off = off + chunkLen;
		prog = ((off / sf.numFrames) * 20).asInteger;
		while({ lastProg < prog }, {
			"#".post;
			lastProg = lastProg + 1;
		});
		0.wait;
	});
	sf.close;
	sf2.close;
	"\n\nDone.\n".postln;
  };
}.value;
// )

}).play( AppClock );
)

// Hmmmmmmm. gelohnt?
