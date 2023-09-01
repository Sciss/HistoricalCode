/*
 * $Id: audioIo.c,v 1.85 2004/02/14 09:56:10 niklaswerner Exp $
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
 *  audioIo.c
 *
 *
 *
 *
 */

#include "sonasound.h"
#include "support.h"
#include "lpc.h"

/* Portaudio Tree */
#ifdef HAVE_PORTAUDIO_H

#ifndef AUDIO_IO_H
#	include "audioIo.h"
#endif

guint8 startIt = 0;

int paCallBack(  void *inputBuffer, void *outputBuffer,
                     unsigned long framesPerBuffer,
                     PaTimestamp outTime, void *userData ) {

	register gfloat *in  = (gfloat *) inputBuffer, *out = (gfloat *) outputBuffer;
	register gfloat *maxP, *minP, *audioOffP = NULL, *windowKoeffP;
	guint16 k = (pointsFFT < framesProBuffer) ? pointsFFT : framesProBuffer;
	register guint16 i = 0;
	register gint32 j = 0;
#ifdef USING_FFTW3
	register gdouble *fftInP, *fftInForwP = NULL;
	extern gdouble *fftIn, *fftInForw;
	extern gdouble *fftTmp, *fftTmpForw;
#else
	register fftw_real *fftInP, *fftInForwP = NULL;
	extern fftw_real *fftIn, *fftInForw;
	extern fftw_real *fftTmp, *fftTmpForw;
#endif
	register gdouble error = 1.0, error2 = 1.0;
	gint8 korrFaktor = 1;
	/* 15/16 */
/* 	const gdouble highPass = -0.9375; */
	extern gfloat highPass;
	extern SNDFILE *sndFile;
	extern guint8 readFromFile;
	extern guint8 numChannels;
	extern guint8 doHighPass;

	startIt = 0;
	fftInP = fftIn;
	maxP = &audioData[lrintf(numFrames) - 1];
	minP = &audioData[0];
	windowKoeffP = &windowKoeff[windowType * k];

	if (overlap == 1) {
		/* Get the pointer at the right Offset for overlap */
		fftInForwP = fftInForw;
		audioOffP = audioDataP;
		for (i = 0; i < framesPerBuffer / 2; i++) {
			audioOffP--;
			if (audioOffP <= minP) {
				audioOffP = maxP;
			}
		}
	}

	if (readFromFile == 1) {
		/* Get Data from Soundfile */
		getAudio(sndFile, out, (sf_count_t) framesPerBuffer);
		if (numChannels == 1) {
			i = 2 * framesPerBuffer - 1;
			for (j = framesPerBuffer - 1; j >= 0; j--) {
				out[i--] = out[j];
				out[i--] = out[j];
			}
		}
	}

/* #################### Begin of framesPerBuffer-Loop ######################## */

	for (i = 0; i < framesPerBuffer; i++) {

		if (audioDataP >= maxP) { /* F***g Ringbuffers */
			audioDataP = minP;
		}
		if (readFromFile == 0) { /* Data comes from Soundcard */
			*audioDataP++ = (*in + *(in + 1)) / (2);
			in += 2;
		} else { /* readFromFile == 1 */
			if (doHighPass == 1 && i > 1) { /* HighPass on Output */
				*(out) += *(out - 2) * highPass;
				*(out - 1) += *(out - 3) * highPass;
			} /* HighPass */
			*audioDataP++ = (*out + *(out + 1)) / 2;
			out += 2;
		} /* readFromFile */
		if (i < pointsFFT) { /* in (rare) case fft is smaller than Buffer */
			if (overlap == 1) {
				if (audioOffP >= maxP) { /* F***g Ringbuffers */
					audioOffP = minP;
				}
				*fftInForwP++ = *windowKoeffP * *(audioDataP - 1);
				*fftInP++ = *windowKoeffP++ * *audioOffP++;
				if (doHighPass == 1 && readFromFile == 0) { /* HighPass on Input */
					if (i > 1) {
						*(fftInP - 1) +=  *(fftInP - 2) * highPass;
						*(fftInForwP - 1) +=  *(fftInForwP - 2) * highPass;
					} /* if (i > 2) */
				} /* doHighPass */
			} else { /* No overlap */
				*fftInP++ = *windowKoeffP++ * *(audioDataP - 1);
				if (doHighPass == 1 && readFromFile == 0) { /* HighPass on Input */
					if (i > 1) {
						*(fftInP - 1) += *(fftInP - 2) * highPass;
					} /* if (i > 1) */
				} /* doHighPass */
			} /* if overlap */
		} /* if i < pointsFFT */
	} /* for framesPerBuffer */

/* #################### End of framesPerBuffer-Loop ######################## */

/* #################### Begin Spectrum calculation ######################## */

	if (specType == lpc) { /* LPC */
		error = calculateLpc(fftIn, fftTmp);
		if (overlap == 1) {
			error2 = calculateLpc(fftInForw, fftTmpForw);
		}
		korrFaktor = -1;
	} else { /* FFT */
#ifdef USING_FFTW3
		fftw_execute(plan);
		if (overlap == 1) {
			fftw_execute(planForw);
		}
#else
		rfftw_one(plan, fftIn, fftTmp);
		if (overlap == 1) {
			rfftw_one(plan, fftInForw, fftTmpForw);
		}
#endif /* USING_FFTW3 */
		korrFaktor = 1;
	} /* if specType */

/* #################### End Spectrum calculation ######################## */


/* ################## Fill global spectrum-Buffer ######################## */
	startIt = 0;
	fftDataP[0] = korrFaktor * 10 * log10(error * (fftTmp[0] * fftTmp[0]));
	if (overlap == 1) {
		fftDataP[pointsFFT / 2 + 1] = korrFaktor * 10 * log10(error * (fftTmpForw[0] * fftTmpForw[0]));
	}
	for (i = 1; i < ( pointsFFT + 1) / 2; i++) {
		/* PowerSpectrum: 20 * log10(sqrt(re^2 + im^2))
		==> 10 * log10((re^2 + im^2))*/
		if (overlap == 1) {
			fftDataP[i + 1 + (pointsFFT / 2)] = korrFaktor * 10 * log10(error2 * (
															fftTmpForw[i] * fftTmpForw[i] +
															fftTmpForw[(pointsFFT - i)] * fftTmpForw[(pointsFFT - i)]
															)
														);
		} /* overlap */

		fftDataP[i] = korrFaktor * 10 * log10(error * (
															fftTmp[i] * fftTmp[i] +
															fftTmp[(pointsFFT -  i)] * fftTmp[(pointsFFT -  i)]
															)
														);
	} /* for pointsFFT */
	if (pointsFFT % 2 == 0) {
		fftDataP[pointsFFT / 2] = korrFaktor * 10 * log10(error * (fftTmp[pointsFFT / 2] * fftTmp[pointsFFT / 2]));
		if (overlap == 1) {
			fftDataP[pointsFFT + 1] = korrFaktor * 10 * log10(error * (fftTmpForw[pointsFFT / 2] * fftTmpForw[pointsFFT / 2]));
		}
	}
/* ################## End Fill global spectrum-Buffer ######################## */
	startIt = 1;
	return 0;
}

void getAudio(SNDFILE *file, gfloat* audioP, sf_count_t frames) {
	sf_readf_float(file, audioP, frames);
}
/* Jack Tree */
#elif defined HAVE_JACK_JACK_H

#include <jack/jack.h>




#endif
