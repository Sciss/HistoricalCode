/**
 *	Wolkenpumpe V
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.25, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
Wolkenpumpe {
	classvar <>jarURL				= "file:///Users/rutz/Documents/workspace/Wolkenpumpe/Wolkenpumpe.jar";
//	classvar <>masterIndex			= 2; // 2;
	classvar <>masterNumChannels	= 2;
	classvar <>soundCardChans;
	classvar <>maxProcChannels		= 2;
	classvar <>soloIndex			= 0;
	classvar <>soloChannels			= 2;
	classvar <>dataFolder;
	classvar <>useAux				= false; // true;
	classvar	<>auxIndex			= 6;

	classvar <>debugTask			= false;
	classvar <>default;
	
	classvar all;

	var <server, <gui, <swing, <uf;
	var <masterGroup, <masterBus, masterSynth, silentBus;
	var <solo;
	var <graph;
	
	var <tapeCues;
	var <>tapeCueVariation = -1; // -1 = random

	var condTask, queueTask, taskThread, routTask;
	var <fadeTime = 0.0;
	var lines;
	
	var <>inspectErrors = true;
			
	var ctrlMapWatcher, transactions, persistIDToObj, persistObjToID, persistID = 0;
	var respSync, condSync, currentSyncID;
	
	*initClass {
		dataFolder		= thisProcess.platform.userAppSupportDir +/+ "nuages";
	}
	
	*new {
		^super.new.prInit;
	}
	
	*find { arg server; ^all[ server ]}
	
	prInit {
		condTask		= Condition.new;
		queueTask		= PriorityQueue.new;
		lines		= IdentityDictionary.new;
		uf			= NuagesUFactory( this );
		persistIDToObj= IdentityDictionary.new;
		persistObjToID= IdentityDictionary.new;
		graph		= NuagesLinkedGraph( this );
		
		if( default.isNil, {
			default	= this;
		});
	}
	
	boot {
		var i, j, jo;
		server = Server.default;
		if( all.isNil, {
			all = IdentityDictionary[ server -> this ];
		}, {
			all.put( server, this );
		});
		server.waitForBoot({
			fork {
				this.prInitAudio;
				swing = SwingOSC.default;
				jo = swing.options.javaOptions;
				i = jo.find( "-Xdock:name=" );
				if( i.notNil, {
					i = i + 12;
					j = (jo.copyToEnd( i ).find( " " ) ?? { jo.size - i }) + i;
					swing.options.javaOptions = jo.copyRange( 0, i - 1 ) ++ "Wolkenpumpe" ++ jo.copyToEnd( j );
//					swing.options.javaOptions.postcs;
				});

				swing.waitForBoot({
					this.prInitGUI;
					this.tryChanged( \booted );
				});
				
//				this.tryChanged( \booted );
			};
		});
	}
	
	*run {
		^this.new.boot;
	}
	
	forkSync {
		currentSyncID = UniqueID.next;
		condSync.test = false;
		^[ '/sync', currentSyncID ];
	}
	
	joinSync {
//		if( currentSyncID.notNil, {
			condSync.wait;
//		});
	}
	
	newBundle { ^NuagesOSCBundle( this )}
	
	masterVolume_ { arg val;
		masterSynth.set( \volume, val );
	}

	// val from 0.0 (pure master) to 1.0 (pure aux)
	auxFade_ { arg val;
		masterSynth.set( \auxFade, val.clip( 0.0, 1.0 ));
	}
	
	getSilentBusÊ{Êarg numChannels;
		if( numChannels > silentBus.numChannels, {
			Error( "numChannels (%) cannot exceed (%)".format( numChannels, silentBus.numChannels )).throw;
		});
		^Bus( \audio, silentBus.index, numChannels );
	}
	
	isSilentBus { arg bus; ^(bus.index == silentBus.index) }

	prInitAudio {
		var def, defName, bndl;
		
		if( soundCardChans.isNil, {
			soundCardChans = 0 ! masterNumChannels;
		}, {
			if( masterNumChannels != soundCardChans.size, {
				Error( "masterNumChannels (%) must match soundCardChans size (%)".format( masterNumChannels, soundCardChans.size )).throw;
			});
		});
		
		ctrlMapWatcher	= NuagesControlMapWatcher( server );
		masterGroup		= Group( server );
//		masterBus			= Bus( \audio, masterIndex, masterChannels );
		masterBus			= Bus.audio( server, masterNumChannels );
		silentBus			= Bus.audio( server, masterNumChannels );		condSync			= Condition.new;
		condSync.test		= true; // initially no need to wait
		
		respSync			= OSCresponderNode( server.addr, '/synced', { arg time, resp, msg;
			if( msg[ 1 ] == currentSyncID, {
				condSync.test = true; condSync.signal;
			});
		}).add;
		
		defName			= \nuagesMaster ++ masterNumChannels;
		def				= SynthDef( defName, { arg in, volume = 1.0, auxFade = 0.0;
			var inp, outp, sum, auxVolume, auxOutp, outChans;
			
			inp	= In.ar( in, masterNumChannels );
			outp	= Limiter.ar( inp * volume, 0.98 );
			if( useAux, {
				auxOutp = Limiter.ar( Mix( inp ) * volume / masterNumChannels.sqrt, 0.98 ) * auxFade.sqrt;
				outp = outp * (1.0 - auxFade).sqrt;
				Out.ar( auxIndex, auxOutp );
			});
			outChans = Control.names([ \outs ]).ir( Array.series( masterNumChannels, 0 ));
//			ReplaceOut.ar( masterIndex, outp );
			outChans.do({ arg ch, i; Out.ar( ch, outp[ i ])});
		});
		masterSynth	= Synth.basicNew( defName, server );
		def.send( server );
		server.sync;
		bndl = this.newBundle;
		bndl.add( masterSynth.newMsg( masterGroup, [ \in, masterBus, \volume, 1.0 ]));
		bndl.add( masterSynth.setnMsg( \outs, soundCardChans ));
		bndl.send;

		(1..maxProcChannels).do({ arg numCh;
			def = SynthDef( "nuages-meter" ++ numCh, { arg i_kOtBs, t_trig;
				var in, rms, peak, i_aInBs, temp;
		
				i_aInBs = Control.names([ \i_aInBs ]).ir( Array.series( numCh ));
				in	= In.ar( i_aInBs );
				rms	= Lag.ar( in.squared, 0.1 );	// Amplitude.ar reports strange things (at least not the RMS)
				peak	= Peak.ar( in, t_trig );
				
				if( numCh > 1, {
					temp = peak;
					peak = peak.first;
					temp.copyToEnd( 1 ).do({ arg p;
						peak = peak.max( p );
					});
					
					rms = Mix( rms ) / numCh;
				});
		
				// we are reading the values asynchronously through
				// a /c_getn on the meter bus. each request is followed
				// by a /n_set to re-trigger the latch so that we are
				// not missing any peak values.
				Out.kr( i_kOtBs, [ Latch.kr( peak, t_trig ), rms ]);
			});
			def.send( server );
		});

//		var cues, path, idx, exists = true;
//		idx = 0;
//
//		cueInfo = (dataFolder ++ "cueinfos.txt").load;
//
		tapeCues = 
			PathName( dataFolder +/+ "tapes" ).files
				.sort({ arg a, b; a.fileName <= b.fileName })
				.collect({ arg p; p.fullPath });
				
		solo = NuagesSoloManager( this );

		routTask = fork { this.prTaskBody };
	}
	
	verifyTapeCues {
		var sf, numChannels, num, tcc, success = true;
		"Verifying tape cues...".postln;
		tapeCues.do({ arg path;
			sf = SoundFile.openRead( path );
			if( sf.isNil, {
				numChannels = numChannels.add( nil );
			}, {
				numChannels = numChannels.add( sf.numChannels );
				sf.close;
			});
		});
		num = numChannels.reject(_.isNil).mean.round.asInteger;
		numChannels.do({ arg ch, i;
			if( ch != num, {
				if( ch.isNil, {
					"  File '%' failed to open!\n".postf( tapeCues[ i ]);
				}, {
					"  File '%' has illegal numChannels of %!\n".postf( tapeCues[ i ], ch );
				});
				success = false;
			});
		});
		if( success, "OK.", "Failed." ).postln;
		^success;
	}
	
	prInitGUI {
		swing.addClasses( jarURL );
		gui = NuagesGUI( this ).makeGUI;
	}
	
	fadeTime_ { arg val;
		if( val != fadeTime, {
			fadeTime = val;
//			clpseFadeTime.instantaneous;
			this.tryChanged( \fadeTime );
		});
	}
	
	addControlMapWatcher { arg bus, func; ctrlMapWatcher.add( bus, func )}
	removeControlMapWatcher { arg bus; ctrlMapWatcher.remove( bus )}
	
     stopLines { arg vertex;
		var dict, rout;

		TypeSafe.checkArgClass( thisMethod, vertex, NuagesV, false );
		
		dict = lines[ vertex ];
		if( dict.notNil, {
			dict.keysValuesDo({ arg attr, rout;
				rout.stop;
			});
			lines.removeAt( vertex );
		});
     }

	stopLine { arg vertex, attr;
		var dict, rout;

		TypeSafe.checkArgClasses( thisMethod, [ vertex, attr ], [ NuagesV, UnitAttr ], [ false, false ]);

		dict = lines[ vertex ];
		if( dict.notNil, {
			rout = dict[ attr ];
			if( rout.notNil, {
				rout.stop;
				this.prRemoveLine( vertex, attr );
			});
		});
	}
	
	prAddLine { arg vertex, attr, rout;
		var dict;

		TypeSafe.checkArgClasses( thisMethod, [ vertex, attr, rout ],
		                                      [ NuagesV, UnitAttr, Routine ],
		                                      [ false, false, false ]);
		
		dict = lines[ vertex ];
		if( dict.isNil, {
			dict = IdentityDictionary.new;
			lines[ vertex ] = dict;
		});
		dict[ attr ] = rout;
	}
	
	prRemoveLine { arg vertex, attr;
		var dict;
		
		TypeSafe.checkArgClasses( thisMethod, [ vertex, attr ], [ NuagesV, UnitAttr ], [ false, false ]);
		                                      
		dict = lines[ vertex ];
		if( dict.notNil, {
			dict.removeAt( attr );
			if( dict.isEmpty, {
				lines.removeAt( vertex );
			});
		});
	}

	prFindAttr { arg vertex, name;
		var unit;
		
		unit = vertex.proc.unit;
		^if( unit.notNil, {
			unit.attributes.detect({ arg attr; attr.name === name });
		});
	}

	// no transactions!
	taskSolo { arg proc;
		var bndl;
		
		TypeSafe.checkArgClass( thisMethod, proc, NuagesProc, true );
		
		bndl = this.newBundle;
		solo.setToBundle( bndl, proc );
		bndl.send( server );
		^true;
	}
	
	taskPlayStop { arg vertex, fdt;
		var unit, bndl, trns;

		TypeSafe.checkArgClass( thisMethod, vertex, NuagesV, false );
		
//		server.sync;
		unit = vertex.proc.unit;
		if( unit.isNil, { ^false });
		
		bndl = this.newBundle;
		trns = NuagesT.procPlay( vertex, if( unit.isPlaying, \stop, \play ), fdt ? fadeTime );
		if( trns.perform( bndl ).not, { ^false });
		bndl.send( server );
		^true;
	}
	
	// XXX should use ToBundle methods
	// XXX trns
	taskRecordStartStop { arg recVertex, sourceVertex, fdt, cancel = false;
		var recUnit, recFadeTime;
		
		TypeSafe.checkArgClasses( thisMethod, [ recVertex, sourceVertex, fdt, cancel ],
		                                      [ NuagesV, NuagesV, SimpleNumber, Boolean ],
		                                      [ false, true, true, false ]);
		
		recUnit = recVertex.proc.unit;
		if( recUnit.isNil, { ^false });
	
		if( recUnit.isRecording, {
			if( cancel, {
				if( recUnit.cancelRecording.not, {
//					this.playSpeechCue( \Failed );
					TypeSafe.methodError( thisMethod, "cancelRecording failed" );
				});
			}, {
				if( recUnit.stopRecording.not, {
//					this.playSpeechCue( \Failed );
					TypeSafe.methodError( thisMethod, "stopRecording failed" );
				});
			});
		}, {
			if( sourceVertex.isNil, {
				TypeSafe.methodError( thisMethod, "No record source (solo'ed proc)" );
			});
		
			recFadeTime = fdt ?? fadeTime;
			if( recUnit.startRecording( sourceVertex.proc, nil, { arg numFrames;
				this.taskSched( nil, \prTaskRecordDone, recVertex, recUnit, numFrames, recFadeTime );
			}).not, {
// NuagesLoopUnit already prints the error
//				this.playSpeechCue( \NoRecordSource );
//				TypeSafe.methodError( thisMethod, "startRecording - no record source" );
			});
		});
		^true;
	}

	prTaskRecordDone { arg vertex, unit, numFrames, recFadeTime;
		var bndl;
		
		TypeSafe.checkArgClasses( thisMethod, [ vertex, unit, numFrames, recFadeTime ],
		                                      [ NuagesV, NuagesU, SimpleNumber, SimpleNumber ],
		                                      [ false, false, false, false ]);
		
//",,,,1".postln;
		if( vertex.proc.unit == unit, {
//",,,,2".postln;
			bndl = this.newBundle;
			vertex.proc.unit.useRecording;
			vertex.proc.crossFadeToBundle( bndl, Dictionary.new, recFadeTime );
//",,,,3".postln;
			bndl.send( server );
		}, {
			unit.trashRecording;
		});
		^true;
	}

	taskDuplicateProcs { arg proc, metaData;
		var chain, pred, succ, bus, oProc, bndl, trns;
	
		TypeSafe.checkArgClass( thisMethod, proc, NuagesProc, false );
		
		if( proc.unit.isKindOf( NuagesOutputUnit ), {
			proc = proc.sources.choose;
			if( proc.isNil, { ^false });
		});
		chain = [ proc ];
		succ  = proc;
		while({ succ.sources.size > 0 }, {
			succ = succ.sources.choose;  // XXX
			chain = chain.add( succ );
		});
		if( chain.isEmpty, { ^false });  // XXX should not happen
				
		chain = chain.reverse;

		bndl		= this.newBundle;
		chain	= chain.collect({ arg orig, i;
			trns = NuagesT.procDup( orig, pred, metaData );
			if( trns.perform( bndl ).not, { ^false });
			metaData	= nil;
			pred		= trns.succ;
		});
		trns = NuagesT.procOutput( pred, -96 );
		if( trns.perform( bndl ).not, { ^false });
		
		bndl.send( server );
		^true;
	}
	
	taskInsertGenerator { arg name, metaData;
		var vertex, oVertex, genBox, outBox, bndl, trns;

//try {		
		TypeSafe.checkArgClass( thisMethod, name, Symbol, false );
		
//"------- Wolkenpumpe:taskInsertGenerator 1".postln;
		bndl = this.newBundle;
//		proc = this.createProcToBundle( bndl, name, nil, nil, metaData );
		trns = NuagesT.procCreate( name, true, metaData );
		if( trns.perform( bndl ).not, { ^false });
		vertex = trns.vertex;

		if( vertex.audioOutlets.size > 0, {
			trns = NuagesT.procOutput( vertex, if( vertex.proc.unit.autoPlay, -96, 0 ));
			if( trns.perform( bndl ).not, { ^false });
			oVertex = trns.oVertex;
		});
		
		if( vertex.proc.unit.autoPlay, {
			trns = NuagesT.procPlay( vertex, \play, 1.0 );
			if( trns.perform( bndl ).not, { ^false });
//			proc.playToBundle( bndl, 1.0 );	// XXX
		});
		
		bndl.send( server );
		^true;
	}
	
	taskInsertFilter { arg name, edge, metaData;
		var vertex, bndl, trns;

		TypeSafe.checkArgClasses( thisMethod, [ name,   edge ],
		                                      [ Symbol, NuagesE ],
		                                      [ false,  false ]);

		bndl = this.newBundle;
		trns = NuagesT.procCreate( name, false );
		if( trns.perform( bndl ).not, { ^false });
		vertex = trns.vertex;

		trns = NuagesT.procInsert( vertex, edge, true, metaData );
		if( trns.perform( bndl ).not, { ^false });
		bndl.send( server );
		^true;
	}
	
	taskConnect { arg sourceVertex, outletIndex, targetVertex, inletIndex;
		var bndl, trns, outlet, inlet;

		TypeSafe.checkArgClasses( thisMethod, [ sourceVertex, outletIndex, targetVertex, inletIndex  ],
		                                      [ NuagesV, Integer,     NuagesV, Integer     ],
		                                      [ false,        false,       false,        false       ]);
		                                      
		if( outletIndex != 0, {
			TypeSafe.methodError( thisMethod, "Currently only left outlet supported" );
			^false;
		});
		outlet = sourceVertex.audioOutlets[ outletIndex ];
		inlet  = targetVertex.audioInlets[ inletIndex ];
		if( outlet.edges.any({ arg e; e.target == inlet }), {
			TypeSafe.methodError( thisMethod, "These ports are already connected" );
			^false;
		});
		
		bndl = this.newBundle;
		trns = NuagesT.connect( sourceVertex, outletIndex, targetVertex, inletIndex );
		if( trns.perform( bndl ).not, { ^false });
		bndl.send( server );
		^true;
	}

	taskFadeRemoveProc { arg vertex, fdt;
		var attr, rout, trns, bndl;
		
		TypeSafe.checkArgClasses( thisMethod, [ vertex, fdt ], [ NuagesV, SimpleNumber ], [ false, true ]);

		fdt = fdt ? fadeTime;
		if( vertex.isNil, { ^false });
		
		if( vertex.proc.unit.numVisibleAudioOutputs == 0, { ^false }); // not allowed for OutputUnit !

		bndl = this.newBundle;

		if( vertex.proc.unit.isKindOf( NuagesUGen ), {  // it's a generator
//			proc.tryChanged( \dying );
			if( vertex.proc.unit.isPlaying.not, {
				this.taskSched( nil, \taskRemoveProc, vertex );
				^true;
			});
			trns = NuagesT.procFadeRemove( vertex, fdt );
			if( trns.perform( bndl ).not, { ^false });
		}, {  // it's a filter
			// trns: works
			attr = this.prFindAttr( vertex, \mix );
			if( attr.isNil, {
				this.taskRemoveProc( vertex );
			}, {
				trns = NuagesT.procDying( vertex );
				if( trns.perform( bndl ).not, { ^false });
//				proc.tryChanged( \dying );
				this.taskLineAttr( vertex, attr, 0.0, fdt, {
					this.taskSched( nil, \taskRemoveProc, vertex );
				});
			});
		});
		bndl.send( server );
		^true;
	}
	
	taskRemoveProc { arg vertex, force = false;
		var bndl, trns;

		TypeSafe.checkArgClasses( thisMethod, [ vertex, force ], [ NuagesV, Boolean ], [ false, false ]);
		
		if( vertex.proc.isNil, { ^false });
		bndl = this.newBundle;
//		if( this.prTaskRemoveProcToBundle( bndl, proc, force ).not, { ^false });
		trns = NuagesT.procRemove( vertex, force );
		if( trns.perform( bndl ).not, { ^false });
		bndl.send( server );
		^true;
	}
		
	taskChangeAttr { arg vertex, attr, newNormVal, fdt, stopLine = true;
		var trns, bndl;
		
		if( vertex.isNil, { ^false });
		bndl	= this.newBundle;
		trns	= NuagesT.procAttr( vertex, attr.name, newNormVal, fdt ? fadeTime, stopLine );
		if( trns.perform( bndl ).not, { ^false });
		bndl.send( server );
		^true;
	}
	
	taskMap { arg sourceVertex, outletIndex, targetVertex, attrIndex, fdt;
		var bndl, trns;
		
		TypeSafe.checkArgClasses( thisMethod, [ sourceVertex, outletIndex, targetVertex, attrIndex, fdt ],
		                                      [ NuagesV,      Integer,     NuagesV,      Integer,   SimpleNumber ],
		                                      [ false,        false,       false,        false,     true ]);
		
		if( sourceVertex.proc.isNil or: {ÊtargetVertex.proc.isNil }, { ^false });
		bndl	= this.newBundle;
		trns	= NuagesT.map( sourceVertex, outletIndex, targetVertex, attrIndex, fdt ? fadeTime );
		if( trns.perform( bndl ).not, { ^false });
		bndl.send( server );
		^true;
	}
	
	// XXX trns
	taskUnmap { arg proc, attr;
		var unit, bndl, ctrlUnit;
		
		TypeSafe.checkArgClasses( thisMethod, [ proc,       attr ],
		                                      [ NuagesProc, UnitAttr ],
		                                      [ false,      false ]);
		
		if( proc.isNil, { ^false });
		unit		= proc.unit;
		if( unit.isNil, { ^false });
		
		bndl	= this.newBundle;
		proc.removeControlMapToBundle( bndl, attr );
		bndl.send( server );
		^true;
	}
	
	// trns: works
	// XXX this could be refactored as a control map input
	taskLineAttr { arg vertex, attr, newNormVal, fdt, doneAction, grain = 0.05;
		var /* upd, */ rout, unit, elapsed, startVal, startTime;
		fdt  = fdt ? fadeTime;
		if( vertex.proc.isNil, { ^false });
		
		this.stopLine( vertex, attr );
		
//		upd = UpdateListener.newFor( proc, { arg upd, proc; rout.stop; upd.remove }, \disposed );
		rout = fork {
			startTime = thisThread.seconds;
			elapsed	= 0.0;
			unit		= vertex.proc.unit;
			if( unit.notNil, {
				startVal	= attr.getNormalizedValue( unit );
				if( startVal != newNormVal, {
					while({ elapsed < fdt }, {
						grain.wait;
						elapsed = thisThread.seconds - startTime;
						this.taskSched( nil, \taskChangeAttr, vertex, attr,
						                     elapsed.linlin( 0, fdt, startVal, newNormVal ), 0.0, false );
					});
				});
//				upd.remove;
				this.prRemoveLine( vertex, attr );
				doneAction.value;
			});
		};
		this.prAddLine( vertex, attr, rout );
		^true;
	}
	
	persistNextID {
		var result = persistID;
		persistID = persistID + 1;
		^result;
	}
	
	persistPut { arg id, object;
if( debugTask, {[ "persistPut", id, object ].postln });
		persistIDToObj[ id ]		= object;
		persistObjToID[ object ]	= id;
	}
	
	persistGet { arg id;
		var result;
		result = persistIDToObj[ id ];
if( debugTask, {[ "persistGet", id, result ].postln });
		^result;
	}

	persistGetID { arg object;
		var result;
		result = persistObjToID[ object ];
if( debugTask, {[ "persistGetID", object, result ].postln });
		^result;
	}
	
	taskRedo { arg ... trnsStrings; var bndl;
		var trns;
		bndl = this.newBundle;
		trnsStrings.do({ arg trnsStr;
if( debugTask, {[ "trns", trns ].postln });
			trns = trnsStr.interpret;
			trns.redo( bndl );
		});
		bndl.send( server );
	}
	
	addTransaction { arg trns;
		transactions = transactions.add( trns );
	}
	
	taskSched { arg delta ... args;
		queueTask.put( /* taskThread.seconds + */ (delta ? 0.0), args );
		condTask.test = true; condTask.signal;
	}

	prTaskBody {
		var task, delta, trnsTime;
		
		taskThread	= thisThread;
//		startTime		= thisThread.seconds;
		
		inf.do({
			while({ queueTask.isEmpty }, {
				condTask.test = false; condTask.wait;
			});
			delta		= queueTask.topPriority; // - thisThread.seconds;
			task			= queueTask.pop;
//[ "delta", delta ].postln;
			if( delta > 0, { delta.wait });
			trnsTime		= thisThread.seconds;
			transactions	= nil;
			try {
				if( debugTask, { task.postln });
				this.perform( *task );
//				server.sync;
			} { arg error;
				error.reportError;
				// XXX transactions.do(_.undo)
				transactions = nil;
//				if( inspectErrors, {{ error.inspect }.defer });
			};
//			transactions.do({ arg trns;
				if( transactions.notNil, { this.tryChanged( \transactions, transactions, trnsTime )});
//			});
		});
	}
}