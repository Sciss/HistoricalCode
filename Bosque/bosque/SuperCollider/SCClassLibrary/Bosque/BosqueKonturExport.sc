BosqueKonturExport {
	var <idCount = 0;
	var domDoc, dict;

	*new { arg domDoc;
		^super.new.prInit( domDoc );	
	}
	
	prInit { arg argDomDoc;
		domDoc	= argDomDoc;
		dict		= IdentityDictionary.new;
	}
	
	put {Êarg obj, id;
		dict[ obj ] = id;
	}
	
	get { arg obj; ^dict[ obj ]}

	createElement { arg name;
		^domDoc.createElement( name );
	}
	
	createElementWithText { arg name, text;
		var elem, txtNode;
		elem		= domDoc.createElement( name );
		txtNode	= domDoc.createTextNode( text.asString );
		elem.appendChild( txtNode );
		^elem;
	}

	setAttribute { arg elem, name, value;
		elem.setAttribute( name, value.asString );
	}
		
	createID {
		var result = idCount;
		idCount = idCount + 1;
		^result
	}
}