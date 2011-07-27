/*
 *	BosqueOSCRecorder
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
 *	@version	0.12, 26-Oct-08
 */
BosqueOSCRecorder {
	var win;
	var <basePath, <path;
	var <oscKey	= '/client';
	var <synced = false;
	var <offset = 0.0;
	var doc, updTransport;
	var <>addr;
	var funcOSC, addedFunc = false;
	var oscRec;
	var recording = false;
	var recStartTime;
	var <ignoreBundleTime = false;

	*new {
		^super.new.prInit;
	}
	
	prInit {
		funcOSC = { arg ... args; this.prReceived( *args )};
	}
	
	prReceived { arg time, replyAddr, msg;
		if( msg.first === oscKey, {
			if( recording, {
//				[ time, time + offset - recStartTime ].postln;
				if( ignoreBundleTime, { time = SystemClock.seconds });
				oscRec.sendBundle( time - recStartTime, msg );
			});
			this.tryChanged( \msg, msg );
		});
	}
	
	makeGUI { arg disposeOnClose = true;
		var view, flow, fntBig, fntSmall, ggRec, ggStop, clpseDisplay, ggPathName, ggDisplay, ggRecPos, taskPlayPos, ggTraffic, clpseTraf, fntMedium, floatPrecision, ggBasePath, updThis, ggOSCKey, ggSync, ggOffset, fTaskPlayPos, ggIgnoreBundleTime;
		
		if( win.notNil, { win.front; ^this });
	
		basePath = "~/Bosque/osc".standardizePath;
		floatPrecision = 0.001;	// that's merely the display!
		
		win		= JSCWindow( "OSC Recorder", Rect( 0, 0, 400, 196 ), resizable: false );
		ScissUtil.positionOnScreen( win, 0.5, 0.75 );
			
		view		= win.view;
		view.decorator = flow = FlowLayout( view.bounds );
	
		fntSmall	= JFont( "Lucida Grande", 11 );
		fntMedium	= JFont( "Monaco", 11 );
		fntBig	= JFont( "Helvetica", 20 );
		
		fTaskPlayPos = {
			inf.do({
				ggRecPos.string_( ScissUtil.toTimeString( SystemClock.seconds - recStartTime ));
				0.05.wait;
			});
		};

		JSCStaticText( view, Rect( 0, 0, 60, 24 )).font_( fntSmall ).align_( \right )
			.string_( "Folder:" );
		ggBasePath = JSCDragSink( view, Rect( 0, 0, 298, 24 ))
			.object_( basePath )
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
				this.basePath = b.object;
			});
		JSCButton( view, Rect( 0, 0, 20, 20 ))
			.font_( fntSmall )
			.canFocus_( false )
			.resize_( 3 )
			.states_([[ "..." ]])
			.action_({ arg b;
				SwingDialog.getPaths({ arg paths;
					this.basePath = paths.first;
				}, maxSize: 1 );
			});
		flow.nextLine;
		
		JSCStaticText( view, Rect( 0, 0, 60, 24 )).font_( fntSmall ).align_( \right )
			.string_( "File:" );
		ggPathName = JSCDragSink( view, Rect( 0, 0, 298, 24 ))
			.font_( fntSmall )
			.interpretDroppedStrings_( false )
			.canReceiveDragHandler_({ false })
			.resize_( 2 );
			
		flow.nextLine;

		JSCStaticText( view, Rect( 0, 0, 60, 24 )).font_( fntSmall ).align_( \right )
			.string_( "Key:" );
		ggOSCKey = JSCTextField( view, Rect( 0, 0, 80, 20 ))
			.string_( oscKey.asString )
			.font_( fntSmall )
			.action_({ arg b;
				this.oscKey = b.string.asSymbol;
			});

		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right )
			.string_( "Offset:" );
		ggOffset = JSCTextField( view, Rect( 0, 0, 100, 20 )).font_( fntSmall )
			.string_( ScissUtil.toTimeString( this.offset ))
			.action_({ arg b;
				this.offset = ScissUtil.fromTimeString( b.string );
			});

		flow.nextLine;
		flow.shift( 60, 0 );
		ggIgnoreBundleTime = JSCCheckBox( view, Rect( 0, 0, 200, 24 ))
			.string_( "Ignore Bundle Times" )
			.value_( ignoreBundleTime )
			.font_( fntSmall )
			.action_({ arg b;
				this.ignoreBundleTime = b.value;
			});
		
		flow.nextLine;
		ggDisplay = JSCStaticText( view, Rect( 0, 0, 380, 34 )).font_( fntMedium );
	
		flow.nextLine;
		ggRecPos = JSCStaticText( view, Rect( 0, 0, 160, 34 )).background_( Color.black )
			.stringColor_( Color.yellow ).align_( \center ).font_( JFont( "Eurostile", 20, 1 ));
		
		ggTraffic = JSCStaticText( view, Rect( 0, 0, 34, 34 )).background_( Color.black );
		clpseTraf = Collapse({
			ggTraffic.background = Color.black;
		}, 0.5 );

		flow.shift( 16, 0 );
			
		ggSync = JSCButton( view, Rect( 0, 0, 34, 34 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x86.asAscii ++ 0x82.asAscii ] ++ if( synced, [ Color.white, Color.blue ])])
			.action_({ arg b;
				this.synced = this.synced.not;
			});

		ggRec = JSCButton( view, Rect( 0, 0, 34, 34 ))
			.font_( fntBig )
			.canFocus_( false )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x97.asAscii ++ 0x89.asAscii ] ++ if( recording, [ Color.white, Color.red ])])
			.action_({ arg b;
				this.rec( this.offset );
			});

		ggStop = JSCButton( view, Rect( 0, 0, 34, 34 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x97.asAscii ++ 0xBC.asAscii ]])  // Solid square
			.action_({ arg b;
				this.stop;
			});
		
		clpseDisplay = Collapse({ arg msg;
			ggDisplay.string = msg.copyToEnd( 1 ).collect({ arg x;
				if( x.isKindOf( Float ), {
					x.round( floatPrecision );
				}, x );
			}).asCompileString.keep( 80 );
		}, 0.05 );
				
		updThis = UpdateListener.newFor( this, { arg upd, me, what, param1;
//[ "updThis", what ].postln;
		
			switch( what,
			\msg, {
				clpseDisplay.instantaneous( param1 );
				if( clpseTraf.started.not, {
					ggTraffic.background = Color.green( 0.7 );
				});
				clpseTraf.defer;
			},
			\path, {
				ggPathName.object 	= this.path.basename;
			},
			\basePath, {
				ggBasePath.object 	= this.basePath;
//				lbStart.string	= ScissUtil.toTimeString( osc.startTime );
//				lbStop.string		= ScissUtil.toTimeString( osc.stopTime );
//				lbPackets.string	= osc.packets.size.asString;
			},
			\synced, {
//				ggPlay.enabled	= this.synced.not;
				ggStop.enabled	= this.synced.not;
				ggSync.states		= [[ ggSync.states.first.first ] ++ if( synced, [ Color.white, Color.blue ])];
			},
			\offset, {
				ggOffset.string	= ScissUtil.toTimeString( this.offset );
			},
			\rec, {
//				playStartPos		= param1;
//				playStartTime		= thisThread.seconds;
				taskPlayPos.stop; taskPlayPos = fTaskPlayPos.fork( AppClock );
				ggRec.states		= [[ ggRec.states.first.first, Color.white, Color.red ]];
			},
			\stop, {
				taskPlayPos.stop;
				taskPlayPos = nil;
				ggRec.states = [[ ggRec.states.first.first ]];
			},
			\ignoreBundleTime, {
				ggIgnoreBundleTime.value = ignoreBundleTime;
			}
			);
		});

		this.prAddFunc;
				
		win.onClose = {
			win = nil;
			updThis.remove;
			if( recording, {
				ggStop.doAction;
			});
			if( disposeOnClose, { this.dispose });
		};
		win.front;
	}
	
	prAddFunc {
		if( addedFunc.not, {
			addedFunc = true;
			thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.addFunc( funcOSC );
		});
	}
		
	prRemoveFunc {
		if( addedFunc, {
			addedFunc = false;
			thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.removeFunc( funcOSC );
		});
	}
	
	basePath_ { arg str;
		this.stop;
		basePath		= str;
		this.tryChanged( \basePath, basePath );
//		this.offset	= if( synced, { osc.startTime }, 0.0 );
	}
	
	offset_ { arg value;
		this.stop;
		if( offset != value, {
			offset = value;
			this.tryChanged( \offset, offset );
		});
	}
	
	oscKey_ { arg value;
		oscKey = value;
		this.tryChanged( \oscKey, oscKey );
	}
	
	ignoreBundleTime_ { arg bool;
		if( bool != ignoreBundleTime, {
			ignoreBundleTime = bool;
			this.tryChanged( \ignoreBundleTime, bool );
		});
	}
	
	synced_ { arg bool;
		if( synced != bool, {
			if( bool, {
				doc = Bosque.default.session;
				updTransport = UpdateListener.newFor( doc.transport, { arg upd, transport, what, param1;
					switch( what,
					\play, {
						this.rec( param1 / doc.timeline.rate + offset );
					},
					\stop, {
						this.stop;
					},
					\pause, {
//						this.stop;
					},
					\resume, {
						this.stop;
						this.rec( param1 / doc.timeline.rate + offset );
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
		this.prRemoveFunc;
		if( win.notNil, { win.close; win = nil });
		this.stop;
		this.synced = false;
	}

	rec { arg pos = 0;
		if( recording, {
			this.stop;
		});
				
		path = basePath +/+ (Date.getDate.stamp ++ ".osc");		this.tryChanged( \path, path );
//		this.offset = pos;
		recStartTime = SystemClock.seconds - pos;
		oscRec = FileNetAddr.openWrite( path );
		if( oscRec.file.isOpen, {
			recording = true;
			this.addFunc;
			this.tryChanged( \rec, pos );
		}, {
			oscRec = nil;
			("BosqueOSCRecorder:rec - cannot open file '" ++ path ++ "'for writing").error;
		});
	}

	stop {
//		[ "oscRec", oscRec ].postln;
		if( oscRec.notNil, {
			protect {
				oscRec.closeFile;
			} {
				oscRec = nil;
				recording = false;
				this.tryChanged( \stop );
			};
		});
	}
}