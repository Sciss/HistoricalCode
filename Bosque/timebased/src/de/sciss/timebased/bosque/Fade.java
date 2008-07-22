package de.sciss.timebased.bosque;

public class Fade {
	public static final int TYPE_LINEAR	= 0;
	public static final int TYPE_EQP	= 1;
	private static final int TYPE_MAXID	= 1;

	public final int type;
	public final int numFrames;
	public final float curve;
	
	public Fade( int type, int numFrames, float curve )
	{
		if( (type < 0) || (type > TYPE_MAXID) ) throw new IllegalArgumentException( String.valueOf( type ));
		if( numFrames < 0 ) throw new IllegalArgumentException( String.valueOf( numFrames ));
		if( (curve < -1f) || (curve > 1f) ) throw new IllegalArgumentException( String.valueOf( curve ));
		
		this.type		= type;
		this.numFrames	= numFrames;
		this.curve		= curve;
	}
	
	public Fade replaceType( int newType )
	{
		return new Fade( newType, numFrames, curve );
	}

	public Fade replaceNumFrames( int newFrames )
	{
		return new Fade( type, newFrames, curve );
	}

	public Fade replaceCurve( float newCurve )
	{
		return new Fade( type, numFrames, newCurve );
	}
}
