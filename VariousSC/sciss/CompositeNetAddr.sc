/**
 *	(C)opyright 2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.1, 23-May-08
 */
CompositeNetAddr : NetAddr {
	var addrs;
	
	addAddr { arg addr;
		addrs = addrs.add( addr );
	}

	removeAddr { arg addr;
		addrs.remove( addr );
	}
	
	sendRaw { arg rawArray;
		addrs.do({ arg addr; addr.sendRaw( rawArray )});
		^super.sendRaw( rawArray );
	}

	sendMsg { arg ... args;
		addrs.do({ arg addr; addr.sendMsg( *args )});
		^super.sendMsg( *args );
	}

	sendBundle { arg time ... args;
		addrs.do({ arg addr; addr.sendBundle( time, *args )});
		^super.sendBundle( time, *args );
	}
}