/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.10, 30-Aug-08
 *	@author	Hanns Holger Rutz
 */
NuagesUMarkus : NuagesUBasicMic {
	classvar <>channel = 5;

	*new { arg server;
		^super.new( server ).prInitMarkus;
	}
	
	prInitMarkus {
		this.addMic( Bus( \audio, server.options.numOutputBusChannels + channel, 1 ));
//		this.addMic( Bus( \audio, server.options.numOutputBusChannels + channel, 2 ));
		this.setMicIndex( 0 );
	}

	protPreferredNumOutChannels { arg idx; ^2 }
}