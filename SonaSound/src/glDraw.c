/*
 *  $Id: glDraw.c,v 1.115 2003/04/03 19:07:42 niklaswerner Exp $
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
 * functions for drawing the three glAreas
 *
 */

#include "sonasound.h"

#include <gtkgl/gtkglarea.h>
#include <GL/glu.h>
#include <GL/glx.h>
#ifndef __glext_h_
#	include <GL/glext.h>
#endif

#include "audioIo.h"
#include "audioInit.h"
#include "waveForm.h"
#include "sonogram.h"
#include "fft.h"
#include "glDraw.h"
#include "support.h"
#include "globalsInit.h"
#include "palette.h"
#include <time.h>
#include <sys/time.h>
#ifdef LINUX_RTC
#	include <linux/rtc.h>
#	include <sys/ioctl.h>
#	include <fcntl.h>
#endif


GLushort *glElements; /* Array for Waveview-drawing */
#ifndef HAVE_FAST_GA
static GLuint textureName; /* texture for Sonogram */
#endif
GLfloat yMax = 0.0f, xMax = 0.0f; /* make DrawSonagram faster */
guint8 oddEven = 0; /* Counter for accessing overlap-Data */
GLfloat *staffLineVertices, *staffLineVerticesLog, *octaveLineVertices, *octaveLineVerticesLog, *staffLineVerticesSonoLog, *staffLineVerticesSono;

gfloat logFFTBySamplingRate = 1;
gfloat logFFTByTwo = 1;
GtkWidget *WaveArea, *FFTArea, *SonoArea;
GTimer *Zeit;
gdouble timeRemaining = 0.0;
guint8 drawWave = 1;

/* Draw everything synced in one go: gets registered as g_idle-function
 * This is rather ugly, but I didn't have the time for proper signal/thread
 * handling. gtk_timeout gets confused by mouse-events! So my own timeout had to
 * be written.
 */
gint glAreaDrawSync(int sig) {
	struct timeval remInterval;
	extern guint8 startIt;
#ifdef LINUX_RTC
	extern gint fd;
	fd_set readfds;
	gint retVal = 0;
#endif

	if ((1000.0 *  g_timer_elapsed(Zeit, NULL) >= (gdouble) interval && startIt == 1) || (sig == 42)) {
		g_timer_reset(Zeit);
		if (drawWave == 1) {
			glAreaDrawWave(WaveArea, NULL);
			drawWave = 0;
		} else {
			drawWave = 1;
		}
		glAreaDrawFFT(FFTArea, NULL);
		glAreaDrawSonogram(SonoArea, NULL);
		timeRemaining = g_timer_elapsed(Zeit, NULL);
	/* see nanosleep() man-page: system timer is soooooooo slow (100Hz)
	 * And RTC only gives you 64Hz, if you're unprivileged...
	 */
#ifdef LINUX_RTC
	} else if (interval - (1000 * timeRemaining) > 16 || fd != -1) {
#elif defined MACOSX
	} else if (interval - (1000 * timeRemaining) > 8) {
#else
	} else if (interval - (1000 * timeRemaining) > 16) {
#endif
#ifdef LINUX_RTC
		FD_ZERO(&readfds);
		if (fd != -1) {
			retVal = ioctl(fd, RTC_PIE_ON, 0);
/* 			if (retVal == -1) { */
/* 				g_print("Couldn't start Timer\n"); */
/* 			} */
			retVal = ioctl(fd, RTC_IRQP_SET, nextPowerOfTwo(lrintf(1.0/((gfloat) interval / 1000 - timeRemaining))));
/* 			if (retVal == -1) { */
/* 				g_print("Couldn't set RTC to %d\n", */
/* 					nextPowerOfTwo(lrintf(1.0 / ((gfloat) interval / 1000 - hm)))); */
/* 			} */
			FD_SET(fd, &readfds);
		}
#endif
		remInterval.tv_sec = 0;
		remInterval.tv_usec = lrintf( 1000*( interval - (1000 * timeRemaining) - 10));
#ifdef LINUX_RTC
		select(fd+1, &readfds, NULL, NULL, &remInterval);
#else
		select(0, NULL, NULL, NULL, &remInterval);
#endif
	}
	return TRUE;
}

/* Initialize glAreas */
gint glAreaInit(GtkWidget *widget)
{
	/*GLenum errorCode;
	const GLubyte *errorString;*/
	const GLubyte* extString = glGetString(GL_EXTENSIONS);
	GLushort *tmpP;
	guint16 i = 0;
#ifndef HAVE_FAST_GA
	guint16 textureDimensX = 0, textureDimensY = 0;
#endif
	WaveArea = lookup_widget(GTK_WIDGET(widget),"WaveArea");
	FFTArea = lookup_widget(GTK_WIDGET(widget),"FFTArea");
	SonoArea = lookup_widget(GTK_WIDGET(widget),"SonoArea");
	Zeit = g_timer_new();
	g_timer_start(Zeit);
  /* OpenGL functions can be called only if make_current returns true */
  if (gtk_gl_area_make_current(GTK_GL_AREA(widget)))
	{
		glViewport(0, 0, (GLsizei) widget->allocation.width, (GLsizei) widget->allocation.height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		if(g_strcasecmp(widget->name,"WaveArea") == 0) {
			tmpP = &glElements[0];
			glElements = g_renew(GLushort, tmpP, 2 * widget->allocation.width);
			for ( i = 0; i < 2 * widget->allocation.width; i++) {
				glElements[i] = 2 * widget->allocation.width - i;
			}
			gluOrtho2D(0.0f, 100.0f, -50.0f, 50.0f);
		} else {
			gluOrtho2D(0.0f, 1000.0f, 0.0f, 100.0f);
		}
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		/* disable everything that might slow down glCopyPixels */
    glDisable(GL_BLEND);
    glDisable(GL_DITHER);
    glDisable(GL_TEXTURE_1D);
#ifdef HAVE_FAST_GA
    glDisable(GL_TEXTURE_2D);
#endif
		glDisable(GL_TEXTURE_3D);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_FOG);
		glDisable(GL_LIGHTING);
		glDisable(GL_LOGIC_OP);
		glDisable(GL_STENCIL_TEST);
    glDisable(GL_MINMAX);
		glDisable(GL_LINE_SMOOTH);
		glDisable(GL_POINT_SMOOTH);
		glDisable(GL_POLYGON_SMOOTH);
		glDisable(GL_CULL_FACE);
		glPixelTransferi(GL_MAP_COLOR, GL_FALSE);
		glPixelTransferi(GL_RED_SCALE, 1);
		glPixelTransferi(GL_RED_BIAS, 0);
		glPixelTransferi(GL_GREEN_SCALE, 1);
		glPixelTransferi(GL_GREEN_BIAS, 0);
		glPixelTransferi(GL_BLUE_SCALE, 1);
		glPixelTransferi(GL_BLUE_BIAS, 0);
		glPixelTransferi(GL_ALPHA_SCALE, 1);
		glPixelTransferi(GL_ALPHA_BIAS, 0);
		glShadeModel (GL_FLAT);
		if (extString != NULL) {
       if (strstr((char*) extString, "GL_EXT_convolution") != NULL) {
           glDisable(GL_CONVOLUTION_1D_EXT);
           glDisable(GL_CONVOLUTION_2D_EXT);
           glDisable(GL_SEPARABLE_2D_EXT);
       }
       if (strstr((char*) extString, "GL_EXT_histogram") != NULL) {
           glDisable(GL_HISTOGRAM_EXT);
           glDisable(GL_MINMAX_EXT);
       }
       if (strstr((char*) extString, "GL_EXT_texture3D") != NULL) {
           glDisable(GL_TEXTURE_3D_EXT);
       }
    }


		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glPixelStorei(GL_PACK_ALIGNMENT, 1);
		glEnableClientState(GL_VERTEX_ARRAY);
		glPolygonMode(GL_FRONT, GL_FILL);
		glPolygonMode(GL_BACK, GL_FILL);

		if(g_strcasecmp(widget->name,"FFTArea") == 0) {
			glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
			staffLineVertices = g_new0(GLfloat, 40);
			staffLineVerticesLog = g_new0(GLfloat, 40);
			octaveLineVertices = g_new0(GLfloat, 36);
			octaveLineVerticesLog= g_new0(GLfloat, 36);
			staffLineVerticesSono = g_new0(GLfloat, 40);
			staffLineVerticesSonoLog = g_new0(GLfloat, 40);
			genNoteTable();
		} else {
			glClearColor(0, 0, 0, 0);
		}
		glClear(GL_COLOR_BUFFER_BIT);

		if (glXIsDirect(glXGetCurrentDisplay() , glXGetCurrentContext() )) {
			glReadBuffer(GL_FRONT);
			g_print("Good! Direct Rendering enabled :-)\n");
		} else {
			glReadBuffer(GL_BACK);
			g_print("Hmm! Direct Rendering NOT enabled :-(\n");
		}

		glDrawBuffer(GL_BACK);
		glDepthMask(GL_FALSE);
		glStencilMask(GL_FALSE);
/* 		glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_FALSE); */


		/* Texture for sonogram */
#ifndef HAVE_FAST_GA
	if(g_strcasecmp(widget->name,"SonoArea") == 0) {
		glEnable(GL_TEXTURE_2D);
		textureDimensX = nextPowerOfTwo(widget->allocation.width);
		textureDimensY = nextPowerOfTwo(widget->allocation.height);
		glGenTextures(1, &textureName);
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		glBindTexture(GL_TEXTURE_2D, textureName);
		/*glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);*/
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	/*	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_ALPHA_SIZE, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1); */
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, textureDimensX, textureDimensY, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
		yMax =  (gfloat) (widget->allocation.height / (gfloat) nextPowerOfTwo(widget->allocation.height));
		xMax = (gfloat) (widget->allocation.width /(gfloat)  nextPowerOfTwo(widget->allocation.width));
/* 		g_print("widget: %d x %d\n", widget->allocation.width,widget->allocation.height ); */
	}
#endif /* HAVE_FAST_GA */
/*	while((errorCode = glGetError()) != GL_NO_ERROR) {
		errorString = gluErrorString(errorCode);
		g_print("OpenGL: %s %s\n", __func__, errorString);
	}*/
	} /* glxMakeCurrent */
	logFFTBySamplingRate = myLog(pointsFFT / samplingRate);
	logFFTByTwo = myLog(pointsFFT / 2);
  return TRUE;
}

/* ********************************************
*                  WaveForm
********************************************* */
gint glAreaDrawWave(GtkWidget *widget, GdkEventMotion *event) {
	/*GLenum errorCode;
	const GLubyte *errorString; */
	areaName name;
  if (gtk_gl_area_make_current(GTK_GL_AREA(widget)))
	{
		name=wave;
		glClear(GL_COLOR_BUFFER_BIT);
		glAreaDrawGrid(name, widget);

		glColor3f(0.35678f, 0.67f, 0.99965f);
		glLineWidth(1);
		calculateWaveForm(widget->allocation.width);
		glVertexPointer(2, GL_FLOAT, 0, verticesWave);
		glDrawElements(GL_LINE_STRIP, 2 * widget->allocation.width, GL_UNSIGNED_SHORT, glElements);
		gtk_gl_area_swap_buffers(GTK_GL_AREA(widget));

	/*	while((errorCode = glGetError()) != GL_NO_ERROR) {
			errorString = gluErrorString(errorCode);
			g_print("OpenGL:%s %s\n", __func__, errorString);
		}*/
	} /* glxMakeCurrent */
	return TRUE;
}

/* ********************************************
*                  Sonagramm
********************************************* */

gint glAreaDrawSonogram(GtkWidget *widget, GdkEventMotion *event) {
/*	GLenum errorCode;
	const GLubyte *errorString; */
	areaName name = sono;
	extern guint8 drawStaves;
#ifndef HAVE_FAST_GA
	register GLubyte *readP, *workP;
#endif

  if (gtk_gl_area_make_current(GTK_GL_AREA(widget)))
	{
#ifdef HAVE_FAST_GA
		glRasterPos2i(0, 0);
		glCopyPixels((GLint) (1), (GLint) 0, (GLsizei) (widget->allocation.width - 1), (GLsizei) (widget->allocation.height), GL_COLOR);
    calculateSonogram((widget->allocation.width), (widget->allocation.height));
		glRasterPos2f(1000.0f - (1000.0f / (widget->allocation.width )), 0.0f);
		glDrawPixels((GLsizei) 1 , (GLsizei) (widget->allocation.height), GL_RGB, GL_UNSIGNED_BYTE, verticesSono);
		if (drawStaves == 1) {
			glAreaDrawGrid(name, widget);
		}
		gtk_gl_area_swap_buffers(GTK_GL_AREA(widget));
#else /* HAVE_FAST_GA */
		readP = &verticesSono[0];
		workP = &verticesSono[3];
		glClear(GL_COLOR_BUFFER_BIT);
    calculateSonogram((widget->allocation.width), (widget->allocation.height));
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0 , (widget->allocation.width), (widget->allocation.height) , GL_RGB, GL_UNSIGNED_BYTE, verticesSono);
/* 		glReadBuffer(GL_BACK); */
/* 		glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0 , 1, 0, (widget->allocation.width-1), (widget->allocation.height) ); */

		glBegin(GL_QUADS);
			glTexCoord2f(0.0f, 0.0f),			glVertex2f(0.0f, 0.0f);
			glTexCoord2f(0.0f, yMax),			glVertex2f(0.0f, 100.0f);
			glTexCoord2f(xMax, yMax),			glVertex2f(1000.0f, 100.0f);
			glTexCoord2f(xMax, 0.0f),			glVertex2f(1000.0f, 0.0f);
		glEnd();
		if (drawStaves == 1) {
			glDisable(GL_TEXTURE_2D);
			glAreaDrawGrid(name, widget);
			glEnable(GL_TEXTURE_2D);
		}
		gtk_gl_area_swap_buffers(GTK_GL_AREA(widget));
		g_memmove(readP, workP, (widget->allocation.width - 1) * widget->allocation.height * 3);
#endif /* HAVE_FAST_GA */
	} /* glxMakeCurrent */
	return TRUE;
}


/* ********************************************
*                  FFT
********************************************* */

gint glAreaDrawFFT(GtkWidget *widget, GdkEventMotion *event) {
	/*GLenum errorCode;
	const GLubyte *errorString;*/
	areaName name;
  if (gtk_gl_area_make_current(GTK_GL_AREA(widget)))
	{
		name=fft;
		glClear(GL_COLOR_BUFFER_BIT);
		glAreaDrawGrid(name, widget);

		glColor3f(0.45678f, 0.77f, 0.75f);
		glLineWidth(1);

		calculateFFTView(widget->allocation.width, widget->allocation.height);

		gtk_gl_area_swap_buffers(GTK_GL_AREA(widget));
/*		while((errorCode = glGetError()) != GL_NO_ERROR) {
			errorString = gluErrorString(errorCode);
			g_print("OpenGL: %s %s\n", __func__, errorString);
		} */
	} /* glxMakeCurrent */
	return TRUE;
}

/* ********************************************
*                 Palette
********************************************* */

gint glAreaDrawPalette(GtkWidget *widget, GdkEventMotion *event) {
	/*GLenum errorCode;
	const GLubyte *errorString;*/
	guint8 j = 0, yFaktor = 0;
	GLfloat y = 0.0f;
	register guint16 r = 0, g = 0, b = 0;
	const GLubyte *readP;
	readP = &palette[0];
  if (gtk_gl_area_make_current(GTK_GL_AREA(widget))) {
		glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glLineWidth(1);
/* 		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); */
		yFaktor = lrintf(ceil((paletteSize) / (gfloat)(widget->allocation.height)));
		glBegin(GL_LINES);
		while(readP <= &palette[sizeof(palette) - 1]) {
			j = 0;
			while(j < yFaktor) {
				r += *readP++;
				g += *readP++;
				b += *readP++;
				j++;
			}
			glColor3ub(lrintf(r / yFaktor), lrintf(g / yFaktor), lrintf(b / yFaktor));
			glVertex2f(0.0f, y);
			glVertex2f(1000.0f, y);
			y += (100.0f / (widget->allocation.height));
			r = 0;
			g = 0;
			b = 0;
		}
		glEnd();

		gtk_gl_area_swap_buffers(GTK_GL_AREA(widget));
/*		while ((errorCode = glGetError()) != GL_NO_ERROR) {
			errorString = gluErrorString(errorCode);
			g_print("OpenGL: %s %s\n", __func__, errorString);
		}*/
	} /* glxMakecurrent */
	return TRUE;
}

/* ********************************************
*                 ReShape
********************************************* */
/* ReDraw Window, when size is changed */
gint glAreaReshape(GtkWidget *widget, GdkEventConfigure *event)
{
/*	GLenum errorCode;
	const GLubyte *errorString;*/
	GLubyte *tmpP;
	GLushort *tmpPElements;
	guint16 i = 0;
	extern guint8 startIt;
#ifndef HAVE_FAST_GA
	guint16 textureDimensX = 0, textureDimensY = 0;
#endif
	gchar startDraw = 0;
/* 	g_print("%s\n", __func__); */
  /* OpenGL functions can be called only if make_current returns true */
  if (gtk_gl_area_make_current(GTK_GL_AREA(widget)))
	{
		glFlush();
		glViewport(0, 0, (GLsizei) widget->allocation.width, (GLsizei) widget->allocation.height);
	  glMatrixMode(GL_PROJECTION);
	  glLoadIdentity();
		if(g_strcasecmp(widget->name,"WaveArea") == 0) {
			tmpPElements = &glElements[0];
			glElements = g_renew(GLushort, tmpPElements, 2 * widget->allocation.width);
			for (i = 0; i < 2 * widget->allocation.width; i++) {
				glElements[i] =2 * widget->allocation.width - i;
			}
			gluOrtho2D(0.0f, 100.0f, -50.0f, 50.0f);
		} else {
			gluOrtho2D(0.0f, 1000.0f, 0.0f, 100.0f);
		}
	  glMatrixMode(GL_MODELVIEW);
	  glLoadIdentity();

		if(killIt != 0)  {
			g_source_remove(killIt);
			killIt = 0;
			startDraw = 1;
		}
		if(g_strcasecmp(widget->name,"SonoArea") == 0) {
			tmpP = &verticesSono[0];
#ifdef HAVE_FAST_GA
			verticesSono = g_renew(GLubyte, tmpP, 3 * (widget->allocation.height));
#else
			verticesSono = g_renew(GLubyte, tmpP, 3 * (widget->allocation.width) * (widget->allocation.height));
#endif
			verticesSonoP = &verticesSono[0];
			logScaleInit(widget);
#ifndef HAVE_FAST_GA
			/* TEXTURE */
			textureDimensX = nextPowerOfTwo(widget->allocation.width);
			textureDimensY = nextPowerOfTwo(widget->allocation.height);
			glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
			glBindTexture(GL_TEXTURE_2D, textureName);
/*			glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); */
			glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, textureDimensX, textureDimensY, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
			yMax =  (gfloat) (widget->allocation.height / (gfloat) nextPowerOfTwo(widget->allocation.height));
			xMax = (gfloat) (widget->allocation.width /(gfloat)  nextPowerOfTwo(widget->allocation.width));
#endif
		 } /* SonoArea */
		if (g_strcasecmp(widget->name, "FFTArea") == 0) {
			logScaleInit(widget);
		}
		if (startDraw == 1)  {
			interval = (intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate) < minInterval) ? minInterval : lrintf(intervalFaktor * ((gfloat) framesProBuffer / (gfloat) samplingRate));
			while (1) {
				if (startIt == 1) {
				killIt = g_idle_add_full(G_PRIORITY_LOW, (GSourceFunc) glAreaDrawSync, NULL, NULL);
				break;
				}
			}
			startDraw = 0;
		} /* StartDraw */
		/*glDrawBuffer(GL_BACK); */
	/*	while((errorCode = glGetError()) != GL_NO_ERROR) {
			errorString = gluErrorString(errorCode);
			g_print("OpenGL: %s %s\n", __func__, errorString);
		}*/
	} /* gl_area_make_current */
  return TRUE;
}

/* Draw grid for Wave- and FFT-view */
gint glAreaDrawGrid(areaName name, GtkWidget *widget) {
	register guint sizePointer = 0;
	register GLfloat *vertLinesFFT, *staffLines = NULL;
	extern guint8 drawStaves;

	const GLfloat horizLinesWave[] =		{
		0.0f, 25.0f,
	100.0f, 25.0f,
	0.0f, 0.0f,
	100.0f, 0.0f,
	0.0f, -25.0f,
	100.0f, -25.0f
	};

	const GLfloat vertLinesWave[] =	{
	25.0f, 50.0f,
	25.0f, -50.0f,
	50.0f, 50.0f,
	50.0f, -50.0f,
	75.0f, 50.0f,
	75.0f, -50.0f
	};

	const GLfloat horizLinesFFT[]	=	{
	0.0f, 25.0f,
	1000.0f, 25.0f,
	0.0f, 50.0f,
	1000.0f, 50.0f,
	0.0f, 75.0f,
	1000.0f, 75.0f
	};


	/* waagerechte Linien */
	glLineWidth(1);
	switch(name) {
		case wave:
			glVertexPointer(2, GL_FLOAT, 0, horizLinesWave);
			sizePointer = sizeof(horizLinesWave) / sizeof(GLfloat);
			break;
		case fft:
			glVertexPointer(2, GL_FLOAT, 0, horizLinesFFT);
			sizePointer = sizeof(horizLinesFFT) / sizeof(GLfloat);
			break;
		case sono:
			/* Draw c' at fixed position */
			glColor3f(1.0f, 1.0f, 1.0f);
			if (displayMode == logarithm) {
				/* 1000.0f * log2(261.63) = 8031.38... */
				/* logFFTBySamplingRate and logFFTByTwo are module-global */
				glBegin(GL_LINES);
					glVertex2f(0.0f, (803.138416780f  + 100 * logFFTBySamplingRate) / logFFTByTwo);
					glVertex2f(1000.0f, (803.138416780f + 100* logFFTBySamplingRate) / logFFTByTwo);
				glEnd();
				staffLines = &staffLineVerticesSonoLog[0];
			} else {
				/* 2 * 1000.0f * 261.63 = 523260 */
				glBegin(GL_LINES);
					glVertex2f(0.0f, 523260 / samplingRate);
					glVertex2f(1000.0f, 523260 / samplingRate);
				glEnd();
				staffLines = &staffLineVerticesSono[0];
			}
	}

	glColor3f(1.0f, 0.0f, 1.0f);
	glDrawArrays(GL_LINES, 0, sizePointer/2);

	/* senkrechte Linien */
	switch(name) {
		case wave:
			glVertexPointer(2, GL_FLOAT, 0, vertLinesWave);
			sizePointer = sizeof(vertLinesWave) / sizeof(GLfloat);
			break;
		case fft:
			/* Draw c' at fixed position */
			glColor3f(1.0,0.9,0.9);
			if (displayMode == logarithm) {
				/* 1000.0f * log2(261.63) = 8031.38... */
				glBegin(GL_LINES);
					glVertex2f((8031.38416780f  + 1000 * logFFTBySamplingRate) / logFFTByTwo, 0.0f);
					glVertex2f((8031.38416780f + 1000 * logFFTBySamplingRate) / logFFTByTwo, 100.0f);
				glEnd();
				vertLinesFFT = &octaveLineVerticesLog[0];
				staffLines = &staffLineVerticesLog[0];
			} else {
				/* 2 * 1000.0f * 261.63 = 523260 */
				glBegin(GL_LINES);
					glVertex2f(523260 / samplingRate, 0.0f);
					glVertex2f(523260 / samplingRate, 100.0f);
				glEnd();
				vertLinesFFT = &octaveLineVertices[0];
				staffLines = &staffLineVertices[0];
			}
			glVertexPointer(2, GL_FLOAT, 0, vertLinesFFT);
			sizePointer = 36;
			break;
		case sono:
		;
	}

	glColor3f(1.0f, 1.0f, 0.0f);
	glDrawArrays(GL_LINES, 0, sizePointer/2);

	/* Staff Lines */
	if ((name == fft || name == sono) && drawStaves == 1) {

		glColor3f(1.0f, 0.0f, 0.0f);
		glVertexPointer(2, GL_FLOAT, 0, staffLines);
		sizePointer = 40;
		glDrawArrays(GL_LINES, 0, sizePointer/2);
	} /* Staff-Lines */

	return TRUE;
}
