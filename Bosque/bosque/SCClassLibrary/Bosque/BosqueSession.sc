/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.15, 05-Sep-07
 */
BosqueSession : Object {
	var <forest;
	
	// data
	var <audioFiles;
	var <timeline;
	var <trail;
	var <tracks;
	var <busConfigs;
	var <volEnv;

	var <transport;
	var <undoManager;
	var <selectedRegions;
	var <selectedTracks;
//	var <selectedBusConfigs;
	var <audioPlayer;
	var dirty = false;
	
	var <path, <name;
	
	var trails;

	*new {
		^super.new.prInit;
	}
	
	prInit {
		forest			= Bosque.default;
		audioFiles		= BosqueSessionCollection.new;
		timeline			= BosqueTimeline( this );
		transport			= BosqueTransport( this );
		trail			= BosqueTrail.new.rate_( timeline.rate );
		volEnv			= Trail.new.rate_( timeline.rate );
		undoManager		= ScissUndoManager.new;
		selectedRegions	= BosqueSessionCollection.new;
		audioPlayer		= BosqueAudioPlayer( this );
		tracks			= BosqueSessionCollection.new;
		selectedTracks	= BosqueSessionCollection.new;
		busConfigs		= BosqueSessionCollection.new;
//		selectedBusConfigs	= BosqueSessionCollection.new;
		
		trails			= [ trail, volEnv ];
		
		UpdateListener.newFor( undoManager, { arg upd, undo, what;
			if( (what === \state) and: { undo.canUndo != dirty }, {
				dirty = undo.canUndo;
				this.changed( \dirty, dirty );
			});
		});
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
	
	doTimeline { arg func; func.value( timeline )}
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
		toDispose.do( _.dispose );
		toDispose = tracks.getAll;
		tracks.clear( this );
		toDispose.do( _.dispose );
		toDispose = busConfigs.getAll;
		busConfigs.clear( this );
		toDispose.do( _.dispose );
	}
	
	isDirty { ^dirty }

	editInsertTimeSpan { arg source, span, ce;
		trails.do({ arg t;
			t.editBegin( ce );
			t.editInsertSpan( source, span, nil, ce );
			t.editEnd( ce );
		});
		ce.addPerform( BosqueTimelineEdit.length( source, this, timeline.length + span.length ));
		if( timeline.visibleSpan.isEmpty, {
			ce.addPerform( BosqueTimelineVisualEdit.scroll( source, this, span ));
		});
	}
	
	editClearTimeSpan { arg source, span, ce, filter = true;
		trail.editBegin( ce );
		trail.editClearSpan( source, span, nil, ce, filter );
		trail.editEnd( ce );
	}

	editRemoveTimeSpan { arg source, span, ce;
		var start, stop, visiSpan, selSpan;
		if( timeline.position > span.start, {
			ce.addPerform( BosqueTimelineVisualEdit.position( source, this, max( 0, timeline.position - span.length )));
		});
		visiSpan	= timeline.visibleSpan;
		stop		= timeline.length - span.length;
		if( visiSpan.stop > span.start, {
			if( visiSpan.start < span.start, {
				if( stop < visiSpan.stop, {
					ce.addPerform( BosqueTimelineVisualEdit.scroll( source, this,
						Span( max( 0, stop - visiSpan.length ), stop )));
				});
			}, {
				start = max( 0, visiSpan.start - span.length );
				ce.addPerform( BosqueTimelineVisualEdit.scroll( source, this,
					Span( start, min( stop, start + visiSpan.length ))));
			});
		});
		selSpan = timeline.selectionSpan;
		if( selSpan.isEmpty.not and: { selSpan.stop > span.start }, {
			if( selSpan.start < span.start, {
				if( stop < selSpan.stop, {
					ce.addPerform( BosqueTimelineVisualEdit.select( source, this,
						Span( max( 0, stop - selSpan.length ), stop )));
				});
			}, {
				start = max( 0, selSpan.start - span.length );
				ce.addPerform( BosqueTimelineVisualEdit.select( source, this,
					Span( start, min( stop, start + selSpan.length ))));
			});
		});
		ce.addPerform( BosqueTimelineEdit.length( source, this, timeline.length - span.length ));
		trails.do({ arg t;
			t.editBegin( ce );
			t.editRemoveSpan( source, span, nil, ce );
			t.editEnd( ce );
		});
	}
}
