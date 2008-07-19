BosqueNetTrailEditor {
	var trail;
	
	*new { arg trail;
		^super.new.prInit( trail );
	}
	
	prInit { arg argTrail;
		trail = argTrail;
	}
	
	value { arg mon, edit, coll;
		var lastCmd, collAddRemove, success = false;
		
//("BosqueNetTrailEditor.value : " ++ coll).postln;
		
		trail.editBegin( edit );
		block { arg break;
			coll.do({ arg cmd;
//("Now cmd is " ++ cmd).postln;
				if( cmd.first !== lastCmd, {
					if( this.prFlush( lastCmd, collAddRemove, edit ).not, break );
//("So i called flush with " ++ lastCmd ++ "; collAddRemove.size is " ++ collAddRemove.size).postln;
					lastCmd		= cmd.first;
					collAddRemove	= nil;
				});
				collAddRemove = collAddRemove.add( this.protCreateStake( cmd.copyToEnd( 1 )));
//("So i added a stake").postln;
			});
			success = this.prFlush( lastCmd, collAddRemove, edit );
//("Finally i called flush with " ++ lastCmd ++ "; collAddRemove.size is " ++ collAddRemove.size).postln;
		};
		trail.editEnd( edit );
		^success;
	}

	prFlush { arg cmd, collAddRemove, edit;
		if( cmd.isNil, { ^true });
		if( collAddRemove.size > 0, {
			switch( cmd,
			\add, { trail.editAddAll( this, collAddRemove, edit )},
			\rem, { trail.editRemoveAll( this, collAddRemove, edit )},
			{ ("Illegal trail edit command: " ++ cmd).error; ^false }
			);
		});
		^true;
	}
	
	protCreateStake { arg oscRepr; this.subclassResponsibility( thisMethod )}
}

BosqueNetMarkerTrailEditor : BosqueNetTrailEditor {
	protCreateStake { arg oscRepr;
		^BosqueMarkerStake( oscRepr[ 0 ], oscRepr[ 1 ]);
	}
}
