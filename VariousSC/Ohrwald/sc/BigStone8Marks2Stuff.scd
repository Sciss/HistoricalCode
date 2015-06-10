(
~marks2 = [ List[ Span(4246590, 4357292), Span(4468258, 4523342), Span(6118871, 6252503), Span(11160374, 11266658), Span(11336401, 11404669), Span(20540557, 20635885), Span(21959701, 22090549), Span(22206722, 22306274), Span(23826408, 24082248), Span(25321864, 25597864), Span(25739715, 25967770), Span(26233758, 26441598), Span(29064084, 29163732), Span(29305492, 29489908), Span(29632599, 29827959), Span(31425878, 31638998), Span(31760122, 32009320), Span(32077997, 32291405), Span(33004337, 33124433), Span(33229602, 33422850), Span(35075673, 35276601), Span(35835114, 36007493), Span(37925974, 38124214), Span(38611601, 38830961), Span(41118165, 41217621), Span(42060356, 42286436), Span(43048040, 43293416), Span(44240791, 44353687), Span(44376332, 44472456), Span(45025429, 45174613), Span(45942176, 46159566), Span(46308742, 46539238), Span(47242268, 47479292), Span(47640870, 47865126), Span(48687451, 48807547), Span(48905478, 49051590), Span(49953315, 50145699), Span(50837138, 50992946), Span(51351290, 51609530), Span(51636006, 51831078), Span(51914228, 52138964), Span(52197341, 52430525), Span(52679050, 52827562), Span(53165073, 53344593), Span(53816368, 54046096), Span(54533222, 54768835), Span(55167929, 55383929), Span(55502771, 55713683), Span(56059322, 56265338), Span(56327196, 56561340), Span(56622338, 56857250), Span(56910576, 57207024), Span(58009249, 58501377), Span(58593709, 58763341), Span(58845542, 58984291), Span(59071711, 59210527), Span(59297880, 59467608), Span(59550507, 59696907) ], List[ Span(4532164, 4661323), Span(4727410, 4822048), Span(4882199, 4981509), Span(5871372, 5943053), Span(5958317, 6095059), Span(10869042, 10979319), Span(10998166, 11072389), Span(11281706, 11329183), Span(11447394, 11834217), Span(11936947, 12150343), Span(13692555, 13796974), Span(17855582, 18105802), Span(20798005, 21094069), Span(24182722, 24438562), Span(26586209, 26719745), Span(28460967, 28573863), Span(28596508, 28728869), Span(33518981, 33729147), Span(33939004, 34160572), Span(34718574, 34910958), Span(35418534, 35654118), Span(36947513, 37144313), Span(38233949, 38462429), Span(38963687, 39224519), Span(39322508, 39562412), Span(40596913, 40768657), Span(40858713, 41048025), Span(41337116, 41551196), Span(41693212, 41867204), Span(42614148, 42924708), Span(43989476, 44080388), Span(44593943, 44791223), Span(45281313, 45567873), Span(45636607, 45874399), Span(46654411, 46833067), Span(50307004, 50494876), Span(50623000, 50770552), Span(51087426, 51289410), Span(52500504, 52667019), Span(52864316, 53056508), Span(53533256, 53718935), Span(54832685, 55056461), Span(55778665, 55989961), Span(57238601, 57638537), Span(57700472, 57906776), Span(59767052, 59979020) ], List[ Span(5645437, 5812381), Span(6542097, 6639943), Span(6774339, 6939267), Span(9521285, 9615522), Span(9628756, 9777129), Span(9824047, 9981975), Span(10084232, 10252167), Span(10636708, 10786117), Span(18216056, 18425448), Span(18521625, 18800985), Span(18892598, 19187126), Span(19276765, 19560925), Span(19681552, 19966000), Span(20198095, 20483503), Span(21216658, 21451649), Span(21541074, 21830088), Span(22403217, 22709169), Span(22837006, 23155918), Span(23269872, 23600494), Span(24518767, 24887497), Span(24963893, 25245173), Span(26958346, 27203626), Span(27287035, 27493531), Span(27591465, 27877449), Span(28012525, 28316461), Span(28857966, 29041971), Span(29983081, 30252169), Span(30345661, 30583453), Span(30655039, 30878431), Span(31023165, 31218333), Span(32423115, 32694123), Span(32806014, 32956638), Span(34280072, 34526216), Span(36143891, 36421427), Span(36557733, 36801093), Span(37282063, 37520133), Span(39828581, 40045445), Span(40206331, 40444219), Span(43447312, 43666288), Span(43806616, 43952329), Span(46915044, 47121444), Span(54104944, 54409552) ], List[ Span(10309655, 10358393) ], List[ Span(13023017, 13172193), Span(49351400, 49531976) ], List[  ] ];
)

~marks2.collect(_.size) // essentially three groups...

~groups1 = ~marks2[0];
~groups1.first;

~groups2 = ~marks2[1];
~groups3 = ~marks2[2];
~groups3.size

//~groups1.detect({ arg span; span.length == 184005 })
//~groups2.detect({ arg span; span.length == 184005 })
x = ~groups3.detect({ arg span; span.length == 184005 }); // GOT IT!!
// --> corresponds to BigStone8Test5_00_603.aif
~groups3.indexOf( x ) // 25

y = ~groups3.detect({ arg span; span.length == 269088 }); // GOT IT!!
// --> corresponds to BigStone8Test5_12_323.aif
~groups3.indexOf( y ) // 26

~num = ~marks2.flatten.size; // 149

////////////////
//
//~groups1.size.factorial
//
//(58*57)+(57*56)+(56*55)+(55*54)+(54*53)+(53*52)+(52*51)+(51*50)
//
//(8*7)+(7*6)+(6*5)+(5*4)+(4*3)+(3*2)+(2*1)
//
//8*8*8/169
//
//8*7*3
//
//(7*6)+(6*5)+(5*4)+(4*3)+(3*2)+(2*1)
//7*7*3

////////// CREATE CUTS

(
var sf, group, sf2, buf, buf2;
//	group = ~groups3; // 1;
	group = ~marks2.flatten;
  Routine({
	sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone8.aif" );
	buf  = Array.fill( sf.numChannels, { Signal.newClear( group.collect(_.length).maxItem )});
	buf2 = [ buf.first ];
	group.do({ arg span, idx;
		sf2 = SoundFile.new;
		sf2.sampleFormat = sf.sampleFormat;
		sf2.openWrite( OhrwaldUtil.workDir +/+ "BigStone8Cut%.aif".format( idx + 1 ));
		sf.seekFrame( span.start );
		sf.readFrames( buf, 0, span.length );
		sf2.writeFrames( buf2, 0, span.length );
		sf2.close;
		((idx + 1) / group.size * 100).asInteger.postln;
		0.yield;
	});
	sf.close;
  }).play( AppClock );	
)

////////// PREP FSCAPE
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
)

////////// CREATE WHITE VERSIONS

~initGUI.value;
(
fork {
~fsc.addr.connect;
~num.do({ arg idx; var doc, docFile, procs, firstIn, lastOut;
	procs = [ "1_FT", "2_Op", "3_Amp", "4_Op", "5_FT" ];
	firstIn = \ReInFile;
	lastOut = \ReOutFile;
	procs.do({ arg proc, j;
		docFile = OhrwaldUtil.workDir +/+ "_tmp%.fsc".format( proc );
		if( j == 0, {
			doc = FScapeDoc.read( docFile );
			doc.put( firstIn, OhrwaldUtil.workDir +/+ "BigStone8Cut%.aif".format( idx + 1 ));
			docFile = "/tmp/first.fsc";
			doc.write( docFile );
		}, { if( j == (procs.size - 1), {
			doc = FScapeDoc.read( docFile );
			doc.put( lastOut, OhrwaldUtil.workDir +/+ "BigStone8Cut%White.irc".format( idx + 1 ));
			docFile = "/tmp/last.fsc";
			doc.write( docFile );
		})});
		~fscPlingGeneric.( proc, docFile );
	});
});
"Done!".postln;
};
)

////////// TRUNC WHITE VERSIONS

(
Routine({ var buf, group;
group = ~marks2.flatten;
buf  = [ Signal.newClear( group.collect(_.length).maxItem )];
group.do({ arg span, idx; var sf1, sf2;
	sf1 = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone8Cut%White.irc".format( idx + 1 ));
	sf2 = SoundFile.newUsing( sf1 );
// XXX libsndfile doesn't produce proper 32bit float IRCAM files...
sf2.headerFormat = "AIFF";
	sf2.openWrite( OhrwaldUtil.workDir +/+ "BigStone8Cut%WhiteT.aif".format( idx + 1 ));
	sf1.readFrames( buf, 0, span.length );
	sf1.close;
	sf2.writeFrames( buf, 0, span.length );
	sf2.close;
	((idx + 1) / group.size * 100).asInteger.postln;
	0.yield;
});
"Done!".postln;
}).play( AppClock );
)

////////// CREATE CONVOLUTIONS

~initGUI.value;
(
Routine({
~fsc.addr.connect;
~num.do({ arg idx; var doc, docFile, proc, firstIn, lastOut;
	proc = "6_Con";
	docFile = OhrwaldUtil.workDir +/+ "_tmp%.fsc".format( proc );
	doc = FScapeDoc.read( docFile );
	doc.put( \InputFile, OhrwaldUtil.workDir +/+ "BigStone8Cut%.aif".format( idx + 1 ));
	doc.put( \ImpulseFile, OhrwaldUtil.workDir +/+ "BigStone8Cut%WhiteT.aif".format( idx + 1 ));
	doc.put( \OutputFile, OhrwaldUtil.workDir +/+ "BigStone8Cut%Conv.aif".format( idx + 1 ));
	docFile = "/tmp/conv.fsc";
	doc.write( docFile );
	~fscPlingGeneric.( proc, docFile );
	((idx + 1) / ~num * 100).asInteger.postln;
	0.yield;
});
"Done!".postln;
}).play( AppClock );
)

////////// LIMITER / COMPRESSOR

(
Routine({ var buf, group, buf2, atk = 0.1, rls = 0.2, w, win, hits, maxVal, iter, maxIter = 100, oldIdx, fCalcMax, sf1, sf2, maxIdx, atkF, rlsF, wd, atkWinName = \hanningWindow, rlsWinName = \welchWindow;
group = ~marks2.flatten;
fCalcMax = { var test, test2;
	maxIdx = buf2.maxIndex; test = buf2.minIndex;
	maxVal = buf2[ÊmaxIdx ]; test2 = buf2[ test ];
	if( test2.abs > maxVal.abs, {
		maxIdx = test; maxVal = test2;
	});
};
group.do({ arg span, idx;
	(idx+1).postln;
	sf1 = SoundFile.openRead( OhrwaldUtil.workDir +/+ "BigStone8Cut%Conv.aif".format( idx + 1 ));
//	if( buf.size < sf1.numFrames, {
		buf2  = Signal.newClear( sf1.numFrames );
		buf   = [ buf2 ];
//	});
	sf2 = SoundFile.newUsing( sf1 );
// XXX libsndfile doesn't produce proper 32bit float IRCAM files...
sf2.headerFormat = "AIFF";
	sf2.openWrite( OhrwaldUtil.workDir +/+ "BigStone8Cut%ConvL.aif".format( idx + 1 ));
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
}).play( AppClock );
)
