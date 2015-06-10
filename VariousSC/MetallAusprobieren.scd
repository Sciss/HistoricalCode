(
SynthDef( \disk, { arg buf, out, amp = 0, rate = 1;
	Out.ar( out, VDiskIn.ar( 1, buf, rate, 1 ) * amp );
}).send( s );
)

~path = "/Users/tma/Desktop/share/tests/MetallScheibe2Test.aif";
~path = "/Users/tma/Desktop/share/tests/MetallScheibe3Test.aif";
~path = "/Users/tma/Desktop/share/tests/MetallScheibe4Test.aif";
~group = Group.before( Amplifikation.masterGroup );

(
	var sf;
	sf = SoundFile.openRead( ~path ); sf.close;

	~synths = 16.collect({ arg i;
		var buf, bndl, path, startFrame, synth;
		bndl = OSCBundle.new;
		buf = Buffer( s, 32768, 1 );
		path = ~path;
		startFrame = rrand( 0, sf.numFrames - 44100 );
		bndl.addPrepare( buf.allocMsg( buf.cueSoundFileMsg( path, startFrame )));
		synth = Synth.basicNew( \disk, s );
		bndl.add( synth.newMsg( ~group, [ \buf, buf, \out, Amplifikation.masterBus.index + i ]));
		synth.onEnd = { buf.close; buf.free };
		bndl.send( s );
		synth;
	});
)

~group.set( \amp, 0 );
~group.set( \amp, 1 );
~group.set( \rate, 0.1 );
~group.set( \amp, 4 );

~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 4, 0.25 ))});
~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 0.25, 4 ))});

~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 4, 0.125 ))});
~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 0.125, 4 ))});

~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 2, 0.125 ))});
~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 0.125, 2 ))});

~group.set( \rate, 0.125 );
~group.set( \rate, 0.25 );
~group.set( \rate, 2.0 );

~synths.do({ arg synth, i; synth.set( \rate, i.linexp( 0, 15, 0.2, 0.3 ))});
~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 2, 0.125 ))});

~group.free;

x = { arg amp = 0.01, freq = 5000; Out.ar( Amplifikation.masterBus.index, HPF.ar( WhiteNoise.ar( (amp ! 4) ++ (Silent.ar ! 4) ++ (amp ! 8)), freq ))}.play;
x = { arg amp = 0.01, freq = 5000; Out.ar( Amplifikation.masterBus.index, HPF.ar( WhiteNoise.ar( amp ! 16 ), freq ))}.play;
x.set( \amp, 0.1 );
x.set( \freq, 7000 );
x.set( \freq, 8000 );
x.set( \freq, 12000 );
x.set( \amp, 0.2 );
x.set( \freq, 14000 );
x.set( \amp, 0.3 );
x.set( \freq, 15000 );
x.set( \amp, 0.5 );

x = { arg amp = 0.01, freq = 5000, modFreq = 0.02; Out.ar( Amplifikation.masterBus.index, HPF.ar( WhiteNoise.ar( (amp ! 16) * SinOsc ), freq ))}.play;
x.free;


x = { arg amp = 0.1, freq = 15000; Out.ar( Amplifikation.masterBus.index, HPF.ar( GrayNoise.ar( (amp ! 4) ++ (Silent.ar ! 4) ++ (amp ! 8)), freq ))}.play;
x.set( \amp, 1 );
x.free;

x = { arg amp = 0.1, freq = 15000, dustFreq = 1000; Out.ar( Amplifikation.masterBus.index, HPF.ar( Dust.ar( dustFreq, (amp ! 4) ++ (Silent.ar ! 4) ++ (amp ! 8)), freq ))}.play;
x.set( \amp, 1 );
x.set( \dustFreq, 200 );
x.set( \amp, 2 );
x.set( \amp, 4 );
x.set( \amp, 8 );
x.set( \freq, 12000 );

x.free;

// DIEGO IM FENSTER (MIT DRONE LUDGER LEISE AUF GEGEN†BER)
x = { arg amp = 8, freq = 12000, dustFreq = 200; Out.ar( Amplifikation.masterBus.index, HPF.ar( Dust.ar( dustFreq, (amp ! 2) ++ (Silent.ar ! 8) ++ (amp ! 6)), freq ))}.play;

x.set( \amp, 8 );
x.set( \freq, 1111 );
x.set( \dustFreq, 20 );
x.free

////////


(
SynthDef( \disk, { arg buf, out, amp = 0, rate = 1;
	Out.ar( out, VDiskIn.ar( 1, buf, rate, 1 ) * amp );
}).send( s );
)

~path = "/Users/tma/Desktop/share/tests/MetallScheibe4Test.aif";
~group = Group.before( Amplifikation.masterGroup );

(
	var sf;
	sf = SoundFile.openRead( ~path ); sf.close;

	~synths = 16.collect({ arg i;
		var buf, bndl, path, startFrame, synth;
		bndl = OSCBundle.new;
		buf = Buffer( s, 32768, 1 );
		path = ~path;
		startFrame = rrand( 0, sf.numFrames - 44100 );
		bndl.addPrepare( buf.allocMsg( buf.cueSoundFileMsg( path, startFrame )));
		synth = Synth.basicNew( \disk, s );
		bndl.add( synth.newMsg( ~group, [ \buf, buf, \out, Amplifikation.masterBus.index + i ]));
		synth.onEnd = { buf.close; buf.free };
		bndl.send( s );
		synth;
	});
)
~synths.do({ arg synth, i; synth.set( \rate, i.linexp( 0, 15, 0.2, 0.3 ))});
~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 2, 0.125 ))});

/////////////////////////////////

(
x = { arg amp = 0.5, freq = 12000, whichFreq = 1;
	var which, trig;
	trig = Impulse.kr( whichFreq );
	which = TIRand.kr( 0, 15, trig );
//	Out.ar( Amplifikation.masterBus.index, Select.ar( which, HPF.ar( WhiteNoise.ar( amp ! 16 ), freq )));
	Out.ar( Amplifikation.masterBus.index, HPF.ar( WhiteNoise.ar( amp ! 16 ), freq ) * { |i| 1 - ((i - which).abs.min( 1 ))}.dup(16) );
}.play;
)

x.set( \freq, 1000 );
x.free;
x.set( \whichFreq, 10 );

x.free;

/////////////////////////////////

(
x = { arg amp = 0.5, freq = 8000, whichFreq = 10;
	var which, trig, sig;
	trig = Dust.kr( whichFreq );
	which = TIRand.kr( 0, 15, trig );
//	Out.ar( Amplifikation.masterBus.index, Select.ar( which, HPF.ar( WhiteNoise.ar( amp ! 16 ), freq )));
//	sig = WhiteNoise.ar( amp ! 16 );
	sig = GrayNoise.ar( amp ! 16 ) * GrayNoise.ar( 1 ! 16 ) * GrayNoise.ar( 1 ! 16 );
	Out.ar( Amplifikation.masterBus.index, HPF.ar( sig, freq ) * { |i| 1 - ((i - which).abs.min( 1 ))}.dup(16) );
}.play;
)

x.set( \freq, 5000 );
x.set( \whichFreq, 100 );
x.set( \whichFreq, 10 );
x.set( \whichFreq, 1 );

x.free;
