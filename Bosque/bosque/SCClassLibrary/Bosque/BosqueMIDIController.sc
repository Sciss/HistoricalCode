// Assumes a BCF2000
BosqueMIDIController {
	classvar <>numFaders = 8;
	classvar <>period = 0.05; // update period in seconds
	
	var bosque, doc, <in, <out;
	var follow, write;
	
	*new { arg bosque;
		^super.new.prInit( bosque );
	}
	
	prInit { arg argBosque;
		var routFollow;
	
		bosque	= argBosque ? Bosque.default;
		out		= bosque.midiOut;
		in		= bosque.midiIn;
		doc		= bosque.session;
		if( out.isNil || in.isNil, { ^this });
		
		follow	= false ! numFaders;
		write	= false ! numFaders;
		
		numFaders.do({ arg chan;
			CCResponder({ arg src, midiChan, num, value;
				follow[ chan ] = value != 0;
			}, in, 0, chan + 16 );
		});
		
		UpdateListener.newFor( doc.transport, { arg upd, trnsp, what, param;
			switch( what,
			\play, {
				routFollow = this.prStartRoutFollow;
			},
			\stop, {
				routFollow.stop; routFollow = nil;
			},
			\pause, {
				routFollow.stop; routFollow = nil;
			},
			\resume, {
				routFollow = this.prStartRoutFollow;
			});
		});
		
		UpdateListener.newFor( doc.timelineView.cursor, { arg upd, csr;
			this.prFollow;
		}, \changed );
	}
	
	prStartRoutFollow {
		^{
			inf.do({
				this.prFollow;
				period.wait;
			});
		}.fork;
	}
	
	prFollow {
		var frame, track, trackOff, level, full, lsb, msb;
		trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
//("trackOff = " ++trackOff).postln;
		if( trackOff.isNil, { ^this });
		follow.do({ arg yes, i;
			if( yes, {
				if( frame.isNil, { frame = doc.transport.currentFrame });
				track = doc.tracks.at( i + trackOff );
				if( track.notNil, {
					level = track.level( frame );
					if( level.notNil, {
//("level["++i++"] = " ++level).postln;
						full = (level * 16129).asInteger;
						lsb  = full & 0x7F;
						msb  = full >> 7;
						out.control( 0, i, msb );
						out.control( 0, i + 32, lsb );
					});
				});
			});
		});
	}
}