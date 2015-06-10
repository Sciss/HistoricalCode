/**
 *	NuagesOSCBundle
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 01-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesOSCBundle : MixedBundle {
	classvar <>maxBundleSize = 32768;
	var <nuages;
	
	*new { arg nuages;
		^super.new.prInit( nuages );
	}
	
	prInit { arg argNuages;
		nuages = argNuages;
	}
	
//	add { arg msg; messages = messages.add( msg )}
//	addPrepare { arg msg; preparationMessages = preparationMessages.add( msg )}
	
	send {
		if( preparationMessages.notNil, {
			if( preparationMessages.bundleSize > (maxBundleSize - 20), {  // 20 = sync msg
				this.prClumpBundles( preparationMessages, maxBundleSize - 20 );
			}, {
				[ preparationMessages ];
			}).do({ arg msgs;
				msgs = msgs.add( nuages.forkSync );
				nuages.server.listSendBundle( nil, msgs );
				nuages.joinSync;
			});
//		}, {
//			nuages.joinSync;
		});
		if( messages.notNil, {
			if( messages.bundleSize > maxBundleSize, {
				this.prClumpBundles( messages, maxBundleSize - 20 ).do({ arg msgs, i;
					if( i > 0, {Ênuages.joinSync });
					msgs = msgs.add( nuages.forkSync );
					nuages.server.listSendBundle( nil, msgs );
				});
			}, {
				nuages.server.listSendBundle( nil, messages );
			});
		});
		if( functions.notNil, {
			this.doFunctions;
		});
	}

	prClumpBundles { arg msgs, maxSize;
		var bndlSize = 0, res, clumps, count = 0, msgSizes;
		msgSizes = msgs.collect({ arg msg; msg.bundleSize });
		msgSizes.do({ arg msgSize, i;
			bndlSize = bndlSize + msgSize;
			if( bndlSize > maxSize, {
				clumps 	= clumps.add( count );
				count  	= 0;
				bndlSize = msgSize;
			});
			count = count + 1;
		});
		^msgs.clumps( clumps );
	}
}