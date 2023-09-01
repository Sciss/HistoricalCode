/*
 *	BosqueMIDIController
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
 *	This class assumes the anatomy of a BCF2000 controller.
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.12, 26-Jul-08
 */
BosqueMIDIController {
	classvar <>numFaders = 8;
	classvar <>smoothing = 0.01;
	classvar <>period    = 0.05; // update period (during transport performance) in seconds
	
	var bosque, doc, <in, <out;
	var follow, write;
	
//	var ccFaderMSB, ccFaderLSB;
	var faderMSBmem;
	var liveEnvs;
	var liveTracks;
	var liveEnvPainter;
	
	*new { arg bosque;
		^super.new.prInit( bosque );
	}
	
	prInit { arg argBosque;
		var routFollow;
	
		bosque		= argBosque ? Bosque.default;
		out			= bosque.midiOut;
		in			= bosque.midiIn;
		doc			= bosque.session;
		if( out.isNil || in.isNil, { ^this });
		
		follow		= false ! numFaders;
		write		= false ! numFaders;
		
		faderMSBmem	= 0 ! numFaders;
		liveEnvs		= nil ! numFaders;
		liveTracks	= nil ! numFaders;
		
		liveEnvPainter = bosque.timelineEditor.panel.liveEnvPainter;
			
		numFaders.do({ arg chan;
			// follow controller is 16 ... 16 + numFaders - 1
			CCResponder({ arg src, midiChan, num, value;
				var onOff = value != 0;
				follow[ chan ] = onOff;
				if( onOff, { this.prFollow });
			}, in, 0, chan + 16 );
			out.control( 0, chan + 16, 0 );
			
			// write controller is 8 ... 8 + numFaders - 1
			CCResponder({ arg src, midiChan, num, value;
				var onOff = value != 0;
				write[ chan ] = onOff;
				if( onOff, {
					// nada
				}, {
					this.prWriteStop( chan );
				});
			}, in, 0, chan + 8 );
			out.control( 0, chan + 8, 0 );

			// fader MSB is 0 ... 0 + numFaders - 1;
			CCResponder({ arg src, midiChan, num, value;
				faderMSBmem[ chan ] = value;
			}, in, 0, chan ); 

			// fader LSB is 32 ... 32 + numFaders - 1;
			CCResponder({ arg src, midiChan, num, value;
				if( write[ chan ] and: { doc.transport.isRunning }, {
					this.prWrite( chan, (value | (faderMSBmem[ chan ] << 7)) / 0x3FFF );
				});
			}, in, 0, chan + 32 ); 
		});
		
		UpdateListener.newFor( doc.transport, { arg upd, trnsp, what, param;
			switch( what,
			\play, {
				routFollow = this.prStartRoutFollow;
			},
			\stop, {
				routFollow.stop; routFollow = nil;
				numFaders.do({ arg chan;
					if( write[ chan ], {
						this.prWriteStop( chan );
						out.control( 0, chan + 8, 0 );
					});
				});
			},
			\pause, {
				routFollow.stop; routFollow = nil;
			},
			\resume, {
				routFollow = this.prStartRoutFollow;
			});
		});
		
		UpdateListener.newFor( doc.timelineView.cursor, { arg upd, csr;
			this.prFollow;
		}, \changed );
	}
	
	prStartRoutFollow {
		^{
			inf.do({
				this.prFollow;
				period.wait;
			});
		}.fork;
	}
	
	prFollow {
		var frame, track, trackOff, level, full, lsb, msb;
		trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
//("trackOff = " ++trackOff).postln;
		if( trackOff.isNil, { ^this });
		follow.do({ arg yes, i;
			if( yes, {
				if( frame.isNil, { frame = doc.transport.currentFrame });
				track = doc.tracks.at( i + trackOff );
				if( track.notNil, {
					level = track.level( frame );
					if( level.notNil, {
//("level["++i++"] = " ++level).postln;
						full = (level * 0x3FFF).asInteger;
						lsb  = full & 0x7F;
						msb  = full >> 7;
						out.control( 0, i, msb );
						out.control( 0, i + 32, lsb );
					});
				});
			});
		});
	}
	
	prWrite { arg chan, level;
		var trackOff, env, segm, frame, hitIdx, hitStake1, newStake1, newStake2, span;
		
		frame = doc.transport.currentFrame;
//		[ chan, level ].postln;
		
		if( liveTracks[ chan ].isNil, {
			trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
			liveTracks[ chan ] = doc.tracks.at( chan + trackOff );
			if( liveTracks[ chan ].isNil, { ^this });
			span = Span( frame, doc.timeline.span.stop );
			segm = BosqueEnvSegmentStake( span.shift( frame.neg ), level, level );
			env = BosqueTrail.new;
			env.add( nil, segm );
			liveEnvs[ chan ] = BosqueEnvRegionStake( span, \Env, liveTracks[ chan ], env: env );
			liveEnvPainter.addTrack( liveTracks[ chan ], liveEnvs[ chan ]);
			liveTracks[ chan ].liveReplc = liveEnvs[ chan ];
		}, {
			frame	= frame - liveEnvs[ chan ].span.start;
			env		= liveEnvs[ chan ].env;
			hitIdx	= env.indexOf( frame );
			if( hitIdx >= -1, { ^this });
			hitStake1		= env.get( (hitIdx + 2).neg );
			Assertion({ hitStake1.span.containsPos( frame )});
			newStake1		= hitStake1.replaceStopWithLevel( frame, level );
			newStake2		= hitStake1.replaceStartAndLevels( frame, level, level );
			env.remove( this, hitStake1 );
			env.addAll( this, [ newStake1, newStake2 ]);
			span			= hitStake1.span; // .shift( pressedStake.span.start );
//			env.modified( this, span );
		});
	}
	
	prWriteStop { arg chan;
		var stake, newStake, segm, newEnv, lastSegm;
		write[ chan ] = false;
		if( liveTracks[ chan ].notNil, {
			liveTracks[ chan ].liveReplc = nil;
			liveEnvPainter.removeTrack( liveTracks[ chan ]);
			liveTracks[ chan ] = nil;
//			liveEnvs[ chan ].dispose;
			stake = liveEnvs[ chan ];
			liveEnvs[ chan ] = nil;
			if( stake.env.numStakes > 1, {
				lastSegm = stake.env.get( stake.env.numStakes - 1 );
				stake.env.remove( nil, lastSegm );
//				newStake = stake.replaceStop( lastSegm.span.start + stake.span.start );
				newEnv = BosqueTrail.new;
				newEnv.addAll( nil, this.prSmooth( stake.env.getAll ));
				newStake = BosqueEnvRegionStake( Span( stake.span.start, lastSegm.span.start + stake.span.start ),
					stake.name, stake.track, stake.colr, stake.fadeIn, stake.fadeOut, stake.gain, newEnv );
				this.prSmooth( stake.env.getAll, smoothing );
				bosque.timelineEditor.editAddEnvStake( newStake );
			});
		});
	}
	
	/**
	 *	Smoothing by successively replacing
	 *	data with a regression line until
	 *	a threshold residual is exceeded.
	 *	("least squares").
	 *
	 *	http://de.wikipedia.org/wiki/Regressionsanalyse#Berechnung_der_Regressionsgeraden
	 *
	 *	So y_est[i] = a + bx[i]
	 *	where b := sum( (x[i] - x_mean)*(y[i] - y_mean)) / sum( (x[i] - x_mean).squared )
	 *	and a := y_mean - b*x_mean
	 *
	 *	and e[i] := y[i] - y_est[i] <= max_error
	 *
	 *	Note that all env segments are considered to have line shapes!
	 */
//	prSmooth { arg coll, maxErr = 0.01;
//		var off, sumx, sumy, b, meanx, meany, dx, dy, sumNom, sumDenom, x, y, n, yEst;
//		var aTest, bTest, nSub, collRes;
//		
//		off  = 0;
//		x    = coll.collect({ arg segm; segm.span.start }).add( coll.last.span.stop );
//		y    = coll.collect({ arg segm; segm.startLevel }).add( coll.last.stopLevel );
//		n    = x.size;
//		while({ off < n }, {
//			nSub = 0;
//			sumx = 0.0;
//			sumy = 0.0;
//			block { arg break;
//				(off .. (n - 1)).do({ arg i, k;
//					sumx     = sumx + x[ i ];
//					sumy     = sumy + y[ i ];
//					meanx    = sumx / (k + 1);
//					meany    = sumy / (k + 1);
//					sumNum   = 0.0;
//					sumDenom = 0.0;
//					(off .. i).do({ arg j;
//						dx       = x[ j ] - meanx;
//						dy       = y[ j ] - meany;
//						sumNum   = sumNom + (dx * dy);
//						sumDenom = sumDenom + dx.squared;
//					});
//					bTest = sumNum / sumDenom;
//					aTest = meany - (bTest * meanx);
//					
//					(off .. i).do({ arg j;
//						yEst = aTest + (bTest * x[ j ]);
//						if( abs( y[ j ] - yEst ) > maxErr, break );
//					});
//		
//					a = aTest;
//					b = bTest;
//					nSub = nSub + 1;
//				});
//			};
//			collRes = collRes.add( BosqueEnvSegmentStake( span, startLevel, stopLevel ));
//			off = off + nSub;
//		});
//	}

	prSmooth { arg coll, maxErr = 0.01;
		var off, b, dx, dy, x, y, n, yEst;
		var bTest, nSub, collRes, y0, x0, i;
		
		off  = 0;
		x    = coll.collect({ arg segm; segm.span.start }).add( coll.last.span.stop );
		y    = coll.collect({ arg segm; segm.startLevel }).add( coll.last.stopLevel );
		n    = x.size;
		while({ off < (n - 1)}, {
			nSub = 0;
			block { arg break;
				x0 = x[ off ];
				y0 = y[ off ];
				b  = 0;
				i  = off + 1;
				while({ i < n }, {
					bTest = (y[ i ] - y0) / (x[ i ] - x0);
					(off .. i).do({ arg j;
						yEst = y0 + (bTest * (x[ j ] - x0));
						if( abs( y[ j ] - yEst ) > maxErr, break );
					});
		
					b    = bTest;
					nSub = nSub + 1;
					i = i + 1;
				});
			};
			collRes = collRes.add( BosqueEnvSegmentStake(
				Span( x0, x[ off + nSub ]), y0, y[ off + nSub ]));
			off = off + nSub;
		});
		^collRes;
	}
}