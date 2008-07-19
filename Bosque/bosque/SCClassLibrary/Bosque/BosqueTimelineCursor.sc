/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 18-Jul-08
 */
BosqueTimelineCursor : Object {
	var <timeline;
	var <position;
	var <java, upd;

	*new { arg timeline, pos;
		^super.new.prInit( timeline, pos );
	}
	
	prInit { arg argTimeline, argPos;
		var swing;
		
		timeline		= argTimeline;
		position		= argPos ?? { timeline.span.start };
		swing		= Bosque.default.swing;
		java			= JavaObject( "de.sciss.timebased.timeline.BasicTimelineCursor", swing, timeline, position );
		
		upd			= UpdateListener.newFor( timeline, { arg upd, tl;
			var tlSpan = tl.span;
			if( tlSpan.containsPos( position ).not, {
				this.position = tlSpan.clip( position );
			});
		}, \changed );
	}
	
	position_ {Êarg newPos;
		if( newPos != position, {
			position = newPos;
			this.changed( \changed, position );
			java.setPosition( java, position );
		});
	}
	
	dispose {
		upd.remove;
		java.dispose; java.destroy; java = nil;
	}

	asSwingArg {
		^java.asSwingArg;
	}
}