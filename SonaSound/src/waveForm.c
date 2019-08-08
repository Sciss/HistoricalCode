/*
 * $Id: waveForm.c,v 1.31 2003/04/03 19:07:45 niklaswerner Exp $
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
 *
 *
 */
#include "sonasound.h"
#include "waveForm.h"
#include "audioIo.h"

#include <gtkgl/gtkglarea.h>
#include <GL/gl.h>

gfloat waveFaktor = 1.0f;

void calculateWaveForm(gint width) {
	register gfloat xWert = 100.0f;
	gfloat  yWert[2], pixPerCentX, yMax = -1.0f, yMin = 1.0f, *minP;
	const gfloat yFaktor = 50.0f;
	register gfloat *readP;

	register guint16 j = 0, xFaktor = 0;

	minP = &audioData[0];
	/* Stepsize in GLUOrtho-Coordinates */
	pixPerCentX = 100.0f / ((gfloat) width);
	/* How many samples to put in two pixels */
	xFaktor = (waveFaktor < width / samplingRate) ? 1 : lrintf(floor((waveFaktor *  samplingRate) / width));
	readP = (audioDataP == &audioData[0]) ? &audioData[numFrames - 1] : audioDataP - 1;
	verticesWaveP = &verticesWave[0];

	while( xWert > 0.0f) {
		yMax=-1.0f, yMin=1.0f;
		for(j = 0; j < xFaktor; j++) {
			/* find max and min in the samples */
			yMin = min(yMin, *readP);
			yMax = max(yMax, *readP);
			/* Ringbuffer woes */
			if (readP <= minP) {
				readP = &audioData[numFrames - 1];
			} else {
				--readP;
			}
		}
		/* Weigh the sample values for use with gluOrtho */
		yWert[0] = yFaktor*yMin;
		yWert[1] = yFaktor*yMax;
		/* advance on pixel to the left */
		xWert -= pixPerCentX;
		/* put it into array to be drawn with glDrawElements */
		*verticesWaveP++ = xWert;
		*verticesWaveP++ = yWert[0];
/* 		xWert -= pixPerCentX; */
		*verticesWaveP++ = xWert;
		*verticesWaveP++ = yWert[1];
	} /* while i */
}	/* __func__ */

