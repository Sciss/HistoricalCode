//~marks2.flatten.collect(_.length).minItem / 44100 // 1.1 sek
//~marks2.flatten.collect(_.length).mean / 44100 // 4.5 sek
//~marks2.flatten.collect(_.length).maxItem / 44100 // 11.2
//
//
//sqrt(1.5*12)

////////// CREATE CUTS

~stoneNum = 4;
////////// PREP FSCAPE //////////////////////////////////////////////////////////////////////////////////////
(
~fsc = FScape.default;

~initGUI = {
	var b;
	b	= GUI.window.screenBounds;
	~fscProgWin	= GUI.window.new( "FScape", Rect( b.width - 660, b.height - 76, 500, 28 ), resizable: false );
	~fscProg		= GUI.rangeSlider.new( ~fscProgWin, Rect( 4, 4, 480, 20 ));
	~fscProgWin.front;
};

~fscProgFunc = { arg prog; { if( ~fscProg.isClosed.not, { ~fscProg.hi = prog; });}.defer; };

~finishFscProg = { arg prog, error;	
	{
		if( ~fscProg.isClosed.not, {
			if( error.isNil, {
				~fscProg.knobColor = if( prog == 1,
					{ Color.green( 0.7 )}, { Color.grey });
			}, {
				~fscProg.lo = max( ~fscProg.lo, 0.1 ); // minimum visible
				~fscProg.knobColor = Color.red;	
			});
		});
	}.defer;
};

~startFscProg = { arg fscDocFile;
	{
		if( ~fscProgWin.isClosed.not, {
			~fscProgWin.name = "FScape : " ++ fscDocFile;
			~fscProg.lo	= 0;
			~fscProg.hi	= 0;
			~fscProg.knobColor = Color.white;
		});
	}.defer;
};

~fscPlingGeneric = { arg name, fscDocFile, cond;
	var success = false;
	
	cond		= cond ?? { Condition.new; };
	cond.test	= false;

	~startFscProg.( fscDocFile );

	~fsc.openAndProcess( fscDocFile, visible: false, doneAction: {
		arg prog, error;
		
		if( (prog == 1) && error.isNil, {
			(name.asString ++ " done.").inform;
			success = true;
		}, {
			error.postln;
		});

		// update gui
		~finishFscProg.( prog, error );

		cond.test = true;
		cond.signal;

	}, progFunc: ~fscProgFunc );

"----1".postln;	// don't ask me why, but thing hangs if we don't print to console ...
	cond.wait;
"----2".postln;
	success;
};

~initGUI.value;
)

//////////////////////////// CORE /////////////////////////////////////////////////////////
(
Routine({

// (
"================= CALC GROUPS =================".postln;
{
var sf, off = 0, len;
sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone%.aif".format( ~stoneNum )); sf.close;
len = (exprand( 1.5, 12 ) * 44100).asInteger;
~group = nil;
while({ off + off < sf.numFrames }, {
	~group = ~group.add( Span( off, off + len ));
	off = off + len;
	len = (exprand( 1.5, 12 ) * 44100).asInteger;
});
~num = ~group.size;
}.value;
// )

// (
"================= CREATE CUTS =================".postln;
{
//	group = ~groups3; // 1;
forkIfNeeded { var sf, group, sf2, buf, buf2;
	group = ~group;
	sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone%.aif".format( ~stoneNum ));
	buf  = Array.fill( sf.numChannels, { Signal.newClear( group.collect(_.length).maxItem )});
	buf2 = [ buf.first ];
	group.do({ arg span, idx;
		sf2 = SoundFile.new;
		sf2.sampleFormat = sf.sampleFormat;
		sf2.openWrite( OhrwaldUtil.workDir +/+ "BigStone%Cut%.aif".format( ~stoneNum, idx + 1 ));
		sf.seekFrame( span.start );
		sf.readFrames( buf, 0, span.length );
		sf2.writeFrames( buf2, 0, span.length );
		sf2.close;
		((idx + 1) / group.size * 100).asInteger.postln;
		0.yield;
	});
	sf.close;
  };
}.value;
// )

////////// CREATE WHITE VERSIONS

// (
"================= CREATE WHITE VERSIONS =================".postln;
{
forkIfNeeded {
~fsc.addr.connect;
~num.do({ arg idx; var doc, docFile, procs, firstIn, lastOut;
	procs = [ "1_FT", "2_Op", "3_Amp", "4_Op", "5_FT" ];
	firstIn = \ReInFile;
	lastOut = \ReOutFile;
	procs.do({ arg proc, j;
		docFile = OhrwaldUtil.workDir +/+ "_tmp%.fsc".format( proc );
		if( j == 0, {
			doc = FScapeDoc.read( docFile );
			doc.put( firstIn, OhrwaldUtil.workDir +/+ "BigStone%Cut%.aif".format( ~stoneNum, idx + 1 ));
			docFile = "/tmp/first.fsc";
			doc.write( docFile );
		}, { if( j == (procs.size - 1), {
			doc = FScapeDoc.read( docFile );
			doc.put( lastOut, OhrwaldUtil.workDir +/+ "BigStone%Cut%White.irc".format( ~stoneNum, idx + 1 ));
			docFile = "/tmp/last.fsc";
			doc.write( docFile );
		})});
		~fscPlingGeneric.( proc, docFile );
	});
});
"Done!".postln;
};
}.value;
// )

////////// TRUNC WHITE VERSIONS

// (
"================= TRUNC WHITE VERSIONS =================".postln;
{
forkIfNeeded { var buf, group;
group = ~group;
buf  = [ Signal.newClear( group.collect(_.length).maxItem )];
group.do({ arg span, idx; var sf1, sf2;
	sf1 = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone%Cut%White.irc".format( ~stoneNum, idx + 1 ));
	sf2 = SoundFile.newUsing( sf1 );
// XXX libsndfile doesn't produce proper 32bit float IRCAM files...
sf2.headerFormat = "AIFF";
	sf2.openWrite( OhrwaldUtil.workDir +/+ "BigStone%Cut%WhiteT.aif".format( ~stoneNum, idx + 1 ));
	sf1.readFrames( buf, 0, span.length );
	sf1.close;
	sf2.writeFrames( buf, 0, span.length );
	sf2.close;
	((idx + 1) / group.size * 100).asInteger.postln;
	0.yield;
});
"Done!".postln;
};
}.value;
// )

////////// CREATE CONVOLUTIONS

//~initGUI.value;
// (
"================= CREATE CONVOLUTIONS =================".postln;
{
forkIfNeeded {
~fsc.addr.connect;
~num.do({ arg idx; var doc, docFile, proc, firstIn, lastOut;
	proc = "6_Con";
	docFile = OhrwaldUtil.workDir +/+ "_tmp%.fsc".format( proc );
	doc = FScapeDoc.read( docFile );
	doc.put( \InputFile, OhrwaldUtil.workDir +/+ "BigStone%Cut%.aif".format( ~stoneNum, idx + 1 ));
	doc.put( \ImpulseFile, OhrwaldUtil.workDir +/+ "BigStone%Cut%WhiteT.aif".format( ~stoneNum, idx + 1 ));
	doc.put( \OutputFile, OhrwaldUtil.workDir +/+ "BigStone%Cut%Conv.aif".format( ~stoneNum, idx + 1 ));
	docFile = "/tmp/conv.fsc";
	doc.write( docFile );
	~fscPlingGeneric.( proc, docFile );
	((idx + 1) / ~num * 100).asInteger.postln;
	0.yield;
});
"Done!".postln;
};
}.value;
// )

////////// LIMITER / COMPRESSOR

// (
"================= LIMITER / COMPRESSOR =================".postln;
{
forkIfNeeded { var buf, group, buf2, atk = 0.1, rls = 0.2, w, win, hits, maxVal, iter, maxIter = 100, oldIdx, fCalcMax, sf1, sf2, maxIdx, atkF, rlsF, wd, atkWinName = \hanningWindow, rlsWinName = \welchWindow;
group = ~group; // ~marks2.flatten;
fCalcMax = { var test, test2;
	maxIdx = buf2.maxIndex; test = buf2.minIndex;
	maxVal = buf2[ÊmaxIdx ]; test2 = buf2[ test ];
	if( test2.abs > maxVal.abs, {
		maxIdx = test; maxVal = test2;
	});
};
group.do({ arg span, idx;
	(idx+1).postln;
	sf1 = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone%Cut%Conv.aif".format( ~stoneNum, idx + 1 ));
//	if( buf.size < sf1.numFrames, {
		buf2  = Signal.newClear( sf1.numFrames );
		buf   = [ buf2 ];
//	});
	sf2 = SoundFile.newUsing( sf1 );
// XXX libsndfile doesn't produce proper 32bit float IRCAM files...
sf2.headerFormat = "AIFF";
	sf2.openWrite( OhrwaldUtil.workDir +/+ "BigStone%Cut%ConvL.aif".format( ~stoneNum, idx + 1 ));
	sf1.readFrames( buf, 0, sf1.numFrames );
	sf1.close;
	iter = 0;
	fCalcMax.value;
	hits = 0;
	while({ (maxVal.abs >= 1) and: { iter < maxIter }}, {
//		w    = (maxVal.abs - 0.99) * maxVal.sign;
		w    = 0.99 / maxVal.abs;
//		w    = maxVal.abs - 0.99;
		wd   = 1.0 - w;
		atkF = (atk * sf1.sampleRate).asInteger.min( maxIdx );
		rlsF = (rls * sf1.sampleRate).asInteger.min( sf1.numFrames - 1 - maxIdx );
//		"iter % -> maxIdx is % -> maxVal is % -> w is % -> atk is % -> rls is %\n".postf( iter, maxIdx, maxVal, w, atkF, rlsF );
//~buf = buf2;
		if( atkF > 0, {
			win = Signal.perform( atkWinName, atkF * 2 );
			atkF.do({ arg i;
				buf2[ maxIdx - 1 - i ] = buf2[ maxIdx - 1 - i ] * (win[ i ] * wd + w);
			});
		});
		if( rlsF > 0, {
			win = Signal.perform( rlsWinName, rlsF * 2 );
			rlsF.do({ arg i;
				buf2[ maxIdx + i ] = buf2[ maxIdx + i ] * (win[ i ] * wd + w);
			});
		});
		oldIdx = maxIdx;
		fCalcMax.value;
		if( oldIdx == maxIdx, {
			iter = 0;
		}, {
			iter = iter + 1;
		});
		hits = hits + 1;
	});
	if( hits > 0, {
		"---> DANG %\n".postf( hits );
	});
	sf2.writeFrames( buf, 0, sf1.numFrames );
	sf2.close;
//	((idx + 1) / group.size * 100).asInteger.postln;
//	0.yield;
	0.1.wait;
});
"Done!".postln;
};
}.value;
// )

}).play( AppClock );
)

