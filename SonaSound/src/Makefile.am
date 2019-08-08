#
# $Id: Makefile.am,v 1.20 2003/02/25 12:21:45 niklaswerner Exp $
# Copyright (C) 2002-2003 Niklas Werner <niklas@niklaswerner.de>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
#
## Process this file with automake to produce Makefile.in
INCLUDES = \
	-I$(top_srcdir)/intl  \
	@GTK_CFLAGS@ \
	@FFTW_INC_PREFIX@ \
	@SNDFILE_INC_PREFIX@

bin_PROGRAMS = sonasound

sonasound_SOURCES = \
	sonasound.h \
	float_cast.h \
	helpers.c helpers.h \
	main.c \
	support.c support.h \
	interface.c interface.h \
	audioIo.c audioIo.h \
	callbacks.c callbacks.h  \
	glDraw.c glDraw.h \
	palette.h \
	sonogram.c sonogram.h \
	window.c window.h \
	fft.c fft.h \
	lpc.c lpc.h \
	audioInit.c audioInit.h \
	globalsInit.c globalsInit.h \
	waveForm.c waveForm.h

sonasound_LDADD = @GTK_LIBS@ @FFTW_LIBS@ @SNDFILE_LIBS@ @GTK_GL_LIBS@ @PORTAUDIO_LIBS@

