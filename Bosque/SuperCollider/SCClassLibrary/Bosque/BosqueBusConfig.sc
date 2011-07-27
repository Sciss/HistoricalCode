/*
 *	BosqueBusConfig
 *	(Bosque)
 *
 *	Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	@author	Hanns Holger Rutz
 *	@version	0.15, 26-Oct-08
 */
BosqueBusConfig {
	var <busCfgID;
	var <bosque;
	var <numInputs, <numOutputs;
	var connections;
	var <bus;
	var <synthPan, <synthMix;
	var <name;
	
	var <groupPan, <groupMix;
	
	var debugSynthDefs = false;

	*new { arg busCfgID, numInputs, numOutputs;
		var bosque, doc, busConfigs;
		
		bosque		= Bosque.default;
		doc			= bosque.session;
		busConfigs	= doc.busConfigs;
		busConfigs.do({ arg b; if( b.busCfgID == busCfgID, { ^b })});
		
		^super.new.prInit( busCfgID, numInputs, numOutputs );
	}
	
	asSwingArg { "BosqueBusConfig:asSwingArg : INVALID".warn; ^nil.asSwingArg }
	
	prInit { arg argID, argNumInputs, argNumOutputs;
		busCfgID		= argID;
		bosque		= Bosque.default;
		numInputs		= argNumInputs;
		numOutputs	= argNumOutputs;
		
		bosque.doWhenScSynthBooted({ this.prAudioInit });
	}

	connections_ { arg conn;
		// stupidly connections_ gets called when loading document
		// for each track the busconfig is used in... so filter it!
		if( connections == conn, { ^this });

		if( (conn.size != numInputs) or: { conn.detect({ arg set; set.size != numOutputs }).notNil }, {
			MethodError( "Connections matrix doesn't match numInputs / numOutputs", thisMethod ).throw;
		});
		
		connections	= conn.copy;
//		[ "connections_", name, connections ].postln;
		if( bus.notNil, {
			this.prCreateConnSynths;
		});
		this.tryChanged( \connections );
	}
	
	name_ { arg symbol;
		name = symbol.asSymbol;
		this.tryChanged( \name );
	}
	
	// use with care!!!
	busCfgID_ { arg id;
		busCfgID = id;		
	}

	editRename { arg source, newName, ce;
		var oldName = name;
		ce.addPerform( BosqueFunctionEdit({ this.name = newName }, { this.name = oldName }, "Change Bus Name", true ));
	}

	editConnections { arg source, newConn, ce;
		var oldConn = connections;
		newConn = newConn.copy;
		ce.addPerform( BosqueFunctionEdit({ this.connections = newConn }, { this.connections = oldConn }, "Change Bus Connections", true ));
	}

	connections { ^connections.copy }

	storeArgs { ^[ busCfgID, numInputs, numOutputs ]}

	storeModifiersOn { arg stream;
		if( name.notNil, {
			stream << ".name_(";
			name.storeOn( stream );
			stream << ")";
		});
		if( connections.notNil, {
			stream << ".connections_(";
			connections.storeOn( stream );
			stream << ")";
		});
	}

	prAudioInit {
//		("prAudioInit : " ++ name).postln;
		bus		= Bus.audio( bosque.scsynth, max( numInputs, numOutputs ));
		groupPan	= Group( bosque.panGroup );
		groupMix	= Group( bosque.mixGroup );
		
		if( connections.notNil, { ^this.prCreateConnSynths });
	}
	
	prCreateConnSynths {
		var bundle, defNamePan, defNameMix, defPan, defMix, numMixChans;
		
//		groupMix.freeAll;
//		groupPan.freeAll;
		
//		("prCreateConnSynths : " ++ name).postln;
//		if( name.asString == "BusStSides", {
//			this.dumpBackTrace;
//		});

		defNamePan = \bosqueBus ++ busCfgID;
		defPan = SynthDef( defNamePan, { arg bus;
			var inp, outp;
			
			inp 	= In.ar( bus, numInputs ).asArray; // .asArray fuer mono !!
			outp	= 0.0 ! numOutputs;
			connections.do({ arg set, i;
				set.do({ arg gain, j;
					// very efficient as BinaryOpUGen uses optimizations
					outp[ j ] = outp[ j ] + (inp[ i ] * gain);
				});
			});
			ReplaceOut.ar( bus, outp );	// 0.0 get replaced with Silent.ar !
		});
		if( debugSynthDefs, { Kurs.viewSynthDef( defPan )});
		numMixChans = min( numOutputs, bosque.masterBus.numChannels );
		defNameMix = \bosqueMix ++ numMixChans;
		defMix = SynthDef( defNameMix, { arg inBus, outBus;
			Out.ar( outBus, In.ar( inBus, numMixChans ));
		});

		bundle = OSCBundle.new;
		bundle.addPrepare([ "/d_recv", defPan.asBytes ]);
		bundle.addPrepare([ "/d_recv", defMix.asBytes ]);
		synthPan = Synth.basicNew( defNamePan, bosque.scsynth );
		synthMix = Synth.basicNew( defNameMix, bosque.scsynth );
		bundle.add( groupMix.freeAllMsg );
		bundle.add( groupPan.freeAllMsg );
		bundle.add( synthPan.newMsg( groupPan, [ \bus, bus.index ]));
		bundle.add( synthMix.newMsg( groupMix, [ \inBus, bus.index, \outBus,  bosque.masterBus.index ]));
		bundle.send( bosque.scsynth ); // , bufferLatency
	}

	dispose {
//		if( java.notNil, { java.destroy });
		groupMix.free; groupMix = nil;
		groupPan.free; groupPan = nil;
//		if( synthMix.notNil, { synthMix.free; synthMix = nil });
//		if( synthPan.notNil, { synthPan.free; synthPan = nil });
		bus.free; bus = nil;
	}
}