/**
 *	Dependancies: ScissPlus
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.14, 04-Jul-09
 */
OhrwaldPlayer {
	classvar <pieces;
	classvar <group, <busA, <busB;
	classvar <>bufSize = 32768;
	classvar fadeBus;
	classvar <mixWin;
	
	classvar <>fadeTimeInteract =  4.0;	// Float : seconds
	classvar <>fadeTimeAutoMin	= 30.0; // 20.0;	// Float : seconds
	classvar <>fadeTimeAutoMax	= 60.0; // 40.0;	// Float : seconds
	classvar <>pieceDurScale	= 0.8;	// Float : factor (should be 1.0 for final installation)

	classvar <>verbose = true;
	classvar <>useAuto = true;		// !! true in the final installation
	
	classvar synthCond;
//	classvar piecesUrn;
	classvar <pieceFreqs;
	classvar grabPieceIdxA, grabPieceIdxB, grabWeight, grabFadeTime;

	*init {
		var s, bndl, synthCirc, synthXFader;
		s 		= Ohrwald.scsynth;
		pieces	= (Ohrwald.workDir +/+ "pieces.scd").loadPath;
		pieces.do( _.calcNumChannels );
		group	= Group( s );
		busA		= Bus.audio( s, Ohrwald.masterNumChannels );
		busB		= Bus.audio( s, Ohrwald.masterNumChannels );
		fadeBus	= Bus.control( s, 1 );
		
		bndl		= OSCBundle.new;
		this.createDefs( IdentitySet.newFrom( pieces.collect( _.numChannels ))).do({ arg def;
			bndl.addPrepare( def.recvMsg );
		});
		synthCirc = Synth.basicNew( \owCirc, s );
		bndl.add( synthCirc.newMsg( group, [ \bus, Ohrwald.masterBus ]));
		
		pieces.do({ arg p;
			p.buf = Buffer( s, bufSize, p.numChannels );
			bndl.addPrepare( p.buf.allocMsg( p.buf.cueSoundFileMsg( p.path )));
			p.synth = Synth.basicNew( \owDisk ++ p.numChannels ++ if( p.spkrEQs.isNil, "", "EQ" ));
			bndl.add( p.synth.newMsg( synthCirc,
				[ \bus, busA.index, \chanOff, p.channelOffset, // important to set initially to Bus A, which corresponds to p.bus
				  \buf, p.buf, \amp, p.gain.dbamp ] ++
				  	if( p.spkrGains.notNil, {[ \spkrGains, p.spkrGains.dbamp ]}) ++
				  	if( p.spkrEQs.notNil, {[ \spkrEQs, p.spkrEQs ]}),
				  if( p.order == \normal, \addAfter, \addBefore )));
			bndl.add( p.synth.runMsg( false ));
			p.synth.onEnd = { p.buf.close; p.buf.free; p.buf = nil; p.synth = nil }; // implies p.synth.register!
		});
		pieceFreqs		= 1 ! pieces.size; // theoretically zero, but then we get divisions by zero
//		piecesUrn = Urn.newUsing( Array.series( pieces.size )).autoReset_( true );
		
		synthXFader = Synth.basicNew( \owXFader, s );
		bndl.add( synthXFader.newMsg( group, [ \in1, busA, \in2, busB, \out, Ohrwald.masterBus, \fadeBus, fadeBus ], \addToTail ));
		bndl.send( s );
		
		synthCond = Condition.new;
		// the wait and sync is crucial because OSCBundle:send will use latency stupidly (cannot hand in nil bundletime)
		Routine({ 1.0.wait; s.sync; this.prRoutBody }).play( SystemClock );
	}
	
//	*pieceIndex { ^OhrwaldModel.normalizedHAngle * (pieces.size - 1) }
	*pieceAngle { arg idx; ^OhrwaldModel.degSpec.map( idx / (pieces.size - 1) )}
	
	*playPiece { arg name;
		var idx;
		idx = pieces.detectIndex({ arg p; p.name == name });
		if( idx.notNil, {
			OhrwaldModel.setAngle( -1, this.pieceAngle( idx ));
		}, {
			TypeSafe.methodWarn( thisMethod, "No piece with name '%' found".format( name ));
		});
	}
	
	*plotFreqs {
		pieceFreqs.postln;
		pieceFreqs.plot( discrete: true );
	}
	
	*prRoutBody {
		var pieceIdxA, pieceIdxB, s, fadeBool = false, bndl, bndl2, newPieceIdxA, newPieceIdxB, weight, newWeight, newFadeBool,
		    startFade, stopFade, fdt;
				
		s = Ohrwald.scsynth;
		
		UpdateListener.newFor( OhrwaldModel, { arg upd, m, who;
			if( verbose, {[ "who", who ].postln });
			this.prPickFromModel;
			synthCond.test = true;
			synthCond.signal;
		}, \angle );
		
		pieceIdxA		= nil;
		pieceIdxB		= nil;
		weight		= 1.0;
//		fadeTime		= 10.0; // this is just the initial startup time

		// -------- start the show... --------
//		if( useAuto, {
			this.prPickPieces;
//		});

		// -------- main loop --------
		inf.do({ var synthFade, oldSynthA, oldSynthB, newSynthA, newSynthB, process;
			
//			#newPieceIdxA, newPieceIdxB, newWeight = this.calcFromModel;
			newPieceIdxA	= grabPieceIdxA;
			newPieceIdxB	= grabPieceIdxB;
			newWeight		= grabWeight;
		
			if( verbose, {[ "newPieceIdxA", newPieceIdxA, "newPieceIdxB", newPieceIdxB, "newWeight", newWeight ].postln });
			
			bndl  = OSCBundle.new;
			bndl2 = OSCBundle.new;

			process = true;
			if( newPieceIdxA == pieceIdxA, {
				//  X X   X X   X X   X X   X X 
				//  O O   O X   O O   O O   O X
				//  X O   X O   X X   O O   O O
				//  O O   O O   O O   O O   O O
				if( newPieceIdxB.isNil, {            // 2
					//  X X   X X
					//  O O   O O
					//  X O   O O
					//  O O   O O
					// fade as proposed
					process = pieceIdxB.notNil;  // ...or ignore if there are no changes
				}, {
					//  X X   X X   X X 
					//  O X   O O   O X
					//  X O   X X   O O
					//  O O   O O   O O
					if( pieceIdxB.isNil, {	          // 3
						//  X X 
						//  O X
						//  O O
						//  O O
						// fade as proposed
					}, {
						//  X X   X X
						//  O X   O O
						//  X O   X X
						//  O O   O O
						if( newPieceIdxB == pieceIdxB, {   // 4
							//  X X
							//  O O
							//  X X
							//  O O
							// fade as proposed
							process = weight != newWeight;  // ...or ignore if there are no changes
						}, {                              // 5
							//  X X
							//  O X
							//  X O
							//  O O
							newWeight		= 1.0;	// reduce to common
							newPieceIdxB	= nil;
						});
					});
				});
			}, {
				//  X O   X O   X O   X O   X O   O O   O O   X O   X O   O X   O X
				//  O X   O X   O X   O X   O X   O X   O X   O O   O O   X X   X X
				//  X O   X X   X O   O O   O X   O O   O X   X X   X X   O O   X O
				//  O O   O O   O X   O O   O O   O O   O O   O O   O X   O O   O O
				if( newPieceIdxA == pieceIdxB, {          // 7
					//  X O   X O
					//  O O   O O
					//  X X   X X
					//  O O   O X
					newWeight		= 1.0;	// reduce to common (newPieceIdxA)
					newPieceIdxB	= nil;
				}, {
					//  X O   X O   X O   X O   X O   O O   O O   O X   O X
					//  O X   O X   O X   O X   O X   O X   O X   X X   X X
					//  X O   X X   X O   O O   O X   O O   O X   O O   X O
					//  O O   O O   O X   O O   O O   O O   O O   O O   O O
					if( pieceIdxA.isNil, {
						//  O O   O O
						//  O X   O X
						//  O O   O X
						//  O O   O O
						if( newWeight < 0.5, {
							newPieceIdxA = newPieceIdxB;
						});
						newWeight		= 1.0;	// reduce to one           // 9
						newPieceIdxB	= nil;
					}, {
						//  X O   X O   X O   X O   X O   O X   O X
						//  O X   O X   O X   O X   O X   X X   X X
						//  X O   X X   X O   O O   O X   O O   X O
						//  O O   O O   O X   O O   O O   O O   O O
						if( newPieceIdxB.isNil, {
							//  X O   X O
							//  O X   O X
							//  X O   O O
							//  O O   O O
							if( pieceIdxB.isNil, {	  // 10
								//  X O
								//  O X
								//  O O
								//  O O
								// fade as proposed
							}, {                         // 11
								//  X O
								//  O X
								//  X O
								//  O O
								// reduce old to strongest
								newPieceIdxA	= if( weight >= 0.5, pieceIdxA, pieceIdxB );
								newWeight		= 1.0;								newPieceIdxB	= nil;
							});
						}, {
							//  X O   X O   X O   O X   O X
							//  O X   O X   O X   X X   X X
							//  X X   X O   O X   O O   X O
							//  O O   O X   O O   O O   O O
							if( pieceIdxB.isNil, {
								//  X O   O X
								//  O X   X X
								//  O X   O O
								//  O O   O O
								if( newPieceIdxB == pieceIdxA, {   // 12
									//  O X
									//  X X
									//  O O
									//  O O
									// fade as proposed
								}, {                             // 13
									//  X O
									//  O X
									//  O X
									//  O O
									// reduce new to strongest
									if( newWeight < 0.5, {
										newPieceIdxA = newPieceIdxB;
									});
									newWeight		= 1.0;									newPieceIdxB	= nil;
								});
							}, {
								//  X O   X O   O X
								//  O X   O X   X X
								//  X X   X O   X O
								//  O O   O X   O O
								if( newPieceIdxB == pieceIdxB, {      // 14
									//  X O
									//  O X
									//  X X
									//  O O
									// reduce to common (newPieceIdxB)
									newWeight		= 1.0;
									newPieceIdxA	= newPieceIdxB;
									newPieceIdxB	= nil;
								}, {
									//  X O   O X
									//  O X   X X
									//  X O   X O
									//  O X   O O
									if( newPieceIdxB == pieceIdxA, {    // 15
										//  O X
										//  X X
										//  X O
										//  O O
										// reduce to common (newPieceIdxB)
										newWeight    = 1.0;
										newPieceIdxA = newPieceIdxB;
										newPieceIdxB = nil;
									}, {                               // 16
										//  X O
										//  O X
										//  X O
										//  O X
										// reduce old to strongest
										newPieceIdxA = if( weight >= 0.5, pieceIdxA, pieceIdxB );
										newWeight    = 1.0;										newPieceIdxB = nil;
									});
								});
							});
						});
					});
				});
			});
				
			if( process, {  // perform fade
				if( verbose, {[ "--> adjusted", "newPieceIdxA", newPieceIdxA, "newPieceIdxB", newPieceIdxB, "newWeight", newWeight ].postln });

				newSynthA = pieces[ newPieceIdxA ].synth;
				if( newSynthA.isRunning.not, {
					if( verbose, {[ "RUN", newPieceIdxA ].postln });
					bndl.add( newSynthA.runMsg( true ));
				});
				if( newWeight != 1.0, {
					newSynthB = pieces[ newPieceIdxB ].synth;
					if( newSynthB.isRunning.not, {
						if( verbose, {[ "RUN", newPieceIdxB ].postln });
						bndl.add( newSynthB.runMsg( true ));
					});
				}, {
					newSynthB = nil;
				});
				if( pieceIdxA.notNil, {
					oldSynthA = pieces[ pieceIdxA ].synth;
					if( (oldSynthA != newSynthA) and: { oldSynthA != newSynthB }, {
						if( verbose, {[ "PAUSE", pieceIdxA ].postln });
						bndl2.add( oldSynthA.runMsg( false ));
					});
					if( weight != 1.0, {
						oldSynthB = pieces[ pieceIdxB ].synth;
						if( (oldSynthB != newSynthA) and: { oldSynthB != newSynthB }, {
							if( verbose, {[ "PAUSE", pieceIdxB ].postln });
							bndl2.add( oldSynthB.runMsg( false ));
						});
					});
				});
				startFade		= if( fadeBool, weight, { 1.0 - weight });
				newFadeBool	= if( newPieceIdxA == pieceIdxA, fadeBool, { fadeBool.not });
				stopFade		= if( newFadeBool, newWeight, { 1.0 - newWeight });
				
				if( pieces[ newPieceIdxA ].bus != newFadeBool, {
//[ "piece", newPieceIdxA, "had bus", if( pieces[ newPieceIdxA ].bus, \B, \A ), "will get bus", if( newFadeBool, \B, \A )].postln;
					pieces[ newPieceIdxA ].bus = newFadeBool;
					if( verbose, {[ "SET", newPieceIdxA, if( pieces[ newPieceIdxA ].bus, \B, \A )].postln });
					bndl.add( newSynthA.setMsg( \bus, if( pieces[ newPieceIdxA ].bus, busB, busA )));
				});
				if( newSynthB.notNil, {
					if( pieces[ newPieceIdxB ].bus == newFadeBool, {
						pieces[ newPieceIdxB ].bus = newFadeBool.not;
						if( verbose, {[ "SET", newPieceIdxB, if( pieces[ newPieceIdxB ].bus, \B, \A )].postln });
						bndl.add( newSynthB.setMsg( \bus, if( pieces[ newPieceIdxB ].bus, busB, busA )));
					});
				});

				// perform
				synthFade	= Synth.basicNew( \owFadeLine, s );
//				fdt		= fadeTime * (weight - newWeight).abs;
				fdt		= grabFadeTime * (stopFade - startFade).abs;
				bndl.add( synthFade.newMsg( group, [ \out, fadeBus, \start, startFade, // group does not matter
				                                     \stop, stopFade,
				                                     \dur, fdt ], \addToTail ));
				if( verbose, {[ "Sending bndl...", "startFade", startFade, "stopFade", stopFade, "fdt", fdt ].postln });
				bndl.send( s );
				synthFade.waitForEnd( fdt + 4 );
				if( bndl2.messages.size > 0, {
					if( verbose, {[ "Sending bndl2..." ]});
					bndl2.send( s );
				});
				s.sync; // !! I don't know why, but this is absolutely necessary, otherwise we occasionally get two RUN's which shouldn't be....
				
				fadeBool				= newFadeBool;
				pieceIdxA				= newPieceIdxA;
				pieceIdxB				= newPieceIdxB;
				weight				= newWeight;

				if( verbose, {[ "After fade", "pieceIdxA", pieceIdxA, "pieceIdxB", pieceIdxB, "weight", weight, "fadeBool", fadeBool ].postln });
				
			}, {

				if( synthCond.test.not, {
					this.prSleep( pieceIdxA, pieceIdxB, weight );
				});
				synthCond.test = false;
			});
		});
	}
	
	*prPickPieces {
		var mix, success, name, pieceWeightsNorm;

		pieceWeightsNorm	= (pieces.collect(_.chance) / pieceFreqs).normalizeSum;
		grabPieceIdxA		= pieceWeightsNorm.windex;
		mix				= pieces[ grabPieceIdxA ].mixProb.coin;
		if( mix, {
			success		= false;
			while({ success.not }, {
				grabPieceIdxB	= pieceWeightsNorm.windex;
				name			= pieces[ grabPieceIdxB ].name.asSymbol;
				success		= (grabPieceIdxB != grabPieceIdxA) and: { pieces[ grabPieceIdxA ].disallowedMixes.detect({ arg name2; name2 == name }).isNil };
			});
			grabWeight	= rrand( 0.05, 0.95 );
			pieceFreqs[ grabPieceIdxA ] = pieceFreqs[ grabPieceIdxA ] + if( grabWeight >= 0.5, 1, 0.5 );
			pieceFreqs[ grabPieceIdxB ] = pieceFreqs[ grabPieceIdxB ] + if( grabWeight >= 0.5, 0.5, 1 );
		}, {
			grabPieceIdxB	= nil;
			grabWeight	= 1.0;
			pieceFreqs[ grabPieceIdxA ] = pieceFreqs[ grabPieceIdxA ] + 1;
		});
		grabFadeTime	= exprand( fadeTimeAutoMin, fadeTimeAutoMax ) * pieces[ grabPieceIdxA ].fadeFactor;
	}
	
	
	*prPickFromModel {
		var floatIdx;
		
		floatIdx		= OhrwaldModel.normalizedHAngle * (pieces.size - 1);
		grabPieceIdxA	= floatIdx.floor;
		grabWeight	= (1 - (floatIdx % 1.0)).round( 0.001 );
		grabPieceIdxB	= if( grabWeight < 1.0, {ÊgrabPieceIdxA + 1 });
		grabFadeTime	= fadeTimeInteract;
	}
	
	*prSleep { arg pieceIdxA, pieceIdxB, weight;
		var clpseStartAuto, dur, dur1, dur2;
		
		dur1	= exprand( pieces[ pieceIdxA ].minDur, pieces[ pieceIdxA ].maxDur ) * pieceDurScale;
		dur	= if( weight != 1.0, {
			dur2	= exprand( pieces[ pieceIdxB ].minDur, pieces[ pieceIdxB ].maxDur );
			weight.linexp( 0.0, 1.0, dur2, dur1 );
		}, {
			dur1;
		});
		if( verbose, { "Pausing... (timeout = %)\n\n\n".postf( dur.round( 0.1 ))});
		clpseStartAuto = Collapse({
			this.prPickPieces;
			synthCond	.test	= true;
			synthCond.signal;
		}, dur );
		if( useAuto, {ÊclpseStartAuto.defer });
		synthCond.wait; // wait for model updates
		clpseStartAuto.cancel;
		if( verbose, { "Resumed.".postln });
	}
	
	*gui {
		var win, ggMixNames, updSynths, funcMixModelUpd, clpseMixModelUpd, routQuery, fadeVal = 0, colrs, colrGray;
	
		if( mixWin.notNil, { ^mixWin.front });

		colrs = [ 0x000000, 0x120406, 0x20070A, 0x290A0D, 0x300C10, 0x340D11, 0x360E12, 0x370F13,
		          0x381014, 0x3A1115, 0x3D1216, 0x3F1317, 0x401418, 0x411518, 0x421617, 0x431717,
		          0x451818, 0x471919, 0x491A1A, 0x4B1B1B, 0x4D1D1C, 0x4E1E1C, 0x4F1F1B, 0x50201C,
		          0x50201C, 0x52221D, 0x53231F, 0x542420, 0x552521, 0x562622, 0x572723, 0x582824,
		          0x592924, 0x5A2A25, 0x5B2B27, 0x5C2C29, 0x5D2D2C, 0x5E2E30, 0x5F2F33, 0x603035,
		          0x623138, 0x63323A, 0x64343C, 0x64353D, 0x65363F, 0x673742, 0x683844, 0x6A3946,
		          0x6C3A48, 0x6E3B4A, 0x713C4D, 0x733D50, 0x753E53, 0x773F56, 0x784059, 0x79405B,
		          0x7A405D, 0x7B405F, 0x7D3F62, 0x7F4066, 0x814069, 0x83406C, 0x843F6D, 0x863F6E,
		          0x893F6F, 0x8B4070, 0x8D4171, 0x8F4273, 0x914376, 0x934479, 0x94447B, 0x95447E,
		          0x964481, 0x974384, 0x984388, 0x99438B, 0x9A438E, 0x9B4490, 0x9C4492, 0x9E4494,
		          0x9F4497, 0xA04399, 0xA1439C, 0xA2439F, 0xA344A3, 0xA444A6, 0xA545A9, 0xA647AC,
		          0xA748AF, 0xA849B3, 0xA94AB6, 0xAA4BB9, 0xAB4CBC, 0xAB4DBE, 0xAB4EC0, 0xAB4FC2,		          0xAA50C4, 0xAA51C6, 0xAA52C8, 0xAB54CB, 0xAB54CD, 0xAC55CF, 0xAE56D1, 0xAF58D3,
		          0xAF59D4, 0xAF5AD5, 0xAF5BD6, 0xAE5CD7, 0xAD5DD6, 0xAC5ED6, 0xAB5FD6, 0xAA60D7,
		          0xAA61D8, 0xAB62D9, 0xAB63DA, 0xAA64DB, 0xA965DC, 0xA866DD, 0xA768DF, 0xA669E0,
		          0xA66BE1, 0xA76DE2, 0xA770E3, 0xA672E3, 0xA574E2, 0xA376E2, 0xA278E3, 0xA17AE4,
		          0xA07DE5, 0x9F7FE6, 0x9E80E7, 0x9D82E7, 0x9C84E6, 0x9B86E6, 0x9A88E7, 0x988AE8,
		          0x978CEA, 0x978EEA, 0x9691EA, 0x9693E9, 0x9695E8, 0x9797E7, 0x9698E5, 0x969BE4,
		          0x969DE3, 0x979FE2, 0x98A1E1, 0x99A3E0, 0x9AA6DF, 0x9BA7DE, 0x9BA8DD, 0x9BA9DC,
		          0x9BAADB, 0x9AACD9, 0x99AED7, 0x98B0D5, 0x97B2D3, 0x95B5D1, 0x94B7D0, 0x93B8CF,
		          0x93BBCF, 0x91BDCE, 0x90BFCC, 0x8FC1CB, 0x8EC3CA, 0x8DC4C9, 0x8CC5C8, 0x8BC6C7,
		          0x8AC7C6, 0x89C8C5, 0x88C9C4, 0x87CAC3, 0x86CBC2, 0x87CCC1, 0x87CDC0, 0x87CEBF,
		          0x86CFBE, 0x85D0BC, 0x83D1BB, 0x83D3BB, 0x82D4BA, 0x82D6BA, 0x82D9B8, 0x83DBB6,
		          0x83DDB2, 0x84DFAE, 0x85E1A9, 0x87E3A5, 0x89E4A3, 0x8BE5A1, 0x8DE69F, 0x90E79E,
		          0x92E89B, 0x94E999, 0x96EA97, 0x98EB95, 0x9AEC93, 0x9DEE90, 0x9FEF8F, 0xA0F08D,
		          0xA2F18B, 0xA3F288, 0xA5F386, 0xA6F484, 0xA8F582, 0xA9F681, 0xABF77F, 0xADF87E,
		          0xAFF97D, 0xB1FA7C, 0xB4FB7B, 0xB8FB7A, 0xBBFB79, 0xBEFB78, 0xC0FA77, 0xC2FA77,
		          0xC4FA77, 0xC7FB78, 0xC9FB78, 0xCCFB7A, 0xCFFB7B, 0xD3FA7C, 0xD7FA7C, 0xDAFA7D,
		          0xDDFA7E, 0xDFFB80, 0xE2FB84, 0xE4FB88, 0xE6FB8C, 0xE8FA90, 0xEAFA94, 0xECFA97,
		          0xEEFB9A, 0xF0FA9C, 0xF1FA9E, 0xF2FAA0, 0xF3FBA3, 0xF3FBA5, 0xF4FBA7, 0xF5FBAA,
		          0xF7FBAF, 0xF8FAB6, 0xF9FABC, 0xFAFAC2, 0xFBFBC8, 0xFBFBCC, 0xFBFBD2, 0xFBFBD7,
		          0xFAFADE, 0xFAFAE6, 0xFAFAEF, 0xFAFAF9, 0xFBFBFF, 0xFBFBFF, 0xFCFCFF, 0xFEFEFE ]
			.collect({ arg rgb; Color.new255( (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF )});
		colrGray		= Color.gray( 0.6 );

		win			= JSCWindow( "Ohrwald : Mix", Rect( 0, 0, 128, pieces.size * 54 + 12 ), resizable: false,
								server: Ohrwald.swing );
		win.view.background = Color.black;
		
		ggMixNames	= pieces.collect({ arg p, y;
			JSCStaticText( win, Rect( 8, 8 + (y * 54), 112, 46 ))
				.align_( \center )
//				.background_( Color.white )
				.string_( p.name );
		});
		
		funcMixModelUpd = {
			var amount;
			ggMixNames.do({ arg ggPiece, y;
				ggPiece.background = if( pieces[ y ].synth.isRunning, {
//					[ "fadeVal", fadeVal ].postln;
					amount = if( pieces[ y ].bus, fadeVal, {Ê1.0 - fadeVal });
//					amount = (1 - min( 1, abs( y - pieceIdxA )));
//					if( amount == 0, {
//						Color.white;
//					}, {
//						Color.white.blend( Color.red, amount );
//					});
					colrs[ (amount * 255).round.asInteger ];
				}, {
					colrGray;
				});
			});
		};
		clpseMixModelUpd = Collapse( funcMixModelUpd, 0.05 );
//		updModel = UpdateListener.newFor( OhrwaldModel, { arg m, what, who; clpseMixModelUpd.instantaneous }, \angle );
		funcMixModelUpd.value;
		
		updSynths = pieces.collect({ arg p;
			UpdateListener.newFor( p.synth, { arg upd, node, what;
				if( (what == \n_on) || (what == \n_off), { clpseMixModelUpd.instantaneous });
			});
		});
		
		routQuery = Routine({
			var oldVal, cond;
			cond = Condition.new;
			inf.do({
				cond.test = false;
				fadeBus.get({ arg val; fadeVal = val; cond.test = true; cond.signal });
				cond.wait;
				if( fadeVal != oldVal, {
					funcMixModelUpd.value;
					oldVal = fadeVal;
				});
				0.05.wait;
			});
		}).play( AppClock );
		
		win.onClose = {
			routQuery.stop;
//			updModel.remove;
			updSynths.do( _.remove );
			mixWin = nil;
		};
		ScissUtil.positionOnScreen( win, 0.2, 0.5 );
		win.front;
		mixWin = win;
		^mixWin;
	}
	
	*createDefs { arg allNumChans;
		var result;
		
		2.do({ arg eqIter;
			result = result.addAll( allNumChans.collect({ arg numChannels;
				SynthDef( \owDisk ++ numChannels ++ if( eqIter == 0, "", "EQ" ), {
					arg bus, chanOff = 0, buf, amp = 1.0, spkrEQs;
					var disk, spkrGains;
					spkrGains = Control.names([ \spkrGains ]).kr( 1 ! numChannels );
					disk = DiskIn.ar( numChannels, buf, 1 );
					if( eqIter == 1, {
						spkrEQs = Control.names([ \spkrEQs ]).kr( 0 ! numChannels );
						disk    = BHiShelf.ar( disk, 1000, 10, spkrEQs );
					});
					Out.ar( bus + chanOff, disk * amp * spkrGains );
				});
			}));
		});
		result = result.add(
			SynthDef( \owCirc, { arg bus;
				var inp, outp;
				inp	= In.ar( bus, Ohrwald.masterNumChannels );
				outp	= nil ! Ohrwald.masterNumChannels;
				Ohrwald.circChansT.do({ arg ch, i; outp[ ch ] = inp[ i ]});
				ReplaceOut.ar( bus, outp );
			});
		);
		result = result.add(
			SynthDef( \owFadeLine, { arg out, start, stop, dur;
				Out.kr( out, Line.kr( start, stop, dur, doneAction: 2 ));
			});
		);
		result = result.add(
			SynthDef( \owFadeLine2, { arg out, start, stop, dur, pauseID;
				var line;
				line = Line.kr( start, stop, dur, doneAction: 2 );
				Pause.kr( Done.kr( line ), pauseID );
				Out.kr( out, line );
			});
		);
		result = result.add(
			SynthDef( \owXFader, { arg in1, in2, fadeBus, out;
				var inp1, inp2, fade, outp;
				inp1	= In.ar( in1, Ohrwald.masterNumChannels );
				inp2	= In.ar( in2, Ohrwald.masterNumChannels );
				fade	= In.kr( fadeBus ) * 2 - 1;
				outp	= XFade2.ar( inp1, inp2, fade ).asArray;
				Out.ar( out, outp );
			});
		);
		^result;
	}
}