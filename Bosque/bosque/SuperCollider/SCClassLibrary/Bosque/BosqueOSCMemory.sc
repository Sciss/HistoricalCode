/*
 *	BosqueOSCMemory
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
 *	@version	0.16, 26-Oct-08
 */
BosqueOSCMemory {
	var win;
	var <path;
	var <synced = false;
	var <offset = 0.0;
	var osc, rout, doc, updTransport;
	var <>addr;
	
	var oscs, paths, oscIdx = 0;

	*new {
		^super.new.prInit;
	}
	
	prInit {
	}
	
	makeGUI {
		var view, flow, fntSmall, fntBig, updThis, ggPathName, lbStart, lbStop, lbPackets, ggOffset, ggSync, ggPlay, ggStop;
		var ggPlayPos, fPlayPosStr, taskPlayPos, fTaskPlayPos, playStartPos, playStartTime;
	
		if( win.notNil, { ^this });
	
		fntSmall	= JFont( "Lucida Grande", 10 );
		fntBig	= JFont( "Helvetica", 20 );

		win = JSCWindow( "OSC Memory", Rect( 0, 0, 380, 130 ), resizable: false );
		ScissUtil.positionOnScreen( win, 0.5, 0.75 );
		view = win.view;
		view.decorator = flow = FlowLayout( view.bounds );
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right )
			.string_( "File:" );
		ggPathName = JSCDragSink( view, Rect( 0, 0, 280, 20 ))
			.font_( fntSmall )
			.interpretDroppedStrings_( false )
			.canReceiveDragHandler_({ arg b;
				JSCView.currentDrag.isKindOf( String ) || JSCView.currentDrag.isKindOf( PathName );
			})
			.receiveDragHandler_({ arg b;
				var drag = JSCView.currentDrag;
				b.object = if( drag.isKindOf( String ), {
					drag;
				}, { if( drag.isKindOf( PathName ), {
					drag.fullPath;
				})});
			})
			.resize_( 2 )
			.action_({ arg b;
				this.path = b.object;
			});
		JSCButton( view, Rect( 730, 10, 20, 20 ))
			.font_( fntSmall )
			.canFocus_( false )
			.resize_( 3 )
			.states_([[ "..." ]])
			.action_({ arg b;
				SwingDialog.getPaths({ arg paths;
					this.path = paths.first;
				}, maxSize: 1 );
			});
			
		flow.nextLine;
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right )
			.string_( "Start:" );
		lbStart = JSCStaticText( view, Rect( 0, 0, 100, 20 )).font_( fntSmall );
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right )
			.string_( "Offset:" );
		ggOffset = JSCTextField( view, Rect( 0, 0, 100, 20 )).font_( fntSmall )
			.string_( ScissUtil.toTimeString( this.offset ))
			.action_({ arg b;
				this.offset = ScissUtil.fromTimeString( b.string );
			});
		flow.nextLine;
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right )
			.string_( "Stop:" );
		lbStop = JSCStaticText( view, Rect( 0, 0, 100, 20 )).font_( fntSmall );
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right )
			.string_( "Packets:" );
		lbPackets = JSCStaticText( view, Rect( 0, 0, 100, 20 )).font_( fntSmall );
		flow.nextLine;
		
		flow.shift( 8, 8 );

		ggSync = JSCButton( view, Rect( 0, 0, 34, 34 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x86.asAscii ++ 0x82.asAscii ] ++ if( synced, [ Color.white, Color.blue ])])
			.action_({ arg b;
				this.synced = this.synced.not;
			});
		
		ggPlay = JSCButton( view, Rect( 0, 0, 34, 34 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x96.asAscii ++ 0xB6.asAscii ] ++ if( rout.notNil, [ Color.white, Color.blue ])])
			.action_({ arg b;
				this.play( this.offset );
			});
		
		ggStop = JSCButton( view, Rect( 0, 0, 34, 34 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x97.asAscii ++ 0xBC.asAscii ]])  // Solid square
			.action_({ arg b;
				this.stop;
			});

		ggPlayPos = JSCStaticText( view, Rect( 0, 0, 160, 34 )).background_( Color.black )
			.stringColor_( Color.yellow ).align_( \center ).font_( JFont( "Eurostile", 20, 1 ));
		fPlayPosStr = { arg sec; ScissUtil.toTimeString( sec )};
//		updTimeline = UpdateListener.newFor( doc.timeline, { arg upd, timeline, what;
//			if( taskPlayPos.isPlaying.not and: { (what === \position) or: { what === \rate }}, {
//				ggPlayPos.string_( fPlayPosStr.value( timeline.position ));
//			});
//		});
		fTaskPlayPos = { inf.do({ ggPlayPos.string_( fPlayPosStr.value(
			if( synced, {
				doc.transport.currentFrame / doc.timeline.rate + offset;
			}, {
				playStartPos + thisThread.seconds - playStartTime;
			}))); 0.05.wait })
		};

		updThis = UpdateListener.newFor( this, { arg upd, me, what, param1;
			switch( what,
			\path, {
				ggPathName.object 	= this.path;
				lbStart.string	= ScissUtil.toTimeString( osc.startTime );
				lbStop.string		= ScissUtil.toTimeString( osc.stopTime );
				lbPackets.string	= osc.packets.size.asString;
			},
			\synced, {
				ggPlay.enabled	= this.synced.not;
				ggStop.enabled	= this.synced.not;
				ggSync.states		= [[ ggSync.states.first.first ] ++ if( synced, [ Color.white, Color.blue ])];
			},
			\offset, {
				ggOffset.string	= ScissUtil.toTimeString( this.offset );
			},
			\play, {
				playStartPos		= param1;
				playStartTime		= thisThread.seconds;
				taskPlayPos.stop; taskPlayPos = fTaskPlayPos.fork( AppClock );
				ggPlay.states = [[ ggPlay.states.first.first, Color.white, Color.black ]];
			},
			\stop, {
				taskPlayPos.stop;
				ggPlay.states = [[ ggPlay.states.first.first ]];
			}
			);
		});

		win.onClose = {
			updThis.remove;
			win = nil;
		};
		win.front;
	}
	
	path_ { arg str;
		this.stop;
		osc			= OSCFile.read( str );	// may throw error
		path			= str;
		this.tryChanged( \path, path );
		this.offset	= if( synced, { osc.startTime }, 0.0 );
	}
	
	paths_ { arg strs;
		this.stop;
		osc			= nil;
		oscs			= strs.collect({ arg str; OSCFile.read( str )});
		paths		= strs;
		path			= paths.first;
		oscIdx		= 0;
		osc			= oscs.first;
		this.tryChanged( \path, path );
		this.offset	= 0.0;
	}
	
	offset_ { arg value;
		this.stop;
		if( offset != value, {
			offset = value;
			this.tryChanged( \offset, offset );
		});
	}
	
	synced_ { arg bool;
		if( synced != bool, {
			if( bool, {
				doc = Bosque.default.session;
				updTransport = UpdateListener.newFor( doc.transport, { arg upd, transport, what, param1;
					switch( what,
					\play, {
						this.play( param1 / doc.timeline.rate + offset );
					},
					\stop, {
						this.stop;
					},
					\pause, {
//						this.stop;
					},
					\resume, {
						this.stop;
						this.play( param1 / doc.timeline.rate + offset );
					});
				});
			}, {
				if( updTransport.notNil, {
					updTransport.removeFrom( doc.transport );
					doc			= nil;
					updTransport	= nil;
				});
			});
			synced = bool;
			this.tryChanged( \synced, synced );
		});
	}
	
	dispose {
		if( win.notNil, { win.close; win = nil });
		this.stop;
		this.synced = false;
		osc = nil;
	}

	play { arg pos = 0;
		if( rout.notNil, {
			this.stop;
		});
		if( oscs.notNil, {
			osc	= oscs[ oscIdx ];
			path	= paths[ oscIdx ];
			oscIdx = (oscIdx + 1) % oscs.size;
			this.tryChanged( \path, path );
		});
		if( osc.notNil, {
			rout = osc.play( offset: pos - (osc.startTime ? 0), addr: addr );
			this.tryChanged( \play, pos );
		});
	}

	stop {
		if( rout.notNil, {
			rout.stop; rout = nil;
			this.tryChanged( \stop );
		});
	}
}