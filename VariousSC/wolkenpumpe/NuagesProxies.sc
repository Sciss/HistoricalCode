/**
 *	NuagesBusProxy
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 17-Oct-08
 *	@author	Hanns Holger Rutz
 */
NuagesBusProxy {
	var bus, proxyServer;
	
	*new { arg bus, proxyServer;
		^super.new.prInit( bus, proxyServer );
	}
	
	prInit { arg argBus, argProxyServer;
		bus			= argBus;
		proxyServer	= argProxyServer;
	}
	
	// sucky shit in Object.sc
	numChannels { ^bus.numChannels }
	rate { ^bus.rate }
	
	asSwingArg {
		^([ '[', '/new', "de.sciss.jcollider.Bus" ] ++ proxyServer.asSwingArg ++ [ bus.rate, bus.index, bus.numChannels, ']' ]);
	}
}

NuagesGroupProxy {
	var group, proxyServer;
	
	*new { arg group, proxyServer;
		^super.new.prInit( group, proxyServer );
	}
	
	prInit { arg argGroup, argProxyServer;
		group		= argGroup;
		proxyServer	= argProxyServer;
	}
	
	nodeID { ^group.nodeID }
	
	asSwingArg {
		^([ '[', '/method', "de.sciss.jcollider.Group", \basicNew ] ++ proxyServer.asSwingArg ++ [ group.nodeID, ']' ]);
	}
}