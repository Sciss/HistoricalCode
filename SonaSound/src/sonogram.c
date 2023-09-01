/*
 * $Id: sonogram.c,v 1.53 2004/02/14 09:56:10 niklaswerner Exp $
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
 */
#include "sonasound.h"
#include "waveForm.h"
#include "fft.h"
#include "audioIo.h"
#include <gtkgl/gtkglarea.h>
#include <GL/gl.h>
#include "palette.h"


/*#########################################################

                    Colour for Sonogram

##########################################################*/
void
colorize(gfloat value, gint width, gint height) {
	register guint16 index, tmpVal;

	tmpVal = CLAMP(lrintf(paletteSize * value / maxDB), 0, paletteSize );
	index = (tmpVal % 3 == 0) ? tmpVal : lrintf(tmpVal / 3) * 3;
	/* choose rgb-value from array (s. palette.h) */
	*verticesSonoP++ = palette[3*index];
	*verticesSonoP++ = palette[3*index+1];
	*verticesSonoP++ = palette[3*index+2];
#ifndef HAVE_FAST_GA
	verticesSonoP += 3 * width - 3;
#endif
}

/*#########################################################

                     Sonogram

##########################################################*/

/* refer to fft.c for documentation */
void
calculateSonogram						(gint width, gint height) {
#ifdef USING_FFTW3
	register gdouble *out;
#else
	register fftw_real *out;
#endif /* USING_FFTW3 */
	register gfloat yWert1 = 0.0f, yWert2 = 0.0f;
	register gint32 deltaPoints = 0;
	register guint32 j = 0, k = 0;
	register guint16 logScalePoint = 0;
	extern guint8 oddEven;

	if (overlap == 1) {
		if (oddEven == 0) {
			out = &fftDataP[0];
			oddEven = 1;
		} else {
			out = &fftDataP[(pointsFFT/2) + 1];
			oddEven = 0;
		}
	} else {
		out = &fftDataP[0];
	}
	j = 0;
	k = 1;
#ifdef HAVE_FAST_GA
	verticesSonoP = &verticesSono[0];
#else
	verticesSonoP = &verticesSono[3 * (width - 1)];
#endif
	while (k < height) {
		deltaPoints = ((logScaleSono[k] - logScaleSono[k-1]) <= 0) ? 1 : (logScaleSono[k] - logScaleSono[k - 1]);
		logScalePoint = logScaleSono[k - 1];
		for(j = 0; j < deltaPoints; j++) {
			yWert2 += out[logScalePoint++];
		}

		yWert1 = (yWert2 / deltaPoints <= 0.0f) ? 0.0f : 2 * (yWert2) / deltaPoints ;
		while(logScaleSono[k+1] == logScaleSono[k]) {
/* 			colorize(CLAMP(yWert1 - 10, 0, maxDB), width, height); */
			colorize(yWert1, width, height);
			k++;
		}
		colorize(yWert1, width, height);
		yWert2 = 0.0f;
		k++;
	}
}

