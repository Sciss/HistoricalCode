s.options.device="Mobile I/O 2882 [2600]";
s.options.numOutputBusChannels=18;
s.boot;

(
f = File( "/Users/rutz/Desktop/Forest2/audio_work/BigStone8_marks.txt", "r" );
~pos = f.readAllString.interpret;
f.close;
~reg = Array( ~pos.size.div(2) ); ~pos.pairsDo({ arg beg, end; ~reg.add( Span( beg, end ))});
)

(
	SynthDef( \playBuf, {Êarg bufnum, out = 0, amp = 1; var player = PlayBuf.ar( 2, bufnum ); FreeSelfWhenDone.kr( player ); Out.ar( out, player * amp )}).send( s );
)

(
	var gui, win, currentSpan, currentGroup, fUpdateGroup, ggSpans, ggGroup, synthGroup, numGroups = 6;

	~groups = ~groups ?? { Array.fill( numGroups, {ÊList.new })};

	gui = GUI.get( \swing );
	win = gui.window.new( "Edit", Rect( 0, 0, 500, 340 ));
	ScissUtil.positionOnScreen( win );
	win.view.resize=5;

	synthGroup = Group( s );
	
	ggSpans = gui.listView.new( win, Rect( 4, 4, 200, 300 ))
		.resize_( 4 )
		.allowsDeselection_( true )
		.items_( ~reg.collect({ arg span; (span.start / 44100).asTimeString ++ " - " ++ (span.stop / 44100).asTimeString }))
		.action_({ arg b;
			currentSpan = ~reg[ b.value ];
		});
//	currentSpan = ~reg.first;
	
	gui.button.new( win, Rect( 208, 4, 80, 20 ))
		.states_([[ "Play" ]])
		.canFocus_( false )
		.action_({ arg b; var span = currentSpan;
			if( span.notNil, {
				Buffer.read( s, "/Users/rutz/Desktop/Forest2/audio_work/BigStone8.aif",
					span.start, span.getLength, {Êarg buf; var synth;
					
					synthGroup.freeAll;
					synth = Synth.basicNew( \playBuf, s );
					UpdateListener.newFor( synth, {Êarg upd, obj, what; if( what === \n_end, {
//						"--end".postln;
						buf.close; buf.free;
					})});
					
					NodeWatcher.newFrom( s ).register( synth );
					s.listSendMsg( synth.newMsg( synthGroup, [ \bufnum, buf.bufnum, \out, 6 ]));
				});
			});
		});

	gui.button.new( win, Rect( 208, 28, 80, 20 ))
		.states_([[ "Stop" ]])
		.canFocus_( false )
		.action_({ arg b; synthGroup.freeAll });
	
	gui.popUpMenu.new( win, Rect( 292, 4, 80, 24 ))
		.allowsReselection_( true )
		.items_( Array.fill( numGroups, { arg idx; (idx+1).asString }))
		.canFocus_( false )
		.action_({ arg b;
			currentGroup = ~groups[ b.value ];
			fUpdateGroup.value;
		});
	
	ggGroup = gui.listView.new( win, Rect( 292, 32, 200, 270 ))
		.resize_( 4 )
		.allowsDeselection_( true )
		.action_({ arg b;
			if( b.value.notNil, {
				currentSpan = currentGroup[ b.value ];
				ggSpans.value = ~reg.indexOf( currentSpan );
			});
		});
		
	fUpdateGroup = {
		if( currentGroup.notNil, {
			ggGroup.value = nil;
			ggGroup.items = currentGroup.collect({ arg span; (span.start / 44100).asTimeString ++ " - " ++ (span.stop / 44100).asTimeString });
		}, {
			ggGroup.items = nil;
		});
	};
		
	numGroups.do({ arg idx;
		gui.button.new( win, Rect( 208, 76 + (24 * idx), 80, 20 ))
			.states_([[ "> " ++ (idx+1), Color.black, Color.hsv( idx/numGroups, 1, 1 )]])
			.canFocus_( false )
			.action_({ arg b; var wasInGroup;
				if( currentSpan.notNil, {
					wasInGroup = currentGroup.notNil and: { currentGroup.includes( currentSpan )};
					~groups.do({ arg group, idx2;
						if( idx == idx2, { group.add( currentSpan )}, { group.remove( currentSpan )});
					});
					if( (idx == ~groups.indexOf( currentGroup )) || wasInGroup, {
						fUpdateGroup.value;
					});
				});
			});
	});
	
//	win.userCanClose = false;
	win.front;
	
	w = win;
)

w.close;

~reg.do({ arg span; var ok = false; ~groups.do({ arg group; ok = ok ||Êgroup.includes( span )}); if( ok.not, { span.postln })}); nil;	// check if some spans haven't been assigned


//~gaga = ~groups.deepCopy;

// store marker positions as compile string
f = File( "~/Desktop/Forest2/audio_work/BigStone8_marks2.txt".standardizePath, "w" );
f.write( ~groups.asCompileString );
f.close;
