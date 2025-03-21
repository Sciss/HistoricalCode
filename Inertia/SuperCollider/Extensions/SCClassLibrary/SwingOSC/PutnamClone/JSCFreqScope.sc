// converted to SwingOSC compatibility ; last mod : 06-jan-07 sciss

// SCFreqScope and FreqScope
// by Lance Putnam
// lance@uwalumni.com

// WARNING: server is defined for JSCView
// and refers to the SwingOSC instance
// ; the original server class variable was
// renamed to audioServer !!
JSCFreqScope : JSCScope {

	classvar <audioServer;
	var <scopebuf, <fftbuf;
	var <active, <node, <inBus, <dbRange, dbFactor, rate, <freqMode;
	var <bufSize;	// size of FFT
	
	*viewClass { ^JSCScope }
	
// JJJ
//	*initClass { server = Server.internal }
	*initClass {
		Class.initClassTree( Server );
		audioServer = Server.default;
	}
	
	*new { arg parent, bounds;
		^super.new(parent, bounds).initSCFreqScope
	}
	
	initSCFreqScope {
		active=false;
		inBus=0;
		dbRange = 96;
		dbFactor = 2/dbRange;
		rate = 4;
		freqMode = 0;
		bufSize = 2048;
		
// JJJ start allocates node!
//		node = audioServer.nextNodeID;
	}
	
	sendSynthDefs {
		// dbFactor -> 2/dbRange
		
		// linear
		SynthDef("freqScope0", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
			var signal, chain, result, phasor, numSamples, mul, add;
			mul = 0.00285;
			numSamples = (BufSamples.kr(fftbufnum) - 2) * 0.5; // 1023 (bufsize=2048)
			signal = In.ar(in);
			chain = FFT(fftbufnum, signal);
			chain = PV_MagSmear(chain, 1);
			// -1023 to 1023, 0 to 2046, 2 to 2048 (skip first 2 elements DC and Nyquist)
			phasor = LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, numSamples, numSamples + 2);
			phasor = phasor.round(2); // the evens are magnitude
			JScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
		}).send(audioServer);
		
		// logarithmic
		SynthDef("freqScope1", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
			var signal, chain, result, phasor, halfSamples, mul, add;
			mul = 0.00285;
			halfSamples = BufSamples.kr(fftbufnum) * 0.5;
			signal = In.ar(in);
			chain = FFT(fftbufnum, signal);
			chain = PV_MagSmear(chain, 1);
			phasor = halfSamples.pow(LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5)) * 2; // 2 to bufsize
			phasor = phasor.round(2); // the evens are magnitude
			JScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
		}).send(audioServer);
		
//		SynthDef("freqScope2", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
//			var signal, chain, result, phasor, numSamples, mul, add;
//			mul = 0.00285;
//			numSamples = (BufSamples.kr(fftbufnum)) - 2;
//			signal = In.ar(in);
//			chain = FFT(fftbufnum, signal);
//			chain = PV_MagSmear(chain, 1);
//			phasor = ((LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5).squared * numSamples)+1).round(2);
//			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
//		}).send(audioServer);
//		
//		SynthDef("freqScope3", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
//			var signal, chain, result, phasor, numSamples, mul, add;
//			mul = 0.00285;
//			numSamples = (BufSamples.kr(fftbufnum)) - 2;
//			signal = In.ar(in);
//			chain = FFT(fftbufnum, signal);
//			chain = PV_MagSmear(chain, 1);
//			phasor = ((LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5).cubed * numSamples)+1).round(2);
//			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
//		}).send(audioServer);

		"SCFreqScope: SynthDefs sent".postln;
	}
	
	allocBuffers {
		
		scopebuf = Buffer.alloc(audioServer, bufSize/4, 1, 
			{ arg sbuf;
				this.bufnum = sbuf.bufnum;
				fftbuf = Buffer.alloc(audioServer, bufSize, 1,
				{ arg fbuf;
					("SCFreqScope: Buffers allocated (" 
						++ sbuf.bufnum.asString ++ ", "
						++ fbuf.bufnum.asString ++ ")").postln;
				});
			});
	}
	
	freeBuffers {
		if( scopebuf.notNil && fftbuf.notNil, {
			("SCFreqScope: Buffers freed (" 
				++ scopebuf.bufnum.asString ++ ", "
				++ fftbuf.bufnum.asString ++ ")").postln;
			scopebuf.free; scopebuf = nil;
			fftbuf.free; fftbuf = nil;
		});
	}
	
	start {
		// sending bundle messes up phase of LFSaw in SynthDef (????)
//		audioServer.sendBundle(audioServer.latency, 
//			["/s_new", "freqScope", node, 1, 0, 
//				\in, inBus, \mode, mode, 
//				\fftbufnum, fftbuf.bufnum, \scopebufnum, scopebuf.bufnum]);

		node = audioServer.nextNodeID; // get new node just to be safe
		audioServer.sendMsg("/s_new", "freqScope" ++ freqMode.asString, node, 1, 0, 
				\in, inBus, \dbFactor, dbFactor, \rate, 4,
				\fftbufnum, fftbuf.bufnum, \scopebufnum, scopebuf.bufnum);
	}
	
	kill {
		this.eventSeq(0.5, {this.active_(false)}, {this.freeBuffers});
	}
	
	// used for sending in order commands to audioServer
	eventSeq { arg delta ... funcs;
		Routine.run({
			(funcs.size-1).do({ arg i;
				funcs[i].value;
				delta.wait;
			});
			funcs.last.value;
			
		}); // JJJ  , 64, AppClock
	}
	
	active_ { arg bool;
		if(audioServer.serverRunning, { // don't do anything unless server is running
		
		if(bool, {
			if(active.not, {
				CmdPeriod.add(this);
				if((scopebuf.isNil) || (fftbuf.isNil), { // first activation
					this.eventSeq(0.5, {this.sendSynthDefs}, {this.allocBuffers}, {this.start});
				}, {
					this.start;
				});
			});
		}, {
			if(active, {
				audioServer.sendBundle(audioServer.latency, ["/n_free", node]);
				CmdPeriod.remove(this);
			});
		});
		active=bool;
		
		});
		^this
	}
	
	inBus_ { arg num;
		inBus = num;
		if(active, {
			audioServer.sendBundle(audioServer.latency, ["/n_set", node, \in, inBus]);
		});
		^this
	}
	
	dbRange_ { arg db;
		dbRange = db;
		dbFactor = 2/db;
		if(active, {
			audioServer.sendBundle(audioServer.latency, ["/n_set", node, \dbFactor, dbFactor]);
		});		
	}
	
	freqMode_ { arg mode;
		freqMode = mode.asInteger.clip(0,1);
		if(active, {
			audioServer.sendMsg("/n_free", node);
// JJJ start allocates node!
//			node = audioServer.nextNodeID;
			this.start;
		});		
	}

	cmdPeriod {
		this.changed(\cmdPeriod);
		if(active == true, {
			CmdPeriod.remove(this);
			active = false;
// JJJ start allocates node!
//			node = audioServer.nextNodeID;
// JJJ
//			this.active_(true);
			{�this.active_(true); }.defer( 0.5 );
		});
	}
	
}
