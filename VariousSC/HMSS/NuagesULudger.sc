/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.11, 27-Mar-09
 *	@author	Hanns Holger Rutz
 */
NuagesULudger : NuagesUBasicMic {
	classvar <>channel		= 4;
	classvar <>numChannels	= 1;

	*new { arg server;
		^super.new( server ).prInitLudger;
	}
	
	prInitLudger {
		this.addMic( Bus( \audio, server.options.numOutputBusChannels + channel, numChannels ));
		this.setMicIndex( 0 );
	}

	protPreferredNumOutChannels { arg idx; ^2 }
}