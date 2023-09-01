// stupid path responder is still broken
// here's a circumvention
ScissOSCPathResponder {
	*new { arg addr, cmdPath, action;
		^OSCresponderNode( addr, cmdPath.first, { arg time, resp, msg;
			block { arg break;
				cmdPath.do({ arg argu, idx;
					if( argu != msg[ idx ], break );
				});
				action.value( time, resp, msg );
			};
		});
	}
}