/*
 * $Id: window.c,v 1.17 2003/03/11 18:03:29 niklaswerner Exp $
 *
 * Copyright (C) 2002-2003 Niklas Werner <niklas@niklaswerner.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Calculate The Window-coefficients once and put them all in one array
 *
 * For Formulae see
 * A. V. Oppenheim & R. W. Schafer, "Discrete-Time Signal Processing"
 * pp 468 - 474
 * and octave and octave-forge source-code
 *
 */
#include "sonasound.h"
#include "window.h"


void getWindowKoeff() {
	gint j = 0, k;

	k = (pointsFFT < framesProBuffer) ? pointsFFT: framesProBuffer;
	for (j = 0; j < k; j++) {
	/* Hamming */
		windowKoeff[hamming * k + j] = 0.54f - 0.46f * cos(2.0f * pi * j / (k));
	/* Hanning */
		windowKoeff[hanning * k + j] = ( 0.5f - 0.5f * cos(2.0f * pi *  j / (k)));
	/* Blackman */
		windowKoeff[blackman * k + j] = 0.42f - 0.5f * cos(2.0f * pi * j / ( k)) + 0.08f * cos(4.0f * pi * j / (k));
	/* Triangle (Bartlett) */
		if(j < floor(k / 2)) {
			windowKoeff[bartlett * k + j] = (2.0f * j / (k));
		} else {
			windowKoeff[bartlett * k + j] = 2.0f - (2.0f * j / (k));
		}
	/* Kaiser (beta= 2*pi)
	 * besseli(0, beta) = 87.10851065339080889771139482036233;
	 */
		windowKoeff[kaiser * k + j] = besseli(4 * pi / k * sqrt(j * (k - j))) / 87.10851065339080889771139482036233;
	/* Rectangular */
		windowKoeff[rect * k + j] = 1.0;
	}
}

/*
 * ############## Compute the 0th Order modified Bessel function ########
 * ############### From www.nr.com (Numerical Recipes) page 237 #########
 */

gfloat besseli(gfloat x) {
	gfloat ax, ans;
	gdouble y;
/* Accumulate polynomials in double precision. */
	if ((ax = fabs(x)) < 3.75) {
		/* Polynomial fit. */
		y = x / 3.75;
		y *= y;
		ans = 1.0 + y * (3.5156229 + y * (3.0899424 + y * (1.2067492 + y * (0.2659732 + y * (0.360768e-1 + y * 0.45813e-2)))));
	} else {
		y = 3.75 / ax;
		ans = (exp(ax) / sqrt(ax)) * (0.39894228 + y * (0.1328592e-1 + y * (0.225319e-2 + y * (-0.157565e-2 + y * (0.916281e-2 + y * (-0.2057706e-1 + y * (0.2635537e-1 + y * (-0.1647633e-1 + y * 0.392377e-2))))))));
	}
	return ans;
}
