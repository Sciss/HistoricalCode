/**
 *	"Au§er ein paar Všgeln war er das hšchste Tier"
 *
 *	@version	0.11, 23-Jun-09
 *	@author	Hanns Holger Rutz
 */

/////////////////////////// LAYER 1

(
~masterChanNum = Ohrwald.circChansT.size;
OhrwaldUtil.workDir = "/Volumes/KarminUser/Projekte/Ohrwald/Users/rutz/audio_work";

// (
~editor = Bosque.default.timelineEditor;
~doc = Bosque.default.session;
~sr = 44100;
~dur = SMPTE.minSec( 10, 0 ).fps_( ~sr );
~editor.insertSpan( ~doc, Span( 0, ~dur.asFrames.asInteger ));
OhrwaldUtil.loadBallons; OhrwaldUtil.loadResps; OhrwaldUtil.loadResps2;
~numBallons = OhrwaldUtil.responses.detectIndex({ arg b; b.span.start == 52193300 });
~ballGroups = [ OhrwaldUtil.ballons.keep( ~numBallons ), OhrwaldUtil.responses.keep( ~numBallons ), OhrwaldUtil.responses2.keep( ~numBallons )];

~l1Groups = [[ 0, 10, 11 ], [ 2, 12, 13 ], [ 4, 14, 15, 16 ], [ 6, 17, 18, 19 ]];
~l1Groups.do({ arg group, i;
	~edit = JSyncCompoundEdit( "Track Customization" );
	group.do({ arg ch, j; var t, busCfg, chans;
		t = ~editor.addTrack( ~doc );
		busCfg = ~editor.addBusConfig( ~doc, 1, ~masterChanNum );
		chans = 0 ! ~masterChanNum;
		chans[ ch ] = 1;
		busCfg.editConnections( -1, [ chans ], ~edit );
		t.editRename( -1, "L1_G%_%".format( i + 1, j + 1 ).asSymbol, ~edit );
		t.editBusConfig( -1, busCfg, ~edit );
	});
	~doc.undoManager.addEdit( ~edit.performAndEnd );
});
// )

// (
~l1Files = [ "BallonsMittelSegMonoRsmp.aif", "BgStn8'b3HPFOpBllns'FRsmp.aif", "BgStn4'CCHPFOpBgStn8'lbLPFMix.aif" ].collect({ arg name; ~editor.addAudioFile( ~doc, OhrwaldUtil.workDir +/+ name )});

~ballIdx = 0;
~l1Groups.do({ arg group, i; var off, colr;
	off     = 0;
	14.do({ arg k;
		colr    = Color.hsv( i / ~l1Groups.size, 0.5, 0.5 );
		group.keep( 3 ).do({ arg ch, j; var t, stake, span, name, fadeIn, fadeOut, af, ball, ballSpan;
			name    = "L1_G%_%".format( i + 1, j + 1 ).asSymbol;
			t       = Bosque.track( name );
			ball    = ~ballGroups[ j ][ ~ballIdx ];
			ballSpan= if( j > 0, ball.span, { Span( ball.span.start << 1, ball.span.stop << 1 )});
			af      = ~l1Files[ j ];
			span    = Span( off, off + ballSpan.length );
			fadeIn  = BosqueFade( \sin, (ball.fadeIn * ~sr).round.asInteger );
			fadeOut = BosqueFade( \sin, (ball.fadeOut * ~sr).round.asInteger );
			stake   = BosqueAudioRegionStake( span, name, t, colr, fadeIn, fadeOut, ball.gain.dbamp, ballSpan.start, af );
			~editor.addStake( ~doc, stake, false );
			
			~ballIdx = (~ballIdx + 1) % ~numBallons;
			off     = off + span.length; // XXX randomize
		});
	});
});
// )


/////////////////////////// LAYER 2


// (

~l2Groups = [[ 8 ], [ 9 ], [ 10, 1 ], [ 11 ], [ 12, 3 ], [ 13 ], [ 14 ], [ 15, 5 ], [ 16 ], [ 17 ], [ 18, 7 ], [ 19 ], [ 20 ]];
~l2Mat = [ "Ghrn1T1SonaRMosLck22'sRsmp.aif", "Ghrn2T1SonaRMosOrdng'FRsmp.aif", "Ghrn3T1SonaRMosQGng1ExtrDyn.aif", "Ghrn4T1SonaRMosPhylt'929sr.aif", "Ghrn5T1SonaRMosNyHnn'17Zns.aif", "Ghrn6T1SonaRMosBnc06'20Wrp.aif", "Ghrn7T1SonaRMosDrnkn'ncPt1.aif", "Ghrn8T1SonaRMosMrnns'90530.aif", "Ghrn9T1SonaRMosMrnns'90530.aif", "Ghrn10T1SnaRMosapfelessen.aif", "Ghrn11T1SnaRMosPhylt'sRsmp.aif", "Ghrn12T1SnaRMosScssK'6EdtA.aif", "Ghrn13T1SnaRMosCls40'mpFME.aif" ];
~l2MatLengths = ~l2Mat.collect({ arg name; var sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ name ); sf.close; sf.numFrames });
~l2WhispLengths = Array.fill( 13, { arg i; var sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ "GehirneWhisperDPA%.aif".format( i + 1 )); sf.close; sf.numFrames });
~l2MatOffset = ~l2WhispLengths.maxItem + (10 * ~sr) + (~l2MatLengths.maxItem >> 1);

~l2Groups.do({ arg group, i; var t, busCfg, chans;
	t = ~editor.addTrack( ~doc );
	busCfg = ~editor.addBusConfig( ~doc, 2, ~masterChanNum );
	~edit = JSyncCompoundEdit( "Track Customization" );
	chans = 0 ! ~masterChanNum ! 2; // group.size;
	chans[ 0 ][ group[ 0 ]] = 1;
	if( group.size > 1, {
		chans[ 1 ][ group[ 1 ]] = -9.dbamp;
	});
	busCfg.editConnections( -1, chans, ~edit );
	t.editRename( -1, "L2_G%".format( i + 1 ).asSymbol, ~edit );
	t.editBusConfig( -1, busCfg, ~edit );
	~doc.undoManager.addEdit( ~edit.performAndEnd );
});
// )

// (
~l2MatFiles   = ~l2Mat.collect({ arg name; ~editor.addAudioFile( ~doc, OhrwaldUtil.workDir +/+ name )});
~l2WhispFiles = Array.fill( 13, { arg i; ~editor.addAudioFile( ~doc, OhrwaldUtil.workDir +/+ "GehirneWhisperDPA%.aif".format( i + 1 ))});

~l2Groups.do({ arg group, i; var off, colr, t, stake, span, name, fadeIn, fadeOut, af, name2;
	colr    = Color.hsv( i / ~l2Groups.size, 0.5, 0.5 );

	name    = "L2_G%".format( i + 1 ).asSymbol;
	t       = Bosque.track( name );
	af      = ~l2WhispFiles[ i ];
	span    = Span( ~sr, ~sr + ~l2WhispLengths[ i ]);
//	fadeIn  = BosqueFade( \sin, (ball.fadeIn * ~sr).round.asInteger );
//	fadeOut = BosqueFade( \sin, (ball.fadeOut * ~sr).round.asInteger );
	name2	= (name ++ "_W").asSymbol;
	stake   = BosqueAudioRegionStake( span, name2, t, colr, nil, nil, 1.0, 0, af );
	~editor.addStake( ~doc, stake, false );
		
	af      = ~l2MatFiles[ i ];
	span    = Span( ~l2MatOffset - (~l2MatLengths[ i ] >> 1), ~l2MatOffset + ((~l2MatLengths[ i ] + 1) >> 1) );
//	fadeIn  = BosqueFade( \sin, (ball.fadeIn * ~sr).round.asInteger );
//	fadeOut = BosqueFade( \sin, (ball.fadeOut * ~sr).round.asInteger );
	name2	= (name ++ "_M").asSymbol;
	stake   = BosqueAudioRegionStake( span, name2, t, colr, nil, nil, 1.0, 0, af );
	~editor.addStake( ~doc, stake, false );
});
// )
)
