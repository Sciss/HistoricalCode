TapeUnitControl : AbstractPlayControl {
	var synthDef, proxy, index;
	var <canReleaseSynth = true, <canFreeSynth = true;
	var nextStartFrame;

	build { arg argProxy, argIndex = 0;
		var ok, rate, numChannels;

		proxy	= argProxy;
		index	= argIndex;
if( TapeUnit2.debugProxy, { "BUILD".postln });

		NodeProxy.buildProxyControl = this;
		synthDef = source.prBuildForProxy( proxy, channelOffset, index );
		NodeProxy.buildProxyControl = nil;
		
		rate = synthDef.rate ?? { if( proxy.rate !== \control, { \audio }, { \control })};
		numChannels = synthDef.numChannels ? proxy.numChannels ? 1;
		ok = proxy.initBus( rate, source.numChannels );
		
		if( ok and: { synthDef.notNil }, {
			paused = proxy.paused;
			canReleaseSynth = synthDef.canReleaseSynth;
			canFreeSynth = synthDef.canFreeSynth;
	
		}, {
			synthDef = nil; 
		});
//		^ok;
	}

//	prepareForPlay { arg group, private, bus;
//		var bundle;
//		bundle = MixedBundle.new;
//		group = group.asGroup;
//		this.prepareToBundle( group, bundle );
//		bundle.send( group.server );
//	}

//	loadToBundle { arg bundle, server;
//"LOAD".postln;
//		source.loadToBundle( bundle, server );
//	}

	loadToBundle { arg bundle, server;
		var bytes, size;
if( TapeUnit2.debugProxy, { "LOAD".postln });
		
		bytes = synthDef.asBytes;
		size = bytes.size;
		size = size - (size bitAnd: 3) + 84; // 4 + 4 + 16 + 16 //appx path length size + overhead
		if( server.options.protocol === \tcp or: { size < 16383 }, { 
			// full size: 65535, but does not work.
			bundle.addPrepare([ 5, bytes ]); // "/d_recv"
			
// synthdefs now are not written, as there is no way to regain
// the same bus indices on a rebooted server currently.

/*			if(writeDefs) { 
				this.writeSynthDefFile(this.synthDefPath, bytes) 
			}; // in case of server reboot
*/			
		}, {
			// bridge exceeding bundle size by writing to disk
//			if(server.isLocal.not) {
				Error("SynthDef too large (" ++ size 
				++ " bytes) to be sent to remote server via udp").throw;
//			};
//			path = this.synthDefPath;
//			this.writeSynthDefFile(path, bytes);
//			bundle.addPrepare([6, path]); // "/d_load"
		});
		source.loadToBundle( bundle, server );
	}

	playToBundle { arg bundle, args, argProxy;
		var result;
if( TapeUnit2.debugProxy, { "PLAY".postln });
		result = source.playToBundle( bundle, args, argProxy, nextStartFrame );
		nextStartFrame = nil;
		^result;
	}
	
	stopToBundle { arg bundle, dt;
if( TapeUnit2.debugProxy, { "STOP".postln });
		^source.stopToBundle( bundle, dt );
	}
	
	freeToBundle { arg bundle, dt;
if( TapeUnit2.debugProxy, { "FREE".postln });
		^source.freeToBundle( bundle, dt );
	}
	
	start {
if( TapeUnit2.debugProxy, { "START PLAIN".postln });
	}

	stop {
if( TapeUnit2.debugProxy, { "STOP PLAIN".postln });
	}
	
	startFrame_ { arg frame;
		nextStartFrame = frame;
		this.prReSend;
	}
	
	prReSend {
		var bndl, dt;
		if( proxy.notNil and: { proxy.server.serverRunning }, {
			dt = proxy.fadeTime;
			bndl = MixedBundle.new;
			if( proxy.isPlaying, { this.stopToBundle( bndl, dt )});
			this.freeToBundle( bndl, dt );
			proxy.sendObjectToBundle( bndl, this, nil, index );
			bndl.schedSend( proxy.server, proxy.clock, proxy.quant );
		});
	
// don't work, we're loosing our control instance
//		if( proxy.notNil, { proxy[ index ] = proxy[ index ]});

// don't work, we're loosing object[0]
//		if( proxy.notNil and: { proxy.server.serverRunning }, {
//			bndl = MixedBundle.new;
//			proxy.removeToBundle( bndl, index );
//			proxy.sendObjectToBundle( bndl, this, nil, index );
//			bndl.schedSend( proxy.server, proxy.clock, proxy.quant );
//		});
	}
}