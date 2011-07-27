/**
 *	@author	Hanns Holger Rutz
 *	@version	0.11, 30-Jul-08
 */
EGMFullbody {
	classvar bones, numBones;
	var msg;
	var minx, miny, minz, maxx, maxy, maxz;
	
	*initClass {
		bones = IdentityDictionary.new;
		[ \pelvis, \lfemur, \ltibia, \lfoot, \rfemur, \rtibia, \rfoot,
		  \thorax, \head, \lclavicle, \lhumerus, \lradius, \lhand,
		  \rclavicle, \rhumerus, \rradius, \rhand ].do({ arg name, i;
		 
			bones.put( name, i );
		});
		numBones = bones.size;
	}

	*new { arg trackerMsg;
		^super.new.prInit( trackerMsg );
	}
	
	prInit { arg argMsg;
		msg = argMsg.copyToEnd( 2 );
		if( msg.size != 102, {
			("msg.size should be 102, but is " ++msg.size).warn;
		});
	}
	
	data { ^msg }
	
	a { arg name;
		var i = bones[ name ] * 6;
		^msg.copyRange( i, i + 2 );
	}

	ax { arg name;
		var i = bones[ name ] * 6;
		^msg[ i ];
	}
	
	ay { arg name;
		var i = bones[ name ] * 6;
		^msg[ i + 1 ];
	}
	
	az { arg name;
		var i = bones[ name ] * 6;
		^msg[ i + 2 ];
	}
	
	t { arg name;
		var i = bones[ name ] * 6;
		^msg.copyRange( i + 3, i + 5 );
	}
		
	tx { arg name;
		var i = bones[ name ] * 6;
		^msg[ i + 3 ];
	}
	
	ty { arg name;
		var i = bones[ name ] * 6;
		^msg[ i + 4 ];
	}
	
	tz { arg name;
		var i = bones[ name ] * 6;
		^msg[ i + 5 ];
	}
	
	dist { arg name1, name2;
		var t1, t2, dx, dy, dz;
		t1 = this.t( name1 );
		t2 = this.t( name2 );
		dx = t2[ 0 ] - t1[ 0 ];
		dy = t2[ 1 ] - t1[ 1 ];
		dz = t2[ 2 ] - t1[ 2 ];
		^sqrt( dx.squared + dy.squared + dz.squared );
	}

	hdist { arg name1, name2;
		var t1, t2, dx, dy;
		t1 = this.t( name1 );
		t2 = this.t( name2 );
		dx = t2[ 0 ] - t1[ 0 ];
		dy = t2[ 1 ] - t1[ 1 ];
		^sqrt( dx.squared + dy.squared );
	}

	vdist { arg name1, name2;
		var t1, t2, dz;
		t1 = this.t( name1 );
		t2 = this.t( name2 );
		dz = t2[ 2 ] - t1[ 2 ];
		^dz;
	}
	
//	volume { arg name1, name2, name3;
//	
//	}

	angle { arg name1, name2, name3, name4;
		var t1, t2, t3, t4;
		t1 = this.t( name1 );
		t2 = this.t( name2 );
		t3 = this.t( name3 );
		t4 = this.t( name4 );
		^this.prAngle( t1, t2, t3, t4 );
	}

	angleDeg { arg name1, name2, name3, name4;
		^(this.angle( name1, name2, name3, name4 ) * 180 / pi);
	}
	
	bent { arg name1, name2;
		var t1, t2;
		t1 = this.t( name1 );
		t2 = this.t( name2 );
		^this.prAngle( t1, t2, [ 0, 0, 0 ], [ 0, 0, 1 ]);
	}
	
	bentDeg { arg name1, name2;
		^(this.bent( name1, name2 ) * 180 / pi);
	}
	
	rota { arg name1, name2;
		var t1, t2;
		t1 = this.t( name1 );
		t2 = this.t( name2 );
		^this.prAngle( t1, t2, [ 0, 0, 0 ], [ 1, 0, 0 ]);
	}
	
	rotaDeg { arg name1, name2;
		^(this.rota( name1, name2 ) * 180 / pi);
	}
	
	prAngle { arg t1, t2, t3, t4;
		var dotProd, dx1, dy1, dz1, dx2, dy2, dz2, dist1, dist2, cosine;
		dx1 = t2[ 0 ] - t1[ 0 ];
		dy1 = t2[ 1 ] - t1[ 1 ];
		dz1 = t2[ 2 ] - t1[ 2 ];
		dx2 = t4[ 0 ] - t3[ 0 ];
		dy2 = t4[ 1 ] - t3[ 1 ];
		dz2 = t4[ 2 ] - t3[ 2 ];
		dotProd = (dx1 * dx2) + (dy1 * dy2) + (dz1 * dz2);
		dist1 = sqrt( dx1.squared + dy1.squared + dz1.squared );
		dist2 = sqrt( dx2.squared + dy2.squared + dz2.squared );
		cosine = dotProd / (dist1 * dist2);
		^acos( cosine );
	}

	// in m^3
	boundingVolume {
		if( minx.isNil, {
			this.prCalcMinMax;
		});
		
		^((maxx - minx) / 1000 * (maxy - miny) / 1000 * (maxz - minz) / 1000);
	}

	// in m^2
	boundingArea {
		if( minx.isNil, {
			this.prCalcMinMax;
		});
		
		^((maxx - minx) / 1000 * (maxy - miny) / 1000);
	}

	// in m
	boundingHeight {
		if( minx.isNil, {
			this.prCalcMinMax;
		});
		
		^((maxz - minz) / 1000);
	}
	
	asymmetry {
		^sqrt(
		 ((this.dist( \lfemur, \pelvis ) - this.dist( \rfemur, \pelvis )).squared +
		  (this.dist( \ltibia, \pelvis ) - this.dist( \rtibia, \pelvis )).squared +
		  (this.dist( \lfoot, \pelvis ) - this.dist( \rfoot, \pelvis )).squared +
		  (this.dist( \lhumerus, \pelvis ) - this.dist( \rhumerus, \pelvis )).squared +
		  (this.dist( \lradius, \pelvis ) - this.dist( \rradius, \pelvis )).squared +
		  (this.dist( \lhand, \pelvis ) - this.dist( \rhand, \pelvis )).squared) / 6 );
	}
	
	prCalcMinMax {
		var off;
		
		off	= 3;
		minx	= msg[ off ];
		maxx	= msg[ off ];
		off  = off + 1;
		miny	= msg[ off ];
		maxy	= msg[ off ];
		off  = off + 1;
		minz	= msg[ off ];
		maxz	= msg[ off ];
		off  = off + 4;
		(numBones - 1).do({
			minx = min( minx, msg[ off ]);
			maxx = max( maxx, msg[ off ]);
			off  = off + 1;
			miny = min( miny, msg[ off ]);
			maxy = max( maxy, msg[ off ]);
			off  = off + 1;
			minz = min( minz, msg[ off ]);
			maxz = max( maxz, msg[ off ]);
			off  = off + 4;
		});
	}
}