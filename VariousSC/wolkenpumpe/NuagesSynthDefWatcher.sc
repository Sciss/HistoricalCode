/**
 *	(C)opyright 2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: TypeSafe, ScissPlus
 *
 *	Changelog:
 *
 *	@version	0.12, 25-Oct-08
 *	@author	Hanns Holger Rutz
 */
NuagesSynthDefWatcher {
	classvar all;

	var <server, upd;
	var defNames;

	// -------------- constructor --------------
	
	*new { arg server;
		^super.new.prInit( server );
	}
	
	*newFrom { arg server;
		var result;

		TypeSafe.checkArgClass( thisMethod, server, Server, false );
		
		if( all.isNil, {
			all = IdentityDictionary.new;
		});
		result = all[ server.name ];
		if( result.isNil, {
			result = this.new( server );
			all.put( server.name, result );
		});
		^result;
	}

	prInit { arg argServer;
		server	= argServer;
		this.clear;
		upd		= UpdateListener.newFor( server, { arg upd, s;
			if( s.serverRunning.not, {
				this.clear;
			});
		}, \serverRunning );
	}
	
	// -------------- public instance methods --------------
	
	add { arg def;
		var defName;
		
		TypeSafe.checkArgClass( thisMethod, def, SynthDef, false );
			                                 
		defName = def.name.asSymbol;
		if( this.isOnline( defName ), { ^this });
	
		if( server.serverRunning, {
			defNames.add( defName );
		}, {
			TypeSafe.methodWarn( thisMethod, "Server not running" );
		});
	}
	
	send { arg def, synth, completionMsg;
		var bndl;

		TypeSafe.checkArgClasses( thisMethod, [ def, synth ], [ SynthDef, Synth ], [ false, false ]);
		
		bndl = OSCBundle.new;
		this.sendToBundle( bndl, def, completionMsg );
		if( bndl.preparationMessages.size > 0, {
			server.listSendBundle( nil, bndl.preparationMessages );
		});
	}
	
	sendToBundle { arg bndl, def, synth, completionMsg;
		var defName, syncID, defs;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, def, synth ],
		                                      [ OSCBundle, SynthDef, Synth ],
		                                      [ false, false, false ]);
		
		defName = def.name.asSymbol;
//[ "sendToBundle", defName, this.isOnline( defName )].postln;
		if( this.isOnline( defName ), { ^this });
		
		bndl.addPrepare( def.recvMsg( completionMsg ));

		synth.register;
		UpdateListener.newFor( synth, { arg upd, node;
			upd.remove;
			defNames.add( defName );
		}, \n_go );
	}
	
	isOnline { arg defName;

		TypeSafe.checkArgClass( thisMethod, defName, Symbol, false );
	
		^defNames.includes( defName );
	}

	clear {
		defNames = IdentitySet.new;
	}
	
	dispose {
		upd.remove; upd = nil;
		this.clear;
	}
		
	// -------------- private instance methods --------------
}
