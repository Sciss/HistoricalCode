/**
 *	@author	Hanns Holger Rutz
 *	@version	0.14, 04-Jul-09
 */
OhrwaldPiece {
	var <>file;				// String : fileName (without folder)
	var <>gain	= 0;			// Float : relative gain in decibels
	var <>order	= \normal;	// Symbol : either of \normal, \circ
	var <>minDur, <>maxDur;		// Float : in seconds
	var <>numChannels;			// Integer : usually gets calculated by callig calcNumChannels
	var <>channelOffset = 0;	// Integer
	var <>chance = 10;			// Integer : chance that piece gets chosen (ratio to other pieces is relevant)
	var <>mixProb = 0.25;		// Float : probability from 0 to 1 that piece gets mixed with another piece
	var <>disallowedMixes;		// Nil or Array[ Symbol ] : names of pieces with which this piece should not be mixed
	var <>fadeFactor = 1.0;		// Float
	var <>spkrGains;
	var <>spkrEQs;
	
	// player support
	var <>synth, <>buf;
	var <>bus = false;			// Boolean: true for BusB, false for BusA
	
	*new { arg file;
		^super.new.prInit( file );
	}
	
	prInit { arg argFile;
		file = argFile;
	}
	
	path { ^(Ohrwald.workDir +/+ "bounce" +/+ file) }
	name { var str = file.splitext.first; str[ 0 ] = str[ 0 ].toUpper; ^str }
	
	calcNumChannels {
		var sf;
		sf = SoundFile.openRead( this.path ); sf.close;
		numChannels = sf.numChannels;
	}

	storeArgs { ^[ file ]}

	storeModifiersOn { arg stream;
		if( gain != 0, {
			stream << ".gain_( ";
			gain.storeOn( stream );
			stream << " )";
		});
		if( order != \normal, {
			stream << ".order_( ";
			order.storeOn( stream );
			stream << " )";
		});
		if( chance != 10, {
			stream << ".chance_( ";
			chance.storeOn( stream );
			stream << " )";
		});
		if( mixProb != 0.25, {
			stream << ".mixProb_( ";
			mixProb.storeOn( stream );
			stream << " )";
		});
		if( fadeFactor != 1.0, {
			stream << ".fadeFactor_( ";
			fadeFactor.storeOn( stream );
			stream << " )";
		});
		if( disallowedMixes.size > 0, {
			stream << ".disallowedMixes_( ";
			disallowedMixes.storeOn( stream );
			stream << " )";
		});
		if( spkrGains.notNil, {
			stream << ".spkrGains_( ";
			spkrGains.storeOn( stream );
			stream << " )";
		});
		if( spkrEQs.notNil, {
			stream << ".spkrEQs_( ";
			spkrEQs.storeOn( stream );
			stream << " )";
		});
		stream << ".minDur_( ";
		minDur.storeOn( stream );
		stream << " )";
		stream << ".maxDur_( ";
		maxDur.storeOn( stream );
		stream << " )";
		if( channelOffset != 0, {
			stream << ".channelOffset_( ";
			channelOffset.storeOn( stream );
			stream << " )";
		});
	}

	printOn { arg stream;
		stream << ("OhrwaldPiece( " ++ this.name ++ " )");
	}
}