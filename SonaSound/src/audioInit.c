/*
 * $Id: audioInit.c,v 1.22 2003/03/05 19:36:47 niklaswerner Exp $
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
#include "audioIo.h"

extern PortAudioStream *stream;
guint8 audioExitHasRun = 0;

void audioInit(guint8 useBoth) {
	PaError err;
	extern guint8 readFromFile;
	gint in = 0, out = 0, inCh = 2, outCh = 0;

	in = (readFromFile == 1 && useBoth == 0) ? paNoDevice : sndDevice;
	out = (readFromFile == 0 && useBoth == 0) ? paNoDevice : sndDeviceOut;
	inCh = (readFromFile == 1 && useBoth == 0) ? paNoDevice : 2;
	outCh = (readFromFile == 0 && useBoth == 0) ? paNoDevice : 2;

	err = Pa_OpenStream( &stream,
										in,
										inCh,
										paFloat32,
										NULL,
										out,
										outCh,
										paFloat32,
										NULL,
										samplingRate,
										framesProBuffer,
										0,
										0,
										paCallBack,
										NULL);

	audioExitHasRun = 0;
	if( err != paNoError ) g_print("Audio-Engine Error (%s): %s\n", __func__, Pa_GetErrorText(err));

}

void audioExit(void) {
	PaError err;
	if (Pa_StreamActive(stream) == 1) {
		err = Pa_StopStream(stream);
		if (err != paNoError ) g_print("Audio-Engine Error (%s): %s\n", __func__, Pa_GetErrorText(err));
	}
	if (audioExitHasRun == 0) {
		err = Pa_CloseStream( stream );
		if (err != paNoError ) g_print("Audio-Engine Error (%s): %s\n", __func__, Pa_GetErrorText(err));
	}
	audioExitHasRun = 1;
}

