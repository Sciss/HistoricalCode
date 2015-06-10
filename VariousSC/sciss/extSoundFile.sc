/*
 *	Changelog:
 *		21-Jun-09  seems seek already uses frames, so seekFrame now assumes that!
 */
 
/**
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 05-Nov-08
 */
+ SoundFile {
	readFrames { arg buf, off, len;
		var raw, buf2;
		raw = Signal.newClear( len * numChannels );
		this.readData( raw );
		buf2 = this.prUnlace( raw );
		numChannels.do({ arg chan;
//			[ "overwrite", buf[ chan ].class.name, buf2[ chan ].class.name, off ].postln;
			buf[ chan ].overWrite( buf2[ chan ], off );
		});
	}
	
	seekFrame { arg frame;
//		this.seek( frame * numChannels );
		this.seek( frame );
	}
	
	writeFrames { arg buf, off, len;
		var raw, buf2;
		buf2 = Array.fill( numChannels, {ÊSignal.newClear( len )});
		numChannels.do({ arg chan;
			buf2[ chan ].overWrite( buf[ chan ], off.neg );
		});
		raw = this.prLace( buf2 );
		this.writeData( raw );
	}
	
	*newUsing {Êarg aSoundFile;
		^SoundFile.new
			.headerFormat_( aSoundFile.headerFormat )
			.sampleFormat_( aSoundFile.sampleFormat )
			.numChannels_( aSoundFile.numChannels )
			.sampleRate_( aSoundFile.sampleRate )
			.path_( aSoundFile.path );
	}

	prLace { arg sig;
		var frames, numSamples, raw;
		
		frames		= sig[0].size;
		numSamples	= frames * numChannels;
		raw			= Signal( numSamples );
		numSamples.do({ arg i;
			raw.add( sig[ i % numChannels ][ i div: numChannels ]);
		});
		^raw;
	}
	
	prUnlace { arg sig;
		var numSamples, numFrames, buf;
		
		numSamples	= sig.size;
		numFrames		= numSamples div: numChannels;
		buf			= Array.fill( numChannels, { Signal( numFrames )});
		sig.do({ arg x, i;
			buf[ i % numChannels ].add( x );
		});
		^buf;
	}
}