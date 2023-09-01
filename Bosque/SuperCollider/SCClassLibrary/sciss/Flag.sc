Flag {
	var <>value;
	
	*new { arg value = false;
		^super.newCopyArgs( value );
	}
}