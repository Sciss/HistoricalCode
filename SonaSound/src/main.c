/*
 * $Id: main.c,v 1.116 2004/02/14 09:56:10 niklaswerner Exp $
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
 */

#ifdef HAVE_CONFIG_H
#  include <config.h>
#endif

#include "sonasound.h"

#include "portaudio.h"
#include "audioIo.h"
#ifndef __gl_h_
#	include <GL/gl.h>
#endif

#ifdef LINUX_RTC
#	include <linux/rtc.h>
#	include <sys/ioctl.h>
#	include <fcntl.h>
#endif

#include <sys/utsname.h>
#include <gtkgl/gtkglarea.h>
#include "interface.h"
#include "support.h"
#include "window.h"
#include "audioInit.h"
#include "globalsInit.h"
#include "callbacks.h"
#include "glDraw.h"

/* I know this is an ugly lot of global variables...
 * detailed docu in sonasound.h
 */


gfloat *audioData, *audioDataP;		/* Keep the samples */
guint32 *logScaleFFT, /* arrays for storing the FFT-points */
	*logScaleSono;			/* corresponding to the pixels in the window */
#ifdef USING_FFTW3
gdouble *fftDataP;	/* Keep the FFT-Data in log10*abs() */
gdouble *fftIn, *fftInForw; /* Buffer for input to FFT/LPC
                                (global is faster than reallocating ;-()*/
gdouble *fftTmp, *fftTmpForw; /* Buffer for ouput from FFT/LPC
                                  (global is faster than reallocating ;-()*/
#else
fftw_real *fftDataP;	/* Keep the FFT-Data in log10*abs() */
fftw_real *fftIn, *fftInForw; /* Buffer for input to FFT/LPC
                                (global is faster than reallocating ;-()*/
fftw_real *fftTmp, *fftTmpForw; /* Buffer for ouput from FFT/LPC
                                  (global is faster than reallocating ;-()*/
#ifdef FFTW_ENABLE_FLOAT
fftw_real *fftForLPC; /* Tmp-Buffer for use by calculateLPC() when
                       * FFTW is single precision
											 */
#endif
#endif /* USING_FFTW3 */
gdouble  *autoKoeff, *reflexKoeff, *lpcKoeff, *tmpBuff;
guint32 numFrames;
gfloat numSeconds = 5.0f;
guint8 overlap = 0;
GString *dateiName;
#ifndef HAVE_ALTIVEC_OSX
const gdouble pi = 3.14159265358979323846264338327;
#endif
guint32 killIt = 0;
guint8 myBase = 2;
gint8 sndDevice = 0;
gint8 sndDeviceOut = 0;
guint8 readFromFile = 0;
guint8 doHighPass = 0;
guint8 drawStaves = 1;
GLubyte *verticesSono, *verticesSonoP;

guint16 framesProBuffer = 2048;
guint16 pointsFFT = 4096;
guint16 pointsLPC = 128;
guint32 minInterval = 8; /* min Drawing Interval */
guint32 interval = 0; /* Drawing Interval */

gfloat intervalFaktor = 500.0f;
gfloat maxDB = 0.0;
gfloat samplingRate = 44100.0;
gfloat referenceA = 440.0f;
gfloat highPass = -0.5;
gfloat *windowKoeff; /*, *windowKoeffFFT;*/
GLfloat *verticesWave, *verticesWaveP;

PortAudioStream *stream;
GtkWidget *DisplayWindow;
GtkWidget *MainWindow;
#ifdef USING_FFTW3
fftw_plan plan;
fftw_plan planForw;
fftw_plan planLPC;
fftw_plan planLPCForw;
#else
rfftw_plan plan;
#endif

displayModeName displayMode;
windowName windowType = hanning;
specTypeName specType = lpc;

GString *summaryString;
GList *windowNameList = NULL;
#ifdef LINUX_RTC
gint fd;
#endif

int
main (int argc, char *argv[])
{
	GtkWidget *SonoArea, *FFTArea;
	struct utsname unamePointer; /* Make waiting for FFTW-wisdom a bit nicer */
	FILE *wisdomFile; /* Load FFTW-Wisdom, if there */
	GString *wisdomFileName; /* Filename for Wisdom-File (~/sonasound.fftw_wisdom) */
	PaError err; /* PortAudio Errors */
	extern SNDFILE *sndFile;
	extern guint8 startIt; /* State variable for starting graphics-thread */
#ifdef LINUX_RTC
	gint retVal = 0;
#endif

	uname(&unamePointer);

	summaryString = g_string_new(_(""));
	stringInit();
	globalsInit(argc, argv); /* init values from commandline, etc... */

	if (pointsLPC > pointsFFT) {
		g_print("\
********* Warning: This makes no Sense!\n\
\tSetting LPC-Points to %d\n\
****************************\n",
		pointsFFT);
		pointsFFT = pointsLPC;
	}

	interval = (intervalFaktor * (framesProBuffer / samplingRate) < minInterval) ? minInterval : lrintf(intervalFaktor * (framesProBuffer / samplingRate));

	/* Maximum energy in one FFT-Bin */
	maxDB = (framesProBuffer < pointsFFT) ? 20.0f * log10(1.27f * framesProBuffer * 1024): 20.0f * log10(1.27f * pointsFFT * 1024);
	windowKoeff = (pointsFFT < framesProBuffer) ? g_new0(gfloat, 6 * pointsFFT) : g_new0(gfloat, 6 * framesProBuffer);
	getWindowKoeff();

#ifdef ENABLE_NLS
  bindtextdomain (PACKAGE, PACKAGE_LOCALE_DIR);
  textdomain (PACKAGE);
#endif
  gtk_set_locale ();
  gtk_init (&argc, &argv);

  add_pixmap_directory (PACKAGE_DATA_DIR "/pixmaps");
  add_pixmap_directory (PACKAGE_SOURCE_DIR "/pixmaps");

   /* Check if OpenGL is supported. */
  if (gdk_gl_query() == FALSE) {
    g_print("OpenGL not supported\n");
    return -1;
  }
	/* Init FFTW, get wisdom or measure fastest method */
	wisdomFileName = g_string_new(getenv("HOME"));
#ifdef USING_FFTW3
	g_string_append(wisdomFileName, "/.sonasound.fftw_wisdom3");
#else
	g_string_append(wisdomFileName, "/.sonasound.fftw_wisdom2");
#endif
	g_print(wisdomFileName->str);
	if((wisdomFile = fopen(wisdomFileName->str, "r"))) {
		g_print("\n**\t\t******************************\n\n\t\tLoading Wisdom\n");
		fftw_import_wisdom_from_file(wisdomFile);
		fclose(wisdomFile);
	}	else {
		g_print("\n**\t\t******************************\n\n\t\tCreating optimal FFT-Settings for your machine\n\t\t(%s - %s - %s)\n \t\t",unamePointer.sysname, unamePointer.release, unamePointer.machine);
		system("uname -p");
		g_print("\n\t\tPlease be patient\n");
	}

#ifdef USING_FFTW3
	fftDataP = g_new0(gdouble, pointsFFT + 2);
	fftIn = fftw_malloc(sizeof(gdouble) * pointsFFT);
	fftTmp = fftw_malloc(sizeof(gdouble) * pointsFFT);
	fftInForw = fftw_malloc(sizeof(gdouble) * pointsFFT);
	fftTmpForw = fftw_malloc(sizeof(gdouble) * pointsFFT);
	lpcKoeff = fftw_malloc(sizeof(gdouble) * pointsFFT);
#else
	fftDataP = g_new0(fftw_real, pointsFFT + 2);
	fftIn = g_new0(fftw_real, pointsFFT);
	fftTmp = g_new0(fftw_real, pointsFFT);
	fftInForw = g_new0(fftw_real, pointsFFT);
	fftTmpForw = g_new0(fftw_real, pointsFFT);
	lpcKoeff = g_new0(gdouble, (pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1);
#ifdef FFTW_ENABLE_FLOAT
	fftForLPC = g_new0(fftw_real, pointsFFT);
#endif
#endif /* USING_FFTW3 */
#ifdef USING_FFTW3
	plan = fftw_plan_r2r_1d(pointsFFT, fftIn, fftTmp, FFTW_R2HC, FFTW_MEASURE);
	planForw = fftw_plan_r2r_1d(pointsFFT, fftInForw, fftTmpForw, FFTW_R2HC, FFTW_MEASURE);
	planLPC = fftw_plan_r2r_1d(pointsFFT, lpcKoeff, fftTmp, FFTW_R2HC, FFTW_MEASURE);
	planLPCForw = fftw_plan_r2r_1d(pointsFFT, lpcKoeff, fftTmpForw, FFTW_R2HC, FFTW_MEASURE);
#else
	plan = rfftw_create_plan(pointsFFT, FFTW_REAL_TO_COMPLEX, FFTW_MEASURE|FFTW_USE_WISDOM);
#endif /* USING_FFTW3 */
	g_print("\n\t\tDone:-)\n******************************\n");

	numFrames = numSeconds * samplingRate;

	audioData = g_new0(gfloat,  numFrames);
	audioDataP = &audioData[0];

	DisplayWindow = create_DisplayWindow ();
  MainWindow = create_MainWindow ();

	SonoArea = lookup_widget(GTK_WIDGET(DisplayWindow), "SonoArea");
	FFTArea = lookup_widget(GTK_WIDGET(DisplayWindow), "FFTArea");

#ifdef HAVE_FAST_GA
	verticesSono = g_new0(GLubyte, 3 * (SonoArea->allocation.height));
#else
	verticesSono = g_new0(GLubyte, 3 * (SonoArea->allocation.height) * (SonoArea->allocation.width));
#endif
	verticesSonoP = &verticesSono[0];
	/* 2048 seems to be max width */
	/* FIXME: lookup define in gl.h */
	/* 4 for min _and_ max of samplerange */
	verticesWave = g_new0(GLfloat, 4 * (2048));
	verticesWaveP = &verticesWave[0];


	logScaleFFT = g_new0(guint32, AREA_WIDTH);
	logScaleSono = g_new0(guint32, AREA_HEIGHT);

/* ######## LPC -Stuff ########## */
	autoKoeff = g_new0(gdouble, pointsLPC + 1);
	reflexKoeff = g_new0(gdouble, pointsLPC + 1);
	tmpBuff = g_new0(gdouble, pointsLPC + 1);

/* ########### StaffLines for FFT-Plot tuneable via referenceA #### */

	err = Pa_Initialize();
	if( err != paNoError ) g_print("Audio-Engine Error (%s): %s\n", __func__, Pa_GetErrorText(err));
	audioInit(0);
	/*########### Gtk Main Loop ##############*/
  gtk_widget_show (DisplayWindow);
  gtk_widget_show (MainWindow);
 /* reference both windows to access on from the other */
	gtk_object_set_data(GTK_OBJECT(MainWindow), "DisplayWindow", DisplayWindow);
	startIt = 1;
/* 	if(signal(SIGALRM, &glAreaDrawSync) == SIG_ERR) */
/*   g_print("signal\n"); */

#ifdef LINUX_RTC
	fd = open ("/dev/rtc", O_RDONLY);
	if (fd == -1) {
		g_print("No RTC found\n");
	} else {
		retVal = ioctl(fd, RTC_IRQP_SET, 256);
		if (retVal == -1) {
			g_print(_("Couldn't set RTC to 256Hz\n"));
			g_print(_("Try \"echo 256 > /proc/sys/dev/rtc/max-user-freq\" as root\n"));
			g_print(_("Disabling RTC and falling back to 100Hz system timer.\n Expect Timing problems!\n"));
			close(fd);
			fd = -1;
		} else {
			retVal = ioctl(fd, RTC_PIE_ON, 0);
			if (retVal == -1) {
				g_print("Couldn't start Timer\n");
			}
		}
	}
#endif

  gtk_main ();
	/*########### Gtk Main Loop ##############*/

	/* cleanup */
	fftw_destroy_plan(plan);
#ifdef USING_FFTW3
	fftw_destroy_plan(planForw);
	fftw_destroy_plan(planLPC);
	fftw_destroy_plan(planLPCForw);
#endif /* USING_FFTW3 */
		if((wisdomFile = fopen(wisdomFileName->str, "w"))) {
			fftw_export_wisdom_to_file(wisdomFile);
			fclose(wisdomFile);
		} else {
			g_print("Error saving wisdom to %s\n", wisdomFileName->str);
		}


		if (sndFile) {
			sf_close(sndFile);
		}
		audioExit();
		err = Pa_Terminate();
		if( err != paNoError ) g_print("Audio-Engine Error (%s): %s\n", __func__, Pa_GetErrorText(err));
		g_string_free(wisdomFileName, TRUE);
#ifdef USING_FFTW3
		fftw_free(fftIn);
		fftw_free(fftTmp);
		fftw_free(fftInForw);
		fftw_free(fftTmpForw);
		fftw_free(lpcKoeff);
#else
		g_free(fftIn);
		g_free(fftTmp);
		g_free(fftInForw);
		g_free(fftTmpForw);
		g_free(autoKoeff);
		g_free(lpcKoeff);
		g_free(reflexKoeff);
		g_free(tmpBuff);
		g_free(verticesSono);
		g_free(verticesWave);
		g_free(logScaleFFT);
		g_free(logScaleSono);
		g_free(fftDataP);
		g_free(audioData);
		g_free(windowKoeff);
#endif /* USING_FFTW3 */
#ifdef LINUX_RTC
	if (fd != -1) close(fd);
#endif

  return 0;
}

