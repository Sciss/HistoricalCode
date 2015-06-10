(
~rec = SimpleRecorder.new;
~rec.channelOffset = Bosque.default.masterBus.index;
~rec.numChannels   = Bosque.default.masterBus.numChannels;
~rec.headerFormat =  "W64";
~rec.sampleFormat =  "int24";
~rec.makeWindow;
)

(
~recGain = -3;
~recLimiter = SynthDef( \recLim, { arg bus, amp = 1.0, ceil = 0.9772;
	ReplaceOut.ar( bus, Limiter.ar( In.ar( bus, ~rec.numChannels ) * amp, ceil ));
}).play( Bosque.default.masterGroup, [ \bus, ~rec.channelOffset, \amp, ~recGain.dbamp ]);
)

~rec.setTarget( ~recLimiter, \addAfter );
~rec.prepareAndRecord( Ohrwald.workDir +/+ "bounce" +/+ "rutz.w64" );
Bosque.default.session.transport.play( 0 );


~rec.stop;

~rec.revealInFinder;
