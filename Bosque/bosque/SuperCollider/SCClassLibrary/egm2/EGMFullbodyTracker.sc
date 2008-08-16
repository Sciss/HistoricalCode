EGMFullbodyTracker {
	classvar resp;
	classvar <msg;

	*start {
		this.stop;
		resp = OSCpathResponder( nil, [ '/client', \tracker ], { arg time, resp, oscMsg;
			msg = EGMFullbody( oscMsg );
			this.changed( \msg, msg );
		}).add;
	}
	
	*stop {
		if( resp.notNil, {
			resp.remove;
			resp = nil;
		});
	}
}