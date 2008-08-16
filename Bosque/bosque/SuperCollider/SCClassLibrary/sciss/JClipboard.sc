/**
 *	(C)opyright 2007 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 16-Aug-07
 */
JClipboard {
	var <name;

	var cbOwner, cbContents;
	
	*new { arg name;
		^super.newCopyArgs( name );
	}
	
	storeArgs { ^[ name ]}

	clear { this.setContents( nil, nil )}

	getContents { arg requestor; ^cbContents }
	
	setContents { arg contents, owner;
		var oldOwner		= cbOwner;
		var oldContents	= cbContents;
		
		cbOwner			= owner;
		cbContents		= contents;
	
		if( oldOwner.notNil and: { oldOwner != owner }, {
			oldOwner.lostOwnership( this, oldContents );
		});
	}
	
	availableDataFlavors {
		if( this.getContents.isNil, { ^nil }, { ^this.getContents.transferDataFlavors });
	}

	isDataFlavorAvailable { arg flavor;
		var cntnts = this.getContents;
		if( flavor.isNil, { MethodError( "Flavor is nil", thisMethod ).throw });

		if( cntnts.isNil, { ^false });
		^cntnts.isDataFlavorSupported( flavor );
	}

	getData { arg flavor;
		var cntnts = this.getContents;
		if( flavor.isNil, { MethodError( "Flavor is nil", thisMethod ).throw });
		if( cntnts.isNil, { MethodError( "Unsupported Flavor " ++ flavor, thisMethod ).throw });
		^cntnts.getTransferData( flavor );
    }
}