package de.sciss.dsp;

import dsp.RemezFIRFilter;

public class BQFFB
{

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
//		int filterOrder=23;
//		double passbandEdge=0.4;
		final RemezFIRFilter flt = new RemezFIRFilter();
	    int numBands = 2;
	    double[] desired = new double[numBands];
	    double[] bands   = new double[2*numBands];
	    double[] weights = new double[numBands];
	    double f2 = 0.249;
	    double trband = 0.249; // 0.02;

	    float ripple = 6; // dB
	    float atten = 60; // dB
	    double deltaP = 0.5 * (1.0 - Math.pow( 10.0, -0.05 * ripple ));
	    double deltaS = Math.pow( 10.0, -0.05 * atten );
	    double rippleRatio = deltaP / deltaS;

//	    int order = (int) Math.round((-10 * Math.log10( deltaP * deltaS ) - 13) / (14.6 * trband ));
int order = 23;

	    desired[0] = 1.0;
	    desired[1] = 0.0;
	    bands[0] = 0.0;
	    bands[1] = f2;
	    bands[2] = f2 + trband;
	    bands[3] = 0.5;
//	    desired[0] = 0.5;
//	    desired[1] = 0.5;
//	    bands[0] = 0.0;
//	    bands[1] = 0.8;
//	    bands[2] = 1;
//	    bands[3] = 1;
	    weights[0] = 1.0;
	    weights[1] = 1.0; // rippleRatio;

		double[] res = flt.remez( order, bands, desired, weights, RemezFIRFilter.BANDPASS );
		
		double max = 0.0;
		for( int i = 0; i < res.length; i++ ) max = Math.max( max, Math.abs( res[ i ]));
		double gain = 1.0 / max;
		for( int i = 0; i < res.length; i++ ) res[ i ] *= gain;
		
		for( int i = 0; i < res.length; i++ ) {
			System.out.println ("[" + i + "] : " + res[ i ]);
		}
		
//		[initH, ripple] = remez(filterOrder, [0 2*passbandEdge 1 1], ...
//					[0.5 0.5 0 0], {256})

	}
}
