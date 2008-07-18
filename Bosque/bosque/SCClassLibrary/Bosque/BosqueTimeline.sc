/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 18-Jul-08
 */
BosqueTimeline : Object {
	var <span;
	var <rate;
	var javaBackEnd, javaNet, javaResp;

	*new { arg rate, span;
		^super.new.prInit( rate, span );
	}
	
	prInit { arg argRate, argSpan;
		var swing;
		
		rate			= argRate ? 1000.0;
		span			= argSpan ??  {ÊSpan.new };
		swing		= Bosque.default.swing;
		javaBackEnd	= JavaObject( "de.sciss.timebased.timeline.BasicTimeline", swing, rate, span );
		javaNet		= JavaObject( "de.sciss.timebased.net.NetTimeline", swing, Bosque.default.master, javaBackEnd );
		javaNet.setID( javaNet.id );
		
		javaResp = ScissOSCPathResponder( swing.addr, [ '/timeline', javaNet.id ], { arg time, resp, msg;
			select( msg[ 2 ],
			\rate, { this.rate = msg[ 3 ]},
			\span, { this.span = Span( msg[ 3 ], msg[ 4 ])}
			);
		}).add;
	}
	
//	storeModifiersOn { arg stream;
//		stream << ".rate_(";
//		rate.storeOn( stream );
//		stream << ")";
//		stream << ".length_(";
//		length.storeOn( stream );
//		stream << ")";
//		stream << ".position_(";
//		position.storeOn( stream );
//		stream << ")";
//		stream << ".visibleSpan_(";
//		visibleSpan.storeOn( stream );
//		stream << ")";
//		stream << ".selectionSpan_(";
//		selectionSpan.storeOn( stream );
//		stream << ")";
//	}
	
	span_ { arg newSpan;
		if( newSpan.equals( span ).not, {
			span = newSpan;
			this.changed( \span, span );
			javaBackEnd.setSpan( javaBackEnd, span );
		});
	}

	rate_ { arg newRate;
		if( newRate != rate, {
			rate = newRate;
			this.changed( \rate, rate );
			javaBackEnd.setRate( javaBackEnd, rate );
		});
	}

	dispose {
		javaResp.remove; javaResp = nil;
		javaNet.dispose; javaNet.destroy; javaNet = nil;
		javaBackEnd.dispose; javaBackEnd.destroy; javaBackEnd = nil;
	}

//	asSwingArg {
//		^javaBackEnd.asSwingArg;
//	}
	
	backend { ^javaBackEnd }
	net { ^javaNet }
}