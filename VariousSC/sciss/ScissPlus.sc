/*
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Changelog
 *		04-Feb-08 - not overwriting Main:recvOSCmessage any more
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.23, 15-Aug-09
 */

/**
 *	EZSlider: Adds valueAction_ and asView methods.
 *
 *	- EZSlider's value_ method behaves like valueAction_
 *	(not like expected value_). We make value_ behave like
 *	regular value_ and add valueAction_ with the expected behaviour.
 *	- asView is added for more general usability.
 *
 *	@warning	the behaviour was modified in June 2006 : value is
 *			NOT overriden any more ; instead valueNoAction_ is added!!!
 */
+ EZSlider {
//	valueNoAction_ { arg val;
//		val				= controlSpec.constrain( val );
//		sliderView.value	= controlSpec.unmap( val );
//		numberView.value	= val.round( round );
//	}
//
//	valueAction_ { arg val;
//		this.valueNoAction_( val );
//		action.value( this );
//	}

	asView { ^numberView }
}

//+ JEZSlider {
//	valueNoAction_ { arg val;
//		val				= controlSpec.constrain( val );
//		sliderView.value	= controlSpec.unmap( val );
//		numberView.value	= val.round( round );
//	}
//
//	valueAction_ { arg val;
//		this.valueNoAction_( val );
//		action.value( this );
//	}
//
//	asView { ^numberView; }
//}

/**
 *	Allows to debug incoming OSC by adding a dependant to
 *	Main waiting for 'osc' changes.
 *
 *	@author	Hanns Holger Rutz
 *	@version	10-Sep-06
 */
//+ Main {
//	recvOSCmessage { arg time, replyAddr, msg;
//		var handled;
//		recvOSCfunc.value( time, replyAddr, msg );
//		handled = OSCresponder.respond( time, replyAddr, msg );
//		Main.changed( \osc, time, replyAddr, msg, handled );
//	}
//}

+ Int8Array {
	hexdump { arg offset = 0, length;
		var txt, stream, stop, j, k, m, n, hex;
		
		hex		= "0123456789abcdef";
		stop		= offset + (length ?? { this.size - offset });
		txt		= String.newClear( 78 ); // automatically filled with 0x32 !
		txt[ 60 ]	= $|;
		^String.streamContents({ arg stream;
			while({ offset < stop }, {
				txt[ 0 ]		= hex[ (offset >> 24) & 0xF ];
				txt[ 1 ]		= hex[ (offset >> 20) & 0xF ];
				txt[ 2 ]		= hex[ (offset >> 16) & 0xF ];
				txt[ 3 ]		= hex[ (offset >> 12) & 0xF ];
				txt[ 4 ]		= hex[ (offset >> 12) & 0xF ];
				txt[ 5 ]		= hex[ (offset >> 8) & 0xF ];
				txt[ 6 ]		= hex[ (offset >> 4) & 0xF ];
				txt[ 7 ]		= hex[ offset & 0xF ];
				j			= 8;
				m			= 61;
				k			= 0;
				while({ (k < 16) and: { offset < stop }}, {
					j		= j + if( (k & 7) == 0, 2, 1 );
					n		= this.at( offset );
					txt[ j ]	= hex[ (n >> 4)  & 0xF ];
					j		= j + 1;
					txt[ j ]	= hex[ n & 0xF ];
					j		= j + 1;
					txt[ m ]	= if( (n > 0x1F) and: { n < 0x7F }, n.asAscii, $. );
					m		= m + 1;
					k		= k + 1;
					offset	= offset + 1;
				});
				txt[ m ]		= $|;
				m			= m + 1;
				while({ j < 58 }, {
					txt[ j ]	= $ ;
					j		= j + 1;
				});
				while({ m < 78 }, {
					txt[ m ]	= $ ;
					m		= m + 1;
				});
				stream << txt;
				stream.nl;
			});
		});
	}
}

+ UnixFILE {
	getString0 {
		var c, str = "", n = 0.asAscii;
		while({ (c = this.next) != n }, { str = str ++ c });
		^str;
	}
}

+ Condition {
	waitTimeOut { arg timeout = 0.0;
		var cancel;
		if( timeout > 0.0, {
			cancel = Task({
				timeout.wait;
				cancel = nil;
				this.unhang;
			}, SystemClock );
			cancel.start;
		});
		this.wait;
		cancel.stop;
	}
}

+ SequenceableCollection {
	tripletsDo { arg function;
		forBy( 0, this.size - 3, 3, { arg i;
			function.value( this[ i ], this[ i+1 ], this[ i+2 ], i );
		});
	}
}

+ List {
	tripletsDo { arg function;
		array.tripletsDo( function );
	}
}

+ SynthDef {
	recvMsg { arg completionMsg;
		^[ "/d_recv", this.asBytes, completionMsg ];
	}
}

//+ Main {
//	run {
//		if( ScissUtil.runPath.notNil, {
//			ScissUtil.runPath.load;
//		});
//	}
//}

+ Object {
	tryChanged { arg ... args;
		dependantsDictionary.at( this ).copy.do({ arg item;
			try {
				item.update( this, *args );
			} { arg e;
				e.reportError;
			};
		});
	}
	
	// like 'while', but with the condition
	// checked at the end of the first loop, so
	// like a do {Ê} while statement in java.
	// note how the receiver and argument are swapped!
	doWhile { arg test;
		this.value;
		^test.while( this );
	}
}

+ Node {
	onEnd_ { arg func;
		UpdateListener.newFor( this.register, { arg upd, node;
			upd.remove;
			func.value( node );
		}, \n_end );
	}
	
	waitForEnd {
		var cond;
		cond = Condition.new;
		this.onEnd = { cond.test = true; cond.signal };
		cond.wait;
	}
}
