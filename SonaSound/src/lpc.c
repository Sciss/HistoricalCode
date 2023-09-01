/*
 * $Id: lpc.c,v 1.46 2004/02/14 09:56:10 niklaswerner Exp $
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
 * LPC based on work from Georg Klein 7.4.1994 (implemented in DSP56K-Assembler)
 *
 *
 * Autocorellation-Method and Levinson-Recursion is applied
 *
 * Original theory to be found in:
 * Markel/Gray: "Linear Prediction of Speech"
 * Springer, Berlin 1976
 */
#include "sonasound.h"
#include "support.h"

#include "lpc.h"

#ifdef USING_FFTW3
gdouble calculateLpc(gdouble *signal, gdouble *outP) {
	register gdouble *sigP1, *sigP2, *sigP3;
#else
gdouble calculateLpc(fftw_real *signal, fftw_real *outP) {
	register fftw_real *sigP1, *sigP2, *sigP3;
#ifdef FFTW_ENABLE_FLOAT
	extern fftw_real *fftForLPC;
#warning "FFTW in single precision is NOT recommended"
#endif
#endif /* USING_FFTW3 */
	register guint32 i = 0, j = 0;
	register gdouble  *reflexKoeffP, *lpcKoeffP, *tmpBuffP, *autoKoeffP;
	register gdouble alpha = 0.0;

	extern gdouble  *autoKoeff, *reflexKoeff, *lpcKoeff, *tmpBuff;
	guint16 framesPerBuffer = (pointsFFT < framesProBuffer) ? pointsFFT : framesProBuffer;

	/* c_ik */
	autoKoeffP = &autoKoeff[0];
	/* a_k */
	lpcKoeffP = &lpcKoeff[0];
	/* k_i */
	reflexKoeffP = &reflexKoeff[0];
	/* b_k */
	tmpBuffP = &tmpBuff[0];
	/* s(n) */
	sigP3 = signal;

/* Autocorrelation */
	for (i = 0; i <= pointsLPC; i++, *autoKoeffP++) {
		sigP1 = signal;
		sigP2 = sigP3++;
		*autoKoeffP = 0.0;
		for (j = 0; j < (framesPerBuffer - i); j++) {
#ifdef FFTW_ENABLE_FLOAT
			*autoKoeffP += ((gdouble) *sigP1++ * (gdouble) *sigP2++);
#else
			*autoKoeffP += (*sigP1++ * *sigP2++);
#endif
		}
	}

/* recursive calculation: LPC-Koeff */
	/* Init */
	alpha = autoKoeff[0];
	reflexKoeff[0] = - autoKoeff[1] / autoKoeff[0];
	lpcKoeff[0] = 1.0;
	lpcKoeff[1] = reflexKoeff[0];
	alpha -= reflexKoeff[0] * reflexKoeff[0] * alpha;

	/* Levinson-recursion */
	for (i = 1; i < pointsLPC; i++) {
		reflexKoeff[i] = 0.0;
		tmpBuffP = tmpBuff;
		lpcKoeffP = &lpcKoeff[i];
		reflexKoeffP = &reflexKoeff[i];
		autoKoeffP = &autoKoeff[i + 1];
		for (j = 0; j <= i; j++) {
			*tmpBuffP++ = *lpcKoeffP--;
			*reflexKoeffP += *autoKoeffP-- * lpcKoeff[j];
		}
		*reflexKoeffP = - *reflexKoeffP / alpha;
		lpcKoeffP = &lpcKoeff[1];
		tmpBuffP = tmpBuff;
		for (j = 1; j <= i; j++) {
			*lpcKoeffP++ += *reflexKoeffP * *tmpBuffP++;
		}
		*lpcKoeffP = *(reflexKoeffP);
		/* calculate alpha */
		alpha -= *reflexKoeffP * *reflexKoeffP * alpha;
	} /* End: Levinson */

/* frequency-response
 * This used to be a sum over every coefficient
 * with a huuuuuuge sin/cos-table
 * now I simply use fftw for this...
 */
#ifdef USING_FFTW3
	fftw_execute(planLPC);
	if (overlap == 1) {
		fftw_execute(planLPCForw);
	}
#else
#ifdef FFTW_ENABLE_FLOAT
	sigP1 = &fftForLPC[0];
	lpcKoeffP = &lpcKoeff[0];
	for (i = 0; i < pointsLPC; i++) {
		*sigP1++ = (fftw_real) *lpcKoeffP++;
	}
	rfftw_one(plan, &fftForLPC[0], outP);
#else
	rfftw_one(plan, &lpcKoeff[0], outP);
#endif /* USING_FFTW3 */
#endif

/* Error is after Markel/Gray page 131
 * sigma^2 = alpha
 */
	return	(1 / (alpha));
}
