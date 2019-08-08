/*
 * $Id: globalsInit.c,v 1.77 2003/04/04 14:51:27 niklaswerner Exp $
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
#include <getopt.h>
#include "globalsInit.h"
#include "window.h"
#include "audioInit.h"
#include "portaudio.h"
#include "support.h"


/* set log-algorithm */
gfloat myLog(gfloat value) {
	gfloat myValue;
	switch(myBase) {
		case 2:
/* incomprehensible optimizations errors on intel */
#ifdef HAVE_LOG2F
			myValue = log2f(value);
#elif defined HAVE_LOG2
			myValue = log2(value);
#else
			myValue = log(value)/log(2);
#endif
			break;
		case 10:
			myValue = log10(value);
			break;
		default:
			myValue = (value);
	}
	return myValue;
}

/* get commandline args */
void globalsInit(int argc, char *argv[]) {
	int	option;
	extern char *optarg;
	/* extern int optind, opterr, optopt; */
	extern guint8 readFromFile;
	extern guint8 doHighPass;
	extern guint8 drawStaves;
	extern gfloat referenceA;
	extern gfloat highPass;

	while(1) {
		option = getopt(argc, argv,"b:f:l:d:hs:w:a:p:D:S:oF:O:g:H:n");
		if(option == -1) {
			break;
		}
		switch(option) {
			case 'b':
				referenceA = CLAMP(atof(optarg), 27.5, 14080.0);
				break;
			case 'f':
				pointsFFT = CLAMP(nextPowerOfTwo(atoi(optarg)), 16, 65535);
				break;
			case 'l':
				pointsLPC = atoi(optarg);
				break;
			case 'd':
				switch(*optarg) {
					case 'i':
						displayMode = linear;
						break;
					case 'g':
						displayMode = logarithm;
						break;
					default:
						displayMode = logarithm;
				}
				break;
			case 'h':
				printUsage(argv);
			case 's':
				samplingRate = (gfloat) atoi(optarg);
				break;
			case 'w':
				windowType = (windowName) atoi(optarg);
				break;
			case 'a':
				pointsFFT = CLAMP(atoi(optarg), 16, 65535);
				break;
			case 'p':
				framesProBuffer = CLAMP(atoi(optarg), 128, 65535);
				break;
			case 'D':
				sndDevice = atoi(optarg);
				break;
			case 'O':
				sndDeviceOut = atoi(optarg);
				break;
			case 'S':
				/* 1.3653 = 2 * 65535 / 96000
				 * minSeconds = 2 (overlap) * maxBufferSize / (maxSamplingRate)
				 */
				numSeconds = CLAMP(atof(optarg), 1.3653f, 20.0f);
				numFrames = numSeconds * samplingRate;
				break;
			case 'o':
				overlap = 1;
				break;
			case 'F':
				g_string_assign(dateiName, optarg);
				readFromFile = 1;
				break;
			case 'g':
				specType = (g_strcasecmp(optarg, "l"))? lpc : dft;
				break;
			case 'H':
				doHighPass = 1;
				highPass = CLAMP(-1 * atof(optarg), -1.0f, -0.001f);
				break;
			case 'n':
				drawStaves = 0;
				break;
			case '?':
				break;
			default:
				g_print ("?? getopt returned character code 0%o ??\n", option);
		}

	}
}

/* ###### calculate, which points have to averaged or interpolated on
 * which pixel ######
 */
void logScaleInit(GtkWidget *widget) {
	guint32 length = 0;
	register guint32 *tmpP = NULL, i = 0, j = 0;
	guint32 k = 1;
	gfloat cmpPoint = 0.0f;


	if (g_strcasecmp(widget->name,"SonoArea") == 0) {

		length = GTK_WIDGET(widget)->allocation.height;
		tmpP = &logScaleSono[0];
		logScaleSono = g_renew(guint32, tmpP, length);
		tmpP = &logScaleSono[0];

	} else if (g_strcasecmp(widget->name,"FFTArea") == 0) {

		length = GTK_WIDGET(widget)->allocation.width;
		tmpP = &logScaleFFT[0];
		logScaleFFT = g_renew(guint32, tmpP, length);
		tmpP = &logScaleFFT[0];

	}

	j = 0;
	k = 0;
	for(i = 0; i < length; i++) {
		*tmpP++ = j;
		if(displayMode == logarithm) {
/*		cmpPoint =  pow(2, ((1.0f + i) / length) * log2(samplingRate / 2)); */
			cmpPoint =  (samplingRate / pointsFFT) * pow(pointsFFT / 2, ((1.0f + i) / length));
		} else { /* DisplayMode == linear */
			cmpPoint = (samplingRate / 2) * ((i + 1.0f) / length);
		}

		while (j  * (samplingRate / pointsFFT) <= cmpPoint  && j <= (pointsFFT / 2) + 1) {
			j++;
		}
	} /* for (i < length) */
/* 	g_print("point: %d\tcmp: %f\t count: %2.10f\t f:%f\n", *(tmpP -length), cmpPoint, myLog(counter), j * (samplingRate / pointsFFT)); */
}

void printUsage (char *argv[]) {
	g_print(
	"Usage: %s [-d {i,g}] [-f %%d | -a %%d] [-l %%d] [-w [0-4] ]\n \
		[-p %%d] [-s %%d] [-D %%d] [-O %%d] [-S %%d] [-o] [-b %%f]\n \
		[-F <path>] [-n] [-H %%f] [-h]\n",
		argv[0]);
	g_print("-d:\t Display Mode: i: linear, g: logarithmic (default)\n");
	g_print("-f:\t Number of FFT-Points: up to 65535 (rounded up to NextPowerOfTwo)\n");
	g_print("\t If you don't like this, specify the \"-a\" option instead \n\t to allow for arbitrary FFT-sizes\n");
	g_print("-l:\t Number of LPC-Coefficients: up to 65535,\n");
	g_print("\t though only up to 256 makes sense\n");
	g_print("-s:\t SamplingRate: whatever suits your soundcard (44100 or 48000 ?)\n");
	g_print("-w:\t Type of Window:\n\t 0: hamming, 1: hanning (default),\n\
\t 2: blackman, 3: bartlett, 4: kaiser, 5: rectangular\n");
	g_print("-p:\t Buffer Size: up to 65535 depending on your soundcard\n");
	g_print("\t powers of two work best)\n");
	g_print("-D:\t Input Sound device: First: 0, Second: 1, ... Disable: -1\n");
	g_print("-O:\t Output Sound device: First: 0, Second: 1, ... Disable: -1\n");
	g_print("-S:\t Maximum Number of Seconds to display in Waveform-view\n\
\t (default: 5, clamped to 1-20)\n");
	g_print("-o:\t switch on overlapping windows of 50%%\n");
	g_print("-F:\t SoundFile to use (instead of Audio-In)\n");
	g_print("-b:\t Reference frequency in [Hz] for the Staff-lines in the spectrum\n");
	g_print("-n:\t Switch Staff-lines off\n");
	g_print("-H:\t Set the HighPass-filter coefficient to a value in the range (0.001, 1.0) default: 0.5\n");
	g_print("-h:\t Print this help\n");
	exit(0);
}

/* Query for Audio-Devices and put them in GList (called from interface.c) */
GList* getDeviceInfo(GList *liste, guint8 inOut) {
	const PaDeviceInfo *pdi;
	gint numDevices, i=0;

	numDevices = Pa_CountDevices();
	if(numDevices == 0) {
		g_print(_("No Sound Device found\n"));
		liste = g_list_append(liste, _("No Device"));
		return liste;
	}
	if (inOut == 0) { /* 0 = INPUT, 1 = OUTPUT */
		if(sndDevice > numDevices) {
			g_print(_("Wrong Sound Device! Disabled\n"));
			sndDevice = -1;
		}
	} else {
		if(sndDeviceOut > numDevices) {
			g_print(_("Wrong Sound Device! Disabled\n"));
			sndDeviceOut = -1;
		}
	}
	for (i = 0; i < numDevices; i++) {
		pdi = Pa_GetDeviceInfo(i);
		liste = g_list_append(liste, (gchar*) pdi->name);
	}
	return liste;
}

/* Query Audio-Device for possible samplingRates (called from interface.c) */
GList* getSamplingRate(GList *liste) {
	extern gint samplingRateIndex;
	const PaDeviceInfo *pdi;
	gint numDevices, j=0, i=0;
	gchar *message;
	gint device = 0;
	numDevices = Pa_CountDevices();
	device = (sndDevice == -1) ? sndDeviceOut : sndDevice;

	if(numDevices == 0) {
		g_print("No Sound Device\n");
		liste = g_list_append(liste, _("No Device"));
		return liste;
	}
	if (device > numDevices) {
		g_print(_("Sound Device %d not available\n"), device);
		liste = g_list_append(liste, _("No Device"));
		return liste;
	}
	pdi = Pa_GetDeviceInfo(device);
	if(pdi->numSampleRates == -1) {
		liste = g_list_append(liste, _("Choose freely"));
		j = 2;
	} else {
		j = lrintf(pdi->numSampleRates);
	}
	message = g_new0(gchar, 6 * j);

	for	(i = 0; i < j; i++) {
		g_snprintf(message, 6, "%ld", lrintf(pdi->sampleRates[i]));
		liste = g_list_append(liste,  message);
		message += 6;
		if(pdi->sampleRates[i] == lrintf(samplingRate)) {
			samplingRateIndex = i;
		}
	}
	return liste;
}

/* print summary of values into Main-window (called everytime something changes) */
void printSummaryString(void) {
	extern guint8 readFromFile;
	extern gfloat highPass;

	g_string_sprintf(summaryString,
		_(
"SamplingRate: %6.1fkHz\n\
Buffer-Size: %d (%3.3fsec %3.2fHz)\n\
FFT-Size: %d (%5.2fHz)\n\
LPC-Coefficients: %d\n\
HighPass-coefficient: %f\n\
Drawing Interval: %dms (%3.2ffps)\n\
Window Type: %s\n\
Spectrum Method: %s\n\
Max WaveForm Range: %1.6fsec\n\
Input: %s%s\
"),
			samplingRate/1000.0,
			framesProBuffer, framesProBuffer/samplingRate, samplingRate/framesProBuffer,
			pointsFFT, samplingRate/pointsFFT,
			pointsLPC,
			-1 * highPass,
			interval, (1000.0f / interval ),
			(gchar*) g_list_nth_data(windowNameList, windowType),
			(specType == dft) ? "FFT" : "LPC",
			numSeconds,
			(readFromFile == 1) ? "Soundfile:\n" : "SoundDevice",
			(readFromFile == 1) ? dateiName->str : ""
			);
}

void windowNameListInit(void) {
	windowNameList = g_list_append(windowNameList, (gchar*) "hamming");
	windowNameList = g_list_append(windowNameList, (gchar*) "hanning");
	windowNameList = g_list_append(windowNameList, (gchar*) "blackmann");
	windowNameList = g_list_append(windowNameList, (gchar*) "bartlett");
	windowNameList = g_list_append(windowNameList, (gchar*) "kaiser");
	windowNameList = g_list_append(windowNameList, (gchar*) "rect");
}


void genNoteTable(void) {
	gfloat semiTone = pow(2, 1/12.0);

	guint8 i = 0;

	register GLfloat *staffP, *staffLogP, *staffSonoP, *staffSonoLogP,
		*octP, *octLogP;

	gfloat *staffLineFreqs, *octaveFreqs, *staffLineP;
	extern gfloat logFFTBySamplingRate, logFFTByTwo;
	extern gfloat *staffLineVertices, *octaveLineVertices, *staffLineVerticesLog,
		*octaveLineVerticesLog, *staffLineVerticesSonoLog, *staffLineVerticesSono;
	extern gfloat referenceA;

	staffLineFreqs = g_new(gfloat, 10);
	staffLineP = staffLineFreqs;
	octaveFreqs = g_new(gfloat, 9);

	/* Calculate Frequencies for notes:
	 * well-tempered tuning:
	 * 1 semitone = 2^(1/12) = 1.05946309435929530984310531493975
	 * 2 semitones = (1 semitone)^2 = 1.12246204830937301721860421821475
	 * 3 semitones = (1 semitone)^3 = 1.18920711500272124894195258093532
	 * a' = 440Hz
	 * c' = a / 1.68179283050742967020596552174538 (a = c * (semitone^9))
	 * c'' = 2 * c'
	 */
	octaveFreqs[0] = referenceA / 8,	/* A_0 */
  octaveFreqs[1] = referenceA / 4,	/* A */
	octaveFreqs[2] = referenceA / 2,	/* a */
	octaveFreqs[3] = referenceA,			/* a' */
	octaveFreqs[4] = referenceA * 2,	/* a'' */
	octaveFreqs[5] = referenceA * 4,	/* a''' */
	octaveFreqs[6] = referenceA * 8,	/* a^4 */
	octaveFreqs[7] = referenceA * 16,	/* a^5 */
	octaveFreqs[8] = referenceA * 32;	/* a^6 */

	/* Bass-schluessel */
		/* G  */
	staffLineFreqs[0] =	referenceA / 4.0 / pow(semiTone, 2),
		/* H  */
	staffLineFreqs[1] =	referenceA / 4.0 * pow(semiTone, 2),
		/* d  */
	staffLineFreqs[2] =	referenceA / 4.0 * pow(semiTone, 5),
		/* f */
	staffLineFreqs[3] =	referenceA / 2.0 / pow(semiTone, 4),
		/* a */
	staffLineFreqs[4] =	referenceA / 2.0,
	/* Violin-schluessel */
		/* e'  */
	staffLineFreqs[5] =	referenceA / pow(semiTone, 5),
		/* g'  */
	staffLineFreqs[6] =	referenceA / pow(semiTone, 2),
		/* h'  */
	staffLineFreqs[7] =	referenceA * pow(semiTone, 2),
		/* d'' */
	staffLineFreqs[8] =	referenceA * pow(semiTone, 5),
		/* f'' */
	staffLineFreqs[9] =	referenceA * 2.0 / pow(semiTone, 4);


		staffP = &staffLineVertices[0];
		staffLogP = &staffLineVerticesLog[0];
		staffSonoP = &staffLineVerticesSono[0];
		staffSonoLogP = &staffLineVerticesSonoLog[0];
		octP = &octaveLineVertices[0];
		octLogP = &octaveLineVerticesLog[0];

		/* Octave Lines */
		for (i = 0; i < 9; i++) {
		/* Log */
			*octLogP = *(octLogP + 2) = 1000.0f * (myLog(octaveFreqs[i]) + logFFTBySamplingRate) / logFFTByTwo;
			octLogP++;
			*octLogP++ = 0.0f;
			octLogP++;
			*octLogP++ = 100.0f;
		/* Linear */
			*octP = *(octP + 2) = 2000.0f * (octaveFreqs[i]) / samplingRate;
			octP++;
			*octP++ = 0.0f;
			octP++;
			*octP++ = 100.0f;
		}

	/* Staff Lines */
		for (i = 0; i < 10; i++) {
		/* Log */
			*staffSonoLogP++ = 0.0f;
			*staffSonoLogP = *(staffSonoLogP + 2) = 100.0f * (myLog(*staffLineP) + logFFTBySamplingRate) / logFFTByTwo;
			staffSonoLogP++;
			*staffSonoLogP++ = 1000.0f;
			staffSonoLogP++;

			*staffLogP = *(staffLogP + 2) =	1000.0f * (myLog(*staffLineP) + logFFTBySamplingRate) / logFFTByTwo;
			staffLogP++;
			*staffLogP++ = 0.0f;
			staffLogP++;
			*staffLogP++ = 100.0f;

		/* Linear */
			*staffSonoP++ = 0.0f;
			*staffSonoP = *(staffSonoP + 2) = 200.0f * *staffLineP / samplingRate;
			staffSonoP++;
			*staffSonoP++ = 1000.0f;
			staffSonoP++;

			*staffP = *(staffP + 2) = 2000.0f * *staffLineP++ / samplingRate;
			staffP++;
			*staffP++ = 0.0f;
			staffP++;
			*staffP++ = 100.0f;
		}

	g_free(staffLineFreqs);
	g_free(octaveFreqs);
}
