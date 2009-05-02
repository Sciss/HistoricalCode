/**
 *	(C)opyright 2006-2007 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	A SuperCollider implementation of the java class de.sciss.timebased.BasicTrail
 *
 *	Class dependancies: Span, Stake, AbstractUndoableEdit, EventManager, IOException
 *
 *	Changelog:
 *		14-Sep-06		a couple of bug fixes
 *		10-Aug-07		re-synced with java version (performable edits)
 *
 *	@version	0.15, 08-Aug-08
 *	@author	Hanns Holger Rutz
 *
 *	@todo	asCompileString
 */
Trail {
	classvar <kTouchNone		= 0;
	classvar <kTouchSplit		= 1;
	classvar <kTouchResize		= 2;

	classvar	<>kDebug			= false;
	classvar	startComparator;
	classvar	stopComparator;
	
	var		collStakesByStart;
	var		collStakesByStop;
	var		collEditByStart;
	var		collEditByStop;
	var		currentEdit;

	var		<rate;
	
	var		elm				= nil;	// lazy creation
	var		dependants		= nil;	// lazy creation
	
	var		touchMode;

	*initClass {
		startComparator	= { arg o1, o2;

			if( o1.respondsTo( \getSpan ), { o1 = o1.span; });
			if( o2.respondsTo( \getSpan ), { o2 = o2.span; });
			
			Span.startComparator.value( o1, o2 );
		};

		stopComparator	= { arg o1, o2;

			if( o1.respondsTo( \getSpan ), { o1 = o1.span; });
			if( o2.respondsTo( \getSpan ), { o2 = o2.span; });
			
			Span.stopComparator.value( o1, o2 );
		};
	}
	
	*new { arg touchMode = kTouchNone;
		^super.new.prInitTrail( touchMode );
	}
	
	prInitTrail { arg argTouchMode;
		touchMode			= argTouchMode;
		collStakesByStart	= List.new;
		collStakesByStop	= List.new;
	}
	
	storeArgs { ^[ touchMode ]}

	storeModifiersOn { arg stream;
		stream << ".rate_(";
		rate.storeOn( stream );
		stream << ")";
		stream << ".addAll(this,";
		stream.nl; stream.tab;
		collStakesByStart.array.storeOn( stream );
		stream << ")";		
	}
	
	getRate {
		^rate;
	}
	
	setRate { arg newRate;
		rate	= newRate;
	}

	// synonym
	rate_ { arg newRate;
		rate	= newRate;
	}

	clear { arg source;
		var wasEmpty, span, stake;
		
		wasEmpty	= this.isEmpty;
		span		= this.span;
	
		while({ collStakesByStart.isEmpty.not }, {
			stake = collStakesByStart.removeAt( 0 );
			stake.setTrail( nil );
			this.protStakeRemoved( stake );
		});
		collStakesByStop.clear;

		// ____ dep ____
		dependants.do( _.clear( source ));

		if( source.notNil && wasEmpty.not, {
			this.prDispatchModification( source, span );
		});
	}

	dispose {
		// ____ dep ____
		dependants.do( _.dispose );

		collStakesByStart.do( _.dispose );
	
		collStakesByStart.clear;
		collStakesByStop.clear;

	}
	
	prEditGetCollByStart { arg ce;
		^if( ce.isNil or: { collEditByStart.isNil }, collStakesByStart, collEditByStart );
	}

	prEditGetCollByStop { arg ce;
		^if( ce.isNil or: { collEditByStop.isNil }, collStakesByStop, collEditByStop );
	}

	getSpan {
		^this.editGetSpan;
	}
	
	// synonym
	span {
		^this.editGetSpan;
	}
	
	editGetSpan { arg ce;
		^Span( this.prEditGetStart( ce ), this.prEditGetStop( ce ));
	}
	
	prEditGetStart { arg ce;
		var coll = this.prEditGetCollByStart( ce );
		^coll.isEmpty.if( 0, { coll.first.span.start });
	}

	prEditGetStop { arg ce;
		var coll = this.prEditGetCollByStop( ce );
		^coll.isEmpty.if( 0, { coll.last.span.stop });
	}

	editBegin { arg ce;
		if( currentEdit.notNil, { MethodError( "Concurrent editing", thisMethod ).throw });

		currentEdit	= ce;
		collEditByStart	= nil;		// dispose ? XXXX
		collEditByStop	= nil;		// dispose ? XXXX

		// ____ dep ____
		dependants.do( _.editBegin( ce ));
	}
	
	editEnd { arg ce;
		this.prCheckEdit( ce );
		currentEdit		= nil;
		collEditByStart	= nil;
		collEditByStop	= nil;
		
		// ____ dep ____
		dependants.do( _.editEnd( ce ));
	}
	
	prCheckEdit { arg ce;
		if( currentEdit.isNil, { MethodError( "Missing editBegin", thisMethod ).throw });
		if( ce != currentEdit, { MethodError( "Concurrent editing", thisMethod ).throw });
	}
	
	prEnsureEditCopy {
		if( collEditByStart.isNil, {
			collEditByStart	= collStakesByStart.copy;
			collEditByStop	= collStakesByStop.copy;
		});
	}
	
	prBinarySearch { arg coll, newObject, function;
		var index;
		var low	= 0;
		var high	= coll.size - 1;

//var cnt=0;
		
//fork { ("coll "++coll++"; newObject "++newObject++"; function "++function).postln; };
//if( true, { ^-1; });
		
		while({ 
			index  = (high + low) div: 2;
//cnt = cnt + 1;
//if( cnt > 10, {
//	("FUCK! low = "++low++"; high = "++high++"; index = "++index).error;
//	^-1;
//});
//			low   <= high;
			low   <= high;
		}, {
//("compare:  coll.at( "++index++" ) = "++coll.at( index ) ++ "; newObject = "++newObject).inform;
//try {
			switch( function.value( coll.at( index ), newObject ),
			0, { ^index; },
			-1, {
				low = index + 1;
			},
			1, {
				high = index - 1;
			},
			{
				"Illegal result from comparator".error;
				^-1;
			});
//} { arg error; error.postln; ("... failed for '"++coll.at( index )++"' vs '" ++ newObject ++ "'").postln };
		});
		^(low.neg - 1);	// as in java.util.Collections.binarySearch !
	}
	
	getRange { arg span, byStart = true;
		^this.editGetRange( span, byStart );
	}
	
	editGetRange { arg span, byStart = true, ce, filter = true;
		var collByStart, collByStop, collUntil, collFrom, collResult, idx;
		
		if( ce.isNil, {
			collByStart	= collStakesByStart;
			collByStop	= collStakesByStop;
		}, {
			this.prCheckEdit( ce );
			collByStart	= collEditByStart ? collStakesByStart;
			collByStop	= collEditByStop ? collStakesByStop;
		});

		// "If the list contains multiple elements equal to the specified object,
		//  there is no guarantee which one will be found"
		idx			= this.prBinarySearch( collByStart, span.stop, startComparator );

		if( idx < 0, {
			idx		= (idx + 1).neg;
		}, {
			idx		= this.editGetRightMostIndex( idx, true, ce ) + 1;
		});
//		collUntil		= collByStart.subList( 0, idx );
		collUntil		= collByStart.copyFromStart( idx - 1 );
		idx			= this.prBinarySearch( collByStop, span.start, stopComparator );

		if( idx < 0, {
			idx		= (idx + 1).neg;
		}, {
			idx		= this.editGetLeftMostIndex( idx, false, ce );
		});
//		collFrom	= collByStop.subList( idx, collStakesByStop.size() );
		collFrom	= collByStop.copyToEnd( idx );

		// XXX should be optimized ; NOTE: indexOf is a lot faster than includes !!!
		collResult	= collUntil.select({ arg stake; collFrom.indexOf( stake ).notNil and: filter.value( stake )});

		^collResult;
	}

//	insert { arg source, span, ce;
//
//		this.insert( source, span, this.getDefaultTouchMode, ce );
//	}
	
	insertSpan { arg source, span, touchMode;
		^this.editInsertSpan( source, span, touchMode );
	}

	editInsertSpan { arg source, span, touchMode, ce;
		var start, stop, totStop, delta, collRange, collToAdd, collToRemove, modSpan, stake, stakeSpan;

		touchMode		= touchMode ?? { this.getDefaultTouchMode };

		start		= span.start;
		stop			= span.stop;
		totStop		= this.prEditGetStop( ce );
		delta		= span.length;
		
		if( (delta == 0) || (start > totStop), { ^this; });
		
		collRange		= this.editGetRange( Span( start, totStop ), true, ce );
		
		if( collRange.isEmpty, { ^this; });
		
		collToAdd		= List.new;
		collToRemove	= List.new;
		
		switch( touchMode,
		kTouchNone, {
			// XXX could use binarySearch ?
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.start >= start, {
					collToRemove.add( stake );
					collToAdd.add( stake.shiftVirtual( delta ));
				});
			});
		},
			
		kTouchSplit, {
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.stop > start, {
					collToRemove.add( stake );

					if( stakeSpan.start >= start, {			// not splitted
						collToAdd.add( stake.shiftVirtual( delta ));
					}, {
						collToAdd.add( stake.replaceStop( start ));
						stake = stake.replaceStart( start );
						collToAdd.add( stake.shiftVirtual( delta ));
						stake.dispose;	// delete temp product
					});
				});
			});
		},
			
		kTouchResize, {
"BasicTrail.insert, touchmode resize : not tested".warn;
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.stop > start, {
					collToRemove.add( stake );
					if( stakeSpan.start > start, {
						collToAdd.add( stake.shiftVirtual( delta ));
					}, {
						collToAdd.add( stake.replaceStop( stakeSpan.stop + delta ));
					});
				});
			});
		},
		
		// default:
		{
			MethodError( "Illegal Argument TouchMode : " ++ touchMode, thisMethod ).throw;
		});

		modSpan		= Span.union( this.prRemoveAll( collToRemove, ce ), this.prAddAll( collToAdd, ce ));

		// ____ dep ____
		dependants.do( _.editInsertSpan( source, span, touchMode, ce ));

		if( source.notNil && modSpan.notNil, {
			if( ce.notNil, {
				ce.addPerform( TrailEdit.newDispatch( this, modSpan ));
			}, {
				this.prDispatchModification( source, modSpan );
			});
		});
	}

//	remove { arg source, span, ce )
//	{
//		this.remove( source, span, this.getDefaultTouchMode, ce );
//	}

	removeSpan { arg source, span, touchMode;
		 ^this.editRemoveSpan( source, span, touchMode );
	}

	/**
	 *	Removes a time span from the trail. Stakes that are included in the
	 *	span will be removed. Stakes that begin after the end of the removed span,
	 *	will be shifted to the left by <code>span.getLength()</code>. Stakes whose <code>stop</code> is
	 *	<code>&lt;=</code> the start of removed span, remain unaffected. Stakes that intersect the
	 *	removed span are traited according to the <code>touchMode</code> setting:
	 *	<ul>
	 *	<li><code>kTouchNone</code></li> : intersecting stakes whose <code>start</code> is smaller than
	 *		the removed span's start remain unaffected ; otherwise they are removed. This mode is usefull
	 *		for markers.
	 *	<li><code>kTouchSplit</code></li> : the stake is cut at the removed span's start and stop ; a
	 *		middle part (if existing) is removed ; the left part (if existing) remains as is ; the right part
	 *		(if existing) is shifted by <code>-span.getLength()</code>. This mode is usefull for audio regions.
	 *	<li><code>kTouchResize</code></li> : intersecting stakes whose <code>start</code> is smaller than
	 *		the removed span's start, will keep their start position ; if their stop position lies within the
	 *		removed span, it is truncated to the removed span's start. if their stop position exceeds the removed
	 *		span's stop, the stake's length is shortened by <code>-span.getLength()</code> . 
	 *		intersecting stakes whose <code>start</code> is greater or equal to the
	 *		the removed span's start, will by shortened by <code>(removed_span_stop - stake_start)</code> and
	 *		shifted by <code>-span.getLength()</code> . This mode is usefull for marker regions.
	 *	</ul>
	 *
	 *	@param	source		source object for event dispatching (or <code>null</code> for no dispatching)
	 *	@param	span		the span to remove
	 *	@param	touchMode	the way intersecting staks are handled (see above)
	 *	@param	ce			provided to make the action undoable ; may be <code>null</code>. if a
	 *						<code>CompoundEdit</code> is provided, disposal of removed stakes is deferred
	 *						until the edit dies ; otherwise (<code>ce == null</code>) removed stakes are
	 *						immediately disposed.
	 */
	editRemoveSpan { arg source, span, touchMode, ce;
		var start, stop, totStop, delta, collRange, collToAdd, collToRemove, modSpan, stake, stakeSpan;

		touchMode		= touchMode ?? { this.getDefaultTouchMode; };

		start		= span.start;
		stop			= span.stop;
		totStop		= this.prEditGetStop( ce );
		delta		= span.length.neg;
		
		if( (delta == 0) || (start > totStop), { ^this; });
		
		collRange		= this.editGetRange( Span( start, totStop ), true, ce );
		
		if( collRange.isEmpty, { ^this; });
		
		collToAdd		= List.new;
		collToRemove	= List.new;
		
		switch( touchMode,
		kTouchNone, {
			// XXX could use binarySearch ?
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.start >= start, {
	
					collToRemove.add( stake );
	
					if( stakeSpan.start >= stop, {
						collToAdd.add( stake.shiftVirtual( delta ));
					});
				});
			});
		},
			
		kTouchSplit, {
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.stop > start, {
					
					collToRemove.add( stake );
	
					if( stakeSpan.start >= start, {			// start portion not splitted
						if( stakeSpan.start >= stop, {		// just shifted
							collToAdd.add( stake.shiftVirtual( delta ));
						}, { if( stakeSpan.stop > stop, {	// stop portion splitted (otherwise completely removed!)
							stake = stake.replaceStart( stop );
							collToAdd.add( stake.shiftVirtual( delta ));
							stake.dispose;	// delete temp product
						})});
					}, {
						collToAdd.add( stake.replaceStop( start ));	// start portion splitted
						if( stakeSpan.stop > stop, {			// stop portion splitted
							stake = stake.replaceStart( stop );
							collToAdd.add( stake.shiftVirtual( delta ));
							stake.dispose;	// delete temp product
						});
					});
				});
			});
		},
			
		kTouchResize, {
"BasicTrail.remove, touchmode resize : not tested".warn;
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.stop > start, {
					
					collToRemove.add( stake );
	
					if( stakeSpan.start >= start, {			// start portion not modified
						if( stakeSpan.start >= stop, {			// just shifted
							collToAdd.add( stake.shiftVirtual( delta ));
						}, { if( stakeSpan.stop > stop, {	// stop portion splitted (otherwise completely removed!)
							stake = stake.replaceStart( stop );
							collToAdd.add( stake.shiftVirtual( delta ));
							stake.dispose;	// delete temp product
						})});
					}, {
						if( stakeSpan.stop <= stop, {
							collToAdd.add( stake.replaceStop( start ));
						}, {
							collToAdd.add( stake.replaceStop( stakeSpan.stop + delta ));
						});
					});
				});
			});
		},
			
		// default:
		{
			MethodError( "Illegal Argument TouchMode : " ++ touchMode, thisMethod ).throw;
		});

if( kDebug, {
	(this.class.name ++ " : removing : ").inform;
	collToRemove.do({ arg stake;
		("  span "++stake.span).inform;
	});
	" : adding : ".inform;
	collToAdd.do({ arg stake;
		("  span "++stake.span).inform;
	});
});
		modSpan		= Span.union( this.prRemoveAll( collToRemove, ce ), this.prAddAll( collToAdd, ce ));

		// ____ dep ____
		dependants.do( _.editRemoveSpan( source, span, touchMode, ce ));

		if( source.notNil && modSpan.notNil, {
			if( ce.notNil, {
				ce.addPerform( TrailEdit.newDispatch( this, modSpan ));
			}, {
				this.prDispatchModification( source, modSpan );
			});
		});
	}

//	clear { arg source, span, ce;
//		this.clear( source, span, this.getDefaultTouchMode, ce );
//	}

	clearSpan { arg source, span, touchMode;
		^this.editClearSpan( source, span, touchMode );
	}
	
	editClearSpan { arg source, span, touchMode, ce, filter = true;
		var start, stop, collRange, collToAdd, collToRemove, modSpan, stake, stakeSpan;

		touchMode		= touchMode ?? { this.getDefaultTouchMode; };

		start		= span.start;
		stop			= span.stop;
		collRange		= this.editGetRange( span, true, ce, filter );
		
//		[ "editClearnSpan, candidates: ", collRange ].postcs;
		
		if( collRange.isEmpty, { ^this });
		
		collToAdd		= List.new;
		collToRemove	= List.new;
		
		switch( touchMode,
		kTouchNone, {
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.start >= start, {
					collToRemove.add( stake );
				});
			});
		},
			
		kTouchSplit, {
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.stop > start, {
					
					collToRemove.add( stake );
	
					if( stakeSpan.start >= start, {			// start portion not splitted
						if( stakeSpan.stop > stop, {			// stop portion splitted (otherwise completely removed!)
							collToAdd.add( stake.replaceStart( stop ));
						});
					}, {
						collToAdd.add( stake.replaceStop( start ));	// start portion splitted
						if( stakeSpan.stop > stop, {				// stop portion splitted
							collToAdd.add( stake.replaceStart( stop ));
						});
					});
				});
			});
		},
			
		kTouchResize, {
"BasicTrail.clear, touchmode resize : not tested".warn;
			collRange.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.stop > start, {
					
					collToRemove.add( stake );
	
					if( stakeSpan.start >= start, {		// start portion not modified
						if( stakeSpan.stop > stop, {		// stop portion splitted (otherwise completely removed!)
							collToAdd.add( stake.replaceStart( stop ));
						});
					}, {
						if( stakeSpan.stop <= stop, {
							collToAdd.add( stake.replaceStop( start ));
						}, {
							collToAdd.add( stake.replaceStop( stakeSpan.stop - span.length ));
						});
					});
				});
			});
		},
			
		// default:
		{
			MethodError( "Illegal Argument TouchMode : " ++ touchMode, thisMethod ).throw;
		});

if( kDebug, {
	(this.class.name ++ " : removing : ").inform;
	collToRemove.do({ arg stake;
		("  span "++stake.span).inform;
	});
	" : adding : ".inform;
	collToAdd.do({ arg stake;
		("  span "++stake.span).inform;
	});
});
		modSpan		= Span.union( this.prRemoveAll( collToRemove, ce ), this.prAddAll( collToAdd, ce ));

		// ____ dep ____
		dependants.do( _.removeSpan( source, span, touchMode, ce ));

		if( source.notNil && modSpan.notNil, {
			if( ce.notNil, {
				ce.addPerform( TrailEdit.newDispatch( this, modSpan ));
			}, {
				this.prDispatchModification( source, modSpan );
			});
		});
	}

	getCuttedTrail { arg span, touchMode, shiftVirtual = 0;
		var trail, stakes;
	
		touchMode	= touchMode ?? { this.getDefaultTouchMode; };
		trail	= this.protCreateEmptyCopy;
		stakes	= this.getCuttedRange( span, true, touchMode, shiftVirtual );
		
//		trail.setRate( this.getRate() );

		trail.prEditGetCollByStart.addAll( stakes );
		stakes.do({ arg stake; this.protStakeAdded( stake )});
//		Collections.sort( stakes, startComparator );
		stakes.sort( stopComparator );
		trail.prEditGetCollByStop.addAll( stakes );
	
		^trail;
	}
	
	protStakeAdded { arg stake; }
	protStakeRemoved { arg stake; }
	
	// XXX could use this.class.new ...
	protCreateEmptyCopy {
//		^this.subclassResponsibility( thisMethod );
		^this.class.new( this.getDefaultTouchMode );
	}

	getDefaultTouchMode { ^touchMode }
	
	// synonym
	defaultTouchMode { ^touchMode }

	*getCuttedRange { arg stakes, span, byStart = true, touchMode, shiftVirtual = 0;
		var collResult, start, stop, shift, stake, stake2, stakeSpan;

		if( stakes.isEmpty, { ^stakes; });
		
		collResult		= List.new;
		start			= span.start;
		stop				= span.stop;
		shift			= shiftVirtual != 0;
		
		switch( touchMode,
		kTouchNone, {
			stakes.do({ arg stake;
				stakeSpan	= stake.span;
				if( stakeSpan.start >= start, {
					if( shift, {
						collResult.add( stake.shiftVirtual( shiftVirtual ));
					}, {
						collResult.add( stake.duplicate );
					});
				});
			});
		},
			
		kTouchSplit, {
			stakes.do({ arg stake;
				stakeSpan	= stake.span;
				
				if( stakeSpan.start >= start, {			// start portion not splitted
					if( stakeSpan.stop <= stop, {			// completely included, just make a copy
						if( shift, {
							collResult.add( stake.shiftVirtual( shiftVirtual ));
						}, {
							collResult.add( stake.duplicate );
						});
					}, {								// adjust stop
						stake = stake.replaceStop( stop );
						if( shift, {
							stake2	= stake;
							stake	= stake.shiftVirtual( shiftVirtual );
							stake2.dispose;	// delete temp product
						});
						collResult.add( stake );
					});
				}, {
					if( stakeSpan.stop <= stop, {			// stop included, just adjust start
						stake = stake.replaceStart( start );
						if( shift, {
							stake2	= stake;
							stake	= stake.shiftVirtual( shiftVirtual );
							stake2.dispose;	// delete temp product
						});
						collResult.add( stake );
					}, {								// adjust both start and stop
						stake2	= stake.replaceStart( start );
						stake	= stake2.replaceStop( stop );
						stake2.dispose;	// delete temp product
						if( shift, {
							stake2	= stake;
							stake	= stake.shiftVirtual( shiftVirtual );
							stake2.dispose;	// delete temp product
						});
						collResult.add( stake );
					});
				});
			});
		},
			
		// default:
		{
			MethodError( "Illegal Argument TouchMode : " ++ touchMode, thisMethod ).throw;
		});
		
		^collResult;
	}

	getCuttedRange { arg span, byStart = true, touchMode, shiftVirtual = 0;
		touchMode	= touchMode ?? { this.getDefaultTouchMode; };
		^Trail.getCuttedRange( this.getRange( span, byStart ), span, byStart, touchMode, shiftVirtual );
	}

	get { arg idx, byStart = true;
		var coll;

		coll = byStart.if( collStakesByStart, collStakesByStop );
		^coll.at( idx );
	}

	editGet { arg idx, byStart = true, ce;
		var coll;
		
		coll = byStart.if({ this.prEditGetCollByStart( ce )}, { this.prEditGetCollByStop( ce )});
		^coll.at( idx );
	}
	
	getNumStakes {
		^collStakesByStart.size;
	}

	// synonym
	numStakes {
		^collStakesByStart.size;
	}
	
	size { ^this.shouldNotImplement( thisMethod )}
	
	isEmpty {
		^collStakesByStart.isEmpty;
	}
	
	contains { arg stake;
		^( this.indexOf( stake, true ) >= 0 );
	}

	indexOf { arg stake, byStart = true;
		^this.editIndexOf( stake, byStart );
	}

	editIndexOf { arg stake, byStart = true, ce;
		var coll, comp, idx, idx2, stake2;

		comp	= byStart.if({ startComparator }, { stopComparator });

		if( ce.isNil, {
			coll = byStart.if( collStakesByStart, collStakesByStop );
		}, {
			this.prCheckEdit( ce );
			coll = byStart.if( collEditByStart ? collStakesByStart,
							collEditByStop ? collStakesByStop );
		});

		// "If the list contains multiple elements equal to the specified object,
		//  there is no guarantee which one will be found"
		idx = this.prBinarySearch( coll, stake, comp );

		if( idx >= 0, {
			stake2 = coll.at( idx );
//			if( stake2.equals( stake ), { ^idx; });
			if( stake2 == stake, { ^idx; });
			idx2 = idx - 1;
			while({ idx2 >= 0 }, {
				stake2 = coll.at( idx2 );
//				if( stake2.equals( stake ), { ^idx2; });
				if( stake2 == stake, { ^idx2; });
				idx2 = idx2 - 1;
			});
			idx2 = idx + 1;
			while({ idx2 < coll.size }, {
				stake2 = coll.at( idx2 );
//				if( stake2.equals( stake ), { ^idx2; });
				if( stake2 == stake, { ^idx2; });
				idx2 = idx2 + 1;
			});
		});
		^idx;
	}

	indexOfPos { arg pos, byStart = true;
		^this.editIndexOfPos( pos, byStart );
	}

	editIndexOfPos { arg pos, byStart = true, ce;
		if( byStart, {
			^this.prBinarySearch( this.prEditGetCollByStart( ce ), pos, startComparator );
		}, {
			^this.prBinarySearch( this.prEditGetCollByStop( ce ), pos, stopComparator );
		});
	}
	
	getLeftMost { arg idx, byStart = true;
		^this.editGetLeftMost( idx, byStart );
	}
	
	editGetLeftMost { arg idx, byStart = true, ce;
		var coll, lastStake, pos, nextStake;
	
		if( idx < 0, {
			idx = (idx + 2).neg;
			if( idx < 0, { ^nil });
		});
		
		coll		= byStart.if({ this.prEditGetCollByStart( ce )}, { this.prEditGetCollByStop( ce )});
		lastStake	= coll.at( idx );
		pos		= byStart.if({ lastStake.span.start }, { lastStake.span.stop });
		
		while({ idx > 0 }, {
			idx			= idx - 1;
			nextStake 	= coll.at( idx );
			if( byStart.if({ nextStake.span.start }, { nextStake.span.stop }) != pos, {
				^lastStake;
			});
			lastStake	= nextStake;
		});
		
		^lastStake;
	}

	// XXX needs testing
	editFilterStakeAt { arg pos, ce, filter = true;
		var coll, idx, lastStake, nextStake;

		coll = this.editGetRange( Span( pos, pos ), true, ce, filter );
		^coll.first;
//		coll = this.prEditGetCollByStart( ce );
//		idx = this.prBinarySearch( coll, pos, startComparator );
//		if( idx < 0, {
//			idx = (idx + 2).neg;
//			if( idx < 0, { ^nil });
//		});
//		
//		lastStake	= coll.at( idx );
//		if( filter.value( lastStake ).not, { lastStake = nil });
//		
//		while({ idx > 0 }, {
//			idx			= idx - 1;
//			nextStake 	= coll.at( idx );
//			if( nextStake.span.stop <= pos, {
//				^lastStake;
//			});
//			if( filter.value( nextStake ), { lastStake = nextStake });
//		});
//		
//		^lastStake;
	}

	getRightMost { arg idx, byStart = true;
		^this.editGetRightMost( idx, byStart );
	}

	editGetRightMost { arg idx, byStart = true, ce;
		var coll, sizeM1, lastStake, pos, nextStake;
	
		coll		= byStart.if({ this.prEditGetCollByStart( ce )}, { this.prEditGetCollByStop( ce )});
		sizeM1	= coll.size - 1;
		
		if( idx < 0, {
			idx = (idx + 1).neg;
			if( idx > sizeM1, { ^nil; });
		});
		
		lastStake	= coll.at( idx );
		pos			= byStart.if({ lastStake.span.start }, { lastStake.span.stop });
		
		while({ idx < sizeM1 }, {
			idx			= idx + 1;
			nextStake	= coll.at( idx );
			if( byStart.if({ nextStake.span.start }, { nextStake.span.stop }) != pos, {
				^lastStake;
			});
			lastStake	= nextStake;
		});
		
		^lastStake;
	}

	getLeftMostIndex { arg idx, byStart = true;
		^this.editGetLeftMostIndex( idx, byStart );
	}

	editGetLeftMostIndex { arg idx, byStart = true, ce;
		var coll, stake, pos;
	
		if( idx < 0, {
			idx = (idx + 2).neg;
			if( idx < 0, { ^-1; });
		});
		
		coll		= byStart.if({ this.prEditGetCollByStart( ce )}, { this.prEditGetCollByStop( ce )});
		stake		= coll.at( idx );
		pos			= byStart.if({ stake.span.start }, { stake.span.stop });
		
		while({ idx > 0 }, {
			stake	= coll.at( idx - 1 );
			if( byStart.if({ stake.span.start }, { stake.span.stop }) != pos, {
				^idx;
			});
			idx		= idx - 1;
		});
		
		^idx;
	}

	getRightMostIndex { arg idx, byStart = true;
		^this.editGetRightMostIndex( idx, byStart );
	}

	editGetRightMostIndex { arg idx, byStart = true, ce;
		var coll, sizeM1, stake, pos;
	
		coll		= byStart.if({ this.prEditGetCollByStart( ce )}, { this.prEditGetCollByStop( ce )});
		sizeM1		= coll.size - 1;
		
		if( idx < 0, {
			idx = (idx + 1).neg;
			if( idx > sizeM1, { ^-1; });
		});
		
		stake		= coll.at( idx );
		pos			= byStart.if({ stake.span.start }, { stake.span.stop });
		
		while({ idx < sizeM1 }, {
			stake = coll.at( idx + 1 );
			if( byStart.if({ stake.span.start }, { stake.span.stop }) != pos, {
				^idx;
			});
			idx = idx + 1;
		});
		
		^idx;
	}

	getAll { arg byStart = true;
		var coll;
		
		coll = byStart.if( collStakesByStart, collStakesByStop );
		^coll.copy;
	}

// XXX missing
//	public List getAll( int startIdx, int stopIdx, boolean byStart )

	add { arg source, stake;
		this.editAddAll( source, List.newUsing( stake ));
	}
	
	/**
	 *	@throws	IOException
	 */
	editAdd { arg source, stake, ce;
		this.editAddAll( source, List.newUsing( stake ), ce );	// ____ dep ____ handled there
	}

	addAll { arg source, stakes;
		^this.editAddAll( source, stakes );
	}

	/**
	 *	@throws	IOException
	 */
	editAddAll { arg source, stakes, ce;
		var span;
	
		if( kDebug, { ("editAddAll "++stakes.size).inform; });
		if( stakes.size == 0, { ^this; });
	
		if( ce.notNil, { this.prCheckEdit( ce )});
	
		span = this.prAddAll( stakes, ce );

		// ____ dep ____
		dependants.do( _.protAddAllDep( source, stakes, ce, span ));

		if( source.notNil && span.notNil, {
			if( ce.notNil, {
				ce.addPerform( TrailEdit.newDispatch( this, span ));
			}, {
				this.prDispatchModification( source, span );
			});
		});
	}
	
	/**
	 *	To be overwritten by dependants.
	 *
	 *	@throws	IOException
	 */
	protAddAllDep { arg source, stakes, ce, span;
	
	}

	prAddAll { arg stakes, ce;
		var start, stop, span;
	
		if( stakes.size == 0, { ^nil });

// XXX BROKEN ON SC INTEL!!!!
//		start	= inf.asInteger;	// XXX 32-bit only!!! Long.MAX_VALUE;
//		stop		= -inf.asInteger;	// XXX 32-bit only!!! Long.MIN_VALUE;
		start	= 0x7FFFFFFF;
		stop		= 0x80000000;
		
		stakes.do({ arg stake;
			this.prSortAddStake( stake, ce );
			start	= min( start, stake.span.start );
			stop		= max( stop, stake.span.stop );
		});
		span	= Span( start, stop );
		if( ce.notNil, { ce.addPerform( this.protTrailEdit( stakes, span, TrailEdit.kEditAdd ))});

		^span;
	}
	
	protTrailEdit { arg stakes, span, cmd;
		^TrailEdit( this, stakes, span, cmd );
	}
	
	remove { arg source, stake;
		^this.editRemoveAll( source, List.newUsing( stake ));
		// ____ dep ____ handled there
	}

	/**
	 *	@throws	IOException
	 */
	editRemove { arg source, stake, ce;
		^this.editRemoveAll( source, List.newUsing( stake ), ce );		// ____ dep ____ handled there
	}

	removeAll { arg source, stakes;
		^this.editRemoveAll( source, stakes );
	}

	/**
	 *	@throws	IOException
	 */
	editRemoveAll { arg source, stakes, ce;
		var span;

		if( stakes.size == 0, { ^this; });

		span = this.prRemoveAll( stakes, ce );

		// ____ dep ____
		dependants.do( _.protRemoveAllDep( source, stakes, ce, span ));

		if( source.notNil && span.notNil, {
			if( ce.notNil, {
				ce.addPerform( TrailEdit.newDispatch( this, span ));
			}, {
				this.prDispatchModification( source, span );
			});
		});
	}
	
	/**
	 *	To be overwritten by dependants.
	 *
	 *	@throws	IOException
	 */
	protRemoveAllDep { arg source, stakes, ce, span;

	}

	prRemoveAll { arg stakes, ce;
		var start, stop, span;

		if( stakes.size == 0, { ^nil });
	
// XXX BROKEN ON SC INTEL!!!!
//		start	= inf.asInteger;	// XXX 32-bit only!!! Long.MAX_VALUE;
//		stop		= -inf.asInteger;	// XXX 32-bit only!!! Long.MIN_VALUE;
		start	= 0x7FFFFFFF;
		stop		= 0x80000000;

		stakes.do({ arg stake;
			this.prSortRemoveStake( stake, ce );
			start	= min( start, stake.span.start );
			stop		= max( stop, stake.span.stop );
			if( ce.isNil, { stake.dispose });
		});
		span	= Span( start, stop );
		if( ce.notNil, { ce.addPerform( this.protTrailEdit( stakes, span, TrailEdit.kEditRemove ))});

		^span;
	}

    debugDump {
		"collStakesByStart : ".postln;
		collStakesByStart.postcs;
		"collStakesByStop : ".postln;
		collStakesByStop.postcs;
		"collEditByStart : ".postln;
		collEditByStart.postcs;
		"collEditByStop : ".postln;
		collEditByStop.postcs;
	}
	
	protAddIgnoreDependants { arg stake;
		^this.prSortAddStake( stake, nil );
	}

	prSortAddStake { arg stake, ce;
		var collByStart, collByStop, idx;

		if( ce.isNil, {
			collByStart	= collStakesByStart;
			collByStop	= collStakesByStop;
		}, {
			this.prEnsureEditCopy;
			collByStart	= collEditByStart;
			collByStop	= collEditByStop;
		});

		idx		= this.editIndexOfPos( stake.span.start, true, ce );	// look for position only!
		if( idx < 0, { idx = (idx + 1).neg });
		collByStart.insert( idx, stake );
		idx		= this.editIndexOfPos( stake.span.stop, false, ce );
		if( idx < 0, { idx = (idx + 1).neg });
		collByStop.insert( idx, stake );
		
		stake.trail = this;	// ???
		if( ce.isNil, { this.protStakeAdded( stake )});
	}
	
	prSortRemoveStake { arg stake, ce;
		var collByStart, collByStop, idx;

		if( ce.isNil, {
			collByStart	= collStakesByStart;
			collByStop	= collStakesByStop;
		}, {
			this.prEnsureEditCopy;
			collByStart	= collEditByStart;
			collByStop	= collEditByStop;
		});
		
		idx		= this.editIndexOf( stake, true, ce );
		if( idx >= 0, { collByStart.removeAt( idx )});
	// look for object equality!
		idx		= this.editIndexOf( stake, false, ce );
		if( idx >= 0, { collByStop.removeAt( idx )});

//		stake.trail = nil;
		if( ce.isNil, { this.protStakeRemoved( stake )});
	}
	
	addListener { arg listener;
		if( elm.isNil, {
			elm = EventManager( this );
		});
		elm.addListener( listener );
	}

	removeListener { arg listener;
		elm.removeListener( listener );
	}

	addDependant { arg sub;
		if( dependants.isNil, {
			dependants = List.new;
		});
//		synchronized( dependants ) {
			if( dependants.indexOf( sub ).notNil, {
				"BasicTrail.addDependant : WARNING : duplicate add".warn;
			});
			dependants.add( sub );
//		}
	}

	removeDependant { arg sub;
//		synchronized( dependants ) {
			if( dependants.remove( sub ).not, {
				"BasicTrail.removeDependant : WARNING : was not in list".warn;
			});
//		}
	}
	
	getNumDependants {
		^dependants.size;  // nil.size == 0 !
	}
	
	// synonym
	numDependants {
		^dependants.size;  // nil.size == 0 !
	}
	
	getDependant { arg i;
//		synchronized( dependants ) {
			^dependants.at( i );
//		}
	}
	
	prDispatchModification { arg source, span;
		if( elm.notNil, {
			elm.dispatchEvent( TrailEvent( this, source, span ));
		});
	}

// ---------------- TreeNode interface ---------------- 

//	public TreeNode getChildAt( int childIndex )
//	{
//		return get( childIndex, true );
//	}
//	
//	public int getChildCount()
//	{
//		return getNumStakes();
//	}
//	
//	public TreeNode getParent()
//	{
//		return null;
//	}
//	
//	public int getIndex( TreeNode node )
//	{
//		if( node instanceof Stake ) {
//			return indexOf( (Stake) node, true );
//		} else {
//			return -1;
//		}
//	}
//	
//	public boolean getAllowsChildren()
//	{
//		return true;
//	}
//	
//	public boolean isLeaf()
//	{
//		return false;
//	}
//	
//	public Enumeration children()
//	{
//		return new ListEnum( getAll( true ));
//	}
// XXX

// --------------------- EventManager.Processor interface ---------------------
	
	/**
	 *  This is called by the EventManager
	 *  if new events are to be processed. This
	 *  will invoke the listener's <code>trailModified</code> method.
	 */
	processEvent { arg e;
		var listener;

		elm.countListeners.do({ arg i;
			listener = elm.getListener( i );
			switch( e.getID,
			TrailEvent.kModified, {
				listener.trailModified( e );
//			},
//			// default:
//			{
//				assert false : e.getID;
//				break;
			});
			i = i + 1;
		});
	}
}

TrailEdit : JBasicUndoableEdit
{
	// undable edits
		
	classvar <kEditAdd		= 0;
	classvar <kEditRemove	= 1;
	classvar <kEditDispatch	= 2;

	var		<trail, <cmd, <stakes, key;
//	var		removed;
	var		disposeWhenDying, <span;

	*newDispatch { arg trail, span;
		^this.new( trail, nil, span, kEditDispatch, "editChangeTrail" );
	}

	*new { arg trail, stakes, span, cmd, key = "editChangeTrail";
		^super.new.prInitTrailEdit( trail, stakes, span, cmd, key );
	}
	
	prInitTrailEdit { arg argTrail, argStakes, argSpan, argCmd, argKey;
		stakes			= argStakes;
		cmd				= argCmd;
		key				= argKey;
		span				= argSpan;
		trail			= argTrail;
//		removed			= false;
		disposeWhenDying	= stakes.notNil;

if( span.isNil, {
	"WARNING: span is nil".warn;
});
	}

	isSignificant {
		^(cmd != kEditDispatch);
	}

	prAddAll {
		stakes.do({ arg stake;
			trail.prSortAddStake( stake, nil );
		});
//		removed		= false;
		disposeWhenDying	= false;
	}

	prRemoveAll {
		stakes.do({ arg stake;
			trail.prSortRemoveStake( stake, nil );
		});
//		removed		= true;
		disposeWhenDying	= true;
	}
	
	prDisposeAll {
		stakes.do( _.dispose );
	}

	undo {
		super.undo;
		
		switch( cmd,
		kEditAdd, {
			this.prRemoveAll;
		},
		kEditRemove, {
			this.prAddAll;
		},
		kEditDispatch, {
			trail.prDispatchModification( trail, span );
		});
	}
	
	redo {
		super.redo;
		this.performEdit;
	}
	
	performEdit {
		switch( cmd,
		kEditAdd, {
			this.prAddAll;
		},
		kEditRemove, {
			this.prRemoveAll;
		},
		kEditDispatch, {
//			"---------------1".postln;
			this.protDispatchModification;
//			"---------------2".postln;
		});
	}
	
	protDispatchModification {
		trail.prDispatchModification( trail, span );
	}
	
	die	{
		super.die;
//		if( removed ... )
		if( disposeWhenDying, {
			this.prDisposeAll;
		});
	}
	
	getPresentationName {
//		return AbstractApplication.getApplication().getResourceString( key );
// XXX
		^key;
	}
	
	addEdit { arg anEdit;
		var old;
		
		if( anEdit.isKindOf( TrailEdit ).not, { ^false });
		
		old = anEdit;
		if( (old.trail == this.trail) and: { old.cmd == this.cmd }, {
			if( (cmd == \kEditAdd) or: { cmd == \kEditRemove }, {
				this.stakes.addAll( old.stakes );
				this.span = this.span.union( old.span );
			}, { if( cmd == \kEditDispatch, {
				this.span = this.span.union( old.span );
			})});
			old.die;
			^true;
		}, {
			^false;
		});
	}
	
	replaceEdit { arg anEdit;
		^this.addEdit( anEdit );	// same behaviour in this case
	}
}
