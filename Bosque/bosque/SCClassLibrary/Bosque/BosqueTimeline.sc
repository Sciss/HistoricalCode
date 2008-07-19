/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 18-Jul-08
 */
BosqueTimeline : Object {
	var <span;
	var <rate;
	var <java;

	*new { arg rate, span;
		^super.new.prInit( rate, span );
	}
	
	prInit { arg argRate, argSpan;
		var swing;
		
		rate			= argRate ? 1000.0;
		span			= argSpan ??  {ÊSpan.new };
		swing		= Bosque.default.swing;
		java			= JavaObject( "de.sciss.timebased.timeline.BasicTimeline", swing, rate, span );
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
			java.setSpan( java, span );
		});
	}

	rate_ { arg newRate;
		if( newRate != rate, {
			rate = newRate;
			this.changed( \rate, rate );
			java.setRate( java, rate );
		});
	}

	dispose {
		java.dispose; java.destroy; java = nil;
	}

	asSwingArg {
		^java.asSwingArg;
	}
}