/*
 *  Warp.java
 *  (JCollider)
 *
 *  Copyright (c) 2004-2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */
 
package de.sciss.jcollider.gui;

/**
 *	Helper classes for warping a control value.
 *	This is a quite direct translation from the SClang classes.
 *
 *	@version	0.28, 29-Jul-06
 *	@author		Hanns Holger Rutz
 *
 *	@see		ControlSpec
 */
public abstract class Warp
{
	public static Warp lin	= new LinearWarp();
	public static Warp exp	= new ExponentialWarp();
	public static Warp sin	= new SineWarp();
	public static Warp cos	= new CosineWarp();
//	public static Warp amp	= new FaderWarp();
//	public static Warp db	= new DbFaderWarp();

	public static Warp curve( double curve )
	{
		if( curve == 0.0 ) {
			return lin;
		} else {
			return new CurveWarp( curve );
		}
	}

	public abstract double map( double value, ControlSpec spec );
	public abstract double unmap( double value, ControlSpec spec );
	
	private static class LinearWarp
	extends Warp
	{
		protected LinearWarp() { /* empty */ }
		
		public double map( double value, ControlSpec spec )
		{
			return( value * spec.getRange() + spec.getMinVal() );
		}

		public double unmap( double value, ControlSpec spec )
		{
			final double range = spec.getRange();
			
			if( range == 0.0 ) {
				return 0.0;
			} else {
				return(( value - spec.getMinVal() ) / range );
			}
		}
	}

	// minval and maxval must both be non zero and have the same sign.
	private static class ExponentialWarp
	extends Warp
	{
		protected ExponentialWarp() { /* empty */ }

		public double map( double value, ControlSpec spec )
		{
			return( Math.pow( spec.getRatio(), value ) * spec.getMinVal() );
		}
		
		public double unmap( double value, ControlSpec spec )
		{
			return( Math.log( value / spec.getMinVal() ) / Math.log( spec.getRatio() ));
		}
	}

	private static class SineWarp
	extends Warp
	{
		private static final double PIH = Math.PI * 0.5;
	
		protected SineWarp() { /* empty */ }

		public double map( double value, ControlSpec spec )
		{
			return( Math.sin( PIH * value ) * spec.getRange() + spec.getMinVal() );
		}

		public double unmap( double value, ControlSpec spec )
		{
			final double range = spec.getRange();
			
			if( range == 0.0 ) {
				return 0.0;
			} else {
				return( Math.asin(( value - spec.getMinVal() ) / range ) / PIH );
			}
		}
	}

	private static class CosineWarp
	extends Warp
	{
		private static final double PIH = Math.PI * 0.5;
	
		protected CosineWarp() { /* empty */ }

		public double map( double value, ControlSpec spec )
		{
			return( (0.5 - Math.cos( Math.PI * value ) * 0.5) * spec.getRange() + spec.getMinVal() );
		}

		public double unmap( double value, ControlSpec spec )
		{
			final double range = spec.getRange();
			
			if( range == 0.0 ) {
				return 0.0;
			} else {
				return( Math.acos( 1.0 - (( value - spec.getMinVal() ) / range ) * 2.0 ) / PIH );
			}
		}
	}

	private static class CurveWarp
	extends Warp
	{
		private final double curve;
		private final double grow;
		private final double oneByOneMGrow;
	
		protected CurveWarp( double curve )
		{
			this.curve		= curve;
			grow			= Math.exp( curve );
			oneByOneMGrow	= 1.0 / (1.0 - grow);
		}
		
		public double map( double value, ControlSpec spec )
		{
			final double a = spec.getRange() * oneByOneMGrow;
			final double b = spec.getMinVal() + a;

			return( b - a * Math.pow( grow, value ));
		}
		
		public double unmap( double value, ControlSpec spec )
		{
			final double a = spec.getRange() * oneByOneMGrow;
			final double b = spec.getMinVal() + a;

			return( Math.log(( b - value ) / a ) / curve );
		}
	}
}