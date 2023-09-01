/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	@version	0.15, 24-Aug-08
 *	@author	Hanns Holger Rutz
 */
VerboseNetAddr : NetAddr {
	var <>dumpOutgoing	= true;
	var <dumpIncoming;
	var <>notThese;
	var <>backTrace = false;
	var <>humanReadable = true;
	
	var upd;
	
	classvar humanStrings;
	
	*new { arg ... args;
		^super.new( *args ).prInitVerboseNetAddr;
	}
	
	*wrap { arg addr;
		^this.new( addr.hostname, addr.port );
	}
	
	prInitVerboseNetAddr {
		notThese			= IdentitySet[ '/status', 'status.reply' ];
		upd				= { arg time, replyAddr, msg;
//			if( (replyAddr.hostname == this.hostname) and: { (replyAddr.port == this.port) and:
//			    { notThese.includes( msg.first.asSymbol ).not }}, {
//				("r: " ++ msg).postln;
//			});
//			if( (replyAddr.socket == this.socket) and: ... ) // doesn't work (still prints all incoming messages)
			if( (replyAddr.port == this.port) and:
			    { notThese.includes( msg.first.asSymbol ).not }, {
				("r: " ++ msg).postln;
			});
		};
		if( humanStrings.isNil, {
			humanStrings = [
				"none", "notify", "status", "quit", "cmd", "d_recv", "d_load", "d_loadDir", "d_freeAll",
				"s_new", "n_trace", "n_free", "n_run", "n_cmd", "n_map", "n_set", "n_setn", "n_fill",
				"n_before", "n_after", "u_cmd", "g_new", "g_head", "g_tail", "g_freeAll", "c_set",
				"c_setn", "c_fill", "b_alloc", "b_allocRead", "b_read", "b_write", "b_free", "b_close",
				"b_zero", "b_set", "b_setn", "b_fill", "b_gen", "dumpOSC", "c_get", "c_getn", "b_get",
				"b_getn", "s_get", "s_getn", "n_query", "b_query", "n_mapn", "s_noid", "g_deepFree",
				"clearSched", "sync", "d_free"
			];
//			humanStrings.size.postln;
		});
		this.dumpIncoming = true;
	}
	
	dumpIncoming_ { arg bool;
		if( bool != dumpIncoming, {
			dumpIncoming = bool;
			if( dumpIncoming, {
//				upd.addTo( Main );
				thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.addFunc( upd );
			}, {
//				upd.removeFromAll;
				thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.removeFunc( upd );
			});
		});
	}
	
	sendMsg { arg ... args;
		if( dumpOutgoing and: { notThese.includes( args.first.asSymbol ).not }, {
			this.prDump( "s: ", args );
		});
		^super.sendMsg( *args );
	}
	
	sendBundle { arg time ... msgs;
		if( dumpOutgoing, {
			("s: [ #bundle, " ++ time ++ ",").postln;
			msgs.do({ arg msg;
				this.prDump( "     ", msg );
			});
			"   ]".postln;
		});
		^super.sendBundle( time, *msgs );
	}
	
	prDump { arg prefix, argList;
		argList = argList.collect({ arg item; if( item.class === Int8Array, "DATA", item )});
		if( humanReadable and: { argList.first.isInteger }, {
			argList[ 0 ] = argList.first.asString ++ " (" ++ humanStrings[ argList.first ] ++ ")";
		});
		(prefix ++ argList).postln;
		if( backTrace, { this.dumpBackTrace });
	}
}