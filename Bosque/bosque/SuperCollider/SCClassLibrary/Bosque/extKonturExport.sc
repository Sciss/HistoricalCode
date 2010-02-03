+ BosqueSession {
	konturExport {
		var doc, elem, exp, elemIDCount, elemAudioFiles, elemColl, elemDiffusions,
		    elemTimelines, elemTimeline, elemTracks;
		doc = DOMDocument.new;
		exp = BosqueKonturExport( doc );
		elem				= exp.createElement( "konturSession" );
		elemAudioFiles	= exp.createElement( "audioFiles" );
		elemAudioFiles.appendChild( audioFiles.konturExport( exp ));
		elemDiffusions	= exp.createElement( "diffusions" );
		elemDiffusions.appendChild( busConfigs.konturExport( exp ));
		elemTimelines		= exp.createElement( "timelines" );
		elemColl			= exp.createElement( "coll" );
		elemTimelines.appendChild( elemColl );
		elemTimeline		= timeline.konturExport( exp );
		elemColl.appendChild( elemTimeline );
		elemTracks		= exp.createElement( "tracks" );
		exp.setAttribute( elemTracks, "id", exp.createID );
		elemTracks.appendChild( tracks.konturExport( exp ));
		elemTimeline.appendChild( elemTracks );
		elemIDCount		= exp.createElementWithText( "idCount", exp.idCount );
		elem.appendChild( elemIDCount );
		elem.appendChild( elemAudioFiles );
		elem.appendChild( elemDiffusions );
		elem.appendChild( elemTimelines );
		doc.appendChild( elem );
		^("<?xml version='1.0' encoding='UTF-8'?>\n" ++ doc.format);
	}
}

+ BosqueSessionCollection {
	konturExport { arg exp;
		var elemColl = exp.createElement( "coll" );
		coll.do({ arg e;
			elemColl.appendChild( e.konturExport( exp ));
		});
		^elemColl;
	}
}

+ Span {
	konturExport { arg exp;
		var elem;
		elem = exp.createElement( "span" );
		exp.setAttribute( elem, "start", start );
		exp.setAttribute( elem, "stop", stop );
		^elem;
	}
}

+ BosqueTrack {
	konturExport { arg exp;
		var elem, matrix, str, elemDiffusion, elemTrail;
		elem = exp.createElement( "audioTrack" );
		exp.setAttribute( elem, "id", exp.createID );
		elem.appendChild( exp.createElementWithText( "name", name ));
		if( busConfig.notNil, {
			elemDiffusion = exp.createElement( "diffusion" );
			exp.setAttribute( elemDiffusion, "idref", exp.get( busConfig ));
			elem.appendChild( elemDiffusion );
		});
		exp.put( \track, this );
		elem.appendChild( trail.konturExport( exp ));
		^elem;
	}
}

+ BosqueTrail {
	konturExport { arg exp;
		var elemColl, track;
		track    = exp.get( \track );
		elemColl = exp.createElement( "trail" );
		collStakesByStart.select({ arg stake;
			stake.respondsTo( \konturExport ) and: { stake.track == track }}).do({ arg e;
				elemColl.appendChild( e.konturExport( exp ));
		});
		^elemColl;
	}
}

+ BosqueAudioRegionStake {
	konturExport { arg exp;
		var elem, elemAudioFile, elemFade;
		elem = exp.createElement( "stake" );
		elem.appendChild( span.konturExport( exp ));
		elemAudioFile = exp.createElement( "audioFile" );
		exp.setAttribute( elemAudioFile, "idref", exp.get( audioFile ));
		elem.appendChild( elemAudioFile );
		elem.appendChild( exp.createElementWithText( "offset", fileStartFrame ));
		if( gain != 1.0, { elem.appendChild( exp.createElementWithText( "gain", gain ))});
		if( fadeIn.notNil and: { fadeIn.numFrames > 0 }, {
			elemFade = exp.createElement( "fadeIn" );
			elemFade.appendChild( fadeIn.konturExport( exp ));
			elem.appendChild( elemFade );
		});
		if( fadeOut.notNil and: { fadeOut.numFrames > 0 }, {
			elemFade = exp.createElement( "fadeOut" );
			elemFade.appendChild( fadeIn.konturExport( exp ));
			elem.appendChild( elemFade );
		});
		^elem;
	}
}

+ BosqueFade {
	konturExport { arg exp;
		var elem, elemShape;
		elem = exp.createElement( "fade" );
		elem.appendChild( exp.createElementWithText( "numFrames", numFrames ));
		elemShape = exp.createElement( "shape" );
		exp.setAttribute( elemShape, "num", Env.shapeNumber( type ));
		exp.setAttribute( elemShape, "curve", curve );
		elem.appendChild( elemShape );
		^elem;
	}
}

+ BosqueBusConfig {
	konturExport { arg exp;
		var elem, matrix, str, id;
		elem = exp.createElement( "diffusion" );
		id = exp.createID;
		exp.setAttribute( elem, "id", id );
		elem.appendChild( exp.createElementWithText( "name", name ));
		elem.appendChild( exp.createElementWithText( "numInputChannels", numInputs ));
		elem.appendChild( exp.createElementWithText( "numOutputChannels", numOutputs ));
		matrix = exp.createElement( "matrix" );
		connections.do({ arg row;
			str = "";
			row.do({ arg c; str = str + c.asString });
			str = str.copyToEnd( 1 );
			matrix.appendChild( exp.createElementWithText( "row", str ));
		});
		elem.appendChild( matrix );
		exp.put( this, id );
		^elem;
	}
}

+ BosqueTimeline {
	konturExport { arg exp;
		var elem;
		elem = exp.createElement( "timeline" );
		exp.setAttribute( elem, "id", exp.createID );
		elem.appendChild( exp.createElementWithText( "name", "Timeline" ));
		elem.appendChild( span.konturExport( exp ));
		elem.appendChild( exp.createElementWithText( "rate", rate ));
		^elem;
	}
}

+ BosqueAudioFile {
	konturExport { arg exp;
		var elem, sf, id;
		elem = exp.createElement( "audioFile" );
		id = exp.createID;
		exp.setAttribute( elem, "id", id );
		elem.appendChild( exp.createElementWithText( "path", path ));
		elem.appendChild( exp.createElementWithText( "numFrames", numFrames ));
		elem.appendChild( exp.createElementWithText( "numChannels", numChannels ));
		sf = SoundFile.openRead( path ); sf.close;
		elem.appendChild( exp.createElementWithText( "sampleRate", sf.sampleRate ));
		exp.put( this, id );
		^elem;
	}
}
