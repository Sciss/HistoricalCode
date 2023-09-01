/*
 *	BosqueObserver
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
 *	@version	0.17, 25-Aug-08
 */
BosqueObserver {
	var <bosque;
	var <window;
	var <doc;
	var <colors;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
		bosque	= Bosque.default;
		doc		= bosque.session;
		bosque.doWhenSwingBooted({ this.prMakeGUI });
	}
	
	prMakeGUI {
		var stake, fntSmall, updRegionSel, view, flow, ggStakeName, ggColor, ggColorChooser, ggFadeIn, ggFadeOut, ggGain, fRegionUpdate, selNum = -1, nan;
		var track, ggTab, ggTrackName, updTrackSel, fTrackUpdate, ggTrackMute;
		var ggBusConfig, ggCtrlSpec, ggCtrlBusIndex;
		var ggFuncEventName, ggFuncModTrack, ggFuncPosition;
		
		window = JSCWindow( "Observer", Rect( 0, 0, 254, 248 ), resizable: false ).userCanClose_( false ).alwaysOnTop_( true ); // .alwaysOnTop_( true );
		ScissUtil.positionOnScreen( window, 0.75, 0.75 );
		
		ggTab = JSCTabbedPane( window, window.view.bounds );
		
		fntSmall	= JFont( "Luicda Grande", 10 );

		// ----------------------- Region Tab -----------------------
		
		view = JSCCompositeView( ggTab, ggTab.bounds ); // window.view;
//		ggTab.setTitleAt( ggTab.numTabs, "Regions" );
		view.decorator = flow = FlowLayout( view.bounds );
		
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Name:" );
		ggStakeName = JSCTextField( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.action_({ arg b;
				this.prModify( \rename, b.string.asSymbol );
			});
		flow.nextLine;
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Color:" );
		ggColor = JSCStaticText( view, Rect( 0, 0, 20, 20 ));
		colors = this.prEqualLumiColours; // Array.fill( 32, { arg i; Color.hsv( i / 32, 1, 1 )});
		ggColorChooser = JSCEnvelopeView( view, Rect( 0, 0, 136, 20 )).value_([ Array.series( 32, 0, 1/31 ), 0 ! 32 ]).thumbWidth_( 160 / 32 ).thumbHeight_( 20 ).editable_( false ).selectionColor_( Color.black ).strokeColor_( Color.clear ).canFocus_( false )
			.action_({ arg b; var colr;
				colr = colors[ b.selection.indexOf( true ) ? -1 ];
				if( colr.notNil, {
					this.prModify( \replaceColor, colr );
				});
			});
		32.do({ arg i; ggColorChooser.setFillColor( i, colors[ i ])});
		flow.nextLine;
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Fade in:" );
		ggFadeIn = JSCNumberBox( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.action_({ arg b; var frames, framesC;
				if( stake.notNil, {
					frames 	= (b.value / 1000 * doc.timeline.rate).asInteger;
					framesC	= frames.clip( 0, stake.span.length - stake.fadeOut.numFrames );
//[ "frames", frames, "b.value", b.value, "rate", doc.timeline.rate, "framesC", framesC, "span.span", stake.span ].postln;
					if( frames != framesC, {
						b.value = (framesC / doc.timeline.rate * 1000).asInteger;
					});
					this.prModify( \replaceFadeIn, stake.fadeIn.replaceFrames( framesC ));
				});
			});
		flow.nextLine;
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Fade out:" );
		ggFadeOut = JSCNumberBox( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.action_({ arg b; var frames, framesC;
				if( stake.notNil, {
					frames 	= (b.value / 1000 * doc.timeline.rate).asInteger;
					framesC	= frames.clip( 0, stake.span.length - stake.fadeIn.numFrames );
					if( frames != framesC, {
						b.value = (framesC / doc.timeline.rate * 1000).asInteger;
					});
					this.prModify( \replaceFadeOut, stake.fadeOut.replaceFrames( framesC ));
				});
			});
		flow.nextLine;
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Gain:" );
		ggGain = JSCNumberBox( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.action_({ arg b; var amp;
				amp = b.value.dbamp;
				if( amp.isNaN.not, {
					this.prModify( \replaceGain, amp );
				});
			});
		flow.nextLine;
		flow.shift( 0, 8 );
		
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "F Evt Var:" );
		ggFuncEventName = JSCTextField( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.action_({ arg b;
				this.prModify( \replaceEventName, b.string );
			});
		flow.nextLine;
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "F Modtrack:" );
		ggFuncModTrack = JSCDragSink( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.canReceiveDragHandler_({ arg b;
				JSCView.currentDrag.isKindOf( BosqueTrack );
			})
			.action_({ arg b; var modTrack = b.object, ce;
				if( stake.notNil and: { modTrack.isKindOf( BosqueTrack )}, {
					b.object = modTrack.name;
//					ce		= JSyncCompoundEdit( "Change Func Mod Track" );
					this.prModify( \replaceModTrack, modTrack );
				}, {
					b.object = "";
				});
			});
		
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "F Position:" );
		ggFuncPosition = JSCPopUpMenu( view, Rect( 0, 0, 60, 20 )).font_( fntSmall )
			.items_([ "Pre", "Post" ])
			.action_({ arg b;
				this.prModify( \replacePosition, [ \pre, \post ][ b.value ]);
			});
		
		nan = 0/0;
		fRegionUpdate = {
			var newSelNum = doc.selectedRegions.size, sth = newSelNum > 0, sth2;
			
//try {
			if( newSelNum != selNum and: { ((newSelNum > 1) && (selNum > 1)).not }, {
				if( newSelNum == 0, {
					stake				= nil;
					ggStakeName.string		= "";
					ggColor.background		= Color.clear;
					ggFadeIn.value		= nan;
					ggFadeOut.value		= nan;
					ggGain.value			= nan;
					sth2					= false;
					ggFuncEventName.string	= "";
					ggFuncModTrack.object	= "";
				}, { // if( newSelNum == 1, { })
					stake				= doc.selectedRegions[0];
					ggStakeName.string		= stake.name.asString;
					ggColor.background		= stake.colr;
					ggFadeIn.value		= (stake.fadeIn.numFrames  / doc.timeline.rate * 1000).asInteger;
					ggFadeOut.value		= (stake.fadeOut.numFrames / doc.timeline.rate * 1000).asInteger;
					ggGain.value			= stake.gain.ampdb;
					sth2					= doc.selectedRegions.detect({ arg stake; stake.isKindOf( BosqueFuncRegionStake ).not }).isNil;
					if( sth2, {
						ggFuncEventName.string	= stake.eventName;
						ggFuncModTrack.object	= if( stake.modTrack.notNil, { stake.modTrack.name }, "" );
						ggFuncPosition.value	= [ \pre, \post ].indexOf( stake.position ) ? 0;
					});
				});
				ggStakeName.enabled	= sth;
				ggColorChooser.enabled	= sth;
				ggFadeIn.enabled		= sth;
				ggFadeOut.enabled		= sth;
				ggGain.enabled		= sth;
				ggFuncEventName.enabled	= sth2;
				ggFuncModTrack.enabled	= sth2;
				ggFuncPosition.enabled	= sth2;
			});
//} { arg error; error.reportError };
		};

		updRegionSel = UpdateListener.newFor( doc.selectedRegions, { arg upd, sc, what ... coll;
			if( (what === \add) or: { what === \remove }, fRegionUpdate );
		});
		
		view.onClose = { updRegionSel.remove };
		
		fRegionUpdate.value;

		// ----------------------- Track Tab -----------------------
		
		view = JSCCompositeView( ggTab, ggTab.bounds );
//		ggTab.setTitleAt( ggTab.numTabs, "Track" );
		view.decorator = flow = FlowLayout( view.bounds );
		
		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Name:" );
		ggTrackName = JSCStaticText( view, Rect( 0, 0, 160, 20 )).font_( fntSmall );
		flow.nextLine;

		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Bus Config:" );
		ggBusConfig = JSCDragSink( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.canReceiveDragHandler_({ arg b;
				JSCView.currentDrag.isKindOf( BosqueBusConfig );
			})
			.action_({ arg b; var busConfig = b.object, ce;
				if( track.notNil and: { busConfig.isKindOf( BosqueBusConfig )}, {
					b.object = busConfig.name;
					ce		= JSyncCompoundEdit( "Change Track Bus" );
					doc.selectedTracks.select({ arg x; x.trackID >= 0 }).do({ arg x; x.editBusConfig( this, busConfig, ce )});
					doc.undoManager.addEdit( ce.performEdit.end );
				}, {
					b.object = "";
				});
			});
		flow.nextLine;

		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Ctrl Spec:" );
		ggCtrlSpec = JSCDragSink( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.canReceiveDragHandler_({ arg b;
				JSCView.currentDrag.isKindOf( ControlSpec );
			})
			.action_({ arg b; var ctrlSpec = b.object, ce;
				if( track.notNil and: { ctrlSpec.isKindOf( ControlSpec )}, {
					b.object = ctrlSpec.storeArgs; // ctrlSpec.name;
					ce		= JSyncCompoundEdit( "Change Track Spec" );
					doc.selectedTracks.select({ arg x; x.trackID >= 0 }).do({ arg x; x.editCtrlSpec( this, ctrlSpec, ce )});
					doc.undoManager.addEdit( ce.performEdit.end );
				}, {
					b.object = "";
				});
			});
		flow.nextLine;

		JSCStaticText( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).align_( \right ).string_( "Ctrl Bus:" );
		ggCtrlBusIndex = JSCDragSink( view, Rect( 0, 0, 160, 20 )).font_( fntSmall )
			.canReceiveDragHandler_({ arg b;
				JSCView.currentDrag.isKindOf( Integer ) or: { JSCView.currentDrag.isKindOf( Bus ) and: { (JSCView.currentDrag.rate === \control) and: { JSCView.currentDrag.numChannels == 1 }}};
			})
			.action_({ arg b; var ctrl = b.object.asUGenInput, ce;
				if( track.notNil, {
					b.object = ctrl.asString;
					ce		= JSyncCompoundEdit( "Change Track Control Bus" );
					doc.selectedTracks.select({ arg x; x.trackID >= 0 }).do({ arg x; x.editCtrlBusIndex( this, ctrl, ce )});
					doc.undoManager.addEdit( ce.performEdit.end );
				}, {
					b.object = "";
				});
			});
		flow.nextLine;

		ggTrackMute = JSCCheckBox( view, Rect( 0, 0, 60, 20 )).font_( fntSmall ).string_( "Mute" )
			.action_({ arg b; var ce, value;
				value	= b.value;
				ce		= JSyncCompoundEdit( "Change Track Mute" );
				doc.selectedTracks.select({ arg x; x.trackID >= 0 }).do({ arg x; x.editMute( this, value, ce )});
				doc.undoManager.addEdit( ce.performEdit.end );
			});
		flow.nextLine;

		fTrackUpdate = {
			var newSelNum = doc.selectedTracks.select({ arg x; x.trackID >= 0 }).size, sth = newSelNum > 0;
			
			if( newSelNum != selNum and: { ((newSelNum > 1) && (selNum > 1)).not }, {
				if( newSelNum == 0, {
					track				= nil;
					ggTrackName.string		= "";
					ggTrackMute.value		= false;
					ggBusConfig.object		= "";
					ggCtrlSpec.object		= "";
					ggCtrlBusIndex.object	= "";
				}, {
					track				= doc.selectedTracks.detect({ arg x; x.trackID >= 0 });
//					ggTrackName.string		= "Track " ++ (doc.tracks.indexOf( track )+1); // track.name.asString;
					ggTrackName.string		= track.name;
					ggTrackMute.value		= track.muted;
					ggBusConfig.object		= track.busConfig.notNil.if({ track.busConfig.name }, "" );
					ggCtrlSpec.object		= track.ctrlSpec.notNil.if({ track.ctrlSpec.storeArgs }, "" );
					ggCtrlBusIndex.object	= track.ctrlBusIndex.notNil.if({ track.ctrlBusIndex.asString }, "" );
				});
				ggTrackMute.enabled	= sth;
			});
		};

		updTrackSel = UpdateListener.newFor( doc.selectedTracks, { arg upd, sc, what ... coll;
			if( (what === \add) or: { what === \remove }, fTrackUpdate );
		});
		
		view.onClose = { updTrackSel.remove };
		
		fTrackUpdate.value;

		ggTab.setTitleAt( 0, "Regions" );
		ggTab.setTitleAt( 1, "Track" );
	}
	
	prModify { arg setter, value;
		var ce, sel;
		ce = JSyncCompoundEdit( "Modify Stakes" );
		sel = doc.selectedRegions.getAll;
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, sel, false ));
		doc.trail.editBegin( ce );
		doc.trail.editRemoveAll( this, sel, ce );
		sel = sel.collect({ arg x; x.perform( setter, value )});
		doc.trail.editAddAll( this, sel, ce );
		doc.trail.editEnd( ce );
		ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, sel, false ));
		doc.undoManager.addEdit( ce.performEdit.end );
	}
	
	prEqualLumiColours { arg num = 32, maxDamp = 0.5;
		var colors, lumis, gains, minLumi;
		
		colors	= Array.fill( 32, { arg i; Color.hsv( i / 32, 1, 1 )});
		lumis	= colors.collect({ arg colr; (colr.red * 0.3) + (colr.green * 0.59) + (colr.blue * 0.11) });
		minLumi	= lumis.minItem;
		if( minLumi == 0, { ^colors });
		gains	= lumis.collect({ arg lumi; max( maxDamp, minLumi / lumi )});
		^colors.collect({ arg color, i; Color( color.red * gains[i], color.green * gains[i], color.blue * gains[i] )});
	}
}