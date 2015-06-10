/**
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	"Au§er ein paar Všgeln war er das hšchste Tier"
 *
 *	@version	0.13, 28-Jun-09
 */

/////////////////////////// LAYER 1

(
// (
~masterChanNum = Ohrwald.circChansT.size;
if( File.exists( "/Volumes/KarminUser/" ), {
 	OhrwaldUtil.workDir = "/Volumes/KarminUser/Projekte/Ohrwald/Users/rutz/audio_work";
});
// )


// (
~editor = Bosque.default.timelineEditor;
~doc = Bosque.default.session;
~sr = 44100;
~dur = SMPTE.minSec( 27, 0 ).fps_( ~sr );
~editor.insertSpan( ~doc, Span( 0, ~dur.asFrames.asInteger ));
OhrwaldUtil.loadBallons; OhrwaldUtil.loadResps; OhrwaldUtil.loadResps2; OhrwaldUtil.loadResps3;
~numBallons = OhrwaldUtil.responses3.size << 1;
//~numBallons = OhrwaldUtil.responses.detectIndex({ arg b; b.span.start == 52193300 });
~ballGroups = [ OhrwaldUtil.ballons.keep( ~numBallons ), OhrwaldUtil.responses.keep( ~numBallons ), OhrwaldUtil.responses2.keep( ~numBallons ),
			  (nil ! OhrwaldUtil.responses3.size) ++ OhrwaldUtil.responses3 ];

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
~l1Files = [ "BallonsMittelSegMonoRsmp.aif", "BgStn8'b3HPFOpBllns'FRsmp.aif", "BgStn4'CCHPFOpBgStn8'lbLPFMix.aif", "BgStn6'CCHlbOpBgStn4'lbLPF.aif" ].collect({ arg name; ~editor.addAudioFile( ~doc, OhrwaldUtil.workDir +/+ name )});

//~ballIdx = 0;
~l1GainOffsets = [ 0, OhrwaldUtil.respGainOffset, OhrwaldUtil.resp2GainOffset, OhrwaldUtil.resp3GainOffset ];
~ballsPerGroup = ~numBallons.div( ~l1Groups.size );
~off		= 0;
~maxOff	= 0;
~ballsPerGroup.do({ arg k;
//	maxOff = ~off;
	~l1Groups.do({ arg group, i; var t, stake, span, name, fadeIn, fadeOut, af, ball, ballSpan, ballIdx, colr, off;
		colr		= Color.hsv( i / ~l1Groups.size, 0.5, 0.5 );
		off		= ~off;
		group.do({ arg ch, j;
			name    = "L1_G%_%".format( i + 1, j + 1 ).asSymbol;
			t       = Bosque.track( name );
			ballIdx = (i * ~ballsPerGroup) + k;
			ball    = ~ballGroups[ j ][ ballIdx ];
			ballSpan= if( j > 0, ball.span, { Span( ball.span.start << 1, ball.span.stop << 1 )});
			af      = ~l1Files[ j ];
			span    = Span( off, off + ballSpan.length );
			fadeIn  = BosqueFade( \sin, (ball.fadeIn * ~sr).round.asInteger );
			fadeOut = BosqueFade( \sin, (ball.fadeOut * ~sr).round.asInteger );
			stake   = BosqueAudioRegionStake( span, name, t, colr, fadeIn, fadeOut, (ball.gain + ~l1GainOffsets[ j ]).dbamp,
				ballSpan.start, af );
			~editor.addStake( ~doc, stake, false );
		
//			~ballIdx = (~ballIdx + 1) % ~numBallons;
			off     = off + span.length; // XXX randomize
			~maxOff  = ~maxOff.max( span.stop );
		});
//		off = off + maxLen;
	});
	~off = ~maxOff;
});
// )


/////////////////////////// LAYER 2


// (

~l2Groups = [[ 8 ], [ 9 ], [ 10, 1 ], [ 11 ], [ 12, 3 ], [ 13 ], [ 14 ], [ 15, 5 ], [ 16 ], [ 17 ], [ 18, 7 ], [ 19 ], [ 20 ]];
~l2Mat = [ "Ghrn1T1SonaRMosLck22'sRsmp.aif", "Ghrn2T1SonaRMosOrdng'FRsmp.aif", "Ghrn3T1SonaRMosQGng1ExtrDyn.aif", "Ghrn4T1SonaRMosPhylt'929sr.aif", "Ghrn5T1SonaRMosNyHnn'17Zns.aif", "Ghrn6T1SonaRMosBnc06'20Wrp.aif", "Ghrn7T1SonaRMosDrnkn'ncPt1.aif", "Ghrn8T1SonaRMosMrnns'90530.aif", "Ghrn9T1SonaRMosMrnns'90530.aif", "Ghrn10T1SnaRMosapfelessen.aif", "Ghrn11T1SnaRMosPhylt'sRsmp.aif", "Ghrn12T1SnaRMosScssK'6EdtA.aif", "Ghrn13T1SnaRMosCls40'mpFME.aif" ];
~l2MatLengths = ~l2Mat.collect({ arg name; var sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ name ); sf.close; sf.numFrames });
~l2WhispLengths = Array.fill( 13, { arg i; var sf = SoundFile.openRead( OhrwaldUtil.workDir +/+ "GehirneWhisperDPA%.aif".format( i + 1 )); sf.close; sf.numFrames });

OhrwaldUtil.loadGehirne;

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
~l2WhispGain  = -12;
~l2MatGain    = -6;

OhrwaldUtil.loadBlaettern;
~blattF = ~editor.addAudioFile( ~doc, OhrwaldUtil.workDir +/+ "BibliothekBlaettern080930RsmpFMono.aif" );
~blaetter = OhrwaldUtil.blaettern.select({ arg sp; sp.length < (~sr * 4.5) });
~blattIdx  = 0;
~blattGain = 0.dbamp;
~l2InitPause = 4 * ~sr;

// offsets right before layer 1 restarts; we could also
// calculate those, but that is a little cumbersome...
[ 0, 542, 1054 ].do({ arg totOffSec, iter; var totOff;
//	totOff = iter * (9 * 60 * ~sr);
	totOff = totOffSec * ~sr;
	~l2MatLeftOffset = ~l2WhispLengths.maxItem + (10 * ~sr) + totOff;
	~l2MatOffset = ~l2MatLeftOffset + (~l2MatLengths.maxItem >> 1);
	~editor.insertSpan( ~doc, Span( totOff, ~l2MatLeftOffset ), Trail.kTouchNone );

	~l2Groups.do({ arg group, i; var off, colr, t, stake, span, name, fadeIn, fadeOut, af, name2,
							      t2, name3, busCfg2, chans2;
		colr    = Color.hsv( i / ~l2Groups.size, 0.5, 0.5 );
	
		name    = "L2_G%".format( i + 1 ).asSymbol;
		t       = Bosque.track( name );
		af      = ~l2WhispFiles[ i ];
		span    = Span( totOff + ~l2InitPause, totOff + ~l2InitPause + ~l2WhispLengths[ i ]);
	//	fadeIn  = BosqueFade( \sin, (ball.fadeIn * ~sr).round.asInteger );
	//	fadeOut = BosqueFade( \sin, (ball.fadeOut * ~sr).round.asInteger );
		name2	= (name ++ "_W").asSymbol;
		stake   = BosqueAudioRegionStake( span, name2, t, colr, nil, nil, ~l2WhispGain.dbamp, 0, af );
		~editor.addStake( ~doc, stake, false );
		
		if( group.size > 1, { // add blaettern
			span  = ~blaetter[ ~blattIdx ];
//			t2 = block { arg break;
//				~l1Groups.do({ arg g2, ii;
//					g2.do({ arg ch, jj;
//						if( ch == group[ 1 ], {
//							break.( Bosque.track( "L1_G%_%".format( ii + 1, jj + 1 ).asSymbol ));
//						});
//					});
//				});
//			};
			name3 = "L2_B%".format( i + 1 ).asSymbol;
			t2 = Bosque.track( name3 );
			if( t2.isNil, {
				t2 = ~editor.addTrack( ~doc );
				busCfg2 = ~editor.addBusConfig( ~doc, 1, ~masterChanNum );
				~edit = JSyncCompoundEdit( "Track Customization" );
				chans2 = 0 ! ~masterChanNum;
				chans2[ group[ 1 ]] = 1;
				busCfg2.editConnections( -1, [ chans2 ], ~edit );
				t2.editRename( -1, name3, ~edit );
				t2.editBusConfig( -1, busCfg2, ~edit );
				~doc.undoManager.addEdit( ~edit.performAndEnd );
			});
			fadeIn	= BosqueFade( \sin, (0.1 * ~sr).round.asInteger );
			fadeOut	= BosqueFade( \sin, (0.1 * ~sr).round.asInteger );
			stake	= BosqueAudioRegionStake( Span( totOff, totOff + span.length ), "L2_B%".format( i + 1 ),
				t2, colr, fadeIn, fadeOut, ~blattGain.dbamp, span.start, ~blattF );
			~editor.addStake( ~doc, stake, false );
			~blattIdx = (~blattIdx + 1) % ~blaetter.size;
		});
			
		af      = ~l2MatFiles[ i ];
		off	   = ~l2MatOffset - (~l2MatLengths[ i ] >> 1);
	
		OhrwaldUtil.gehirne[ i ].do({ arg ball, k;
			span    = Span( off + ball.span.start, off + ball.span.stop );
			fadeIn  = BosqueFade( \sin, (ball.fadeIn * ~sr).round.asInteger );
			fadeOut = BosqueFade( \sin, (ball.fadeOut * ~sr).round.asInteger );
			name2	= (name ++ "_M%").format( k + 1 ).asSymbol;
			stake   = BosqueAudioRegionStake( span, name2, t, colr, fadeIn, fadeOut, (ball.gain + ~l2MatGain).dbamp, ball.span.start, af );
			~editor.addStake( ~doc, stake, false );
		});
	});
});
// )

// (
~l3Files = 4.collect({ arg i; ~editor.addAudioFile( ~doc, OhrwaldUtil.workDir +/+ "OrdingBuhne_081230_%.aif".format( i + 1 ))});
~durFrames = ~dur.asFrames;
~l3Repeats = ~l3Files.collect({ arg af; ~durFrames.div( af.numFrames )});
~l3Chans = [ 7, 5, 3, 1 ];
~l3Gains = [ 0, 0, 0, 0 ].dbamp;

~l3Files.do({ arg af, i; var col, t, busCfg, chans, off, name2, fadeIn, fadeOut, colr, span, stake, offBase;
	colr = Color.hsv( i / ~l3Files.size, 0.5, 0.5 );
	t = ~editor.addTrack( ~doc );
	busCfg = ~editor.addBusConfig( ~doc, 1, ~masterChanNum );
	~edit = JSyncCompoundEdit( "Track Customization" );
	chans = 0 ! ~masterChanNum;
	chans[ ~l3Chans[ i ]] = 1;
	busCfg.editConnections( -1, [ chans ], ~edit );
	t.editRename( -1, "L3_G%".format( i + 1 ).asSymbol, ~edit );
	t.editBusConfig( -1, busCfg, ~edit );
	~doc.undoManager.addEdit( ~edit.performAndEnd );
	name2	= af.path.basename;
	~l3Repeats[ i ].do({ arg j;
//		off		= ((~durFrames / ~l3Repeats[ i ] * (j * 2 + 1) - af.numFrames) / 2).asInteger;
		offBase	= ~durFrames.div( ~l3Repeats[ i ]);
		off		= offBase * j + i.linlin( 0, ~l3Files.size - 1, 0, offBase - af.numFrames).asInteger;
		span		= Span( off, off + af.numFrames );
		fadeIn	= BosqueFade( \cubed, af.numFrames.div( 2 ));
		fadeOut	= BosqueFade( \cubed, af.numFrames.div( 2 ));
		stake	= BosqueAudioRegionStake( span, name2, t, colr, fadeIn, fadeOut, ~l3Gains[ i ].dbamp, 0, af );
		~editor.addStake( ~doc, stake, false );
	});
});

// )
)
