/**
 *	NuagesTRecorder
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTRecorder {
	var <nuages, rPlay, updRec, recTime, <win, <>path, file;
//	var <events;
	
	*new { arg nuages;
		^super.new.prInit( nuages );
	}
	
	prInit { arg argNuages;
		nuages	= argNuages;
		updRec	= UpdateListener({ arg upd, n, transactions, trnsTime;
			transactions.do( _.persist );
//			events = events.add( (trnsTime - recTime) -> transactions );
			file.putString( "%\n".format( ((trnsTime - recTime) -> transactions.size).asCompileString.replace( "\n", " " )));
			transactions.do({ arg trns;
				file.putString( " %\n".format( trns.asCompileString ));
			});
		}, \transactions );
	}
	
	record {
		this.stop;
//		events	= nil;
		this.prOpenForWrite;
		updRec.addTo( nuages );
		recTime	= thisThread.seconds;
	}
	
	prOpenForWrite {
		if( path.isNil, { MethodError( "No path has been specified", thisMethod ).throw });
		file = File( path, "w" );
	}

	prOpenForRead {
		if( path.isNil, { MethodError( "No path has been specified", thisMethod ).throw });
		file = File( path, "r" );
	}
	
	stop {
		if( file.notNil, {
			file.close; file = nil;
		});
		rPlay.stop; rPlay = nil;
		updRec.remove;
	}
	
	revealInFinder {
		var p;
		
		if( path.isNil, { MethodError( "No path has been specified", thisMethod ).throw });
		
		p = PathName( path );
		unixCmd( "osascript -e 'tell application \"Finder\"' -e activate -e 'open location \"file:\/\/" ++ p.pathOnly ++
			"\"' -e 'select file \"" ++ p.fileName ++ "\" of folder of the front window' -e 'end tell'" );
	}
	
	play {
		this.stop;
		this.prOpenForRead;
		rPlay = Routine({ var line, event;
			var playTime, trnsTime, numTrns, trnsStrings;
			playTime = thisThread.seconds;
//			events.do({ arg event; })
			while({ (line = file.getLine).notNil }, {
				event		= line.interpret;
				trnsTime		= event.key;
//				transactions	= event.value;
				numTrns		= event.value;
				trnsStrings	= Array.fill( numTrns, {Êfile.getLine });
				(trnsTime - (thisThread.seconds - playTime)).wait;
				nuages.taskSched( nil, \taskRedo, *trnsStrings );
			});
			this.stop;
		}).play( SystemClock );
	}
	
	makeGUI {
		var w, ggRec, ggStop, ggPlay, fnt;
		if( win.notNil, { ^win });
		w = JSCWindow( "Transaction Rec", Rect( 0, 0, 128, 40 ), resizable: false );
		fnt = JFont( "Helvetica", 8 );
		ggPlay = JSCButton( w, Rect( 4, 4, 32, 24 )).states_([[ ">" ]])
			.canFocus_( false )
			.enabled_( false )
			.action_({ arg b;
				b.enabled = false;
				ggStop.enabled = true;
				ggRec.enabled = false;
				this.play;
			});
		ggStop = JSCButton( w, Rect( 40, 4, 32, 24 )).states_([[ "[]" ]])
			.canFocus_( false )
			.enabled_( false )
			.action_({ arg b;
				b.enabled = false;
				ggPlay.enabled = true;
				ggPlay.value = 0;
				ggRec.enabled = true;
				ggRec.value = 0;
				this.stop;
			});
		ggRec = JSCButton( w, Rect( 76, 4, 32, 24 )).states_([[ "o" ]])
			.canFocus_( false )
			.action_({ arg b;
				b.enabled = false;
				ggStop.enabled = true;
				ggPlay.enabled = false;
				this.record;
			});
		ScissUtil.positionOnScreen( w, 0.5, 0.985 );
		w.front;
		win = w;
		^w;
	}
}