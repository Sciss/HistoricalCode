//x = { arg amp = 2, freq = 14000, dustFreq = 200; Out.ar( Wolkenpumpe.default.masterBus.index, HPF.ar( Dust.ar( dustFreq, (amp ! 2) ++ (Silent.ar ! 8) ++ (amp ! 6)), freq ))}.play;

~knirsch = { arg amp = 1.5, freq = 14000, dustFreq = 200; Out.ar( Wolkenpumpe.default.masterBus.index, HPF.ar( Dust.ar( dustFreq, (amp ! 16)), freq ))}.play;



~knirsch.set( \amp, 2 );
~knirsch.set( \freq, 14000 );

~knirsch.set( \dustFreq, 20 );
~knirsch.free

//////// + DIEGO AT WINDOW

(
~zipper = { arg amp = 0.5, freq = 8000, whichFreq = 10;
	var which, trig, sig;
	trig = Dust.kr( whichFreq );
	which = TIRand.kr( 0, 15, trig );
//	Out.ar( Amplifikation.masterBus.index, Select.ar( which, HPF.ar( WhiteNoise.ar( amp ! 16 ), freq )));
//	sig = WhiteNoise.ar( amp ! 16 );
	sig = GrayNoise.ar( amp ! 16 ) * GrayNoise.ar( 1 ! 16 ) * GrayNoise.ar( 1 ! 16 );
	Out.ar( Wolkenpumpe.default.masterBus.index, HPF.ar( sig, freq ) * { |i| 1 - ((i - which).abs.min( 1 ))}.dup(16) );
}.play;
)

~zipper.free;

//////// + DIEGO AT WINDOW


(
SynthDef( \disk, { arg buf, out, amp = 0, rate = 1;
	Out.ar( out, VDiskIn.ar( 1, buf, rate, 1 ) * amp );
}).send( s );
)

~path = "/Users/tma/Desktop/share/tests/MetallScheibe4Test.aif";
~group = Group.before( Wolkenpumpe.default.masterGroup );

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
		bndl.add( synth.newMsg( ~group, [ \buf, buf, \out, Wolkenpumpe.default.masterBus.index + i ]));
		synth.onEnd = { buf.close; buf.free };
		bndl.send( s );
		synth;
	});
)
//~synths.do({ arg synth, i; synth.set( \rate, i.linexp( 0, 15, 0.2, 0.3 ))});
//~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 2, 0.125 ))});
//
//~synths.do({ arg synth, i; synth.set( \amp, i.linexp( 0, 15, 2, 0.125 ) * 1 )});
~group.set( \amp, 3 );

~group.free;
