/*
 * $Id: fft.c,v 1.37 2004/02/14 09:56:10 niklaswerner Exp $
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
 *
 * calculate the vertices of the FFT-Display using
 * the precalculated array logScaleFFT (see globalsInit.c)
 *
 */
#include "sonasound.h"
#include "waveForm.h"
#include "audioIo.h"
#include "fft.h"

#include <gtkgl/gtkglarea.h>
#include <GL/gl.h>


/*#########################################################

                        FFT

##########################################################*/
void
calculateFFTView						(gint width, gint height) {
#ifdef USING_FFTW3
	register gdouble *out;				/* pointer to FFT-Data */
	register gdouble yWert1 = 0.0f, yWert2 = 0.0f;			/* [0]: y-Value; [1]: tmp-space for mean-value */
#else
	register fftw_real *out;				/* pointer to FFT-Data */
	register fftw_real		yWert1 = 0.0f, yWert2 = 0.0f;			/* [0]: y-Value; [1]: tmp-space for mean-value */
#endif
	register guint16 j = 0, k = 1; /* (hopefully) fast counters */
	register gfloat xWert = 0.0f; /* x-Value */
	register gfloat  pixPerCentX;	/* pixels in gluOrtho2D-grid */
	register guint16 logScalePoint = 0;
	register gint32 deltaPoints = 0;	/* number of Points for one pixel */
	extern guint8 oddEven;


	if (overlap == 1) {
		if (oddEven == 0) {
			out = &fftDataP[0];
		} else {
			out = &fftDataP[(pointsFFT / 2) + 1];
		}
	} else {
		out = &fftDataP[0];
	}
	pixPerCentX = 1000.0f / (width);
glBegin(GL_LINE_STRIP);
	while ( k < width) { /* while every pixel in glArea */
		/* Number of FFT-Points to average */
		deltaPoints = ((logScaleFFT[k] - logScaleFFT[k-1]) <= 0) ? 1 : (logScaleFFT[k] - logScaleFFT[k - 1]);
		logScalePoint = logScaleFFT[k - 1];
		for(j = 0; j < deltaPoints; j++) {
			yWert2 += out[logScalePoint++];
		}
		/* mean-value of deltaPoints weighted with maxDB for better scaling in the window */
		yWert1 = (yWert2 / (gfloat) deltaPoints <= 0.0f) ? 0 : height * (yWert2 / deltaPoints) / maxDB;
		/* No new Points, so draw on */
		glVertex2f((GLfloat) xWert,(GLfloat) yWert1);
		k++;
		while(logScaleFFT[k+1] == logScaleFFT[k]) {
/* 			glVertex2f((GLfloat) xWert,(GLfloat) yWert1); */
/* Let OpenGL do the lienar interpolation */
			xWert += pixPerCentX;
			k++;
		}
		/* There must be one pixel remaining */
		xWert += pixPerCentX;
		/* reset the y-Value to get the right new mean value */
		yWert2 = 0.0f;
	}
glEnd();
} /* __func__ */

