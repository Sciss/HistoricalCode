/*
 *	BosqueTransport
 *	(Bosque)
 *
 *	Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	@author	Hanns Holger Rutz
 *	@version	0.30, 26-Oct-08
 */
BosqueTransport : Object {
	var <doc;
	var running	= false;
	var paused	= false;
	var clpseStop;
	var playRate, playRate2, playStartPos, playStartTime;
	var loop;
	var loopUsed	= false;
//	var funcClpse;
	var clpsePoint;
	
	*new { arg doc;
		^super.new.prInit( doc );
	}
	
	prInit { arg argDoc;
		doc = argDoc;
		clpseStop = Collapse({ this.prCollapse });
//		funcClpse = { this.prCollapse };
		UpdateListener.newFor( doc.timelineView, { arg upd, timelineView, what, what2;
			var pos, wasPaused;
			
			wasPaused = paused;
			if( running, {
				switch( what,
				\positioned, {
					pos = timelineView.cursor.position;
					this.stop( setPosition: false );
					this.play( pos );
					if( wasPaused, { this.pause });
				},
				\changed, {
					what2.postln;
					switch( what2,
					\span, { this.prUpdateCollapse },
					\rate, {
						this.stop( setPosition: true );
						this.play( pos );
						if( wasPaused, { this.pause });
					});
				});
			});
		});
	}
	
	loop_ { arg span;
		if( loop != span, {
			loop = span;
			if( running, { this.prUpdateCollapse });
			this.tryChanged( \loop, loop );
		});
	}
	
	play { arg pos, rate = 1;
		if( running, { ^this });
		
		pos			= pos ?? { doc.timelineView.cursor.position };
		if( rate >= 0, {
			if( pos >= doc.timeline.span.stop, { pos = doc.timeline.span.start });
		}, {
			if( pos <= 0, { pos = doc.timeline.span.stop });
		});
		playStartTime	= thisThread.seconds;
		playStartPos	= pos;
		playRate		= rate;
		playRate2		= playRate * doc.timeline.rate;
		running		= true;
		paused		= false;
		this.prUpdateCollapse( pos );
		this.tryChanged( \play, pos, rate );
	}
	
	rate { ^playRate }
	
	rate_ { arg newRate;
		var pos;
	
//		newRate = newRate.max( 0.0 );
		if( running.not || (newRate == playRate), { ^this });
		
		pos			= this.currentFrame;
//		if( pos >= doc.timeline.span.stop, { pos = doc.timeline.span.start });
		playStartTime	= thisThread.seconds;
		playStartPos	= pos;
		playRate		= newRate;
		playRate2		= playRate * doc.timeline.rate;
		running		= true;
		paused		= false;
		this.prUpdateCollapse( pos );
		this.tryChanged( \rate, pos, newRate );
	}
	
	prCollapse {
//		[ "STOP!", loopUsed ].postln;
//		clpseStop = nil;
		this.stop;
		if( loopUsed, {
			this.play( if( playRate >= 0, loop.start, loop.stop ), playRate );
		});
	}

//	prMakeCollapse { arg pos;
//		var dur;
//		pos = pos ?? { this.currentFrame };
//		if( clpseStop.notNil, { clpseStop.cancel; clpseStop = nil });
//		dur		= this.prCalcCollapseDur( pos );
//		clpseStop	= Collapse( funcClpse, dur, SystemClock ).defer;
//	}

	prCalcCollapsePoint { arg pos;
//		var dur;
		if( playRate2 >= 0, {
			loopUsed	= loop.notNil and: { pos < loop.stop };
		}, {
			loopUsed	= loop.notNil and: { pos > loop.start };
		});
		if( loopUsed, {
			if( playRate2 >= 0, {
				clpsePoint = loop.stop;
//				dur	= (loop.stop - pos) / playRate2;
			}, {
				clpsePoint = loop.start;
//				dur	= (pos - loop.start) / playRate2.neg;
			});
		}, {
			if( playRate2 >= 0, {
				clpsePoint = doc.timeline.span.stop;
//				dur	= (doc.timeline.span.stop - pos) / playRate2;
			}, {
				clpsePoint = doc.timeline.span.start;
//				dur	= (pos - doc.timeline.span.start - pos) / playRate2.neg;
			});
		});
//		("collapse dur = " ++ dur).postln;
//		^dur;
	}

	prUpdateCollapse { arg pos;
		var dur;
		pos = pos ?? { this.currentFrame };
//		if( clpseStop.isNil, { ^this.prMakeCollapse( pos )});
		this.prCalcCollapsePoint( pos );
//		clpseStop.rescheduleWith( dur );
	}
	
	stop { arg setPosition = true;
		var pos;

		if( running.not, { ^this });

//		if( clpseStop.notNil, { clpseStop.cancel; clpseStop = nil });
//		if( clpseStop.started, { clpseStop.cancel });
		pos 			= this.currentFrame;
		running		= false;
		paused		= false;
		this.tryChanged( \stop );
		if( setPosition, { doc.editPosition( this, pos )});
	}
	
	pause {
		if( running.not || paused, { ^this });
		
//		if( clpseStop.notNil, { clpseStop.cancel; clpseStop = nil });
//		if( clpseStop.started, { clpseStop.cancel });
		playStartPos 	= this.currentFrame;
		playStartTime	= thisThread.seconds;
		paused		= true;
		this.tryChanged( \pause );
	}
	
	resume { arg pos;
		if( running.not || paused.not, { ^this });
		
		paused		= false;
		pos			= pos ? playStartPos;
		playStartPos 	= pos;
		playStartTime	= thisThread.seconds;
		this.prUpdateCollapse( pos );
		this.tryChanged( \resume, pos, playRate );
	}
	
	isRunning { ^running }
	isPaused  { ^paused }
	
	currentFrame {
		var frame;
		if( running, {
			frame = playStartPos + ((thisThread.seconds - playStartTime) * playRate2).asInteger;
			if( playRate2 >= 0, {
				if( frame >= clpsePoint, {
					clpseStop.instantaneous;
					^clpsePoint;
				}, {
					^frame;
				});
			}, {
				if( frame <= clpsePoint, {
					clpseStop.instantaneous;
					^clpsePoint;
				}, {
					^frame;
				});
			});
//			^doc.timeline.span.clip( playStartPos + ((thisThread.seconds - playStartTime) * playRate2).asInteger )
		}, {
			^doc.timelineView.cursor.position;
		});
	}
}
