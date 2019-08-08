/*
 * $Id: sonasound.h,v 1.68 2004/02/14 09:56:10 niklaswerner Exp $
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
 */
#ifndef _SONASOUND_H
#	define _SONASOUND_H 1

#ifdef HAVE_CONFIG_H
#	include <config.h>
#endif

#include "float_cast.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>

#define max(x, y) ((x > y) ? x : y)
#define min(x, y) ((x < y) ? x : y)

/* Initial size of GL-Drawing-Area */
#define AREA_WIDTH 475
#define AREA_HEIGHT 250

#if (defined MACOSX && defined HAVE_ALTIVEC_OSX)
#	include <vecLib/vecLib.h>
#	include <vecLib/vDsp.h>
#endif

#include <gtk/gtk.h>

#ifdef USING_FFTW3
#include <fftw3.h>
#else
#ifdef HAVE_FFTW_H
# include <rfftw.h>
#elif defined HAVE_DFFTW_H
#	include <drfftw.h>
#elif defined HAVE_SFFTW_H
#	include <srfftw.h>
#endif /* HAVE_FFTW_H */
#endif /* FFTW3 */

#include <GL/gl.h>
#include <portaudio.h>
#include <sndfile.h>
#include "helpers.h"

/* Names for the GL-Areas */
typedef enum { wave, fft, sono } areaName;
/* Names for the window-types */
typedef enum { hamming, hanning, blackman, bartlett, kaiser, rect } windowName;
/* Names for displayModes */
typedef enum { logarithm, linear } displayModeName;
/* Names for specModes */
typedef enum { dft, lpc } specTypeName;
/* store floats in GLubytes */
typedef union {
	gfloat f;
	GLubyte b[sizeof(gfloat)];
} gFloatToGLubyte;

#ifndef HAVE_ALTIVEC_OSX
extern const gdouble pi;
#endif

extern guint32 killIt; /* ID for gtk_timeout */
extern guint8 myBase; /* LogBase */
extern guint8 overlap; /* Overlap On/Off */
extern gint8 sndDevice; /* Device-ID */
extern gint8 sndDeviceOut; /* Device-ID Output*/
extern GLubyte *verticesSono, *verticesSonoP; /* RGB-Values for Sonogram */

extern guint16 framesProBuffer; /* Samples to read in one go */
extern guint32 interval; /* gtk_timeout interval */
extern guint32 minInterval; /* minimum gtk_timeout interval */

extern guint16 pointsFFT; /* Number of points to calculate */
extern guint16 pointsLPC; /* Number of LPC-coefficients */


extern guint32 *logScaleFFT, *logScaleSono; /* keep FFT-Points for pixels */
extern guint32 numFrames; /* Number of Samples to display in Waveview */

extern gfloat *audioDataP, *audioData; /* Audio-Buffer (length: numFrames) */
extern gfloat intervalFaktor; /* faktor to multiply drawing interval with (overlap, ...) */
extern gfloat maxDB; /* Scaling factor for FFT-/Sono-Display */
extern gfloat numSeconds; /* Number of Seconds to display in Waveview */
extern gfloat samplingRate;
extern GLfloat *verticesWave, *verticesWaveP; /* Array for drawing wave-data */
extern gfloat *windowKoeff; /* Array for windowing funcions */

extern GString *dateiName; /* Not used atm */
extern GString *summaryString; /* display summary in main window */
#ifdef USING_FFTW3
extern gdouble *fftDataP; /* Spectral data */
extern fftw_plan plan; /* plan for FFT-calculation */
extern fftw_plan planForw;
extern fftw_plan planLPC;
extern fftw_plan planLPCForw;
#else
extern fftw_real *fftDataP; /* Spectral data */
extern rfftw_plan plan; /* plan for FFT-calculation */
#endif /* USING_FFTW3 */

extern GtkWidget *DisplayWindow; /* I need that very often ... */

extern displayModeName displayMode; /* linear or logarithm */
extern windowName windowType; /* window type: hamming, hanning,... */
extern specTypeName specType; /* lpc or fft */

extern GList *windowNameList; /* List contains all available windows */

#endif /* _SONASOUND_H */
