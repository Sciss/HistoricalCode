/*
 *	BosqueTrack
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
 *	@version	0.17, 26-Oct-08
 */
BosqueTrack {
	var <trackID;
	var java;
//	var <>y = 0, <>height = 1;
	var <muted = false;
	var <bosque;
	
	var <busConfig;
	var <name;
	var <trail;
	var <ctrlSpec;
	var <ctrlBusIndex;
	
	// level display
	var lvlStr;
	var <>liveReplc;
	
	*new { arg trackID, trail;
	
		var bosque, doc, tracks;
		
		bosque		= Bosque.default;
		doc			= bosque.session;
		tracks		= doc.tracks;
		tracks.do({ arg t; if( t.trackID == trackID, { ^t })});
		
		^super.new.prInit( trackID, trail );
	}

	prInit { arg argTrackID, argTrail;
		trackID	= argTrackID;
		trail	= argTrail;
		bosque	= Bosque.default;
		java		= JavaObject( "de.sciss.timebased.bosque.BosqueTrack", bosque.swing, trail, trackID );
//		java.setID( java.id );
	}
	
	// use with care!
	prSetTrail { arg t;
		trail = t;
	}
	
//	setLevelString { arg str;
//		java.server.listSendMsg([ '/method', java.id, \setLevelString ] ++ str.asSwingArg );
//	}

	updateLevelString { arg frame;
		this.displayLevel( this.map( frame ));
	}
	
	displayLevel { arg mapped;
		var newStr;
		
		if( mapped.notNil, {
			if( ctrlSpec.notNil, {
				newStr = mapped.round( 0.01 ).asString ++ ctrlSpec.units;
			}, {
				newStr = mapped.round( 0.01 ).asString;
			});
		}, {
			newStr = "";
		});
		if( newStr != lvlStr, {
			lvlStr = newStr;
			java.server.listSendMsg([ '/method', java.id, \setLevelString ] ++ newStr.asSwingArg );
		});
	}
	
	/**
	 *	@param	frame	the frame at which to query the level.
	 *					if nil, the current timeline position is used
	 *	@return	the current level for a given time frame, in the range
	 *			[ 0 ... 1 ], or nil if no envelope found.
	 */
	level { arg frame;
		var idx, stake, level;
		frame = frame ?? { Bosque.default.session.transport.currentFrame };
		if( liveReplc.notNil, {
			level = liveReplc.level( frame );
			if( level.notNil, { ^level });
		});
		idx   = trail.indexOfPos( frame );
		if( idx < 0, { idx = (idx + 2).neg });
		stake = trail.get( idx );
		while({ stake.notNil }, {
//			if( stake.isKindOf( BosqueEnvRegionStake ), { ^stake.level( frame )});
			// goddammit we need a FilterTrail... XXX
			if( stake.isKindOf( BosqueEnvRegionStake ) and: { stake.track === this }, { ^stake.level( frame )});
			idx = idx - 1;
			stake = trail.get( idx );
		});
		^nil;
	}
	
	/**
	 *	@param	frame	the frame at which to query the level.
	 *					if nil, the current timeline position is used
	 *	@return	the current level for a given time frame, mapped to the
	 *			track's control spec, or nil if no envelope found.
	 */
	map { arg frame;
		var raw = this.level( frame );
		if( raw.isNil or: { ctrlSpec.isNil }, { ^raw });
		^ctrlSpec.map( raw );
	}	
	
	muted_ { arg bool;
		if( bool != muted, {
			muted = bool;
//			if( mon.notNil, {
//				this.prMonRun;
//			});
			this.tryChanged( \muted, muted );
		});
	}
	
	busConfig_ { arg cfg;
		busConfig = cfg;
		this.tryChanged( \busConfig, cfg );
	}
	
	ctrlSpec_ { arg spec;
		ctrlSpec = spec;
		this.tryChanged( \ctrlSpec, spec );
	}

	ctrlBusIndex_ { arg idx;
		ctrlBusIndex = idx;
		this.tryChanged( \ctrlBusIndex, idx );
	}

	name_ { arg str;
		name = str;
		java.setName( name );
	   // so.getMap().dispatchOwnerModification( source, SessionObject.OWNER_RENAMED, newName );
		java.server.sendMsg( '/methodr', '[', '/method', java.id, \getMap, ']', \dispatchOwnerModification, -1, 0x1000, str );
		this.tryChanged( \name );
	}
	
	// use with care!!!
	trackID_ { arg id;
		trackID = id;
		java.setID( java.id );
	}

	editMute { arg source, bool, ce;
		ce.addPerform( BosqueFunctionEdit({ this.muted = bool }, { this.muted = bool.not }, "Change Track Mute", false ));
	}
	
	editBusConfig { arg source, cfg, ce;
		var oldCfg = busConfig;
		ce.addPerform( BosqueFunctionEdit({ this.busConfig = cfg }, { this.busConfig = oldCfg }, "Change Track Bus", true ));
	}

	editCtrlSpec { arg source, spec, ce;
		var oldSpec = ctrlSpec;
		ce.addPerform( BosqueFunctionEdit({ this.ctrlSpec = spec }, { this.ctrlSpec = oldSpec }, "Change Track Spec", true ));
	}

	editCtrlBusIndex { arg source, idx, ce;
		var oldIdx = ctrlBusIndex;
		ce.addPerform( BosqueFunctionEdit({ this.ctrlBusIndex = idx }, { this.ctrlBusIndex = oldIdx }, "Change Track Control Bus", true ));
	}

	editRename { arg source, newName, ce;
		var oldName = name;
		ce.addPerform( BosqueFunctionEdit({ this.name = newName }, { this.name = oldName }, "Change Track Name", true ));
	}
	
	storeArgs { ^[ trackID ]}

	storeModifiersOn { arg stream;
		if( name.notNil, {
			stream << ".name_(";
			name.storeOn( stream );
			stream << ")";
		});
//		if( y != 0, {
//			stream << ".y_(";
//			y.storeOn( stream );
//			stream << ")";
//		});
//		if( height != 1, {
//			stream << ".height_(";
//			height.storeOn( stream );
//			stream << ")";
//		});
		if( muted, {
			stream << ".muted_(";
			muted.storeOn( stream );
			stream << ")";
		});
		if( busConfig.notNil, {
			stream << ".busConfig_(";
			busConfig.storeOn( stream );
			stream << ")";
		});
		if( ctrlSpec.notNil, {
			stream << ".ctrlSpec_(";
			ctrlSpec.storeOn( stream );
			stream << ")";
		});
		if( ctrlBusIndex.notNil, {
			stream << ".ctrlBusIndex_(";
			ctrlBusIndex.storeOn( stream );
			stream << ")";
		});
	}
	
	asSwingArg { ^java.asSwingArg }
	
	dispose {
//		java.dispose;
		if( java.notNil, { java.destroy; java = nil });
//		if( mon.notNil, { mon.stop });
//		if( bus.notNil, { bus.free });
	}
}
