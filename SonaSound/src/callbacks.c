/*
 * $Id: callbacks.c,v 1.105 2004/02/14 09:56:10 niklaswerner Exp $
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
#ifdef HAVE_CONFIG_H
#  include <config.h>
#endif

#include "sonasound.h"

#include <gtkgl/gtkglarea.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <sys/utsname.h>
#include <sys/time.h>

#include "audioIo.h"
#include "callbacks.h"
#include "interface.h"
#include "support.h"
#include "glDraw.h"
#include "window.h"
#include "audioInit.h"
#include "globalsInit.h"
#include "palette.h"

extern PortAudioStream *stream;
SNDFILE *sndFile;
guint8 numChannels = 2;

void
stringInit						(void)
{
	dateiName = g_string_new("");
}

void
on_open_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

	GtkWidget	*fileSelection;
	fileSelection = create_fileSelection();
	gtk_widget_show(fileSelection);
	gtk_file_selection_set_filename(GTK_FILE_SELECTION(fileSelection), dateiName->str);
	gtk_object_set_data(GTK_OBJECT(fileSelection), "fileSel", GTK_OBJECT(lookup_widget(GTK_WIDGET(menuitem),"MainWindow")));
}


void
on_new_activate                        (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

  GtkWidget *DisplayWindow;
  DisplayWindow = create_DisplayWindow ();
  gtk_widget_show (DisplayWindow);
	gtk_object_set_data(GTK_OBJECT(lookup_widget(GTK_WIDGET(menuitem),"MainWindow")), "DisplayWindow", DisplayWindow);
}


void
on_save_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_quit_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_full_screen_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_100_activate                        (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_75_activate                         (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_50_activate                         (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_25_activate                         (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{

}


void
on_about_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	GtkWidget *aboutDialog;

	aboutDialog = create_About_SonaSound();
	gtk_widget_show(aboutDialog);

}


void
on_tutorial_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	GtkWidget *tutorial;

	tutorial = createTutorial();
	gtk_widget_show(tutorial);

}


void
on_ok_button_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	gchar *tmpCharName = NULL;
	GtkWidget *fileSelection = NULL;
	extern GtkWidget *MainWindow;
	GtkCombo *samplingRateCombo;
	SF_INFO sfinfo;
	gchar *message;

	message = g_new0(gchar, 6);
	if (button != NULL) {
		/* called from create_MainWindow, because Commandline
		 * specified Filename
		 */
		fileSelection = lookup_widget(GTK_WIDGET(button),"fileSelection");
	}
	if (button != NULL) {
		samplingRateCombo = GTK_COMBO(lookup_widget(GTK_WIDGET(MainWindow), "samplingRateCombo"));
	} else {
		samplingRateCombo = GTK_COMBO(lookup_widget(GTK_WIDGET(user_data), "samplingRateCombo"));
	}
	if (button != NULL) {
		tmpCharName = gtk_file_selection_get_filename(GTK_FILE_SELECTION(fileSelection));
		g_string_assign(dateiName, tmpCharName);
	}
	if (sndFile) {
		sf_close(sndFile);
	}
	if (! (sndFile = sf_open(dateiName->str, SFM_READ, &sfinfo))) {
		/* Open failed so print an error message. */
		g_print("Not able to open input file %s.\n", dateiName->str) ;
		/* Print the error message fron libsndfile. */
		sf_perror (sndFile) ;
	} else {
		numChannels = sfinfo.channels;
		if (button != NULL) {
			g_snprintf(message, 6, _("%d"), sfinfo.samplerate);
			gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(samplingRateCombo) -> entry), message), samplingRate = (gfloat) sfinfo.samplerate;
		} else {
			samplingRate = (gfloat) sfinfo.samplerate;
		}
	}
	g_free(message);
	printSummaryString();
	if (button != NULL) {
		gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(MainWindow), "summaryLabel")), summaryString->str);
		gtk_widget_destroy(GTK_WIDGET(fileSelection));
	}
}


void
on_close_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	if (killIt != 0) {
		g_source_remove(killIt);
/* 		g_print("removed\n"); */
		killIt = 0;
	}
	gtk_widget_destroy(GTK_WIDGET(DisplayWindow));
}

/* ####################### Draw Once ####################### */
void
on_button1_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	glAreaDrawSync(0);
}

/* ####################### Stop Audio-Engine ####################### */
void
on_button2_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	PaError err;
	/* audioExit();*/
	err = Pa_StopStream(stream);
	if( err != paNoError ) g_print("Audio Error (%s): %s\n", __func__, Pa_GetErrorText(err));
	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button6"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "fftSizeEntry"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "closeDeviceButton"), TRUE);
/*	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "waveSecondsEntry"), TRUE); */
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "po2FFT"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "open"), TRUE);
}

/* ####################### Start Audio-Engine ####################### */
void
on_button6_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	PaError err;
	/* audioInit(); */
  err = Pa_StartStream( stream );
	if( err != paNoError ) g_print("Audio Error (%s): %s\n", __func__, Pa_GetErrorText(err));
	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button2"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "closeDeviceButton"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "openDeviceButton"), FALSE);
/* 	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "waveSecondsEntry"), FALSE); */
/*	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "po2FFT"), FALSE); */
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "open"), FALSE);
}

/* ####################### Start Drawing ####################### */
void
on_button3_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	extern guint8 startIt;

	interval = (intervalFaktor * (framesProBuffer / samplingRate) < minInterval) ? minInterval : lrintf(intervalFaktor * (framesProBuffer / samplingRate));

	if (Pa_StreamActive(stream) == 1) {
		while (1) {
			if (startIt == 1) {
				killIt = g_idle_add_full(G_PRIORITY_LOW, (GSourceFunc) glAreaDrawSync, NULL, NULL);
/* 				g_print("TO: %d\n", interval); */
				break;
			}
		}
	} else {
			killIt = g_idle_add_full(G_PRIORITY_LOW, (GSourceFunc) glAreaDrawSync, NULL, NULL);
/* 		g_print("removed\n"); */
	}

	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button7"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button1"), FALSE);

}

/* ####################### Start Drawing _AND_ Audio-Engine ####################### */
void
on_button5_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	if (Pa_StreamActive(stream) != 1) {
		on_button6_clicked(GTK_BUTTON(lookup_widget(GTK_WIDGET(button), "button6")), user_data);
	}
	/* sleep(1); */
	if(killIt == 0) {
		on_button3_clicked(GTK_BUTTON(lookup_widget(GTK_WIDGET(button), "button3")), user_data);
	}
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button8"), TRUE);
	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);

}

/* ####################### Stop Drawing _AND_ Audio-Engine ####################### */
void
on_button8_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	if (Pa_StreamActive(stream) == 1 || Pa_StreamActive(stream) < 0) {
		on_button2_clicked(GTK_BUTTON(lookup_widget(GTK_WIDGET(button), "button2")), user_data);
	}
	if(killIt != 0) {
		on_button7_clicked(GTK_BUTTON(lookup_widget(GTK_WIDGET(button), "button7")), user_data);
	}
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button5"), TRUE);
	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);

}

/* ####################### Stop Drawing ####################### */
void
on_button7_clicked                   (GtkButton       *button,
                                        gpointer         user_data)
{
	g_source_remove(killIt);
/* 		g_print("removed\n"); */
	killIt = 0;
	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button3"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button1"), TRUE);
}

/* ####################### Close Device ####################### */
void
on_closeDeviceButton_clicked          (GtkButton       *button,
                                        gpointer         user_data) {
	audioExit();
	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button6"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button2"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button5"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "openDeviceButton"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "sndDeviceCombo"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "sndDeviceOutCombo"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "readFromFileToggle"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "samplingRateCombo"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "presetCombo"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "bufferSizeEntry"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "po2Buffer"), TRUE);

}

/* ####################### Open Device ####################### */
void
on_openDeviceButton_clicked          (GtkButton       *button,
                                        gpointer         user_data) {
	extern guint8 startIt;

	audioInit(0);
	startIt = 1;
	gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button6"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button2"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "button5"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "closeDeviceButton"), TRUE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "sndDeviceCombo"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "sndDeviceOutCombo"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "readFromFileToggle"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "samplingRateCombo"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "presetCombo"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "bufferSizeEntry"), FALSE);
	gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(button), "po2Buffer"), FALSE);
}

/* ####################### Reset AudioFile ####################### */
void
on_resetFileButton_clicked          (GtkButton       *button,
                                        gpointer         user_data) {
	SF_INFO sfinfo;

	if (sndFile) {
		sf_close(sndFile);
	}
	if (! (sndFile = sf_open(dateiName->str, SFM_READ, &sfinfo))) {
		/* Open failed so print an error message. */
		g_print("Not able to open input file %s.\n", dateiName->str) ;
		/* Print the error message fron libsndfile. */
		sf_perror (sndFile) ;
	} else {
		numChannels = sfinfo.channels;
	}


}
/* ####################### resize widget with Scalebar ############ */
void
resizeWidget													(GtkObject *adjustment,
																						gpointer user_data) {
	extern gfloat waveFaktor;
	/*gtk_drawing_area_size(GTK_DRAWING_AREA(user_data), (gint) (GTK_ADJUSTMENT(adjustment))->value, (GTK_WIDGET(user_data))->allocation.height);*/
	waveFaktor = (GTK_ADJUSTMENT(adjustment)->value > numSeconds) ? numSeconds: (GTK_ADJUSTMENT(adjustment))->value;
}

/* ##################### Set MaxDB-Faktor ############################## */

void adjustFFT												(GtkObject *adjustment,
																					gpointer user_data) {
	maxDB = GTK_ADJUSTMENT(adjustment)->value;
}


/* ##################### Print Status Bar ############################## */

void
printStatus														(GtkObject *widget, GdkEventMotion *event, gpointer user_data) {
	gfloat yRange = 0.0, xRange = 0.0;
	gchar *message;
	GString *widgetName = NULL;
	GString *widgetData;
	gint *x, *y;
	guint32 i = 0;
	gfloat yTemp = 0.0, dBValue = 0.0;
	GLubyte pixelValue[3];
	extern gfloat waveFaktor;

	x = g_new(gint, 1);
	y = g_new(gint, 1);
	message = g_new(gchar, 64);
	widgetData = g_string_new(_(""));
	yRange = (gfloat) GTK_WIDGET(widget)->allocation.height;
	xRange = (gfloat) GTK_WIDGET(widget)->allocation.width;
	gtk_widget_get_pointer(GTK_WIDGET(widget), x, y);

	if(g_strcasecmp(gtk_widget_get_name(GTK_WIDGET(widget)), "WaveArea") == 0) { /* WidgetName */
		/* ################ WaveArea ######################## */

		widgetName = g_string_new(_("Waveform"));
		g_string_sprintfa(widgetData, _("%2.2f%% @-%3.3fsec"),
											 -1 * ((200.0 * *y / yRange) - 100.0),
											 ((xRange - *x) / xRange) * waveFaktor
											);

	} else if (g_strcasecmp(gtk_widget_get_name(GTK_WIDGET(widget)), "FFTArea") == 0) {
		/* ################ FFTArea ######################## */
		widgetName = g_string_new(_("Spectrum"));
		if(displayMode == logarithm) {
			/* (Fs/2) * (log10(2) / log10(24000)) */
			g_string_sprintfa(widgetData, _("%3.2fdB @%5.3fHz"),
												(yRange - *y) / yRange * maxDB,
												(samplingRate / pointsFFT) * logScaleFFT[(*x)]
												);
		} else { /* displayMode != logarithm */
			g_string_sprintfa(widgetData, _("%3.2fdB @%5.3fHz"),
												(yRange - *y) / yRange * maxDB,
												(*x / xRange) * (samplingRate / 2)
												);
		}


	} else if (g_strcasecmp(gtk_widget_get_name(GTK_WIDGET(widget)), "SonoArea") == 0) {
		/* ################ SonoArea ######################## */
		widgetName = g_string_new(_("Sonogram"));
		if (gtk_gl_area_make_current(GTK_GL_AREA(widget))) {
			glReadPixels(*x, lrintf(yRange - *y), 1, 1, GL_RGB, GL_UNSIGNED_BYTE, (void *) pixelValue);
			for (i = 0; i < paletteSize; i++) {
				if ((palette[3 * i] == pixelValue[0] &&
						palette[3 * i + 1] == pixelValue[1] &&
						palette[3 * i + 2] == pixelValue[2] )
						) {
					dBValue = maxDB * i / paletteSize;
					break;
				}
			}
		}
		if(displayMode == logarithm) {
			/* (Fs/2) * (log10(2) / log10(24000)) */
			g_string_sprintfa(widgetData, _("%3.2fdB @%5.3fHz @-%3.3fsec"),
												dBValue,
												(samplingRate / pointsFFT) * logScaleSono[lrintf(yRange - *y)],
												(xRange - *x) * 0.001f * interval
												);
		} else { /* displayMode != logarithm */
			g_string_sprintfa(widgetData, _("%3.2fdB @%5.3fHz @-%3.3fsec"),
												dBValue,
												(samplingRate / pointsFFT) * logScaleSono[lrintf(yRange - *y)],
												(xRange - *x) * 0.001f * interval
												);
		}
	} else if (g_strcasecmp(gtk_widget_get_name(GTK_WIDGET(widget)), "PaletteArea") == 0) {
		/* ################ PaletteArea ######################## */

			widgetName = g_string_new(_("Palette"));
			yTemp = paletteSize / lrintf(ceil(paletteSize / yRange));
			if (*y < yRange - yTemp) {
				g_string_sprintfa(widgetData, _("Outside Palette")
												);
			} else {
				g_string_sprintfa(widgetData, _("%3.2fdB"),
												(yRange - *y) / yTemp * maxDB
												);
			}
	} /* WidgetName */


	g_snprintf(message, 64, "%s: %s", widgetName->str, widgetData->str);

	gtk_statusbar_pop(GTK_STATUSBAR(user_data), 1);
	gtk_statusbar_push(GTK_STATUSBAR(user_data), 1, message);

	g_free(message);
	g_free(x);
	g_free(y);
	g_string_free(widgetName, TRUE);
	g_string_free(widgetData, TRUE);
}
/* ######################## setSamplingRate ############################ */
void setSamplingRate	(GtkEntry *entry, gpointer *user_data) {
	GtkWidget *FFTArea, *SonoArea;
	gfloat *tmpP;
	extern gfloat logFFTBySamplingRate;

	FFTArea = lookup_widget(GTK_WIDGET(DisplayWindow), "FFTArea");
	SonoArea = lookup_widget(GTK_WIDGET(DisplayWindow), "SonoArea");
	samplingRate = atof(gtk_entry_get_text(GTK_ENTRY(entry)));
	logScaleInit(FFTArea);
	logScaleInit(SonoArea);
	numFrames = numSeconds * samplingRate;
	interval = (intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate) < minInterval) ? minInterval : lrintf(intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate)) ;
	printSummaryString();
	tmpP = &audioData[0];
	audioData = g_renew(gfloat, tmpP, numFrames);
	audioDataP = &audioData[0];
	memset(audioDataP, 0, sizeof(gfloat) * numFrames);
	logFFTBySamplingRate = myLog(pointsFFT / samplingRate);
	genNoteTable();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setBufferSize ############################ */
void setBufferSize (GtkObject *adjustment,  gpointer *user_data) {
	gfloat *tmpP;
	gchar *message;
	extern GtkObject *fftAdjustment;
#ifdef USING_FFTW3
	extern gdouble *fftIn, *fftInForw;
#else
	extern fftw_real *fftIn, *fftInForw;
#endif
	if(GTK_TOGGLE_BUTTON(user_data) -> active) {
		if(GTK_ADJUSTMENT(adjustment)->value < framesProBuffer) {
			framesProBuffer = CLAMP(lrintf(prevPowerOfTwo(GTK_ADJUSTMENT(adjustment)->value)), GTK_ADJUSTMENT(adjustment)->lower, 65535);
		} else {
			framesProBuffer = CLAMP(lrintf(nextPowerOfTwo(GTK_ADJUSTMENT(adjustment)->value)), GTK_ADJUSTMENT(adjustment)->lower, 65535);
		}
		message = g_new(gchar, 6);
		g_snprintf(message, 6, "%d", framesProBuffer);
		gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(user_data), "bufferSizeEntry")), message);
		g_free(message);
	} else {
		framesProBuffer = CLAMP(lrintf(GTK_ADJUSTMENT(adjustment)->value), GTK_ADJUSTMENT(adjustment)->lower, 65535);
	}
	if (pointsLPC > framesProBuffer) {
		message = g_new(gchar, 6);
		g_print("*********Warning: This makes no Sense!\n\tSetting Buffer-Size to %d\n", pointsLPC + 1);
		framesProBuffer = pointsLPC + 1;
		GTK_ADJUSTMENT(adjustment)->value = (gfloat) framesProBuffer;
		g_snprintf(message, 6, "%d", framesProBuffer);
		gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(user_data), "bufferSizeEntry")), message);
		g_free(message);
	}
	tmpP = &windowKoeff[0];
	windowKoeff = (pointsFFT < framesProBuffer) ? g_renew(gfloat, tmpP, 6 * pointsFFT) : g_renew(gfloat, tmpP, 6 * framesProBuffer);
	getWindowKoeff();
#ifdef USING_FFTW3
	memset(fftIn, 0, sizeof(gdouble) * pointsFFT);
	memset(fftInForw, 0, sizeof(gdouble) * pointsFFT);
#else
	memset(fftIn, 0, sizeof(fftw_real) * pointsFFT);
	memset(fftInForw, 0, sizeof(fftw_real) * pointsFFT);
#endif
	if (framesProBuffer < pointsFFT) {
		maxDB = 20.0f * log10(1.27f * framesProBuffer * 1024);
	}
	gtk_adjustment_set_value(GTK_ADJUSTMENT(fftAdjustment), maxDB);
	interval = (intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate) < minInterval) ? minInterval : lrintf(intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate));
	printSummaryString();
	GTK_ADJUSTMENT(adjustment)->value = (gfloat) framesProBuffer;
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setFFTSize ############################ */
void setFFTSize (GtkObject *adjustment,  gpointer *user_data) {
	gfloat *tmpP;
#ifdef USING_FFTW3
	gdouble *fftP;
	extern gdouble *fftIn, *fftInForw;
	extern gdouble *fftTmp, *fftTmpForw;
#else
	fftw_real *fftP;
	extern fftw_real *fftIn, *fftInForw;
	extern fftw_real *fftTmp, *fftTmpForw;
#ifdef FFTW_ENABLE_FLOAT
	extern fftw_real *fftForLPC;
#warning "FFTW in single precision is NOT recommended"
#endif
#endif /* USING_FFTW3 */
	extern gdouble *lpcKoeff;
	gdouble *lpcP;
	struct utsname unamePointer;
	GtkWidget *FFTArea, *SonoArea;
	gchar *message;
	extern GtkObject *fftAdjustment;
	extern gfloat logFFTBySamplingRate, logFFTByTwo;
	PaError err;
	guint8 startStream = 0;

	message = g_new(gchar, 6);
	if (Pa_StreamActive(stream) == 1) {
		/* audioExit(); */
		err = Pa_StopStream(stream);
		if( err != paNoError ) g_print("Audio-Engine Error (%s): %s\n", __func__, Pa_GetErrorText(err));
		startStream = 1;
	}

	FFTArea = lookup_widget(GTK_WIDGET(DisplayWindow), "FFTArea");
	SonoArea = lookup_widget(GTK_WIDGET(DisplayWindow), "SonoArea");
	uname(&unamePointer);
	if(GTK_TOGGLE_BUTTON(user_data) -> active) {
		if(GTK_ADJUSTMENT(adjustment)->value < pointsFFT) {
			pointsFFT = CLAMP(lrintf(prevPowerOfTwo(GTK_ADJUSTMENT(adjustment)->value)), GTK_ADJUSTMENT(adjustment)->lower, 65535);
		} else {
			pointsFFT = CLAMP(lrintf(nextPowerOfTwo(GTK_ADJUSTMENT(adjustment)->value)), GTK_ADJUSTMENT(adjustment)->lower, 65535);
		}
		g_snprintf(message, 6, "%d", pointsFFT);
		gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(user_data), "fftSizeEntry")), message);
	} else {
		pointsFFT = CLAMP(lrintf(GTK_ADJUSTMENT(adjustment)->value), GTK_ADJUSTMENT(adjustment)->lower, 65535);
	}
	if (pointsLPC > pointsFFT) {
		g_print("*********Warning: This makes no Sense!\n\tSetting FFT-Points to %d\n", pointsLPC);
		pointsFFT = pointsLPC;
		g_snprintf(message, 6, "%d", pointsFFT);
		gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(user_data), "fftSizeEntry")), message);
	}
	logScaleInit(FFTArea);
	logScaleInit(SonoArea);

	logFFTBySamplingRate = myLog(pointsFFT / samplingRate);
	logFFTByTwo = myLog(pointsFFT / 2);
	genNoteTable();
	
	fftw_destroy_plan(plan);
#ifdef USING_FFTW3
	fftw_destroy_plan(planForw);
	fftw_destroy_plan(planLPC);
	fftw_destroy_plan(planLPCForw);
#endif /* USING_FFTW3 */
	tmpP = &windowKoeff[0];
	windowKoeff = (pointsFFT < framesProBuffer) ? g_renew(gfloat, tmpP, 6 * pointsFFT) : g_renew(gfloat, tmpP, 6 * framesProBuffer);
	getWindowKoeff();
	if (pointsFFT < framesProBuffer) {
		maxDB = 20.0f * log10(1.27f * pointsFFT * 1024);
	}
	gtk_adjustment_set_value(GTK_ADJUSTMENT(fftAdjustment), maxDB);

	fftP = &fftDataP[0];
	fftDataP = g_renew(gdouble, fftP, pointsFFT + 2);
#ifdef USING_FFTW3
	fftw_free(fftIn);
	fftIn = fftw_malloc(sizeof(gdouble) * pointsFFT);
	memset(fftIn, 0, sizeof(gdouble) * pointsFFT);
	fftw_free(fftTmp);
	fftTmp = fftw_malloc(sizeof(gdouble) * pointsFFT);
	fftw_free(fftInForw);
	fftInForw = fftw_malloc(sizeof(gdouble) * pointsFFT);
	memset(fftInForw, 0, sizeof(gdouble) * pointsFFT);
	fftw_free(fftTmpForw);
	fftTmpForw = fftw_malloc(sizeof(gdouble) * pointsFFT);
	fftw_free(lpcKoeff);
	lpcKoeff = fftw_malloc(sizeof(gdouble) * pointsFFT);
	memset(lpcKoeff, 0, sizeof(gdouble) * pointsFFT);

	plan = fftw_plan_r2r_1d(pointsFFT, fftIn, fftTmp, FFTW_R2HC, FFTW_MEASURE);
	planForw = fftw_plan_r2r_1d(pointsFFT, fftInForw, fftTmpForw, FFTW_R2HC, FFTW_MEASURE);
	planLPC = fftw_plan_r2r_1d(pointsFFT, lpcKoeff, fftTmp, FFTW_R2HC, FFTW_MEASURE);
	planLPCForw = fftw_plan_r2r_1d(pointsFFT, lpcKoeff, fftTmpForw, FFTW_R2HC, FFTW_MEASURE);
#else
	plan = rfftw_create_plan(pointsFFT, FFTW_REAL_TO_COMPLEX, FFTW_MEASURE|FFTW_USE_WISDOM);
	fftP = &fftIn[0];
	fftIn = g_renew(fftw_real, fftP, pointsFFT);
	memset(fftIn, 0, sizeof(fftw_real) * pointsFFT);
	fftP = &fftTmp[0];
	fftTmp = g_renew(fftw_real, fftP, pointsFFT);
	fftP = &fftInForw[0];
	fftInForw = g_renew(fftw_real, fftP, pointsFFT);
	memset(fftInForw, 0, sizeof(fftw_real) * pointsFFT);
	fftP = &fftTmpForw[0];
	fftTmpForw = g_renew(fftw_real, fftP, pointsFFT);
	lpcP = &lpcKoeff[0];
	lpcKoeff = g_renew(gdouble, lpcP,  (pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1);
	memset(lpcKoeff, 0, sizeof(gdouble) * ((pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1));
#ifdef FFTW_ENABLE_FLOAT
	tmpP = &fftForLPC[0];
	fftForLPC = g_renew(fftw_real, tmpP, (pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1);
	memset(fftForLPC, 0, sizeof(fftw_real) * ((pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1));
#endif
#endif /* USING_FFTW3 */
	printSummaryString();
	GTK_ADJUSTMENT(adjustment)->value = (gfloat) pointsFFT;
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
	if (Pa_StreamActive(stream) != 1 && startStream == 1) {
		/* audioInit(); */
	  err = Pa_StartStream( stream );
		if( err != paNoError ) g_print("Error3: %s\n",Pa_GetErrorText(err));
	}
	if (killIt == 0) {
		glAreaDrawSync(42);
	}
	g_free(message);
}

/* ######################## setLPCSize ############################ */
void setLPCSize (GtkObject *adjustment,  gpointer *user_data) {
	PaError err;
	guint8 startStream = 0;
	gdouble *tmpP;
#ifdef FFTW_ENABLE_FLOAT
	fftw_real *fftP;
	extern fftw_real *fftForLPC;
#warning "FFTW in single precision is NOT recommended"
#endif
	gchar *message;
	extern gdouble  *autoKoeff, *reflexKoeff, *lpcKoeff, *tmpBuff;
	if (Pa_StreamActive(stream) == 1) {
		/* audioExit(); */
		err = Pa_StopStream(stream);
		if( err != paNoError ) g_print("Error3: %s\n",Pa_GetErrorText(err));
		startStream = 1;
	}
	pointsLPC = lrintf(GTK_ADJUSTMENT(adjustment)->value);
	if (pointsLPC > pointsFFT) {
		message = g_new(gchar, 6);
		g_print("*********Warning: This makes no Sense!\n\tSetting LPC-Points to %d\n", pointsFFT);
		pointsLPC = pointsFFT;
		GTK_ADJUSTMENT(adjustment)->value = (gfloat) pointsFFT;
		g_snprintf(message, 6, "%d", pointsFFT);
		gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(user_data), "lpcSizeEntry")), message);
		g_free(message);
	}
	if (pointsLPC > framesProBuffer) {
		message = g_new(gchar, 6);
		g_print("*********Warning: This makes no Sense!\n\tSetting LPC-Points to %d\n", framesProBuffer - 1);
		pointsLPC = framesProBuffer - 1;
		GTK_ADJUSTMENT(adjustment)->value = (gfloat) pointsLPC;
		g_snprintf(message, 6, "%d", pointsLPC);
		gtk_entry_set_text(GTK_ENTRY(lookup_widget(GTK_WIDGET(user_data), "lpcSizeEntry")), message);
		g_free(message);
	}
	tmpP = &autoKoeff[0];
	autoKoeff = g_renew(gdouble, tmpP, pointsLPC + 1);
/* 	memset(autoKoeff, 0, sizeof(gdouble) * (pointsLPC + 1)); */
	tmpP = &lpcKoeff[0];
#ifdef USING_FFTW3
	fftw_free(lpcKoeff);
	lpcKoeff = fftw_malloc(sizeof(gdouble) * pointsFFT);
#else
	lpcKoeff = g_renew(gdouble, tmpP,  (pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1);
#endif /* USING_FFTW3 */
	memset(lpcKoeff, 0, sizeof(gdouble) * ((pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1));
	tmpP = &reflexKoeff[0];
	reflexKoeff = g_renew(gdouble, tmpP, pointsLPC + 1);
/* 	memset(reflexKoeff, 0, sizeof(gdouble) * (pointsLPC + 1)); */
	tmpP = &tmpBuff[0];
	tmpBuff = g_renew(gdouble, tmpP, pointsLPC + 1);
/* 	memset(tmpBuff, 0, sizeof(gdouble) * (pointsLPC + 1)); */
#ifdef FFTW_ENABLE_FLOAT
	fftP = &fftForLPC[0];
	fftForLPC = g_renew(fftw_real, fftP, (pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1);
	memset(fftForLPC, 0, sizeof(fftw_real) * ((pointsLPC < pointsFFT) ? pointsFFT : pointsLPC + 1));
#endif
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
	if (Pa_StreamActive(stream) != 1 && startStream == 1) {
		/* audioInit(); */
	  err = Pa_StartStream( stream );
		if( err != paNoError ) g_print("Error3: %s\n",Pa_GetErrorText(err));
	}
}
/* ######################## setWindowType ############################ */
void setWindowType	(GtkEntry *entry, gpointer *user_data) {
	gchar *p;
	guint16 i = 0;
	p = gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1);
	/* g_list_index() didn't work... */
	for(i = 0; i < g_list_length(windowNameList); i++) {
		if(g_strcasecmp(p, g_list_nth_data(windowNameList, i)) == 0) {
			windowType = i;
			break;
		}
	}
	g_free(p);
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setDisplayType ############################ */
void setDisplayType	(GtkEntry *entry, gpointer *user_data) {
	GtkWidget *FFTArea, *SonoArea;
	gchar startDraw = 0;
	extern guint8 startIt;

	if(killIt != 0)  {
		g_source_remove(killIt);
/* 		g_print("removed\n"); */
		killIt = 0;
		startDraw = 1;
	}

	FFTArea = lookup_widget(GTK_WIDGET(DisplayWindow), "FFTArea");
	SonoArea = lookup_widget(GTK_WIDGET(DisplayWindow), "SonoArea");
	if(g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("linear")) == 0) {
		displayMode	= linear;
	 } else {
		displayMode = logarithm;
	}
	logScaleInit(FFTArea);
	logScaleInit(SonoArea);
/* 	glAreaDrawSync(SIGALRM); */
	if(startDraw == 1)  {
		interval = (intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate) < minInterval) ? minInterval : lrintf(intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate));
		while (1) {
			if (startIt == 1) {
				killIt = g_idle_add_full(G_PRIORITY_LOW, (GSourceFunc) glAreaDrawSync, NULL, NULL);
/* 				g_print("TO: %d\n", interval); */
				break;
			}
		}
		startDraw = 0;
	}
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setReferenceA ############################ */
void setReferenceA	(GtkObject *adjustment, gpointer *user_data) {
	extern gfloat referenceA;
	referenceA = GTK_ADJUSTMENT(adjustment)->value;
	genNoteTable();
}

/* ######################## setSndDevice ############################ */
void setSndDevice	(GtkEntry *entry, gpointer *user_data) {
	GList *samplingRateList=NULL;
	extern GList *sndDeviceList;
	gchar *p;
	guint16 i = 0;
	p = gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1);
	/* g_list_index() didn't work... */

	for(i = 0; i < g_list_length(sndDeviceList); i++) {
		if(g_strcasecmp(p, g_list_nth_data(sndDeviceList, i)) == 0) {
			sndDevice = i;
			break;
		}
	}
	g_free(p);
	g_print("Input Device: %d\n", sndDevice);
	samplingRateList = getSamplingRate(samplingRateList);
	gtk_combo_set_popdown_strings(GTK_COMBO(lookup_widget(GTK_WIDGET(user_data), "samplingRateCombo")), samplingRateList);
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setSndDeviceOut ############################ */
void setSndDeviceOut	(GtkEntry *entry, gpointer *user_data) {
	GList *samplingRateList=NULL;
	extern GList *sndDeviceList;
	gchar *p;
	guint16 i = 0;
	p = gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1);
	/* g_list_index() didn't work... */

	for(i = 0; i < g_list_length(sndDeviceList); i++) {
		if(g_strcasecmp(p, g_list_nth_data(sndDeviceList, i)) == 0) {
			sndDeviceOut = i;
			break;
		}
	}
	g_free(p);
	g_print("Output Device : %d\n", sndDeviceOut);
	samplingRateList = getSamplingRate(samplingRateList);
	gtk_combo_set_popdown_strings(GTK_COMBO(lookup_widget(GTK_WIDGET(user_data), "samplingRateCombo")), samplingRateList);
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}



/* ######################## setSpecType ############################ */
void setSpecType	(GtkEntry *entry, gpointer *user_data) {
	if(g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("FFT")) == 0) {
		specType	= dft;
	 } else {
		specType = lpc;
	}
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setPreset ############################ */
void setPreset	(GtkEntry *entry, gpointer *user_data) {
	extern GtkObject *fftSizeAdjustment;
	extern GtkObject *bufferSizeAdjustment;
	extern GtkObject *lpcSizeAdjustment;
	extern GtkObject *scaleAdjustment;
	extern guint8 readFromFile;
	GtkCombo *specTypeCombo, *samplingRateCombo, *windowTypeCombo, *displayTypeCombo;
	GtkWidget *po2FFT, *po2Buffer;

	specTypeCombo = GTK_COMBO(lookup_widget(GTK_WIDGET(user_data), "specTypeCombo"));
	samplingRateCombo = GTK_COMBO(lookup_widget(GTK_WIDGET(user_data), "samplingRateCombo"));
	windowTypeCombo = GTK_COMBO(lookup_widget(GTK_WIDGET(user_data), "windowTypeCombo"));
	displayTypeCombo = GTK_COMBO(lookup_widget(GTK_WIDGET(user_data), "displayTypeCombo"));
	po2FFT = lookup_widget(GTK_WIDGET(user_data), "po2FFT");
	po2Buffer = lookup_widget(GTK_WIDGET(user_data), "po2Buffer");

	if (g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("Vocal / Choir")) == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(specTypeCombo) -> entry),
			_("LPC")), specType = lpc;
		if (readFromFile == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(samplingRateCombo) -> entry),
			_("44100")), samplingRate = 44100.0f;
		}
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(windowTypeCombo) -> entry),
		_("hamming")), windowType = hamming;
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(displayTypeCombo) -> entry),
			_("logarithmic")), displayMode = logarithm;
		gtk_adjustment_set_value(GTK_ADJUSTMENT(lpcSizeAdjustment),
			192);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(fftSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2FFT) -> active) ? 2047 : 2048);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(bufferSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2Buffer) -> active) ? 2047 : 2048);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(scaleAdjustment), 1.5f);
		goto end;
	}
	if (g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("Noises")) == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(specTypeCombo) -> entry),
			_("FFT")), specType = dft;
		if (readFromFile == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(samplingRateCombo) -> entry),
			"48000"),	samplingRate = 48000.0f;
		}
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(windowTypeCombo) -> entry),
			_("rect")),	windowType = kaiser;
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(displayTypeCombo) -> entry),
			_("logarithmic")), displayMode = logarithm;
		gtk_adjustment_set_value(GTK_ADJUSTMENT(lpcSizeAdjustment),
			256);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(fftSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2FFT) -> active) ? 8191 : 8192);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(bufferSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2Buffer) -> active) ? 1023 : 1024);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(scaleAdjustment), 0.1f);
		goto end;
	}
	if (g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("Speech")) == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(specTypeCombo) -> entry),
			_("LPC")), specType = lpc;
		if (readFromFile == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(samplingRateCombo) -> entry),
			_("16000")), samplingRate = 16000.0f;
		}
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(windowTypeCombo) -> entry),
			_("bartlett")), windowType = bartlett;
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(displayTypeCombo) -> entry),
			_("logarithmic")), displayMode = logarithm;
		gtk_adjustment_set_value(GTK_ADJUSTMENT(lpcSizeAdjustment),
			32);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(fftSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2FFT) -> active) ? 1023 : 1024);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(bufferSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2Buffer) -> active) ? 511 : 512);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(scaleAdjustment), 0.5f);
		goto end;
	}
	if (g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("Orchestra")) == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(specTypeCombo) -> entry),
			_("LPC")), specType = lpc;
		if (readFromFile == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(samplingRateCombo) -> entry),
			"48000"), samplingRate = 48000.0f;
		}
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(windowTypeCombo) -> entry),
			_("blackman")), windowType = blackman;
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(displayTypeCombo) -> entry),
			_("logarithmic")), displayMode = logarithm;
		gtk_adjustment_set_value(GTK_ADJUSTMENT(lpcSizeAdjustment),
			164);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(fftSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2FFT) -> active) ? 4095 : 4096);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(bufferSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2Buffer) -> active) ? 2047 : 2048);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(scaleAdjustment), 2.5f);
		goto end;
	}
	if (g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("Rock/Pop")) == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(specTypeCombo) -> entry),
			_("LPC")), specType = lpc;
		if (readFromFile == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(samplingRateCombo) -> entry),
			"441000"), samplingRate = 44100.0f;
		}
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(windowTypeCombo) -> entry),
			_("hamming")), windowType = hamming;
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(displayTypeCombo) -> entry),
			_("logarithmic")), displayMode = logarithm;
		gtk_adjustment_set_value(GTK_ADJUSTMENT(lpcSizeAdjustment),
			220);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(fftSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2FFT) -> active) ? 8191 : 8192);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(bufferSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2Buffer) -> active) ? 2047 : 2048);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(scaleAdjustment), 1.5f);
		goto end;
	}
	if (g_strcasecmp(gtk_editable_get_chars(GTK_EDITABLE(entry),0, -1), _("Scientific")) == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(specTypeCombo) -> entry),
			_("FFT")), specType = dft;
		if (readFromFile == 0) {
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(samplingRateCombo) -> entry),
			"48000"), samplingRate = 48000.0f;
		}
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(windowTypeCombo) -> entry),
			_("blackman")), windowType = blackman;
		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(displayTypeCombo) -> entry),
			_("linear")), displayMode = linear;
		gtk_adjustment_set_value(GTK_ADJUSTMENT(lpcSizeAdjustment),
			285);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(fftSizeAdjustment),
			(GTK_TOGGLE_BUTTON(po2FFT) -> active) ? 16383 : 16384);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(bufferSizeAdjustment),
		(GTK_TOGGLE_BUTTON(po2Buffer) -> active) ? 1023 : 1024);
		gtk_adjustment_set_value(GTK_ADJUSTMENT(scaleAdjustment), 0.001f);
		goto end;
	}
	end:
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setWaveFormSize ############################ */
/* void setWaveFormSize (GtkObject *adjustment,  gpointer *user_data) { */
/* 	gfloat *tmpP; */
/*  */
/* 	numSeconds = GTK_ADJUSTMENT(adjustment)->value; */
/* 	numFrames = numSeconds * samplingRate; */
/* 	tmpP = &audioData[0]; */
/* 	audioData = g_renew(gfloat, tmpP, numFrames); */
/* 	memset(audioData, 0, numFrames * sizeof(gfloat)); */
/* 	audioDataP = &audioData[0]; */
/* 	printSummaryString(); */
/* 	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str); */
/* } */

/* ######################## setOverlap ############################ */
void setOverlap (GtkObject *button,  gpointer *user_data) {

	if (GTK_TOGGLE_BUTTON(button) -> active) {
		overlap = 1;
	} else {
		overlap = 0;
	}

	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setHighPass ############################ */
void setHighPass (GtkObject *button,  gpointer *user_data) {
	extern guint8 doHighPass;

	if (GTK_TOGGLE_BUTTON(button) -> active) {
		doHighPass = 1;
	} else {
		doHighPass = 0;
	}

	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setOverlap ############################ */
void setReadFromFile (GtkObject *button,  gpointer *user_data) {
	extern guint8 readFromFile;
	if (GTK_TOGGLE_BUTTON(button) -> active) {
		readFromFile = 1;
		gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(user_data), "resetFileButton"), TRUE);
	} else {
		readFromFile = 0;
		gtk_widget_set_sensitive(lookup_widget(GTK_WIDGET(user_data), "resetFileButton"), FALSE);
	}
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}

/* ######################## setDrawStaves ############################ */
void setDrawStaves (GtkObject *button,  gpointer *user_data) {
	extern guint8 drawStaves;

	if (GTK_TOGGLE_BUTTON(button) -> active) {
		drawStaves = 1;
	} else {
		drawStaves= 0;
	}
	if (killIt == 0) {
	glAreaDrawSync(42);
	}
	printSummaryString();
	gtk_label_set_text(GTK_LABEL(lookup_widget(GTK_WIDGET(user_data), "summaryLabel")), summaryString->str);
}
