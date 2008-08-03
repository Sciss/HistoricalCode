/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: ScissPlus (+UnixFILE), FileNetAddr
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.14, 03-Aug-08
 */
OSCFile {
	var <packets;
	var <startTime, <stopTime;

	*read { arg path;
		^this.new.prRead( path );
	}

	write { arg path, startIndex = 0, stopIndex;
		var fna, idx, p;
		idx		= startIndex;
		stopIndex	= stopIndex ?? { packets.size };
		fna		= FileNetAddr.openWrite( path );
		while({ idx < stopIndex }, {
			p	= packets[ idx ];
			fna.sendRaw( p.asRawOSC );
			idx	= idx + 1;
		});
		fna.closeFile;
	}
	
	// note: offset is re rate=1.0
	play { arg rate = 1.0, offset = 0.0, dispatcher, addr;
		var nextTime, lastTime, idx, p;
		^fork {
			dispatcher	= dispatcher ?? { thisProcess };
			idx			= this.indexOfTime( startTime + offset ) ?? 0;
			lastTime		= ((startTime ? 0.0) + offset) / rate;
			while({ idx < packets.size }, {
				p = packets[ idx ];
				if( p[0].isFloat, {
					nextTime = p[0] / rate;
					(nextTime - lastTime).wait;
					lastTime = nextTime;
//					dispatcher.recvOSCbundle( lastTime, addr, *(p.drop(1)) );
					dispatcher.recvOSCbundle( SystemClock.seconds, addr, *(p.drop(1)) );
				}, {
//					dispatcher.recvOSCmessage( lastTime, addr, p );
					dispatcher.recvOSCmessage( SystemClock.seconds, addr, p );
				});
				idx = idx + 1;
			});
		};
	}
	
	duration {
		if( startTime.isNil, { ^0.0 });
		^(stopTime - startTime);
	}

	// index of packet whose time >= t
	indexOfTime { arg t;
		var index, pt;
		var low	= 0;
		var high	= packets.size - 1;
		
//		^block { arg break; packets.do({ arg p, idx; if( p[0].isFloat and: { p[0] >= t }, { break.value( idx )})}); nil };

		while({ 
			index  = (high + low) div: 2;
			low   <= high;
		}, {
			pt = packets.at( index )[0];
			if( pt == t, { ^index });
			if( pt < t, {
				low = index + 1;
			}, {
				high = index - 1;
			});
		});
//		^(low.neg - 1);
		^low;
	}
	
	findMessage { arg cmd, startIndex = 0, stopIndex;
		var idx, p;
		
		^block { arg break;
			cmd		= cmd.asSymbol;
			idx		= startIndex;
			stopIndex	= stopIndex ?? { packets.size };
			while({ idx < stopIndex }, {
				p = packets[ idx ];
				if( p[0].isFloat, {
					p.drop(1).do({ arg msg;
						if( msg.first === cmd, { break.value( msg )});
					});
				}, {
					if( p.first === cmd, { break.value( p )});
				});
				idx = idx + 1;
			});
			nil;
		};
	}
	
	prRead { arg path;
		var file, n;
		packets = nil;
		file = File( path, "rb" );
		protect {
			while({ (file.pos + 4) <= file.length }, {
				n = file.getInt32;
				if( n >= 4, {
					packets = packets.add( this.prReadPacket( file ));
				});
			});
			startTime	= block { arg break; packets.do({ arg p; if( p[0].isFloat, { break.value( p[0] )})}); nil };
			stopTime	= block { arg break; packets.reverseDo({ arg p; if( p[0].isFloat, { break.value( p[0] )})}); nil };
		} {
			file.close;
		};
	}

	prReadPacket { arg file;
		var n2, len, cmd, time1, time2, bndl, oldPos, newPos;

		cmd = file.getString0;
		len = (cmd.size + 4) & -4;
//		n = n - len;
		file.seek( len - (cmd.size + 1), 1 );
		if( cmd == "#bundle", {
			time1	= file.getInt32;
			time2	= file.getInt32;
			if( time2 >= 0, {
				time2 = time2 / 4294967296.0;
			}, { // getInt32 is signed only!!!
				time2 = time2 / 4294967296.0 + 1.0;
			});
			n2		= file.getInt32;
//			n		= n - 12;
			bndl		= [ time1 + time2 ];
//("time = " ++ bndl[0].round( 0.01 )).postln;
			oldPos	= file.pos;
			while({ n2 > 4 }, {
				bndl	= bndl.add( this.prReadPacket( file ));
				newPos = file.pos;
				n2	= n2 - (newPos - oldPos);
				oldPos = newPos;
			});
			^bndl;
		}, {
			^this.prReadMsg( file, cmd );
		});

	}
	
	prReadMsg { arg file, cmd;
		var len, typeTags, msg, str;

		typeTags	= file.getString0;
		len		= (typeTags.size + 4) & -4;
		file.seek( len - (typeTags.size + 1), 1 );
		msg = Array( typeTags.size );
		typeTags = typeTags.drop(1); // remove leading comma
		msg.add( cmd.asSymbol );
		typeTags.do({ arg type;
			switch( type,
			$i, { msg.add( file.getInt32 )},
			$f, { msg.add( file.getFloat )},
			$s, { str = file.getString0; msg.add( str.asSymbol );
				len = (str.size + 4) & -4; file.seek( len - (str.size + 1), 1 )},
			{ MethodError( "Unknown type tag '" ++ type ++ "'", thisMethod ).throw });
		});
		^msg;
	}
}
