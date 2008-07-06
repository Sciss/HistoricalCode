/**
 *	(C)opyright 2007 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: (none)
 */
Assertion {
	classvar <>enabled = true;

	*new { arg expression, message;
		if( enabled and: { expression.value.not }, { AssertionError( message ).throw });
	}
}
