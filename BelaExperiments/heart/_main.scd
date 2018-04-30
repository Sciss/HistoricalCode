s = Server.default;

s.options.numAnalogInChannels = 2; // can only be 2, 4 or 8
s.options.numAnalogOutChannels = 2;
s.options.numDigitalChannels = 0;
s.options.maxLogins = 16;  	   // set max number of clients

s.options.blockSize = 16;
s.options.numInputBusChannels = 2;
s.options.numOutputBusChannels = 2;

s.waitForBoot{
	"Server Booted".postln;
	(
	SynthDef("heart-sensor", {
	    var in, freq, gain, sig, min, max, noise, norm, median, minGain, maxGain, minBPM, maxBPM, runFreq, runMin, runMax, runMinL, runMaxL, runImp, bpm0, bpm, hasBPM, sigBPM;
	    minGain = -48;
	    maxGain = -9;
	    minBPM  = 40;
	    maxBPM  = 150;
		in      = AnalogIn.ar(0) * 5;
		// in.poll(2, "in-0 [V]");
		min     = 1.3;
		max     = 3.7;
		noise   = GrayNoise.ar;
		norm    = in.clip(min, max).linlin(min, max, 0, 1);
		median  = Median.ar(in: norm, length: 5);
		
		runFreq = Impulse.ar(minBPM / 60.0);
		runImp  = Delay1.ar(runFreq);
		runMax  = RunningMax.ar(median, runImp);
		runMin  = RunningMin.ar(median, runImp);
		runMaxL = Lag.ar(runMax);
		runMinL = Lag.ar(runMin);
		// runMin.poll(runFreq, "MIN");
		// runMax.poll(runFreq, "MAX");
		bpm0    = ZeroCrossing.ar(median.linlin(runMinL, runMaxL.max(runMinL + 0.1), -1, 1)) * 60.0;
		// bpm0    = OnePole.ar(bpm0, 0.999);
		hasBPM	= bpm0 > minBPM;
		// bpm0    = Slew.ar(bpm0, up: Select.ar(hasBPM, [DC.ar(5), DC.ar(1)]), dn: 2);
		bpm0    = Slew.ar(bpm0, up: Line.ar(100, 1, 5), dn: 2);
		bpm     = Gate.ar(bpm0, bpm0 <= maxBPM);
		hasBPM  = bpm > 0;
		bpm.poll(1, "bpm");
		
		freq    = norm.linexp(0, 1, 200, 5000);
		gain    = Lag.ar(norm.linlin(0, 1, minGain, maxGain)).dbamp;
		sigBPM  = BPF.ar(Pulse.ar(bpm.clip(minBPM, maxBPM).linexp(minBPM, maxBPM, 5.0, 20.0)), 700, 0.5) * hasBPM * 0.2;
		sigBPM  = BRF.ar(DelayC.ar(sigBPM, 0.02, LFNoise1.ar(50)* 0.01 + 0.01), 1400, 0.5);
		sig     = Resonz.ar(noise, freq) * gain;
		Out.ar(0, Pan2.ar(sig, -0.25) + Pan2.ar(sigBPM, 0.25));
	}).send(s);
	);

	s.sync;
	Synth.new("heart-sensor", target: s);
};

