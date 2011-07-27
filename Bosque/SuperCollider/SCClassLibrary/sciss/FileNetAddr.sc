/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.11, 23-May-08
 *
 *	@warning	bundle times are ignored at the moment
 *			(messages are supposed to be executed sequentially
 *			 as they are written to the file)
 */
FileNetAddr : NetAddr {
	var <file;
	var pending, pendingSize = 16;
	var <>time;
	var <>autoAdvanceTime = false;
	
	classvar maxBundleSize	= 8192;

	*new { arg file;
		^super.new.prInitFileNetAddr( file );
	}
	
	prInitFileNetAddr { arg argFile;
		file		= argFile;
		pending	= List.new;
	}
	
	*openWrite { arg path;
		var f;
		
		f = File( path, "wb" );
		^this.new( f );
	}

	sendRaw { arg rawArray;
		this.prFlushMessages;
		file.write( rawArray.size );
		file.write( rawArray );
	}
	sendMsg { arg ... args;
		var raw, rawSize;
		
		raw		= args.asRawOSC;
		rawSize	= raw.size + 4;
		if( (pendingSize + rawSize) > maxBundleSize, {
			this.prFlushMessages;
		});
		pending.add( args );
		pendingSize = pendingSize + rawSize;
	}
	sendBundle { arg time ... args;
		var raw;
		this.prFlushMessages;
		time		= time ?? { this.time };
		raw		= ([ time ] ++ args).asRawOSC;
		if( raw.size > maxBundleSize, {
			("WARNING: bundle too big (" ++ raw.size ++ ")").postln;
		});
		file.write( raw.size );
		file.write( raw );
		if( autoAdvanceTime, {
			this.time_( time );
		});
	}
	
	closeFile {
		this.prFlushMessages;
		file.close;
		^file;
	}
	
	prFlushMessages {
		var raw;
		if( pending.notEmpty, {
			raw	= ([ time ] ++ pending).asRawOSC;
			file.write( raw.size );
			file.write( raw );
			pending.clear;
			pendingSize = 16;
		});
	}
		
	// called from Server -> cmdPeriod
	recover {
		^this;
	}
}