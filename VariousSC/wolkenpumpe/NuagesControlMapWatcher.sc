/**
 *	NuagesControlMapWatcher
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Changelog:
 *
 *	@version	0.10, 16-Apr-09
 *	@author	Hanns Holger Rutz
 */
NuagesControlMapWatcher {
	var <server, <>period, rout, resp, msg, indices, busses, funcs;
	
	*new { arg server, period = 0.05;
		^super.new.prInit( server, period );
	}
	
	prInit { arg argServer, argPeriod;
		server	= argServer;
		period	= argPeriod;
	}
	
	add { arg bus, func;
		if( (bus.rate != \control) or: { bus.numChannels != 1 }, {
			TypeSafe.methodError( thisMethod, "Only mono kbusses supported: " + bus );
			^this;
		});
		
		busses = busses.add( bus );
		funcs  = funcs.add( func );
		this.prRecreateMessage;
		if( busses.size == 1, {
			rout = Routine({
				inf.do({
					server.listSendMsg( msg );
					period.wait;
				});
			}).play( AppClock ); // AppClock jitter is ok in this case
		});
		this.prRecreateResponder;
	}
	
	remove { arg bus;
		var idx;
		
		idx = busses.indexOf( bus );
		if( idx.isNil, { ^this });
		
		busses.removeAt( idx );
		funcs.removeAt( idx );
		
		this.prRecreateMessage;
		
		if( busses.size == 0, {
			rout.stop; rout = nil;
			resp.remove; resp = nil;
		}, {
			this.prRecreateResponder;
		});
	}
	
	prRecreateMessage {
		indices	= busses.collect( _.index );
		msg		= [ '/c_get' ] ++ indices;
	}
		
	prRecreateResponder {
		resp.remove; resp = nil;
		resp = OSCpathResponder( server.addr, [ '/c_set', msg[ 1 ]], { arg time, resp, received;
			// make sure the message is up-to-date
			block { arg break;
				indices.do({ arg num, i;
					if( received[ (i << 1) + 1 ] != num, break );
				});
				// ok, let's call the funcs
				funcs.do({ arg func, i;
					try {
						func.value( received[ (i << 1) + 2 ]);
					} { arg e;
						e.reportError;
					};
				});
			};
		}).add;
	}
}