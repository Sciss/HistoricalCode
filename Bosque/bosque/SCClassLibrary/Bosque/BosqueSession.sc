/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.16, 20-Jul-08
 */
BosqueSession : Object {
	var <forest;
	
	// data
	var <audioFiles;
	var <timeline;
	var <timelineView;
	var <trail;
	var <tracks;
	var <selectedTracks;
	var <busConfigs;
	var <volEnv;

	var <transport;
	var <undoManager;
	var <selectedRegions;
//	var <selectedBusConfigs;
	var <markers, <markerTrack;
	var <audioPlayer;
	var dirty = false;
	
	var <path, <name;
	
	var trails;

	*new { arg bosque;
		^super.new.prInit( bosque );
	}
	
	prInit { arg bosque;
		bosque.session	= this;
		forest			= Bosque.default;
		audioFiles		= BosqueSessionCollection.new;
		timeline			= BosqueTimeline( 44100 );
		timelineView		= BosqueTimelineView( timeline );
		transport			= BosqueTransport( this );
		trail			= BosqueTrail.new.rate_( timeline.rate );
		volEnv			= Trail.new.rate_( timeline.rate );
		undoManager		= ScissUndoManager.new;
		selectedRegions	= BosqueSessionCollection( false );
		audioPlayer		= BosqueAudioPlayer( this );
		tracks			= BosqueSessionCollection.new;
		selectedTracks	= BosqueSessionCollection( false );
		busConfigs		= BosqueSessionCollection.new;
		markers			= BosqueTrail.new;
		markerTrack		= BosqueTrack( -1, markers ).name_( "Markers" );
		tracks.add( nil, markerTrack );
//		selectedBusConfigs	= BosqueSessionCollection.new;
		
		trails			= [ trail, volEnv ];
		
		UpdateListener.newFor( undoManager, { arg upd, undo, what;
			if( (what === \state) and: { undo.canUndo != dirty }, {
				dirty = undo.canUndo;
				this.changed( \dirty, dirty );
			});
		});
	}
	
	editPosition { arg source, pos;
		undoManager.addEdit( BosqueTimelineViewEdit.position( source, timelineView, pos ).performEdit );	
	}

	editScroll { arg source, span;
		undoManager.addEdit( BosqueTimelineViewEdit.scroll( source, timelineView, span ).performEdit );	
	}

	editSelect { arg source, span;
		undoManager.addEdit( BosqueTimelineViewEdit.select( source, timelineView, span ).performEdit );
	}

	path_ { arg string;
		path = string;
		name = if( path.notNil, { path.basename.splitext.first }, "Untitled" );
		this.changed( \path );
	}
	
	storeModifiersOn { arg stream;
		stream.nl; stream.tab;
		stream << ".doTimeline({ arg tl; tl";
		timeline.storeModifiersOn( stream );
		stream << "})";
		stream.nl; stream.tab;
		stream << ".doAudioFiles({ arg af; af";
		audioFiles.storeModifiersOn( stream );
		stream << "})";
		stream.nl; stream.tab;
		stream << ".doBusConfigs({ arg b; b";
		busConfigs.storeModifiersOn( stream );
		stream << "})";
		stream.nl; stream.tab;
		stream << ".doTracks({ arg tr; tr";
		tracks.storeModifiersOn( stream );
		stream << "})";
		stream.nl; stream.tab;
		stream << ".doTrail({ arg tr; tr";
		trail.storeModifiersOn( stream );
		stream << "})";
		stream.nl; stream.tab;
		stream << ".doVolEnv({ arg tr; tr";
		volEnv.storeModifiersOn( stream );
		stream << "})";
	}
	
	// note: this is now operating on a fake object!
	doTimeline { arg func;
		func.value( BosqueLegacityTimeline( this ))
	}
	
	doTrail { arg func; func.value( trail )}
	doAudioFiles { arg func; func.value( audioFiles )}
	doTracks { arg func; func.value( tracks )}
	doBusConfigs { arg func; func.value( busConfigs )}
	doVolEnv { arg func; func.value( volEnv )}
	
	clear {
		var toDispose;
		transport.stop;
		this.path = nil;
		forest.clipBoard.clear;
		undoManager.discardAllEdits;
		selectedRegions.clear( this );
		selectedTracks.clear( this );
//		selectedBusConfigs.clear( this );
		trail.clear( this );
		volEnv.clear( this );
		timeline.clear;
		toDispose = audioFiles.getAll;
		audioFiles.clear( this );
		toDispose.do({ arg x; x.dispose });
		toDispose = tracks.getAll;
		tracks.clear( this );
		toDispose.do({ arg x; x.dispose });
		toDispose = busConfigs.getAll;
		busConfigs.clear( this );
		toDispose.do({ arg x; x.dispose });
	}
	
	isDirty { ^dirty }

	editInsertTimeSpan { arg source, span, ce;
		trails.do({ arg t;
			t.editBegin( ce );
			t.editInsertSpan( source, span, nil, ce );
			t.editEnd( ce );
		});
		ce.addPerform( BosqueTimelineEdit.span( source, this, timeline.span.replaceStop( timeline.span.stop + span.length )));
		if( timelineView.span.isEmpty, {
			ce.addPerform( BosqueTimelineViewEdit.scroll( source, timelineView, span ));
		});
	}
	
	editClearTimeSpan { arg source, span, ce, filter = true;
		trail.editBegin( ce );
		trail.editClearSpan( source, span, nil, ce, filter );
		trail.editEnd( ce );
	}

	editRemoveTimeSpan { arg source, span, ce;
		var start, stop, visiSpan, selSpan;
		if( timelineView.cursor.position > span.start, {
			ce.addPerform( BosqueTimelineViewEdit.position( source, timelineView, max( timeline.span.start, timelineView.cursor.position - span.length )));
		});
		visiSpan	= timelineView.span;
		stop		= timeline.span.length - span.length;
		if( visiSpan.stop > span.start, {
			if( visiSpan.start < span.start, {
				if( stop < visiSpan.stop, {
					ce.addPerform( BosqueTimelineViewEdit.scroll( source, timelineView,
						Span( max( 0, stop - visiSpan.length ), stop )));
				});
			}, {
				start = max( 0, visiSpan.start - span.length );
				ce.addPerform( BosqueTimelineViewEdit.scroll( source, timelineView,
					Span( start, min( stop, start + visiSpan.length ))));
			});
		});
		selSpan = timelineView.selection.span;
		if( selSpan.isEmpty.not and: { selSpan.stop > span.start }, {
			if( selSpan.start < span.start, {
				if( stop < selSpan.stop, {
					ce.addPerform( BosqueTimelineViewEdit.select( source, timelineView,
						Span( max( 0, stop - selSpan.length ), stop )));
				});
			}, {
				start = max( 0, selSpan.start - span.length );
				ce.addPerform( BosqueTimelineViewEdit.select( source, timelineView,
					Span( start, min( stop, start + selSpan.length ))));
			});
		});
		ce.addPerform( BosqueTimelineEdit.span( source, this, timeline.span.replaceStop( timeline.span.stop - span.length )));
		trails.do({ arg t;
			t.editBegin( ce );
			t.editRemoveSpan( source, span, nil, ce );
			t.editEnd( ce );
		});
	}
}
