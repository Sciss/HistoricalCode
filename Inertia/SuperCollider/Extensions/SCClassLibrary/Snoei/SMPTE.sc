
// W. Snoei 2006
// please note that dropped frames are not supported

SMPTE[slot] : RawArray {
	
	classvar <>defaultFps = 25; // standard for europe
	classvar <>defaultSubFps = 100; 
	
	var <hours, <minutes, <seconds, <frames, <subFrames;
	var <>currentIndex = 0;
	var <fps, <>subFps; 
	
	*new { |inSeconds = 0, fps|
		^super.newClear( 5 )
			.currentIndex_( 0 )
			.subFps_( defaultSubFps )
			.initSeconds( inSeconds, fps ? defaultFps );
		}
				
	*hours { |inHours = 0, fps| ^SMPTE( inHours * 60 * 60, fps ); }
	
	*minutes { |inMinutes = 0, fps| ^SMPTE( inMinutes * 60, fps ); }
	
	*frames { |inFrames = 0, fps| ^SMPTE( inFrames / (fps ? defaultFps), fps ); }
	
	*subFrames { |inSubFrames = 0, fps| 
		^SMPTE( inSubFrames / (fps ? defaultFps) / defaultSubFps, fps ); }
	
	*array { |inArray, fps| ^SMPTE.with( *inArray ).setFps( fps ? defaultFps ); }
	
	*type { |input = 0, type, fps|
		if(type.isNil) { type = \seconds };
		case { type == \seconds }
			{ ^SMPTE( input, fps ); }
			{ type == \minutes }
			{ ^SMPTE.minutes( input, fps ); }
			{ type == \hours }
			{ ^SMPTE.hours( input, fps ); }
			{ type == \frames }
			{ ^SMPTE.frames( input, fps ); }
			{ type == \subFrames }
			{ ^SMPTE.subFrames( input, fps ); };
		}
	
	*fromMIDIFileArray { |inArray|
		// fps is coded in hours first two bits
		var frameRate;
		inArray = inArray ? [32, 0, 0, 0, 0 ];
		frameRate = [24,25,29.97,30][ inArray[0] >> 5 ];   // framerate
		inArray = [ inArray[0] % 32 ] ++ inArray[1..];
		^SMPTE.array( inArray, frameRate );
		}
		
	*fromDate { |date|
		^SMPTE( date.rawSeconds );
		}
		
	*fromDateHours { |date|
		^SMPTE[ date.hour, date.minute, 0,0,0].seconds_( date.rawSeconds % 60 );
		}
		
	*localtime { ^this.fromDateHours( Date.localtime ) }
			
	initSeconds { |inSeconds = 0, inFps|
		fps = (inFps ? fps) ? defaultFps;
		frames = (inSeconds * fps) % fps;
		subFrames = (frames.frac * subFps).round(1.0e-12);
		frames = frames.floor.max(0);
		seconds = (inSeconds % 60).floor.max(0);
		minutes = ((inSeconds % (60*60)) / 60).floor.max(0);
		hours = (inSeconds / (60*60)).floor; // hours can be negative
		}
	
	add { |whatToAdd = 0| // don't use; this is only for SMPTE[n,n,n,n,n] syntax support
		var out;
		this.put( currentIndex, whatToAdd );
		currentIndex = (currentIndex + 1).min(4);
		}
		
	frameRate { ^fps }
	
	fps_ { |newFps| ^this.initSeconds( this.asSeconds, newFps ? defaultFps ); } 
		// recalculate with new fps
	
	setFps { |newFps| fps = newFps ? defaultFps; } // set, without internal data change
		
	asSeconds { ^(hours * (60*60)) + (minutes * 60) + seconds + 
					(frames / fps) + (subFrames / (fps*subFps) ); }
					
	asHours { ^this.asSeconds / (60*60); }
	asMinutes { ^this.asSeconds / (60); }
	asFrames { ^this.asSeconds * fps; }
	asSubFrames { ^this.asSeconds * fps * subFps; }
	asMinSec { ^[minutes + (hours * 60 ), 
		seconds + (frames / fps) + (subFrames / (fps*subFps) ) ]; } 
	
	asArray { ^[hours, minutes, seconds, frames, subFrames] }
	asMIDIFileArray { ^this.asArray + [ (24: 0, 25: 32, 29.97: 64, 30: 96)[fps] ? 0, 0,0,0,0]; }
	
	toString { 
		^"%:%:%:%".format( *([hours, minutes, seconds]
			.collect({ |item| item.asInteger.asSizedString( 2 ) })
				++ [ frames.asInteger.asSizedString( (fps-1).asInteger.asString.size )] ) );
		}
		
	*fromString { |string, fps|
		string = string ? "00:00:00:00";
		^this.array( 
			( string.split( $: )
				.collect( _.interpret ) ++ [0])
			.reverse.extend( 5, 0 ).reverse, 
			fps );
		}
		
	*minSec { |minutes = 0, seconds = 0|
		^SMPTE[ 0, minutes, seconds, 0, 0 ].reCalculate;
		}
		
	
	hours_ { |newHours|
		newHours = newHours ? hours;
		hours = newHours;
		this.reCalculate;
		}
		
	minutes_ { |newMinutes|
		newMinutes = newMinutes ? minutes;
		minutes = newMinutes;
		this.reCalculate;
		}
		
	seconds_ { |newSeconds|
		newSeconds = newSeconds ? seconds;
		seconds = newSeconds;
		this.reCalculate;
		}
		
	frames_ { |newFrames|
		newFrames = newFrames ? frames;
		frames = newFrames;
		this.reCalculate;
		}
		
	subFrames_ {  |newSubFrames|
		newSubFrames = newSubFrames ? subFrames;
		subFrames = newSubFrames;
		this.reCalculate;
		}
		
	
	reCalculate { ^this.initSeconds( this.asSeconds, fps ); }
	
	asSMPTE { ^this }
	
	asCollection { ^[ this ] }
	
	size { ^5 } // fixed size
	
	asType { |type|	
		if( type.isNil ) { type = \seconds };
		case { type == \seconds }
			{ ^this.asSeconds }
			{ type == \minutes }
			{ ^this.asMinutes }
			{ type == \hours }
			{ ^this.asHours }
			{ type == \frames }
			{ ^this.asFrames }
			{ type == \subFrames }
			{ ^this.asSubFrames };
		}
	
	+ { |aSMPTE, adverb|
		aSMPTE = aSMPTE ? SMPTE(0);
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^SMPTE( this.asSeconds + aSMPTE.asSeconds, fps );
		}
		
	- { |aSMPTE, adverb|
		aSMPTE = aSMPTE ? SMPTE(0);
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^SMPTE( this.asSeconds - aSMPTE.asSeconds, fps );
		}
	
	* { |aSMPTE, adverb|
		aSMPTE = aSMPTE ? SMPTE(1);
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^SMPTE( this.asSeconds * aSMPTE.asSeconds, fps );
		}
	
	/ { |aSMPTE, adverb|
		aSMPTE = aSMPTE ? SMPTE(1);
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^SMPTE( this.asSeconds / aSMPTE.asSeconds, fps );
		}
		
	> { |aSMPTE, adverb|
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^this.asSeconds > aSMPTE.asSeconds;
		}
		
	< { |aSMPTE, adverb|
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^this.asSeconds < aSMPTE.asSeconds;
		}
	
	>= { |aSMPTE, adverb|
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^this.asSeconds >= aSMPTE.asSeconds;
		}
		
	<= { |aSMPTE, adverb|
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^this.asSeconds <= aSMPTE.asSeconds;
		}
	
	== { |aSMPTE, adverb|
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^this.asSeconds == aSMPTE.asSeconds;
		}
	
	!= { |aSMPTE, adverb|
		aSMPTE = aSMPTE.asSMPTE( nil, adverb );
		^this.asSeconds != aSMPTE.asSeconds;
		}
		
	// asDate { ^Date.fromSMPTE( this ); } // needs work; there's no conversion provided in Date
		
	}
	