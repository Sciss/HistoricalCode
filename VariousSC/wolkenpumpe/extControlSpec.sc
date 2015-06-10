+ ControlSpec {
	// changed to work with UGen minval, maxval
	init {
		warp = warp.asWarp(this);
		clipLo = min( minval, maxval );
		clipHi = max( minval, maxval );
	}
}