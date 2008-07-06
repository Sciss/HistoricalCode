/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.1, 13-Aug-07
 */
BosqueFade {
	var <type, <numFrames, <curve;
	
	*new { arg type = \lin, numFrames = 0, curve = 0;
		^super.newCopyArgs( type, numFrames, curve );
	}
	
	replaceFrames { arg newFrames;
		var args = this.storeArgs;
		args[ 1 ] = newFrames;
		^this.class.new( *args );
	}
	
	asSwingArg {
		^([ '[', '/new', "de.sciss.timebased.Fade", [ \lin ].indexOf( type ) ? 0, numFrames, curve, ']' ]);
	}
	
	storeArgs { ^[ type, numFrames, curve ]}
}