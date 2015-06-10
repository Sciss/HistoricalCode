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
NuagesUPiezo2 : NuagesUBasicMic {
	classvar <>channel		= 6;
	classvar <>numChannels	= 1;

	*new { arg server;
		^super.new( server ).prInitPiezo;
	}
	
	prInitPiezo {
		this.addMic( Bus( \audio, server.options.numOutputBusChannels + channel, numChannels ));
//		this.addMic( Bus( \audio, server.options.numOutputBusChannels + channel, 2 ));
		this.setMicIndex( 0 );
	}

	protPreferredNumOutChannels { arg idx; ^2 }
}