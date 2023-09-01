/**
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 29-Jul-08
 */
EGM_unwrap {	
	var last, add = 0;
		
	*new {
		^super.new;
	}
	
	reset {
		last = nil;
	}
	
	value { arg inval;
		var wrapped;
		
		wrapped = inval + add;
		if( last.isNil, {
			last = inval;
		});
		if( (wrapped - last) > pi, {
			add = add - (((wrapped - last) + pi).div( 2pi ) * 2pi);
			wrapped = inval + add;
		}, { if( (last - wrapped) > pi, {
			add = add + (((last - wrapped) + pi).div( 2pi ) * 2pi);
			wrapped = inval + add;
		})});
		last = wrapped;
		^wrapped;
	}
}

EGM_thresh {
	var <>hiThresh, <>loThresh;
	var crossed;
	
	*new { arg hiThresh = 0, loThresh = 0;
		^super.new.prInit( hiThresh, loThresh ).reset;
	}
	
	reset {
		crossed = false;
	}
	
	prInit { arg argHiThresh, argLoThresh;
		hiThresh	= argHiThresh;
		loThresh	= argLoThresh;
	}
	
	// Note: outputs a Boolean, append
	// .binaryValue if you want 0 and 1 instead
	value { arg inval;
		if( crossed, {
			if( inval <= loThresh, {
				crossed = false;
			});
		}, {
			if( inval > hiThresh, {
				crossed = true;
			});
		});
		^crossed;
	}
}

// like EGM_thresh, but outputs only
// one impulse of "true"
EGM_thresh1 : EGM_thresh {
	value { arg inval;
		if( crossed, {
			if( inval <= loThresh, {
				crossed = false;
			});
		}, {
			if( inval > hiThresh, {
				crossed = true;
				^true;
			});
		});
		^false;
	}
}

// like EGM_thresh1, but doesn't retrigger
// for a given number of frames
EGM_thresh1T : EGM_thresh1 {
	var <>numFrames;
	var frameCount, trigFrame;
	
	// numFrames: minimum number of frames to occur between
	// two triggers
	*new { arg hiThresh = 0, loThresh = 0, numFrames = 1;
		// dirty: reset gets called twice
		^super.new( hiThresh, loThresh ).prInit1T( numFrames ).reset;
	}
	
	reset {
		frameCount = numFrames;
		trigFrame  = 0;
		^super.reset;
	}
	
	prInit1T { arg argNumFrames;
		numFrames = argNumFrames;
	}
	
	// Note: outputs a Boolean, append
	// .binaryValue if you want 0 and 1 instead
	value { arg inval;
		frameCount = frameCount + 1;
		if( crossed, {
			if( inval <= loThresh, {
				crossed = false;
			});
		}, {
			if( inval > hiThresh, {
				if( (frameCount - trigFrame) >= numFrames, {
					crossed = true;
					trigFrame = frameCount;
					^true;
				});
			});
		});
		^false;
	}
}

// like EGM_thresh1T but inverse
EGM_thresh1IT : EGM_thresh1T {
	value { arg inval;
		frameCount = frameCount + 1;
		if( crossed, {
			if( inval > hiThresh, {
				crossed = false;
			});
		}, {
			if( inval <= loThresh, {
				if( (frameCount - trigFrame) >= numFrames, {
					crossed = true;
					trigFrame = frameCount;
					^true;
				});
			});
		});
		^false;
	}
}