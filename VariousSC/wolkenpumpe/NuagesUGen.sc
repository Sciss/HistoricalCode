/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: NuagesUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.13, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUGen : NuagesU {
	// ----------- public instance methods -----------

	numAudioInputs { ^0 }
	numControlInputs { ^0 }
	
	// ----------- protected instance methods -----------

	protPreferredNumInChannels { arg idx; ^0 }
}

//// damn... scala's mixins would be useful...
//NuagesAudioGeneratorUnit : NuagesGeneratorUnit {
//	numAudioOutputs { ^1 }
//	getAudioOutputName { arg idx; ^nil }
//
//	numControlOutputs { ^0 }
//}
//
//NuagesControlGeneratorUnit : NuagesGeneratorUnit {
//	numControlOutputs { ^1 }
//	getControlOutputName { arg idx; ^nil }
//
//	numAudioOutputs { ^0 }
//}

//// damn... scala's mixins would be useful...
//NuagesUGenAudio : NuagesUGen {
//	numAudioOutputs { ^1 }
//	getAudioOutputName { arg idx; ^nil }
//
//	numControlOutputs { ^0 }
//	isControl { ^false }
//}
//
//NuagesUGenControl : NuagesUGen {
//	numControlOutputs { ^1 }
//	getControlOutputName { arg idx; ^nil }
//
//	numAudioOutputs { ^0 }
//	protPreferredNumOutChannels { arg idx; ^0 }
//	isControl { ^true }
//}
