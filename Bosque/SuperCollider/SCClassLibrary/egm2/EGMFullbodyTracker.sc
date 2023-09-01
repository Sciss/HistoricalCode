/**
 *	@version	0.11, 26-Oct-08
 *	@author	Hanns Holger Rutz
 */
EGMFullbodyTracker {
	classvar resp;
	classvar <msg;

	*start {
		this.stop;
		resp = OSCpathResponder( nil, [ '/client', \tracker ], { arg time, resp, oscMsg;
			msg = EGMFullbody( oscMsg );
			this.tryChanged( \msg, msg );
		}).add;
	}
	
	*stop {
		if( resp.notNil, {
			resp.remove;
			resp = nil;
		});
	}
}