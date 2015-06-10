// (C)opyright 2009 Hanns Holger Rutz.

~baseFile = OhrwaldUtil.workDir +/+ "BigStone6CutConvLHPFCCHlb2.fsc";
//~shifts = Array.series( 300, 0, 1 );
//~shifts = Array.geom( 100, 2.pow(1/12), 2.pow(1/12) );
//~shifts.asCompileString;
~shifts = Array.rand( 100, 0.0, 400.0 );

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

////////// LOOP

// (
{
forkIfNeeded { var doc;
~fsc.addr.connect;
doc = FScapeDoc.read( ~baseFile );
~shifts.do({ arg shift, idx; var docFile;
//	"----B".postln;
	doc.put( \ReOutFile, OhrwaldUtil.workDir +/+ "BigStone6CutConvLHPFCCHlb%.irc".format( idx + 1 ));
	doc.put( \Freq, "%,3".format( shift ));
	docFile = "/tmp/shift.fsc";
	doc.write( docFile );
	~fscPlingGeneric.( "Shift down by % Hz".format( shift ), docFile );
	(((idx + 1) / ~shifts.size) * 100).asInteger.postln;
	0.yield;
});
"Done!".postln;
};
}.value;
// )

}).play( AppClock );
)

