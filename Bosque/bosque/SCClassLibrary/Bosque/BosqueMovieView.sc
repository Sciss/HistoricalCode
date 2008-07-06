/**
 *	(C)opyright 2007-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 31-Aug-07
 */
BosqueMovieView {
	var win, view;
	var <path;
	var <synced = false;
	var <offset = 0.0;
	var doc, updTransport, updTimeline;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
	}
	
	makeGUI { arg path;
		var fntSmall, ggSync, ggOffset, gui, updThis;
		
		if( win.notNil, { ^this });

		gui = GUI.get( \cocoa );
		win = gui.window.new( "Movie", Rect( 0, 0, 324, 288 ));
		ScissUtil.positionOnScreen( win, 0.2, 0.8 );

		fntSmall	= gui.font.new( if( gui.id === \cocoa, "Lucida Grande", "Lucida Grande" ), 10 );

		gui.button.new( win, Rect( 2, 2, 22, 22 ))
			.font_( fntSmall )
			.canFocus_( false )
			.states_([[ "..." ]])
			.action_({ arg b;
				File.openDialog( "Select Movie", { arg path; view.path = path });
			});

		gui.staticText.new( win, Rect( 28, 2, 60, 22 )).font_( fntSmall ).align_( \right )
			.string_( "Offset:" );
		ggOffset = gui.textField.new( win, Rect( 92, 2, 100, 22 )).font_( fntSmall )
			.string_( ScissUtil.toTimeString( this.offset ))
			.action_({ arg b;
				this.offset = ScissUtil.fromTimeString( b.string );
			});
		ggSync = gui.button.new( win, Rect( 196, 2, 22, 22 )).canFocus_( false )
			.font_( fntSmall )
//			.states_([[ "" ++ 0xE2.asAscii ++ 0x86.asAscii ++ 0x82.asAscii ]]) // Triangle pointing to the right
			.states_([[ "S" ]]) // Triangle pointing to the right
			.action_({ arg b;
				this.synced = this.synced.not;
			});

		view = gui.movieView.new( win, win.view.bounds.insetAll( 2, 30, 2, 2 ))
			.resize_( 5 )
//			.path_( path )
			.muted_( true );
					
		updThis = UpdateListener.newFor( this, { arg upd, me, what, param1;
			switch( what,
			\path, {
				view.path			= this.path;
			},
			\synced, {
//				ggPlay.enabled	= this.synced.not;
//				ggStop.enabled	= this.synced.not;
				ggSync.states		= [[ ggSync.states.first.first ] ++ if( synced, [ Color.white, Color.blue ])];
				if( gui.id === \cocoa, { ggSync.refresh });
			},
			\offset, {
				ggOffset.string	= ScissUtil.toTimeString( this.offset );
			},
			\play, {
				{ view.currentTime	= param1;
				  view.start; }.defer;
			},
			\stop, {
				{ view.stop; }.defer;
			},
			\position, {
				{ view.currentTime	= param1;
				  view.stop; }.defer;	// sucky SCMovieView starts playback when setting the time
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
		path			= str;
		this.changed( \path, path );
	}
	
	offset_ { arg value;
		this.stop;
		if( offset != value, {
			offset = value;
			this.changed( \offset, offset );
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
						this.stop;
					},
					\resume, {
						this.play( param1 / doc.timeline.rate + offset );
					});
				});
				updTimeline = UpdateListener.newFor( doc.timeline, { arg upd, timeline, what, param1;
					switch( what,
					\position, {
						this.position = param1 / doc.timeline.rate + offset;
					},
					\rate, {
						this.position = param1 / doc.timeline.rate + offset;
					});
				});
			}, {
				if( updTransport.notNil, {
					updTransport.remove;
					updTransport	= nil;
				});
				if( updTimeline.notNil, {
					updTimeline.remove;
					updTimeline	= nil;
				});
				doc	= nil;
			});
			synced = bool;
			this.changed( \synced, synced );
		});
	}
	
	dispose {
		if( win.notNil, { win.close; win = nil });
		this.stop;
		this.synced = false;
	}

	play { arg pos = 0;
		this.changed( \play, pos );
	}

	stop {
		this.changed( \stop );
	}
	
	position_ { arg pos;
		this.changed( \position, pos );
	}
}