/**
 *	NuagesT
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 28-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesT {
	classvar <>verbose = false;
	
	var <nuages, <>randState;
	
	////////// FACTORY
	
	*connect {Êarg sourceVertex, outletIndex, targetVertex, inletIndex;
		^NuagesTConnect( nil, sourceVertex, outletIndex, targetVertex, inletIndex );
	}
	
	*procAttr {Êarg vertex, attrName, newNormVal, fdt, stopLine;
		^NuagesTProcAttr( nil, vertex, attrName, newNormVal, fdt, stopLine );
	}
	
	*procCreate { arg name, notify, metaData;
		^NuagesTProcCreate( nil, name, notify, metaData );
	}
	
	*procDup { arg orig, pred, metaData;
		^NuagesTProcDup( nil, orig, pred, metaData );
	}
	
	*procDying {Êarg vertex;
		^NuagesTProcDying( nil, vertex );
	}
	
	*procFadeRemove {Êarg vertex, fdt;
		^NuagesTProcFadeRemove( nil, vertex, fdt );
	}
	
	*procInsert {Êarg vertex, edge, notify, metaData;
		^NuagesTProcInsert( nil, vertex, edge, notify, metaData );
	}
	
	*map { arg sourceVertex, outletIndex, targetVertex, attrIndex, fdt;
		^NuagesTMap( nil, sourceVertex, outletIndex, targetVertex, attrIndex, fdt );
	}
	
	*procOutput {Êarg vertex, initialGain, metaData;
		^NuagesTProcOutput( nil, vertex, initialGain, metaData );
	}
	
	*procPlay {Êarg vertex, mode, fdt;
		^NuagesTProcPlay( nil, vertex, mode, fdt );
	}

	*procRemove {Êarg vertex, force;
		^NuagesTProcRemove( nil, vertex, force );
	}
	
	////////// CONSTRUCTOR
	
	*new { arg nuages;
		^super.new.prInitTrans( nuages );
	}
	
	prInitTrans { arg argNuages;
		nuages = argNuages ?? { Wolkenpumpe.default };
	}
	
	factoryMethod {
		var name;
		name			= this.class.name.asString.copyToEnd( 7 );
		name[ 0 ]		= name[ 0 ].toLower;
		^name.asSymbol;
	}

	storeOn { arg stream;
		var args;
		stream << ("NuagesT.%".format( this.factoryMethod ));
		args = this.storeArgs.drop( 1 );
		stream << "(" <<<* args << ")";
		this.storeModifiersOn( stream );
	}
	
	storeModifiersOn { arg stream;
		if( randState.notNil, {
			stream << ".randState_(" <<< randState << ")";
		});
	}
	
	perform { arg bndl;
		var success;
if( verbose, {[ "perform", this ].postln });
		TypeSafe.checkArgClass( thisMethod, bndl, OSCBundle, false );
		if( this.usesRandom, { randState = thisThread.randData });
		success = this.protPerform( bndl, false );
		if( success, {
			nuages.addTransaction( this );
		});
		^success;
	}
	
	redo { arg bndl;
		var oldRandState;
if( verbose, {[ "redo", this ].postln });
		TypeSafe.checkArgClass( thisMethod, bndl, OSCBundle, false );
		^if( this.usesRandom, {
//[ "Resetting to...", randState ].postln;
			oldRandState			= thisThread.randData;
			thisThread.randData	= randState;
			{ this.protPerform( bndl, true )}.protect({ thisThread.randData = oldRandState });
		}, {
			this.protPerform( bndl, true );
		});
	}
	
	// undo { ... }
	
	persist {
		var oldState;
if( verbose, {[ "persist", this ].postln });
		^this.protPersist;
	}
	
	// subclasses must override this
	protPerform { arg bndl, recall; }

	// subclasses may override this
	protPersist { }

	// subclasses may override this
	usesRandom { ^false }
}