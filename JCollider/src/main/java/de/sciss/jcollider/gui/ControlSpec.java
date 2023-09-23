/*
 *  ControlSpec.java
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
 *	Specification for a control input such as a synth control.
 *	This is a quite direct translation from the SClang class.
 *	Specs are immutable though (minVal and maxVal can't be changed).
 *
 *	@version	0.28, 29-Jul-06
 *	@author		Hanns Holger Rutz
 */
public class ControlSpec
{
	private final double	minVal, maxVal, clipLo, clipHi, step, defaultVal;
	private final String	units;
	private final Warp		warp;
	
	public static final ControlSpec	defaultSpec	= new ControlSpec();

	public ControlSpec()
	{
		this( 0.0, 1.0 );
	}
	
	public ControlSpec( double minVal, double maxVal )
	{
		this( minVal, maxVal, Warp.lin );
	}
	
	public ControlSpec( double minVal, double maxVal, Warp warp )
	{
		this( minVal, maxVal, warp, 0.0 );
	}
	
	public ControlSpec( double minVal, double maxVal, Warp warp, double step )
	{
		this( minVal, maxVal, warp, step, minVal );
	}

	public ControlSpec( double minVal, double maxVal, Warp warp, double step, double defaultVal )
	{
		this( minVal, maxVal, warp, step, defaultVal, null );
	}
	
	public ControlSpec( double minVal, double maxVal, Warp warp, double step, double defaultVal, String units )
	{
		this.minVal		= minVal;
		this.maxVal		= maxVal;
		this.warp		= warp;
		this.step		= step;
		this.defaultVal	= defaultVal;
		this.units		= units;
		
		clipLo			= Math.min( minVal, maxVal );
		clipHi			= Math.max( minVal, maxVal );
	}
	
	public double getMinVal()
	{
		return minVal;
	}

	public double getMaxVal()
	{
		return maxVal;
	}
	
	public double getClipLo()
	{
		return clipLo;
	}

	public double getClipHi()
	{
		return clipHi;
	}
	
	public double getDefaultVal()
	{
		return defaultVal;
	}
		
	public double getStep()
	{
		return step;
	}
		
	public Warp getWarp()
	{
		return warp;
	}
	
	public String getUnits()
	{
		return units;
	}
		
	public double constrain( double value )
	{
		value = Math.max( clipLo, Math.min( clipHi, value ));
		
		if( step == 0.0 ) {
			return value;
		} else {
			return Math.round( value / step ) * step;
		}
	}
	
	public double getRange()
	{
		return( maxVal - minVal );
	}
	
	public double getRatio()
	{
		return( maxVal / minVal );
	}
	
	// maps a value from [0..1] to spec range
	public double map( double value )
	{
		value	= warp.map( Math.max( 0.0, Math.min( 1.0, value )), this );
		
		if( step == 0.0 ) {
			return value;
		} else {
			return Math.round( value / step ) * step;
		}
	}

	// maps a value from spec range to [0..1]
	public double unmap( double value )
	{
		if( step != 0.0 ) {
			value = Math.round( value / step ) * step;
		}
		return warp.unmap( Math.max( clipLo, Math.min( clipHi, value )), this );
	}

//	*initClass {
//		specs = specs.addAll([
//			// set up some ControlSpecs for common mappings
//			// you can add your own after the fact.
//			
//			unipolar -> ControlSpec(0, 1),
//			bipolar -> ControlSpec(-1, 1, default: 0),
//
//			\\freq -> ControlSpec(20, 20000, \\exp, 0, 440, units: " Hz"),
//			\\lofreq -> ControlSpec(0.1, 100, \\exp, 0, 6, units: " Hz"),
//			\\midfreq -> ControlSpec(25, 4200, \\exp, 0, 440, units: " Hz"),
//			\\widefreq -> ControlSpec(0.1, 20000, \\exp, 0, 440, units: " Hz"),
//			\\phase -> ControlSpec(0, 2pi),
//			\\rq -> ControlSpec(0.001, 2, \\exp, 0, 0.707),
//
//			\\audiobus -> ControlSpec(0, 128, step: 1),
//			\\controlbus -> ControlSpec(0, 4096, step: 1),
//
//			\\midi -> ControlSpec(0, 127, default: 64),
//			\\midinote -> ControlSpec(0, 127, default: 60),
//			\\midivelocity -> ControlSpec(1, 127, default: 64),
//			
//			\\db -> ControlSpec(0.ampdb, 1.ampdb, \\db, units: " dB"),
//			\\amp -> ControlSpec(0, 1, \\amp, 0, 0),
//			\\boostcut -> ControlSpec(-20, 20, units: " dB"),
//			
//			\\pan -> ControlSpec(-1, 1, default: 0),
//			\\detune -> ControlSpec(-20, 20, default: 0, units: " Hz"),
//			\\rate -> ControlSpec(0.125, 8, \\exp, 0, 1),
//			\\beats -> ControlSpec(0, 20, units: " Hz"),
//			
//			\\delay -> ControlSpec(0.0001, 1, \\exp, 0, 0.3, units: " secs")
//		]);
//	}
}
