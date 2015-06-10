OhrwaldUtil.loadBallons; OhrwaldUtil.loadResps; OhrwaldUtil.loadResps2;
~numBallons = OhrwaldUtil.responses.detectIndex({ arg b; b.span.start == 52193300 });
~numGroups = 4; // ~l1Groups.size;
//~numResp3 = (~numBallons / ~numGroups * ~l1Groups.count({ arg g; g.size == 4 })).asInteger;
~numResp3 = (~numBallons / ~numGroups * 2).asInteger;
// --> 54
~numBallons = ~numResp3 * 2;	// --> 108

~resp3Len = 26151798;  // with CC overlap 50%
~avail = OhrwaldUtil.responses2.detectIndex({ arg b; b.span.stop > ~resp3Len });
// --> 44 ;-C

~resp3Len = 31087061;	// with CC overlap 40%
~avail = OhrwaldUtil.responses2.detectIndex({ arg b; b.span.stop > ~resp3Len });
// --> 52 ;-C

~resp3Len = 32074108;	// with CC overlap 38%
~avail = OhrwaldUtil.responses2.detectIndex({ arg b; b.span.stop > ~resp3Len });
// --> 55 ;-3

//OhrwaldUtil.responses[ 53 ].span.start;
//OhrwaldUtil.responses[ 53 ].span.stop;

// ... unsinn, muss mit den letzten spans funktionieren:

~resp2Trunc = OhrwaldUtil.responses2.copyToEnd( ~numResp3 );
~resp2Trunc = ~resp2Trunc.keep( ~numResp3 );
~resp2Len = (~resp2Trunc.last.span.stop - ~resp2Trunc.first.span.start);

~resp3Len = 26151798;  // with CC overlap 50%
~resp3Len > ~resp2Len; // ;-3

~resp3Len = 21216562;	// with CC overlap 60%
~resp3Len > ~resp2Len; // ;-3


////////////////// EDITING //////////////////

(
OhrwaldUtil.loadBallons; OhrwaldUtil.loadResps; OhrwaldUtil.loadResps2;
~numBallons = OhrwaldUtil.responses.detectIndex({ arg b; b.span.start == 52193300 });
~numGroups = 4; // ~l1Groups.size;
~numResp3 = (~numBallons / ~numGroups * 2).asInteger;
~numBallons = ~numResp3 * 2;	// --> 108
OhrwaldUtil.ballons    = OhrwaldUtil.ballons.copyToEnd( ~numResp3 ).keep( ~numResp3 );
OhrwaldUtil.responses  = OhrwaldUtil.responses.copyToEnd( ~numResp3 ).keep( ~numResp3 );
OhrwaldUtil.responses2 = OhrwaldUtil.responses2.copyToEnd( ~numResp3 ).keep( ~numResp3 );
)

//OhrwaldUtil.responses2.first.span.start;
//OhrwaldUtil.responses2.last.span.stop;

e = Eisenkraut.default; e.addr.connect;

	// to create cut
	(
	e.sendMsg( '/doc', \open, OhrwaldUtil.workDir +/+ "BgStn4'CCHPFOpBgStn8'lbLPFMix.aif" );
	e.sendMsg( '/doc/active/timeline', \select, OhrwaldUtil.responses2.first.span.start, 	OhrwaldUtil.responses2.last.span.stop );
	// --> save as "BgStn4'CCHPFOpBgStn8'lbLPFMixCut.aif"
	)

	// to create initial resp3
	(
	OhrwaldUtil.responses3 = OhrwaldUtil.responses2.collect({ arg b;
				b = b.copy;
				b.gain = 0.0;
				b.span = b.span.shift( OhrwaldUtil.responses2.first.span.start.neg );
				b;
			});
	)
	
OhrwaldUtil.loadResps3;

// open BgStn6'CCHlbOpBgStn4'lbLPF.aif in EisK
e.sendMsg( '/doc', \open, OhrwaldUtil.workDir +/+ "BgStn6'CCHlbOpBgStn4'lbLPF.aif" );

//OhrwaldUtil.responses3.asCompileString
OhrwaldUtil.ballonEditGUI( \resp3 );

//OhrwaldUtil.resp3GainOffset = 10;
//OhrwaldUtil.resp2GainOffset = 9;
//OhrwaldUtil.resp3GainOffset = 15;
