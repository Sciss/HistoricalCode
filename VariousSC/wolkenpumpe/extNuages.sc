/**
 *	extNuages
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 11-Aug-08
 *	@author	Hanns Holger Rutz
 */
//+ Synth {
//	asSwingArg {
//		^([ '[', '/method', "de.sciss.jcollider.Synth", \basicNew, this.defName ] ++ nil.asSwingArg ++ [ this.nodeID, ']' ]);
//	}
//}
//
//+ Group {
//	asSwingArg {
//		^([ '[', '/method', "de.sciss.jcollider.Group", \basicNew ] ++ nil.asSwingArg ++ [ this.nodeID, ']' ]);
//	}
//}
//
//
//+ Bus {
//	asSwingArg {
//		^([ '[', '/new', "de.sciss.jcollider.Bus" ] ++ nil.asSwingArg ++ [ this.rate, this.index, this.numChannels, ']' ]);
//	}
//}
//
//+ Buffer {
//	asSwingArg {
//		^([ '[', '/new', "de.sciss.jcollider.Buffer" ] ++ nil.asSwingArg ++ [ this.numFrames, this.numChannels, this.bufNum, ']' ]);
//	}
//}
