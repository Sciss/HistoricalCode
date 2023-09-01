/*
 * $Id: callbacks.h,v 1.24 2003/03/08 16:31:04 niklaswerner Exp $
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


void
stringInit														(void);
void
on_open_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_new_activate                        (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_quit_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_full_screen_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_100_activate                        (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_75_activate                         (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_50_activate                         (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_25_activate                         (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_about_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_tutorial_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_ok_button_clicked                   (GtkButton       *button,
                                        gpointer         user_data);

void
on_close_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);
void
on_button1_clicked                   (GtkButton       *button,
                                        gpointer         user_data);
void
on_button2_clicked                   (GtkButton       *button,
                                        gpointer         user_data);
void
on_button6_clicked                   (GtkButton       *button,
                                        gpointer         user_data);
void
on_button3_clicked                   (GtkButton       *button,
                                        gpointer         user_data);
void
on_button5_clicked                   (GtkButton       *button,
                                        gpointer         user_data);
void
on_button7_clicked                   (GtkButton       *button,
                                        gpointer         user_data);
void
on_button8_clicked                   (GtkButton       *button,
                                        gpointer         user_data);
void
on_closeDeviceButton_clicked          (GtkButton       *button,
                                        gpointer         user_data);
void
on_openDeviceButton_clicked          (GtkButton       *button,
                                        gpointer         user_data);
void
on_resetFileButton_clicked          (GtkButton       *button,
                                        gpointer         user_data);

/* resize widget with Scalebar */
void
resizeWidget(GtkObject *adjustment,
								gpointer user_data);

void
adjustFFT(GtkObject *adjustment,
								gpointer user_data);

void
printStatus														(GtkObject *widget, GdkEventMotion *event, gpointer user_data);

void
setSamplingRate	(GtkEntry *entry, gpointer *user_data);

void
setBufferSize	(GtkObject *adjustment, gpointer *user_data);

void
setFFTSize	(GtkObject *adjustment, gpointer *user_data);

void
setLPCSize	(GtkObject *adjustment, gpointer *user_data);

void
setWindowType	(GtkEntry *entry, gpointer *user_data);

void
setDisplayType	(GtkEntry *entry, gpointer *user_data);

void
setReferenceA	(GtkObject *adjustment, gpointer *user_data);

void
setSndDevice	(GtkEntry *entry, gpointer *user_data);

void
setSndDeviceOut	(GtkEntry *entry, gpointer *user_data);

void
setSpecType	(GtkEntry *entry, gpointer *user_data);

void
setPreset	(GtkEntry *entry, gpointer *user_data);

void
setWaveFormSize (GtkObject *adjustment,  gpointer *user_data);

void
setOverlap (GtkObject *button,  gpointer *user_data);

void
setHighPass (GtkObject *button,  gpointer *user_data);

void
setReadFromFile (GtkObject *button,  gpointer *user_data);

void
setDrawStaves (GtkObject *button,  gpointer *user_data);
