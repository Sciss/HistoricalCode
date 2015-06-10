/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: TypeSafe, Ping, SynthDefCache
 *
 *	Changelog
 *	- 08-Mar-06 	bus argument is MappedObject, not Bus directly
 *	- 13-Jun-06	stripped down for ping usage
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.12, 26-Oct-08
 *
 *	@todo	verschiedene synthdef namen quatsch, weil speech cues nur auf
 *			erstem kanal gespielt werden (sollen)
 */
NuagesSoloManager : Object
{
	var <>verbose		= false;

	var <server;
	var <bus;
	var <currentSolo	= nil;	// NuagesProc
	var <soloVolume	= 1.0;

	var <pompe;
	var updProc;
	
	*new { arg pompe;
		^super.new.prInitSoloManager( pompe );
	}
	
	prInitSoloManager { arg argPompe;
		pompe 	= argPompe;
		bus   	= Bus( \audio, Wolkenpumpe.soloIndex, Wolkenpumpe.soloChannels );
		updProc	= UpdateListener({ arg upd, proc,
			currentSolo = nil;
		}, \disposed );
	}
	
	current {
		^currentSolo;
	}
	
	set { arg source;
		var bndl;
		bndl = OSCBundle.new;
		this.setToBundle( bndl, source );
		bndl.send( server );
	}
	
	setToBundle { arg bndl, source;
		var oldSolo = currentSolo;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, source ], [ OSCBundle, NuagesProc ], [ false, true ]);

		if( oldSolo.notNil, {
//			server.sync;
			updProc.removeFrom( oldSolo );
			oldSolo.removePlayBusToBundle( bndl, bus );
			if( verbose, { ("SoloManager: unsolo'ed " ++ oldSolo).postln; });
		});
		currentSolo = source;
		// call .changed here because the listener might want
		// to query the new solo source
		if( oldSolo.notNil, { oldSolo.tryChanged( \soloLost );});
		if( currentSolo.isNil.not, {
//			server.sync;
			currentSolo.addPlayBusToBundle( bndl, bus, soloVolume );
			updProc.addTo( currentSolo );
			currentSolo.tryChanged( \soloGained );
		});
		if( verbose, { ("SoloManager: solo'ed " ++ currentSolo).postln; });
	}
	
	// has to be called from inside routine
	clear {
		if( currentSolo.isNil.not, {
//			server.sync;
			updProc.removeFrom( currentSolo );
			currentSolo.removePlayBus( bus );
			currentSolo.tryChanged( \soloLost );
			if( verbose, { ("SoloManager: unsolo'ed " ++ currentSolo).postln; });
			currentSolo = nil;
		});
	}

	volume_ { arg vol;
		soloVolume = vol;
		if( currentSolo.isNil.not, {
			currentSolo.setPlayBusVolume( bus, soloVolume );
		});
	}

	dispose {
		// nada
	}
}