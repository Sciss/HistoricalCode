/**
 *	(C)opyright 2010 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: ScissUtil, ScissPlus, SwingOSC
 *
 *	@version	0.10, 08-Feb-10
 *	@author	Hanns Holger Rutz
 */
SweepRecorder {
	var win, <server, <folder, <filePrefix = "Sweep", <numSweeps = 1, <sweepDur = 24, <reverbTime = 4;
//	var <deviceInChannels, <deviceOutChannels, <device, <sampleRate;
	var <metaData = true, <mic, <site, <comments, <saveDry = true;
	var <inChannels, <numInChannels = 1, <outChannel = 0, <amp = 0.5;
	var running = false;
	var micSynth;

	*new { arg server;
		^super.new.prInit( server );
	}
	
	metaData_   { arg bool; if( bool != metaData,   {ÊmetaData   = bool; this.tryChanged( \metaData )})}
	mic_        { arg text; if( text != mic,        {Êmic        = text; this.tryChanged( \mic )})}
	site_       { arg text; if( text != site,       {Êsite       = text; this.tryChanged( \site )})}
	comments_   { arg text; if( text != comments,   {Êcomments   = text; this.tryChanged( \comments )})}
	inChannels_ { arg iarr; if( iarr != inChannels, {ÊinChannels = iarr; numInChannels = inChannels.size; this.tryChanged( \inChannels )})}
	outChannel_ { arg intg; if( intg != outChannel, {ÊoutChannel = intg; this.tryChanged( \outChannel )})}
	amp_        { arg floa; if( floa != amp,        {Êamp        = floa; this.tryChanged( \amp )})}
	saveDry_    { arg bool; if( bool != saveDry,    {ÊsaveDry    = bool; this.tryChanged( \saveDry )})}
	folder_     { arg text; if( text != folder,     { folder     = text; this.tryChanged( \folder )})}
	filePrefix_ { arg text; if( text != filePrefix, { filePrefix = text; this.tryChanged( \filePrefix )})}
	numSweeps_  { arg intg; if( intg != numSweeps,  { numSweeps  = intg; this.tryChanged( \numSweeps )})}
	sweepDur_   { arg intg; if( intg != sweepDur,   { sweepDur   = intg; this.tryChanged( \sweepDur )})}
	reverbTime_ { arg intg; if( intg != reverbTime, { reverbTime = intg; this.tryChanged( \reverbTime )})}
	
	prInit { arg argServer;
		server			= argServer ?? { Server.default };
		folder			= thisProcess.platform.recordingsDir;
//		device			= server.options.device;
//		deviceInChannels	= server.options.numInputBusChannels;
//		deviceOutChannels	= server.options.numOutputBusChannels;
//		sampleRate		= server.options.sampleRate;
		inChannels		= [ 1 ];
	}
	
	makeGUI {
		var f, view, gui, ggNumSweeps, ggSweepDur, ggReverbTime, ggSweepAmp, ggFolder, x, y, w1, w2, h,
		    ggSaveDry, ggMetaData, ggMic, ggSite, ggComments, upd, fMetaData, fMic, fSite, fComments,
		    ggRun, ggAbort, ggInChannels, fInChannels, fSaveDry, ggOutChannel, fOutChannel, ggAmp, fAmp,
		    fRunning, fNumSweeps, fSweepDur, fReverbTime;
	
		if( win.notNil, { win.front; ^win });
		
		gui = SwingGUI;
	  SwingOSC.default.waitForBoot({ GUI.use( gui, { // we are using the peak meter
		f = gui.window.new( "Sweep Recorder", Rect( 0, 0, 400, 360 ), false );
		ScissUtil.positionOnScreen( f );
//		view = f.view;
		
		x = 4; y = 4; w1 = 90; w2 = 40; h = 24;
		view = gui.compositeView.new( f, Rect( x - 4, y - 4, x + w1 + w2 + 16, (h + 4) * 4 + 4 ));
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Num Sweeps:" )
			.toolTip_( "Number of sweeps performed for one recording" );
		ggNumSweeps = gui.numberBox.new( view, Rect( x + w1 + 4, y, w2, h ))
			.maxDecimals_( 0 ).clipLo_( 1 )
			.action_({ arg b; this.numSweeps = b.value });
		y = y + h + 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Sweep Dur:" )
			.toolTip_( "Duration of each sweep in seconds" );
		ggSweepDur = gui.numberBox.new( view, Rect( x + w1 + 4, y, w2, h ))
			.maxDecimals_( 0 ).clipLo_( 1 )
			.action_({ arg b; this.sweepDur = b.value });
		y = y + h + 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Reverb Time:" )
			.toolTip_( "Extra time span in seconds recorded after each sweep to account for reverb decay" );
		ggReverbTime = gui.numberBox.new( view, Rect( x + w1 + 4, y, w2, h ))
			.maxDecimals_( 0 ).clipLo_( 1 )
			.action_({ arg b; this.reverbTime = b.value });
		y = y + h + 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Save Dry Sweep:" )
			.toolTip_( "Whether a second audio file for the dry sweep signal should be created" );
		ggSaveDry = gui.checkBox.new( view, Rect( x + w1 + 4, y, w2, h ))
			.canFocus_( false )
			.action_({ arg b; this.saveDry = b.value });

		y = y + h + 24;
		w1 = 90; w2 = 160;
		view = gui.compositeView.new( f, Rect( x - 4, y - 4, x + w1 + w2 + 16, 80 + 8 + (h * 2) ));
		y = 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Mic Chans:" )
			.toolTip_( "Microphone input channels (counting from 1)" );
		h = 80;
		ggInChannels = gui.envelopeView.new( view, Rect( x + w1 + 4, y, w2, h ))
			.thumbWidth_( 16 ).thumbHeight_( 16 )
			.fillColor_( Color.white ).selectionColor_( Color.red )
			.canFocus_( false ).drawLines_( false )
			.action_({ arg b;
				if( b.selection.notEmpty, {
					this.inChannels = b.selection.collect({ arg b, i; if( b, i + 1 )}).reject( _.isNil );
				}, fInChannels );
			});
		y = y + h + 4;
 		h = 24; w2 = 40;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Speaker Chan:" )
			.toolTip_( "Speaker output channel (counting from 1)" );
		ggOutChannel = gui.numberBox.new( view, Rect( x + w1 + 4, y, w2, h ))
			.maxDecimals_( 0 ).clipLo_( 1 );
		y = y + h + 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Amplitude:" )
			.toolTip_( "Speaker output amplitude (linear)" );
		ggAmp = gui.numberBox.new( view, Rect( x + w1 + 4, y, w2, h ))
			.minDecimals_( 2 ).maxDecimals_( 2 ).step_( 0.05 ).clipLo_( 0.05 ).clipHi_( 1 );
 
 		ggRun = gui.button.new( f, Rect( f.view.bounds.width >> 1 - 40, f.view.bounds.height - 40, 80, 30 ))
 			.canFocus_( false )
 			.action_({
 				if( running, {
 					this.abort;
 				}, {
 					this.run;
 				});
 			});
 			
 		fRunning = { ggRun.states = if( running, [[ "Abort", Color.white, Color.red ]], [[ "Run!", Color.white, Color.blue ]])};
 		fRunning.value;

		x = 148; y = 4; w1 = 80; w2 = 140;
		view = gui.compositeView.new( f, Rect( x - 4, y - 4, x + w1 + w2 + 16, (h + 4) * 4 + 4 ));
		x = 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Meta Data:" )
			.toolTip_( "Whether a meta data text file should be created" );
		ggMetaData = gui.checkBox.new( view, Rect( x + w1 + 4, y, w2, h ))
			.canFocus_( false )
			.action_({ arg b; this.metaData = b.value; if( b.value, { ggMic.focus })});
		y = y + h + 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Microphone:" )
			.toolTip_( "Meta date: What microphone was used" );
		ggMic = gui.textField.new( view, Rect( x + w1 + 4, y, w2, h ))
			.action_({ arg b; this.mic = if( b.value != "", b.value )});
		y = y + h + 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Site:" )
			.toolTip_( "Meta date: Which site was recorded" );
		ggSite = gui.textField.new( view, Rect( x + w1 + 4, y, w2, h ))
			.action_({ arg b; this.site = if( b.value != "", b.value )});
		y = y + h + 4;
		gui.staticText.new( view, Rect( x, y, w1, h ))
			.align_( \right ).string_( "Comments:" )
			.toolTip_( "Meta date: Additional comments" );
		ggComments = gui.textField.new( view, Rect( x + w1 + 4, y, w2, h ))
			.action_({ arg b; this.comments = if( b.value != "", b.value )});
		
		fSaveDry = {ÊggSaveDry.value = saveDry }; fSaveDry.value;
		
		fMetaData = {
			ggMetaData.value	= metaData;
			ggSite.enabled	= metaData;
			ggMic.enabled		= metaData;
			ggComments.enabled	= metaData;
		};
		fMetaData.value;
		
		fMic = { ggMic.value = mic ? "" }; fMic.value;
		fSite = { ggSite.value = site ? "" }; fSite.value;
		fComments = { ggComments.value = comments ? "" }; fComments.value;

		fNumSweeps = { ggNumSweeps.value = numSweeps }; fNumSweeps.value;
		fSweepDur = { ggSweepDur.value = sweepDur }; fSweepDur.value;
		fReverbTime = { ggReverbTime.value = reverbTime }; fReverbTime.value;
		
		fOutChannel = { ggOutChannel.value = outChannel }; fOutChannel.value;
		fAmp = { ggAmp.value = amp }; fAmp.value;
		
		fInChannels = {
			var set, pt, x, y, num, inChans;
			inChans = server.options.numInputBusChannels;
			num   = inChans.min( 32 );
			set   = inChannels.asSet;
			x     = nil ! num; y = nil ! num;
			num.do({ arg ch;
				x[ ch ] = (ch % 8).linlin( 0, 7, 0, 1 );
				y[ ch ] = ch.div( 8 ).linlin( 0, 3, 1, 0 );
			});
			ggInChannels.value		= [ x, y ];
			ggInChannels.strings	= Array.fill( num, { arg ch; (ch + 1).asString });
			num.do({ arg ch;
				if( set.includes( ch + 1 ), {
					ggInChannels.selectIndex( ch );
				}, {
					ggInChannels.deselectIndex( ch );
				});
			});
			ggInChannels.editable_( false );
		};
		fInChannels.value;
		
		upd = UpdateListener.newFor( this, { arg upd, rec, what;
			switch( what,
			\numSweeps, fNumSweeps,
			\sweepDur, fSweepDur,
			\reverbTime, fReverbTime,
			\saveDry, fSaveDry,
			\outChannel, fOutChannel,
			\amp, fAmp,
			\inChannels, fInChannels,
			\metaData, fMetaData,
			\mic, fMic,
			\site, fSite,
			\comments, fComments,
			\running, fRunning
			);
		});
		
		win = f;
		f.onClose = { upd.remove; win = nil };
		f.front;
	  })});
		
		^f;
	}
	
	run {
		var micBuf, dryBuf, micDef, dryDef, bndl, drySynth, intBus, path, micPath, dryPath, metaPath, metaFile, date;

		if( running, {
			"Still performing".error;
			^this;
		});
		if( server.serverRunning.not, {
			"Server not running".error;
			^this;
		});
		
		running	= true;

		date		= Date.localtime;
		path		= folder +/+ filePrefix ++ "_" ++ date.stamp;
		micPath	= path ++ ".aif";

		bndl = OSCBundle.new;
		micBuf = Buffer( server, 32768, numInChannels );
		bndl.addPrepare( micBuf.allocMsg( micBuf.writeMsg( micPath, "aiff", "float", 0, 0, true )));
		intBus = Bus.audio( server, 1 );
		micDef = SynthDef( \sweepMic ++ numInChannels, {
			arg buf, out = 2, lowFreq = 20, dur = 24, space = 4, amp = 0.1, numSweeps = 8, intBus;
			var inChans, inp, outp, outpEnv, env, totalLine, trig, bruttoDur, sine, freq;
			
			bruttoDur	= dur + space;
			trig		= Impulse.ar( bruttoDur.reciprocal );
			PulseCount.ar( trig ).poll( trig, "Iter" );
			Line.ar( 0, 0, bruttoDur * numSweeps + (BufDur.kr( buf ) * 1.5), doneAction: 2 );
			env		= EnvGen.ar( Env.linen( 0.05, dur - 0.1, 0.05, 1 ), trig );
			freq		= Sweep.ar( trig, dur.reciprocal ).linexp( 0, 1, lowFreq, SampleRate.ir / 2 );
			sine		= SinOsc.ar( freq, 0, env );
			inChans	= Control.names([ \inChannels ]).ir( Array.series( numInChannels, 1, 1 )) + NumOutputBuses.ir - 1;
			inp		= In.ar( inChans );
			outpEnv	= EnvGen.ar( Env.linen( 0, dur, 0, 1 ));
			DiskOut.ar( buf, inp );
			Out.ar( out, sine * amp );
			Out.ar( intBus, sine );
		});
		bndl.addPrepare( micDef.recvMsg );
		micSynth = Synth.basicNew( micDef.name, server );
		bndl.add( micSynth.newMsg( server, [ \buf, micBuf, \inChannels, inChannels, \out, outChannel - 1, \numSweeps, numSweeps,
			\dur, sweepDur, \space, reverbTime, \amp, amp, \intBus, intBus ]));

		if( saveDry, {
			dryPath	= path ++ "_dry.aif";
			dryBuf = Buffer( server, 32768, 1 );
			bndl.addPrepare( dryBuf.allocMsg( dryBuf.writeMsg( dryPath, "aiff", "float", 0, 0, true )));
			dryDef = SynthDef( \sweepDry, {
				arg buf, in;
				var inp;
				inp = In.ar( in );
				DetectSilence.ar( inp, doneAction: 2 );
				DiskOut.ar( buf, inp );
			});
			bndl.addPrepare( dryDef.recvMsg );
			drySynth = Synth.basicNew( dryDef.name, server );
			bndl.add( drySynth.newMsg( micSynth, [ \buf, dryBuf, \in, intBus ], \addAfter ));
			drySynth.onEnd = { dryBuf.close; dryBuf.free };
		});
		
		if( metaData, {
			metaPath = path ++ "_info.txt";
			metaFile = File( metaPath, "w" );
			metaFile.putString( "ImpulseResponse=" ++ micPath ++ "\n" );
			if( saveDry, { metaFile.putString( "DrySignal=" ++ dryPath ++ "\n" )});
			metaFile.putString( "Date=" ++ date ++ "\n" );
			metaFile.putString( "NumSweeps=" ++ numSweeps ++ "\n" );
			metaFile.putString( "SweepDur=" ++ sweepDur ++ "\n" );
			metaFile.putString( "ReverbTime=" ++ reverbTime ++ "\n" );
			if( mic.notNil, { metaFile.putString( "Mic=" ++ mic ++ "\n" )});
			if( site.notNil, { metaFile.putString( "Site=" ++ site ++ "\n" )});
			if( comments.notNil, { metaFile.putString( "Comments=" ++ comments ++ "\n" )});
			metaFile.putString( "InChannels=" ++ inChannels ++ "\n" );
			metaFile.putString( "OutChannel=" ++ outChannel ++ "\n" );
			metaFile.putString( "Amp=" ++ amp ++ "\n" );
			metaFile.close;
		});

		micSynth.onEnd = {
			micSynth = nil;
			micBuf.close;
			micBuf.free;
			intBus.free;
			"Done.".postln;
			running = false;
			this.tryChanged( \running );
		};
		bndl.send( server );
		this.tryChanged( \running );
	}
	
	abort {
		micSynth.free;
	}
}