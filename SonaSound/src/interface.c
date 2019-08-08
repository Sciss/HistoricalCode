/*
 * $Id: interface.c,v 1.92 2003/04/05 01:04:17 niklaswerner Exp $
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


#include "sonasound.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>

#include <gdk/gdkkeysyms.h>

#include <gtkgl/gtkglarea.h>
#include <GL/gl.h>

#include "callbacks.h"
#include "interface.h"
#include "support.h"
#include "glDraw.h"
#include "globalsInit.h"

gint samplingRateIndex = 0; /* For SamplingRate-List */
GtkObject *fftAdjustment; /* For adjusting maxDB-scaling-factor */
GtkObject *fftSizeAdjustment;
GtkObject *bufferSizeAdjustment;
GtkObject *lpcSizeAdjustment;
GtkObject *scaleAdjustment;
GList *sndDeviceList = NULL;

GtkWidget*
create_MainWindow (void)
{
  GtkWidget *MainWindow;
  GtkWidget *hbox1;
  GtkWidget *vbox2;
  GtkWidget *alignment1;
  GtkWidget *menueLeiste;
  GtkWidget *file;
  GtkWidget *file_menu;
  GtkAccelGroup *file_menu_accels;
  GtkWidget *trennlinie2;
  GtkWidget *open;
  GtkWidget *new;
  GtkWidget *save;
  GtkWidget *close;
  GtkWidget *quit;
  GtkWidget *view;
  GtkWidget *view_menu;
  GtkAccelGroup *view_menu_accels;
  GtkWidget *trennlinie3;
  GtkWidget *full_screen;
  GtkWidget *_100;
  GtkWidget *_75;
  GtkWidget *_50;
  GtkWidget *_25;
  GtkWidget *help;
  GtkWidget *help_menu;
  GtkAccelGroup *help_menu_accels;
  GtkWidget *about;
  GtkWidget *tutorial;
  GtkWidget *toolbar1;
  GtkWidget *button6;
  GtkWidget *tmp_toolbar_icon;
  GtkWidget *start_icon;
  GtkWidget *reset_icon;
  GtkWidget *start_drawing_icon;
  GtkWidget *stop_drawing_icon;
  GtkWidget *start_and_draw_icon;
  GtkWidget *stop_and_dont_draw_icon;
  GtkWidget *stop_icon;
  GtkWidget *close_device_icon;
  GtkWidget *open_device_icon;
  GtkWidget *button1;
  GtkWidget *button2;
  GtkWidget *button3;
	GtkWidget *button7;
  GtkWidget *button5;
  GtkWidget *button8;
  GtkWidget *closeDeviceButton;
  GtkWidget *openDeviceButton;
	GtkWidget *resetFileButton;
  GtkWidget *sRLabel;
	GtkWidget *wTLabel; /* WindowTypeLabel */
	GtkWidget *dTLabel; /* displayTypeLabel */
	GtkWidget *sDLabel; /* sndDeviceLabel */
	GtkWidget *sDOLabel; /* sndDeviceOutLabel */
	GtkWidget *sTLabel; /* specTypeLabel */
  GtkWidget *sRHbox; /* SamplingRate, ... */
  GtkWidget *wTHbox; /* windowType, ... */
  GtkWidget *sDHbox; /* sndDevice, Summary, ... */
  GtkWidget *sTVbox; /* specType, sndDevice, ... */
	GtkWidget *oHHbox; /* Overlap and Higpass-toggle */
	GtkWidget *presetHbox;
  GtkWidget *vSep1;
  GtkWidget *vSep2;
  GtkWidget *vSep3;
  GtkWidget *vSep4;
  GtkWidget *hSep1;
  GtkWidget *hSep2;
	GtkWidget *bufferSizeEntry;
  GtkWidget *bSLabel;
	GtkWidget *fftSizeEntry;
  GtkWidget *fSLabel;
	GtkWidget *lpcSizeEntry;
  GtkWidget *lSLabel;
	GtkWidget *referenceAEntry;
	GtkWidget *rALabel; /* Reference A Label */
/* 	GtkWidget *waveSecondsEntry; */
/*   GtkWidget *wSLabel; */
  GtkWidget *presetLabel;
  GtkWidget *overlapToggle;
  GtkWidget *highPassToggle;
  GtkWidget *po2FFT;
  GtkWidget *po2Buffer;
  GtkWidget *readFromFileToggle;
  GtkWidget *drawStavesToggle;
  GtkAccelGroup *accel_group;
	GtkTooltips *menuTooltips;
	GtkCombo *samplingRateCombo;
	GtkCombo *sndDeviceCombo;
	GtkCombo *sndDeviceOutCombo;
	GtkCombo *windowTypeCombo;
	GtkCombo *displayTypeCombo;
	GtkCombo *specTypeCombo;
/* 	GtkCombo *logBaseCombo; */
	GtkCombo *presetCombo;
	GList *samplingRateList = NULL;
	GList *displayTypeList = NULL;
	GList *specTypeList = NULL;
/* 	GList *logBaseList = NULL; */
	GList *presetList = NULL;
	GtkWidget *summaryLabel;
	GtkObject *referenceAAdjustment;
	extern guint8 readFromFile;
	extern guint8 drawStaves;
	extern gfloat referenceA;
	extern guint8 doHighPass;

  accel_group = gtk_accel_group_new ();
	menuTooltips = gtk_tooltips_new();
  MainWindow = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  gtk_widget_set_name (MainWindow, "MainWindow");
  gtk_object_set_data (GTK_OBJECT (MainWindow), "MainWindow", MainWindow);
  gtk_container_set_border_width (GTK_CONTAINER (MainWindow), 1);
  GTK_WIDGET_SET_FLAGS (MainWindow, GTK_CAN_FOCUS);
/*  gtk_widget_add_accelerator (MainWindow, "delete_event", accel_group,
                              GDK_Q, GDK_CONTROL_MASK,
                              GTK_ACCEL_VISIBLE); */
  gtk_window_set_title (GTK_WINDOW (MainWindow), _("SonaSound"));
  gtk_window_set_policy (GTK_WINDOW (MainWindow), TRUE, TRUE, TRUE);

	po2FFT = gtk_check_button_new_with_label(_("(2)^x"));
	po2Buffer = gtk_check_button_new_with_label(_("(2)^x"));
	overlapToggle = gtk_check_button_new_with_label(_("Overlap"));
	highPassToggle = gtk_check_button_new_with_label(_("HighPass"));
	readFromFileToggle = gtk_check_button_new_with_label(_("Use Soundfile"));
	drawStavesToggle = gtk_check_button_new_with_label(_("Draw Staff-Lines"));
	gtk_widget_show(po2FFT);
	gtk_widget_show(po2Buffer);
	gtk_widget_show(overlapToggle);
	gtk_widget_show(highPassToggle);
	gtk_widget_show(readFromFileToggle);
	gtk_widget_show(drawStavesToggle);
	gtk_widget_ref(GTK_WIDGET(overlapToggle));
	gtk_widget_ref(GTK_WIDGET(highPassToggle));
	gtk_widget_ref(GTK_WIDGET(po2FFT));
	gtk_widget_ref(GTK_WIDGET(po2Buffer));
	gtk_widget_ref(GTK_WIDGET(readFromFileToggle));
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "readFromFileToggle", readFromFileToggle,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "overlapToggle", overlapToggle,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "highPassToggle", highPassToggle,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "po2FFT", po2FFT,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "po2Buffer", po2Buffer,
                            (GtkDestroyNotify) gtk_widget_unref);
	samplingRateCombo = GTK_COMBO(gtk_combo_new());
	sndDeviceCombo = GTK_COMBO(gtk_combo_new());
	sndDeviceOutCombo = GTK_COMBO(gtk_combo_new());
	windowTypeCombo = GTK_COMBO(gtk_combo_new());
	displayTypeCombo = GTK_COMBO(gtk_combo_new());
	specTypeCombo = GTK_COMBO(gtk_combo_new());
/* 	logBaseCombo = GTK_COMBO(gtk_combo_new()); */
	presetCombo = GTK_COMBO(gtk_combo_new());
	gtk_combo_set_value_in_list(samplingRateCombo, FALSE, FALSE);
	gtk_combo_set_value_in_list(sndDeviceCombo, TRUE, FALSE);
	gtk_combo_set_value_in_list(sndDeviceOutCombo, TRUE, FALSE);
	gtk_combo_set_value_in_list(windowTypeCombo, TRUE, FALSE);
	gtk_combo_set_value_in_list(displayTypeCombo, TRUE, FALSE);
	gtk_combo_set_value_in_list(specTypeCombo, TRUE, FALSE);
/* 	gtk_combo_set_value_in_list(logBaseCombo, TRUE, FALSE); */
	gtk_combo_set_value_in_list(presetCombo, TRUE, FALSE);
	samplingRateList = getSamplingRate(samplingRateList);
	sndDeviceList = getDeviceInfo(sndDeviceList, 0);
	displayTypeList = g_list_append(displayTypeList, _("logarithmic"));
	displayTypeList = g_list_append(displayTypeList, _("linear"));
/* 	logBaseList = g_list_append(logBaseList, "2"); */
/* 	logBaseList = g_list_append(logBaseList, "10"); */
	specTypeList = g_list_append(specTypeList, "FFT");
	specTypeList = g_list_append(specTypeList, "LPC");
	presetList	= g_list_append(presetList, _("Vocal / Choir"));
	presetList	= g_list_append(presetList, _("Noises"));
	presetList	= g_list_append(presetList, _("Speech"));
	presetList	= g_list_append(presetList, _("Orchestra"));
	presetList	= g_list_append(presetList, _("Rock/Pop"));
	presetList	= g_list_append(presetList, _("Scientific"));
	windowNameListInit();
	gtk_combo_set_popdown_strings(samplingRateCombo, samplingRateList);
	gtk_combo_set_popdown_strings(sndDeviceCombo, sndDeviceList);
	gtk_combo_set_popdown_strings(sndDeviceOutCombo, sndDeviceList);
	gtk_combo_set_popdown_strings(windowTypeCombo, windowNameList);
	gtk_combo_set_popdown_strings(displayTypeCombo, displayTypeList);
	gtk_combo_set_popdown_strings(specTypeCombo, specTypeList);
/* 	gtk_combo_set_popdown_strings(logBaseCombo, logBaseList); */
	gtk_combo_set_popdown_strings(presetCombo, presetList);
	gtk_widget_show(GTK_WIDGET(samplingRateCombo));
	gtk_widget_show(GTK_WIDGET(sndDeviceCombo));
	gtk_widget_show(GTK_WIDGET(sndDeviceOutCombo));
	gtk_widget_show(GTK_WIDGET(windowTypeCombo));
	gtk_widget_show(GTK_WIDGET(displayTypeCombo));
	gtk_widget_show(GTK_WIDGET(specTypeCombo));
/* 	gtk_widget_show(GTK_WIDGET(logBaseCombo)); */
	gtk_widget_show(GTK_WIDGET(presetCombo));
	gtk_widget_set_usize(GTK_WIDGET(samplingRateCombo), 75, 15);
	gtk_widget_set_usize(GTK_WIDGET(sndDeviceCombo), 75, 15);
	gtk_widget_set_usize(GTK_WIDGET(sndDeviceOutCombo), 75, 15);
	gtk_widget_set_usize(GTK_WIDGET(windowTypeCombo), 75, 15);
	gtk_widget_set_usize(GTK_WIDGET(displayTypeCombo), 75, 15);
	gtk_widget_set_usize(GTK_WIDGET(specTypeCombo), 75, 15);
/* 	gtk_widget_set_usize(GTK_WIDGET(logBaseCombo), 50, 15); */
	gtk_widget_set_usize(GTK_WIDGET(presetCombo), 75, 15);
	gtk_widget_ref(GTK_WIDGET(samplingRateCombo));
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "samplingRateCombo", samplingRateCombo,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "sndDeviceCombo", sndDeviceCombo,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "sndDeviceOutCombo", sndDeviceOutCombo,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_widget_ref(GTK_WIDGET(windowTypeCombo));
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "windowTypeCombo", windowTypeCombo,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_widget_ref(GTK_WIDGET(displayTypeCombo));
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "displayTypeCombo", displayTypeCombo,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "specTypeCombo", specTypeCombo,
                            (GtkDestroyNotify) gtk_widget_unref);
/* 	gtk_widget_ref(GTK_WIDGET(logBaseCombo)); */
/*   gtk_object_set_data_full (GTK_OBJECT (MainWindow), "logBaseCombo", logBaseCombo, */
/*                             (GtkDestroyNotify) gtk_widget_unref); */
	gtk_widget_ref(GTK_WIDGET(presetCombo));
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "presetCombo", presetCombo,
                            (GtkDestroyNotify) gtk_widget_unref);

	fftSizeAdjustment = gtk_adjustment_new(pointsFFT,
													16,
													65535 ,  1,
													128, 1);
	bufferSizeAdjustment = gtk_adjustment_new(framesProBuffer ,
													128,
													65535 ,  1,
													128, 1);
	lpcSizeAdjustment = gtk_adjustment_new(pointsLPC ,
													4,
													2048 ,  1,
													64, 1);
	referenceAAdjustment = gtk_adjustment_new(referenceA,
													27.5,
													14080.0,  0.01,
													10, 1);
/* 	waveSecondsAdjustment = gtk_adjustment_new(numSeconds , */
/* 													0.011, */
/* 													10.0 ,  0.01, */
/* 													0.5, 0.1); */

	fftSizeEntry = gtk_spin_button_new(GTK_ADJUSTMENT(fftSizeAdjustment), 1.0, 0);
	gtk_spin_button_set_numeric(GTK_SPIN_BUTTON(fftSizeEntry), TRUE);
	gtk_spin_button_set_update_policy(GTK_SPIN_BUTTON(fftSizeEntry), GTK_UPDATE_IF_VALID);
  gtk_widget_ref (fftSizeEntry);
	gtk_widget_set_usize(fftSizeEntry, 75, 15);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "fftSizeEntry", fftSizeEntry,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_widget_show(fftSizeEntry);
	lpcSizeEntry = gtk_spin_button_new(GTK_ADJUSTMENT(lpcSizeAdjustment), 1.0, 0);
	gtk_spin_button_set_numeric(GTK_SPIN_BUTTON(lpcSizeEntry), TRUE);
	gtk_spin_button_set_update_policy(GTK_SPIN_BUTTON(lpcSizeEntry), GTK_UPDATE_IF_VALID);
  gtk_widget_ref (lpcSizeEntry);
	gtk_widget_set_usize(lpcSizeEntry, 75, 15);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "lpcSizeEntry", lpcSizeEntry,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_widget_show(lpcSizeEntry);
	referenceAEntry = gtk_spin_button_new(GTK_ADJUSTMENT(referenceAAdjustment), 1.0, 3);
	gtk_spin_button_set_numeric(GTK_SPIN_BUTTON(referenceAEntry), TRUE);
	gtk_spin_button_set_update_policy(GTK_SPIN_BUTTON(referenceAEntry), GTK_UPDATE_IF_VALID);
  gtk_widget_ref (referenceAEntry);
	gtk_widget_set_usize(referenceAEntry, 75, 15);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "referenceAEntry", referenceAEntry,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_widget_show(referenceAEntry);
	bufferSizeEntry = gtk_spin_button_new(GTK_ADJUSTMENT(bufferSizeAdjustment), 1.0, 0);
  gtk_widget_ref (bufferSizeEntry);
	gtk_widget_set_usize(bufferSizeEntry, 75, 15);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "bufferSizeEntry", bufferSizeEntry,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_spin_button_set_numeric(GTK_SPIN_BUTTON(bufferSizeEntry), TRUE);
	gtk_spin_button_set_update_policy(GTK_SPIN_BUTTON(bufferSizeEntry), GTK_UPDATE_IF_VALID);
	gtk_widget_show(bufferSizeEntry);
/* 	waveSecondsEntry = gtk_spin_button_new(GTK_ADJUSTMENT(waveSecondsAdjustment), 1.0, 5); */
/* 	gtk_spin_button_set_numeric(GTK_SPIN_BUTTON(waveSecondsEntry), TRUE); */
/* 	gtk_spin_button_set_update_policy(GTK_SPIN_BUTTON(waveSecondsEntry), GTK_UPDATE_IF_VALID); */
/*   gtk_widget_ref (waveSecondsEntry); */
/* 	gtk_widget_set_usize(waveSecondsEntry, 75, 15); */
/*   gtk_object_set_data_full (GTK_OBJECT (MainWindow), "waveSecondsEntry", waveSecondsEntry, */
/*                             (GtkDestroyNotify) gtk_widget_unref); */
/* 	gtk_widget_show(waveSecondsEntry); */

	vSep1 = gtk_vseparator_new();
	vSep2 = gtk_vseparator_new();
	vSep3 = gtk_vseparator_new();
	vSep4 = gtk_vseparator_new();
	hSep1 = gtk_hseparator_new();
	hSep2 = gtk_hseparator_new();
	gtk_widget_show(vSep1);
	gtk_widget_show(vSep2);
	gtk_widget_show(vSep3);
	gtk_widget_show(vSep4);
	gtk_widget_show(hSep1);
	gtk_widget_show(hSep2);
	sRLabel = gtk_label_new(_("SamplingRate"));
	fSLabel = gtk_label_new(_("FFT Size"));
	lSLabel = gtk_label_new(_("LPC Size"));
	rALabel = gtk_label_new(_("Reference A"));
	bSLabel = gtk_label_new(_("Buffer Size"));
	wTLabel = gtk_label_new(_("Window Type"));
	dTLabel = gtk_label_new(_("Display Type"));
	sTLabel = gtk_label_new(_("Spectrum Type"));
/* 	lBLabel = gtk_label_new(_("Logarithm Base")); */
	sDLabel = gtk_label_new(_("AudioIn Device"));
	sDOLabel = gtk_label_new(_("AudioOut Device"));
	presetLabel = gtk_label_new(_("Analysis Presets"));
	printSummaryString();
	summaryLabel = gtk_label_new(summaryString->str);
	gtk_widget_ref(summaryLabel);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "summaryLabel", summaryLabel,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_label_set_justify(GTK_LABEL(summaryLabel), GTK_JUSTIFY_LEFT);
	gtk_label_set_line_wrap(GTK_LABEL(summaryLabel), TRUE);
	gtk_widget_show(sRLabel);
	gtk_widget_show(bSLabel);
	gtk_widget_show(fSLabel);
	gtk_widget_show(lSLabel);
	gtk_widget_show(rALabel);
	gtk_widget_show(wTLabel);
	gtk_widget_show(sTLabel);
	gtk_widget_show(dTLabel);
/* 	gtk_widget_show(lBLabel); */
	gtk_widget_show(sDLabel);
	gtk_widget_show(sDOLabel);
	gtk_widget_show(summaryLabel);
	gtk_widget_show(presetLabel);
	sRHbox = gtk_vbox_new(FALSE, 1);
	sDHbox = gtk_hbox_new(FALSE, 1);
	wTHbox = gtk_hbox_new(FALSE, 1);
	oHHbox = gtk_hbox_new(FALSE, 1);
	hbox1 = gtk_hbox_new(FALSE, 1);
	presetHbox = gtk_hbox_new(FALSE, 1);
  gtk_widget_ref (sRHbox);
  gtk_widget_ref (sDHbox);
  gtk_widget_ref (wTHbox);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "sRHbox", sRHbox,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "sDHbox", sDHbox,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "wTHbox", wTHbox,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (sRHbox);
  gtk_widget_show (sDHbox);
  gtk_widget_show (wTHbox);
	gtk_widget_show (oHHbox);
  gtk_widget_show (hbox1);
  gtk_widget_show (presetHbox);
  vbox2 = gtk_vbox_new (FALSE, 0);
  sTVbox = gtk_hbox_new (FALSE, 0);
  gtk_widget_set_name (vbox2, "vbox2");
  gtk_widget_ref (vbox2);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "vbox2", vbox2,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (vbox2);
  gtk_widget_show (sTVbox);
  gtk_container_add (GTK_CONTAINER (MainWindow), vbox2);

  alignment1 = gtk_alignment_new (0.5, 0.5, 1, 1);
  gtk_widget_set_name (alignment1, "alignment1");
  gtk_widget_ref (alignment1);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "alignment1", alignment1,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (alignment1);
  gtk_box_pack_start (GTK_BOX (vbox2), alignment1, FALSE, FALSE, 0);

  menueLeiste = gtk_menu_bar_new ();
  gtk_widget_set_name (menueLeiste, "menueLeiste");
  gtk_widget_ref (menueLeiste);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "menueLeiste", menueLeiste,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (menueLeiste);
  gtk_container_add (GTK_CONTAINER (alignment1), menueLeiste);
  gtk_container_set_border_width (GTK_CONTAINER (menueLeiste), 1);

  file = gtk_menu_item_new_with_label (_("File"));
  gtk_widget_set_name (file, "file");
  gtk_widget_ref (file);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "file", file,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (file);
  gtk_container_add (GTK_CONTAINER (menueLeiste), file);

  file_menu = gtk_menu_new ();
  gtk_widget_set_name (file_menu, "file_menu");
  gtk_widget_ref (file_menu);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "file_menu", file_menu,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_menu_item_set_submenu (GTK_MENU_ITEM (file), file_menu);
  file_menu_accels = gtk_menu_ensure_uline_accel_group (GTK_MENU (file_menu));

  trennlinie2 = gtk_menu_item_new ();
  gtk_widget_set_name (trennlinie2, "trennlinie2");
  gtk_widget_ref (trennlinie2);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "trennlinie2", trennlinie2,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (trennlinie2);
  gtk_container_add (GTK_CONTAINER (file_menu), trennlinie2);
  gtk_widget_set_sensitive (trennlinie2, FALSE);

  open = gtk_menu_item_new_with_label (_("Open"));
  gtk_widget_set_name (open, "open");
  gtk_widget_ref (open);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "open", open,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (open);
  gtk_container_add (GTK_CONTAINER (file_menu), open);
  gtk_widget_add_accelerator (open, "activate", accel_group,
                              GDK_O, GDK_CONTROL_MASK,
                              GTK_ACCEL_VISIBLE);

  new = gtk_menu_item_new_with_label (_("New"));
  gtk_widget_set_name (new, "new");
  gtk_widget_ref (new);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "new", new,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (new);
  gtk_container_add (GTK_CONTAINER (file_menu), new);
  gtk_widget_add_accelerator (new, "activate", accel_group,
                              GDK_N, GDK_CONTROL_MASK,
                              GTK_ACCEL_VISIBLE);

  save = gtk_menu_item_new_with_label (_("Save"));
  gtk_widget_set_name (save, "save");
  gtk_widget_ref (save);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "save", save,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (save);
  gtk_container_add (GTK_CONTAINER (file_menu), save);
  gtk_widget_add_accelerator (save, "activate", accel_group,
                              GDK_S, GDK_CONTROL_MASK,
                              GTK_ACCEL_VISIBLE);

  close = gtk_menu_item_new_with_label (_("Close"));
  gtk_widget_set_name (close, "close");
  gtk_widget_ref (close);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "close", close,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (close);
  gtk_container_add (GTK_CONTAINER (file_menu), close);
  gtk_widget_add_accelerator (close, "activate", accel_group,
                              GDK_W, GDK_CONTROL_MASK,
                              GTK_ACCEL_VISIBLE);

  quit = gtk_menu_item_new_with_label (_("Quit"));
  gtk_widget_set_name (quit, "quit");
  gtk_widget_ref (quit);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "quit", quit,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (quit);
  gtk_container_add (GTK_CONTAINER (file_menu), quit);
  gtk_widget_add_accelerator (quit, "activate", accel_group,
                              GDK_Q, GDK_CONTROL_MASK,
                              GTK_ACCEL_VISIBLE);

  view = gtk_menu_item_new_with_label (_("View"));
  gtk_widget_set_name (view, "view");
  gtk_widget_ref (view);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "view", view,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (view);
  gtk_container_add (GTK_CONTAINER (menueLeiste), view);

  view_menu = gtk_menu_new ();
  gtk_widget_set_name (view_menu, "view_menu");
  gtk_widget_ref (view_menu);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "view_menu", view_menu,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_menu_item_set_submenu (GTK_MENU_ITEM (view), view_menu);
  view_menu_accels = gtk_menu_ensure_uline_accel_group (GTK_MENU (view_menu));

  trennlinie3 = gtk_menu_item_new ();
  gtk_widget_set_name (trennlinie3, "trennlinie3");
  gtk_widget_ref (trennlinie3);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "trennlinie3", trennlinie3,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (trennlinie3);
  gtk_container_add (GTK_CONTAINER (view_menu), trennlinie3);
  gtk_widget_set_sensitive (trennlinie3, FALSE);

  full_screen = gtk_menu_item_new_with_label (_("Full Screen"));
  gtk_widget_set_name (full_screen, "full_screen");
  gtk_widget_ref (full_screen);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "full_screen", full_screen,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (full_screen);
  gtk_container_add (GTK_CONTAINER (view_menu), full_screen);

  _100 = gtk_menu_item_new_with_label (_("100%"));
  gtk_widget_set_name (_100, "_100");
  gtk_widget_ref (_100);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "_100", _100,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (_100);
  gtk_container_add (GTK_CONTAINER (view_menu), _100);

  _75 = gtk_menu_item_new_with_label (_("75%"));
  gtk_widget_set_name (_75, "_75");
  gtk_widget_ref (_75);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "_75", _75,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (_75);
  gtk_container_add (GTK_CONTAINER (view_menu), _75);

  _50 = gtk_menu_item_new_with_label (_("50%"));
  gtk_widget_set_name (_50, "_50");
  gtk_widget_ref (_50);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "_50", _50,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (_50);
  gtk_container_add (GTK_CONTAINER (view_menu), _50);

  _25 = gtk_menu_item_new_with_label (_("25%"));
  gtk_widget_set_name (_25, "_25");
  gtk_widget_ref (_25);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "_25", _25,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (_25);
  gtk_container_add (GTK_CONTAINER (view_menu), _25);

  help = gtk_menu_item_new_with_label (_("Help"));
  gtk_widget_set_name (help, "help");
  gtk_widget_ref (help);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "help", help,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (help);
  gtk_container_add (GTK_CONTAINER (menueLeiste), help);
  gtk_menu_item_right_justify (GTK_MENU_ITEM (help));

  help_menu = gtk_menu_new ();
  gtk_widget_set_name (help_menu, "help_menu");
  gtk_widget_ref (help_menu);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "help_menu", help_menu,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_menu_item_set_submenu (GTK_MENU_ITEM (help), help_menu);
  help_menu_accels = gtk_menu_ensure_uline_accel_group (GTK_MENU (help_menu));

  about = gtk_menu_item_new_with_label (_("About"));
  gtk_widget_set_name (about, "about");
  gtk_widget_ref (about);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "about", about,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (about);
  gtk_container_add (GTK_CONTAINER (help_menu), about);

  tutorial = gtk_menu_item_new_with_label (_("Tutorial"));
  gtk_widget_set_name (tutorial, "tutorial");
  gtk_widget_ref (tutorial);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "tutorial", tutorial,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_add_accelerator (tutorial, "activate", accel_group,
                              GDK_F1, 0,
                              GTK_ACCEL_VISIBLE);
  gtk_widget_show (tutorial);
  gtk_container_add (GTK_CONTAINER (help_menu), tutorial);

  toolbar1 = gtk_toolbar_new (GTK_ORIENTATION_VERTICAL, GTK_TOOLBAR_BOTH);
  gtk_widget_set_name (toolbar1, "toolbar1");
  gtk_widget_ref (toolbar1);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "toolbar1", toolbar1,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (toolbar1);
  gtk_toolbar_set_space_style (GTK_TOOLBAR (toolbar1), GTK_TOOLBAR_SPACE_LINE);


  tmp_toolbar_icon = create_pixmap (MainWindow, "draw.xpm");
  button1 = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Draw"),
                                NULL, NULL,
                                tmp_toolbar_icon, NULL, NULL);
  gtk_widget_set_name (button1, "button1");
  gtk_widget_ref (button1);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "button1", button1,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button1);

  start_icon = create_pixmap (MainWindow, "start.xpm");
  button6 = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Start"),
                                NULL, NULL,
                                start_icon, NULL, NULL);
  gtk_widget_set_name (button6, "button6");
  gtk_widget_ref (button6);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "button6", button6,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button6);

  stop_icon = create_pixmap (MainWindow, "stop.xpm");
  button2 = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Stop"),
                                NULL, NULL,
                                stop_icon, NULL, NULL);
  gtk_widget_set_name (button2, "button2");
  gtk_widget_ref (button2);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "button2", button2,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button2);

	reset_icon = create_pixmap (MainWindow, "reset.xpm");
  resetFileButton = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Reset File"),
                                NULL, NULL,
                                reset_icon, NULL, NULL);
  gtk_widget_set_name (resetFileButton, "resetFileButton");
  gtk_widget_ref (resetFileButton);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "resetFileButton", resetFileButton,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (resetFileButton);

  gtk_toolbar_append_space (GTK_TOOLBAR (toolbar1));

  start_drawing_icon = create_pixmap (MainWindow, "draw.xpm");
  button3 = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Start Drawing"),
                                NULL, NULL,
                                start_drawing_icon, NULL, NULL);
  gtk_widget_set_name (button3, "button3");
  gtk_widget_ref (button3);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "button3", button3,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button3);
  gtk_container_set_border_width (GTK_CONTAINER (button3), 1);

  stop_drawing_icon = create_pixmap (MainWindow, "dont_draw.xpm");
  button7 = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Stop Drawing"),
                                NULL, NULL,
                                stop_drawing_icon, NULL, NULL);
  gtk_widget_set_name (button7, "button7");
  gtk_widget_ref (button7);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "button7", button7,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button7);

  gtk_toolbar_append_space (GTK_TOOLBAR (toolbar1));

  start_and_draw_icon = create_pixmap (MainWindow, "start_and_draw.xpm");
  button5 = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Start All"),
                                NULL, NULL,
                                start_and_draw_icon, NULL, NULL);
  gtk_widget_add_accelerator (button5, "clicked", accel_group,
                              GDK_KP_Enter, 0,
                              GTK_ACCEL_VISIBLE);
  gtk_widget_set_name (button5, "button5");
  gtk_widget_ref (button5);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "button5", button5,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button5);

	stop_and_dont_draw_icon = create_pixmap (MainWindow, "stop_and_dont_draw.xpm");
  button8 = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Stop All"),
                                NULL, NULL,
                                stop_and_dont_draw_icon, NULL, NULL);
  gtk_widget_add_accelerator (button8, "clicked", accel_group,
                              GDK_KP_0, 0,
                              GTK_ACCEL_VISIBLE);
  gtk_widget_set_name (button8, "button8");
  gtk_widget_ref (button8);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "button8", button8,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button8);

  gtk_toolbar_append_space (GTK_TOOLBAR (toolbar1));

	close_device_icon = create_pixmap (MainWindow, "close_device.xpm");
  closeDeviceButton = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Close Devices"),
                                NULL, NULL,
                                close_device_icon, NULL, NULL);
  gtk_widget_set_name (closeDeviceButton, "closeDeviceButton");
  gtk_widget_ref (closeDeviceButton);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "closeDeviceButton", closeDeviceButton,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (closeDeviceButton);
	open_device_icon = create_pixmap (MainWindow, "open_device.xpm");
  openDeviceButton = gtk_toolbar_append_element (GTK_TOOLBAR (toolbar1),
                                GTK_TOOLBAR_CHILD_BUTTON,
                                NULL,
                                _("Open Devices"),
                                NULL, NULL,
                                open_device_icon, NULL, NULL);
  gtk_widget_set_name (closeDeviceButton, "closeDeviceButton");
  gtk_widget_ref (openDeviceButton);
  gtk_object_set_data_full (GTK_OBJECT (MainWindow), "openDeviceButton", openDeviceButton,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (openDeviceButton);
/* ############################ ToolTips ############################ */
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), button1, _("Draw everything once"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), button6, _("Start Audio-Engine"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), button2, _("Stop Audio-Engine"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), resetFileButton, _("Start Audio-file from beginning"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), button3, _("Start Drawing the data"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), button5, _("Start Drawing and Audio-Engine"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), button8, _("Stop Drawing and Audio-Engine"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), button7, _("Stop Drawing the data"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), closeDeviceButton, _("Close the Sounddevices to allow for second instance of SonaSound or other programs to open the SoundDevices OR to switch from Soundfile to Soundcard input"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), openDeviceButton, _("Open the Sounddevices"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), overlapToggle, _("Switch Overlap (50%) of the analysis windows on/off: Increases resolution of high frequencies"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), highPassToggle, _("Switch HighPass-Filter (1st Order) on to clean the LPC-Spectrum a bit"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), readFromFileToggle, _("Switch between Soundfile (open it via File->Open first) and Sound-In: When checked, \"Close Device\" and \"Open Device\" will only use the Output-Channels"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), GTK_WIDGET(GTK_ENTRY(samplingRateCombo->entry)), _("Set the SamplingRate: If a free Range is supported instead of discrete values you will have to type in the desired rate yourself. Only the upper and lower frequency is printed."), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), GTK_WIDGET(GTK_ENTRY(windowTypeCombo->entry)), _("Set the Analysis-Window Type (This affects how the ends of Analysis-blocks are attenuated)"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), GTK_WIDGET(GTK_ENTRY(sndDeviceCombo->entry)), _("Choose the Audio Device for input (Doesn't have an effect, when \"Use Soundfile\" is active)"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), GTK_WIDGET(GTK_ENTRY(sndDeviceOutCombo->entry)), _("Choose the Audio Device for Output (Doesn't have an effect, when \"Use Soundfile\" is inactive)"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), GTK_WIDGET(GTK_ENTRY(displayTypeCombo->entry)), _("Set the Display Type"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), GTK_WIDGET(GTK_ENTRY(specTypeCombo->entry)), _("Set the Method for spectrum generation"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), GTK_WIDGET(GTK_ENTRY(presetCombo->entry)), _("Choose a preset: this will change various settings to gain the best display for the given signal. Press <Enter> when you have chosen a Preset to activate it."), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), fftSizeEntry, _("Set the FFT-Size and the number of points to calculate the frequency response from the LPC-Filter for. (This may take some time to recalculate, so better type in the desired value, instead of scrolling there) NOTE: You cannot set the number of LPC-points to more than the number of FFT-points!"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), lpcSizeEntry, _("Set the Number of LP-Coefficients to calculate. NOTE: You cannot set the number of LPC-points to more than the number of FFT-points!"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), referenceAEntry, _("Set frequency of the Reference-A in Hz for the Staff-Lines"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), bufferSizeEntry, _("Set the Buffer-Size: Remember that this also affects the Redraw rate, so your computer may choke, if set too low..."), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), po2Buffer, _("Round the Buffer-Size to power of 2"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), po2FFT, _("Round the FFT-Size to power of 2 (highly recommended)"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(menuTooltips), drawStavesToggle, _("Switch the Stafflines on or off"), NULL);


/* ########################### Packing ############################## */
  gtk_box_pack_start (GTK_BOX (hbox1), toolbar1, TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(sRLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(samplingRateCombo), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(vSep1), TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(fSLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sDHbox), GTK_WIDGET(fftSizeEntry), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sDHbox), GTK_WIDGET(po2FFT), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), sDHbox, TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(lSLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(lpcSizeEntry), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(vSep2), TRUE, TRUE, 1);
 /* gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(sRSep), TRUE, TRUE, 1); */
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(bSLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sTVbox), GTK_WIDGET(bufferSizeEntry), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sTVbox), GTK_WIDGET(po2Buffer), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), sTVbox, TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(wTLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(windowTypeCombo), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(vSep3), TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(dTLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(displayTypeCombo), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(vSep4), TRUE, TRUE, 1);
/*   gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(lBLabel), FALSE, FALSE, 1); */
/*   gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(logBaseCombo), FALSE, FALSE, 1); */
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(sTLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(specTypeCombo), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(rALabel), FALSE, FALSE, 1);
	gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(referenceAEntry), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(sDLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(sndDeviceCombo), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(sDOLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(sndDeviceOutCombo), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(presetLabel), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(presetCombo), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (oHHbox), GTK_WIDGET(overlapToggle), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (oHHbox), GTK_WIDGET(highPassToggle), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(oHHbox), FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), GTK_WIDGET(drawStavesToggle), FALSE, FALSE, 1);


	/* Put HBoxes into VBox */
  gtk_box_pack_start (GTK_BOX (hbox1), sRHbox, TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (sRHbox), wTHbox, TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (vbox2), hbox1, TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (vbox2), GTK_WIDGET(hSep1), TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (vbox2), readFromFileToggle, FALSE, FALSE, 1);
  gtk_box_pack_start (GTK_BOX (vbox2), GTK_WIDGET(hSep2), TRUE, TRUE, 1);
  gtk_box_pack_start (GTK_BOX (vbox2), summaryLabel, TRUE, TRUE, 1);

/* ########################## Signals ############################### */
  gtk_signal_connect (GTK_OBJECT (MainWindow), "delete_event",
                      GTK_SIGNAL_FUNC (gtk_main_quit),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (open), "activate",
                      GTK_SIGNAL_FUNC (on_open_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (new), "activate",
                      GTK_SIGNAL_FUNC (on_new_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (save), "activate",
                      GTK_SIGNAL_FUNC (on_save_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (close), "activate",
                      GTK_SIGNAL_FUNC (on_close_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (quit), "activate",
                      GTK_SIGNAL_FUNC (gtk_main_quit),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (full_screen), "activate",
                      GTK_SIGNAL_FUNC (on_full_screen_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (_100), "activate",
                      GTK_SIGNAL_FUNC (on_100_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (_75), "activate",
                      GTK_SIGNAL_FUNC (on_75_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (_50), "activate",
                      GTK_SIGNAL_FUNC (on_50_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (_25), "activate",
                      GTK_SIGNAL_FUNC (on_25_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (about), "activate",
                      GTK_SIGNAL_FUNC (on_about_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (tutorial), "activate",
                      GTK_SIGNAL_FUNC (on_tutorial_activate),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button1), "clicked",
                      GTK_SIGNAL_FUNC (on_button1_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button2), "clicked",
                      GTK_SIGNAL_FUNC (on_button2_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button3), "clicked",
                      GTK_SIGNAL_FUNC (on_button3_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button5), "clicked",
                      GTK_SIGNAL_FUNC (on_button5_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button8), "clicked",
                      GTK_SIGNAL_FUNC (on_button8_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button7), "clicked",
                      GTK_SIGNAL_FUNC (on_button7_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button6), "clicked",
                      GTK_SIGNAL_FUNC (on_button6_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (closeDeviceButton), "clicked",
                      GTK_SIGNAL_FUNC (on_closeDeviceButton_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (openDeviceButton), "clicked",
                      GTK_SIGNAL_FUNC (on_openDeviceButton_clicked),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (resetFileButton), "clicked",
                      GTK_SIGNAL_FUNC (on_resetFileButton_clicked),
                      NULL);
	gtk_list_select_item(GTK_LIST(samplingRateCombo->list), samplingRateIndex);
  gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(samplingRateCombo->entry)), "changed",
														     GTK_SIGNAL_FUNC(setSamplingRate), MainWindow);
  gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(sndDeviceCombo->entry)), "changed",
														     GTK_SIGNAL_FUNC(setSndDevice), MainWindow);
  gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(sndDeviceOutCombo->entry)), "changed",
														     GTK_SIGNAL_FUNC(setSndDeviceOut), MainWindow);
  gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(windowTypeCombo->entry)), "changed",
														     GTK_SIGNAL_FUNC(setWindowType), MainWindow);
  gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(displayTypeCombo->entry)), "changed",
														     GTK_SIGNAL_FUNC(setDisplayType), MainWindow);
  gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(specTypeCombo->entry)), "changed",
														     GTK_SIGNAL_FUNC(setSpecType), MainWindow);
/*   gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(logBaseCombo->entry)), "changed", */
/* 														     GTK_SIGNAL_FUNC(setLogBase), MainWindow); */
  gtk_signal_connect(GTK_OBJECT(GTK_ENTRY(presetCombo->entry)), "changed",
														     GTK_SIGNAL_FUNC(setPreset), MainWindow);

	gtk_window_add_accel_group (GTK_WINDOW (MainWindow), accel_group);

	gtk_signal_connect(GTK_OBJECT(fftSizeAdjustment), "value_changed",
				GTK_SIGNAL_FUNC(setFFTSize), po2FFT);
	gtk_signal_connect(GTK_OBJECT(lpcSizeAdjustment), "value_changed",
				GTK_SIGNAL_FUNC(setLPCSize), MainWindow);
	gtk_signal_connect(GTK_OBJECT(referenceAAdjustment), "value_changed",
				GTK_SIGNAL_FUNC(setReferenceA), MainWindow);
	gtk_signal_connect(GTK_OBJECT(fftSizeAdjustment), "changed",
				GTK_SIGNAL_FUNC(setFFTSize), po2FFT);
	gtk_signal_connect(GTK_OBJECT(bufferSizeAdjustment), "value_changed",
				GTK_SIGNAL_FUNC(setBufferSize), po2Buffer);
/*	gtk_signal_connect(GTK_OBJECT(bufferSizeAdjustment), "changed",
				GTK_SIGNAL_FUNC(setBufferSize), po2Buffer); */
/*  gtk_signal_connect (GTK_OBJECT (po2FFT), "toggled",
                      GTK_SIGNAL_FUNC (setFFTSize),
                     po2FFT); */
  gtk_signal_connect (GTK_OBJECT (overlapToggle), "toggled",
                      GTK_SIGNAL_FUNC (setOverlap),
                      MainWindow);
  gtk_signal_connect (GTK_OBJECT (highPassToggle), "toggled",
                      GTK_SIGNAL_FUNC (setHighPass),
                      MainWindow);
	gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(drawStavesToggle), (drawStaves == 1) ? TRUE : FALSE);
  gtk_signal_connect (GTK_OBJECT (drawStavesToggle), "toggled",
                      GTK_SIGNAL_FUNC (setDrawStaves),
                      MainWindow);
  gtk_signal_connect (GTK_OBJECT (readFromFileToggle), "toggled",
                      GTK_SIGNAL_FUNC (setReadFromFile),
                      MainWindow);
/* #########################	Set Adjustments to defaults ################ */

	if (readFromFile == 1) {
		gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(readFromFileToggle), TRUE);
		on_ok_button_clicked(NULL, MainWindow);
	}
/* 	gtk_list_select_item(GTK_LIST(logBaseCombo->list), (myBase==10) ? 1 : 0); */
	gtk_list_select_item(GTK_LIST(windowTypeCombo->list), windowType);
	gtk_list_select_item(GTK_LIST(specTypeCombo->list), specType);
	gtk_list_select_item(GTK_LIST(displayTypeCombo->list), displayMode);
/*	gtk_widget_set_sensitive(open, FALSE); */
	gtk_widget_set_sensitive(new, FALSE);
	gtk_widget_set_sensitive(save, FALSE);
	gtk_widget_set_sensitive(close, FALSE);
	gtk_widget_set_sensitive(view, FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(button2), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(button7), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(openDeviceButton), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(sndDeviceCombo), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(samplingRateCombo), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(presetCombo), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(sndDeviceOutCombo), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(bufferSizeEntry), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(po2Buffer), FALSE);
	gtk_widget_set_sensitive(GTK_WIDGET(button8), FALSE);
	if (readFromFile == 0) {
		gtk_widget_set_sensitive(GTK_WIDGET(readFromFileToggle), FALSE);
		gtk_widget_set_sensitive(GTK_WIDGET(resetFileButton), FALSE);
	}
	gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(po2FFT), TRUE);
	gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(highPassToggle), (doHighPass == 1) ? TRUE : FALSE);
  return MainWindow;
}

GtkWidget*
create_DisplayWindow (void)
{
  GtkWidget *DisplayWindow;
  GtkWidget *vbox1;
  GtkWidget *vbox2;
  GtkWidget *hbox1;
  GtkWidget *hboxFFT;
  GtkWidget *WaveArea;
	GtkWidget *waveEvent;
  GtkWidget *FFTArea;
	GtkWidget *FFTEvent;
  GtkWidget *SonoArea;
  GtkWidget *SonoEvent;
  GtkWidget *PaletteArea;
  GtkWidget *PaletteEvent;
  GtkWidget *eventbox1;
  GtkWidget *statusBar;
  GtkWidget *statusBarEvent;
	GtkTooltips *displayTooltips;
	GtkWidget *scaleWave;
	GtkWidget *scaleFFT;
	GtkWidget *paneFFTSono;
	GtkWidget *paneWaveFFTSono;
	GtkWidget *panePaletteRest;

  int attrlist[] = {
    GDK_GL_RGBA,
    GDK_GL_RED_SIZE,5,
    GDK_GL_GREEN_SIZE,5,
    GDK_GL_BLUE_SIZE,5,
/*    GDK_GL_DEPTH_SIZE,5, */
	/*	GDK_GL_ALPHA_SIZE,0,
    GDK_GL_STENCIL_SIZE,0,
		GDK_GL_ACCUM_RED_SIZE,0,*/
    GDK_GL_DOUBLEBUFFER,
    GDK_GL_NONE
  };


	displayTooltips = gtk_tooltips_new();
  DisplayWindow = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  gtk_widget_set_name (DisplayWindow, "DisplayWindow");
  gtk_object_set_data (GTK_OBJECT (DisplayWindow), "DisplayWindow", DisplayWindow);
  gtk_container_set_border_width (GTK_CONTAINER (DisplayWindow), 1);
  gtk_window_set_title (GTK_WINDOW (DisplayWindow), _("SonaSound - Display"));
  gtk_window_set_policy (GTK_WINDOW (DisplayWindow), TRUE, TRUE, TRUE);

  vbox1 = gtk_vbox_new (FALSE, 0);
  vbox2 = gtk_vbox_new (FALSE, 0);
	hbox1 = gtk_hbox_new (FALSE, 0);
	hboxFFT = gtk_hbox_new (FALSE, 0);
  gtk_widget_set_name (vbox1, "vbox1");
  gtk_widget_set_name (vbox2, "vbox2");
  gtk_widget_set_name (hbox1, "hbox1");
  gtk_widget_set_name (hboxFFT, "hboxFFT");
  gtk_widget_ref (vbox1);
  gtk_widget_ref (vbox2);
  gtk_widget_ref (hbox1);
  gtk_widget_ref (hboxFFT);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "vbox1", vbox1,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "vbox2", vbox2,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "hbox1", hbox1,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "hboxFFT", hboxFFT,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (vbox1);
  gtk_widget_show (vbox2);
  gtk_widget_show (hbox1);
  gtk_widget_show (hboxFFT);
  gtk_container_add (GTK_CONTAINER (DisplayWindow), hbox1);

	/*################# WaveView #####################################*/
	paneWaveFFTSono = gtk_vpaned_new();
	waveEvent = gtk_event_box_new();
	gtk_widget_show(waveEvent);
  WaveArea = gtk_gl_area_new (attrlist);
	g_assert(WaveArea != NULL);
	scaleAdjustment = gtk_adjustment_new( 1.0f,
													0.00001f,
													numSeconds ,  0.00001f,
													1, 0.05f);

	scaleWave = gtk_hscale_new( GTK_ADJUSTMENT(scaleAdjustment));
	gtk_range_set_update_policy(GTK_RANGE(scaleWave), GTK_UPDATE_CONTINUOUS);
	gtk_scale_set_draw_value(GTK_SCALE(scaleWave), TRUE);
	gtk_scale_set_digits(GTK_SCALE(scaleWave), 5);
	gtk_scale_set_value_pos(GTK_SCALE(scaleWave), GTK_POS_LEFT);
	gtk_widget_set_usize(GTK_WIDGET(scaleWave), AREA_WIDTH, 10);

  gtk_widget_set_events(GTK_WIDGET(WaveArea),
/* 			GDK_EXPOSURE_MASK| */
			GDK_POINTER_MOTION_MASK
/* 			GDK_POINTER_MOTION_HINT_MASK| */
/* 			GDK_BUTTON1_MASK */
			);

  gtk_widget_set_name (WaveArea, "WaveArea");
  gtk_widget_ref (WaveArea);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "WaveArea", WaveArea,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (WaveArea);
  gtk_container_add (GTK_CONTAINER (vbox2), waveEvent);
	gtk_container_add(GTK_CONTAINER(waveEvent), WaveArea);
/*   gtk_box_pack_start (GTK_BOX (vbox2), GTK_WIDGET(WaveArea), TRUE, TRUE, 2); */
  gtk_box_pack_start (GTK_BOX (vbox2), GTK_WIDGET(scaleWave), FALSE, FALSE, 1);
	gtk_paned_pack1(GTK_PANED(paneWaveFFTSono), vbox2, TRUE, TRUE);
  gtk_box_pack_start (GTK_BOX (vbox1), GTK_WIDGET(paneWaveFFTSono), TRUE, TRUE, 2);

  gtk_widget_set_usize(GTK_WIDGET(WaveArea), AREA_WIDTH-100, AREA_HEIGHT/2);
	gtk_widget_show(GTK_WIDGET(scaleWave));
  gtk_widget_show (paneWaveFFTSono);

	/*################# FFT #####################################*/
	paneFFTSono = gtk_vpaned_new();
	FFTEvent = gtk_event_box_new();
	gtk_widget_show(FFTEvent);

  FFTArea = gtk_gl_area_new (attrlist);
  gtk_widget_set_events(GTK_WIDGET(FFTArea),
/* 			GDK_EXPOSURE_MASK| */
			GDK_POINTER_MOTION_MASK
/* 			GDK_POINTER_MOTION_HINT_MASK */
/* 			GDK_BUTTON1_MASK */
			);

	fftAdjustment = gtk_adjustment_new( maxDB,
													10.0,
													276.95 ,  1,
													10, 1);

	scaleFFT = gtk_vscale_new( GTK_ADJUSTMENT(fftAdjustment));
	gtk_range_set_update_policy(GTK_RANGE(scaleFFT), GTK_UPDATE_CONTINUOUS);
	gtk_scale_set_draw_value(GTK_SCALE(scaleFFT), TRUE);
	gtk_scale_set_digits(GTK_SCALE(scaleFFT), 0);
	gtk_scale_set_value_pos(GTK_SCALE(scaleFFT), GTK_POS_TOP);
  gtk_widget_set_name (FFTArea, "FFTArea");
  gtk_widget_ref (FFTArea);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "FFTArea", FFTArea,
                            (GtkDestroyNotify) gtk_widget_unref);
/*   gtk_box_pack_start (GTK_BOX (hboxFFT), FFTArea, TRUE, TRUE, 2); */
  gtk_container_add (GTK_CONTAINER (hboxFFT), FFTEvent);
	gtk_container_add(GTK_CONTAINER(FFTEvent), FFTArea);
  gtk_box_pack_start (GTK_BOX (hboxFFT), scaleFFT, FALSE, FALSE, 1);
	gtk_paned_pack1(GTK_PANED(paneFFTSono), hboxFFT, TRUE, TRUE);

  gtk_widget_set_usize(GTK_WIDGET(FFTEvent), AREA_WIDTH, AREA_HEIGHT / 2);
  gtk_widget_show (FFTArea);
	gtk_widget_show(paneFFTSono);
	gtk_widget_show(GTK_WIDGET(scaleFFT));

	/*################# SONOGRAMM #####################################*/

  SonoArea = gtk_gl_area_new (attrlist);
	SonoEvent = gtk_event_box_new();
	gtk_widget_show(SonoEvent);
  gtk_widget_set_events(GTK_WIDGET(SonoArea),
/* 			GDK_EXPOSURE_MASK| */
			GDK_POINTER_MOTION_MASK
/* 			GDK_POINTER_MOTION_HINT_MASK| */
/* 			GDK_BUTTON1_MASK */
			);
  gtk_widget_set_usize(GTK_WIDGET(SonoArea), AREA_WIDTH, AREA_WIDTH - 70);

  gtk_widget_set_name (SonoArea, "SonoArea");
  gtk_widget_ref (SonoArea);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "SonoArea", SonoArea,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (SonoArea);

/* 	gtk_paned_pack2(GTK_PANED(paneFFTSono), SonoArea, TRUE, TRUE); */
  gtk_container_add (GTK_CONTAINER (paneFFTSono), SonoEvent);
	gtk_container_add(GTK_CONTAINER(SonoEvent), SonoArea);
	gtk_paned_pack2(GTK_PANED(paneWaveFFTSono), paneFFTSono, TRUE, TRUE);


	/* ################ PaletteView ####################################### */
	panePaletteRest = gtk_hpaned_new();
	gtk_widget_show(panePaletteRest);
  PaletteArea = gtk_gl_area_new (attrlist);
	PaletteEvent = gtk_event_box_new();
	gtk_widget_show(PaletteEvent);
  gtk_widget_set_usize(GTK_WIDGET(PaletteEvent), 20, AREA_WIDTH - 70);
  gtk_widget_set_usize(GTK_WIDGET(PaletteArea), 20, AREA_WIDTH - 70);
  gtk_widget_set_name (PaletteArea, "PaletteArea");
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "PaletteArea", PaletteArea,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (PaletteArea);
  gtk_widget_set_events(GTK_WIDGET(PaletteArea),
/* 			GDK_EXPOSURE_MASK| */
			GDK_POINTER_MOTION_MASK
/* 			GDK_POINTER_MOTION_HINT_MASK| */
/* 			GDK_BUTTON1_MASK */
			);
  gtk_container_add (GTK_CONTAINER (panePaletteRest), PaletteEvent);
	gtk_container_add(GTK_CONTAINER(PaletteEvent), PaletteArea);
	gtk_paned_pack1(GTK_PANED(panePaletteRest), PaletteEvent, TRUE, TRUE);
/* 	gtk_box_pack_start (GTK_BOX (hbox1), PaletteArea, FALSE, FALSE, 2); */
  gtk_box_pack_start (GTK_BOX (hbox1), GTK_WIDGET(panePaletteRest), TRUE, TRUE, 2);
/* 	gtk_box_set_child_packing(GTK_BOX(hbox1), GTK_WIDGET(vbox1), TRUE) */
	gtk_paned_pack2(GTK_PANED(panePaletteRest), vbox1, TRUE, TRUE);
/*   gtk_container_add (GTK_CONTAINER (hbox1), vbox1); */


	/* ################ StatusBar ####################################### */
  eventbox1 = gtk_event_box_new ();
  gtk_widget_set_name (eventbox1, "eventbox1");
  gtk_widget_ref (eventbox1);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "eventbox1", eventbox1,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (eventbox1);
  gtk_box_pack_start (GTK_BOX (vbox1), eventbox1, FALSE, FALSE, 0);
	gtk_box_set_child_packing(GTK_BOX(vbox1), eventbox1, FALSE, FALSE, 0, GTK_PACK_START);

  statusBar = gtk_statusbar_new ();
  gtk_widget_set_name (statusBar, "statusBar");
  gtk_widget_ref (statusBar);
  gtk_object_set_data_full (GTK_OBJECT (DisplayWindow), "statusBar", statusBar,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (statusBar);
  gtk_container_add (GTK_CONTAINER (eventbox1), statusBar);
  gtk_container_set_border_width (GTK_CONTAINER (statusBar), 1);
  GTK_WIDGET_SET_FLAGS (statusBar, GTK_CAN_FOCUS);

/* ################ ToolTips ####################################### */
	gtk_tooltips_set_tip(GTK_TOOLTIPS(displayTooltips), scaleFFT, _("Adjust the maximum (in dB) to display at the top of the area. Tthis scales the FFT-Display and the Sonogram-colours"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(displayTooltips), scaleWave, _("Adjust the width of the Waveform-display"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(displayTooltips), FFTEvent, _("Short-time spectrum of one buffer:\n X-axis displays frequency, Y-axis magnitude: move the mouse to see the exact values in the Status Bar"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(displayTooltips), waveEvent, _("Waveform:                        \n X-axis displays relative time from right border, Y-axis sample-value (-100% -- +100%): move the mouse to see the exact values in the Status Bar"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(displayTooltips), SonoEvent, _("Sonogram:                         \n X-axis displays relative time from right border, Y-axis frequency, colour represents magnitude (brighter is louder): move the mouse to see the exact values in the Status Bar"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(displayTooltips), PaletteEvent, _("Colour Palette used for the Sonogram: Move the mouse to see the corresponding decibel values in the Status Bar"), NULL);
	gtk_tooltips_set_tip(GTK_TOOLTIPS(displayTooltips), eventbox1, _("Status Bar: Move the mouse in fields above to see the exact values here"), NULL);
/* ################ Signals ####################################### */
  /* Redraw image when exposed. */
  gtk_signal_connect(GTK_OBJECT(WaveArea), "expose_event",
		     GTK_SIGNAL_FUNC(glAreaDrawWave), NULL);
/*  gtk_signal_connect(GTK_OBJECT(WaveArea), "button_press_event",
		     GTK_SIGNAL_FUNC(glAreaDrawWave), NULL); */
  /* When window is resized viewport needs to be resized also. */
  gtk_signal_connect(GTK_OBJECT(WaveArea), "configure_event",
		     GTK_SIGNAL_FUNC(glAreaReshape), NULL);
	/* Do initialization when widget has been realized. */
  gtk_signal_connect(GTK_OBJECT(WaveArea), "realize",
		     GTK_SIGNAL_FUNC(glAreaInit), NULL);
/* 	gtk_signal_connect(GTK_OBJECT(WaveArea), "button_press_event", */
/* 				GTK_SIGNAL_FUNC(printStatus), statusBar); */
	gtk_signal_connect(GTK_OBJECT(WaveArea), "motion_notify_event",
				GTK_SIGNAL_FUNC(printStatus), statusBar);

  /* Redraw image when exposed. */
  gtk_signal_connect(GTK_OBJECT(FFTArea), "expose_event",
		     GTK_SIGNAL_FUNC(glAreaDrawFFT), NULL);
/*  gtk_signal_connect(GTK_OBJECT(FFTArea), "button_press_event",
		     GTK_SIGNAL_FUNC(glAreaDrawFFT), NULL); */
  /* When window is resized viewport needs to be resized also. */
  gtk_signal_connect(GTK_OBJECT(FFTArea), "configure_event",
		     GTK_SIGNAL_FUNC(glAreaReshape), NULL);
	/* Do initialization when widget has been realized. */
  gtk_signal_connect(GTK_OBJECT(FFTArea), "realize",
		     GTK_SIGNAL_FUNC(glAreaInit), NULL);
	gtk_signal_connect(GTK_OBJECT(FFTArea), "button_press_event",
				GTK_SIGNAL_FUNC(printStatus), statusBar);
	gtk_signal_connect(GTK_OBJECT(FFTArea), "motion_notify_event",
				GTK_SIGNAL_FUNC(printStatus), statusBar);


  /* Redraw image when exposed. */
  gtk_signal_connect(GTK_OBJECT(SonoArea), "expose_event",
		     GTK_SIGNAL_FUNC(glAreaDrawSonogram), NULL);
/*  gtk_signal_connect(GTK_OBJECT(SonoArea), "button_press_event",
		     GTK_SIGNAL_FUNC(glAreaDrawSonogram), NULL); */
  /* When window is resized viewport needs to be resized also. */
  gtk_signal_connect(GTK_OBJECT(SonoArea), "configure_event",
		     GTK_SIGNAL_FUNC(glAreaReshape), NULL);
	/* Do initialization when widget has been realized. */
  gtk_signal_connect(GTK_OBJECT(SonoArea), "realize",
		     GTK_SIGNAL_FUNC(glAreaInit), NULL);
/* 	gtk_signal_connect(GTK_OBJECT(SonoArea), "button_press_event", */
/* 				GTK_SIGNAL_FUNC(printStatus), statusBar); */
	gtk_signal_connect(GTK_OBJECT(SonoArea), "motion_notify_event",
				GTK_SIGNAL_FUNC(printStatus), statusBar);

  /* Redraw image when exposed. */
  gtk_signal_connect(GTK_OBJECT(PaletteArea), "expose_event",
		     GTK_SIGNAL_FUNC(glAreaDrawPalette), NULL);
/*  gtk_signal_connect(GTK_OBJECT(SonoArea), "button_press_event",
		     GTK_SIGNAL_FUNC(glAreaDrawSonogram), NULL); */
  /* When window is resized viewport needs to be resized also. */
  gtk_signal_connect(GTK_OBJECT(PaletteArea), "configure_event",
		     GTK_SIGNAL_FUNC(glAreaReshape), NULL);
	/* Do initialization when widget has been realized. */
  gtk_signal_connect(GTK_OBJECT(PaletteArea), "realize",
		     GTK_SIGNAL_FUNC(glAreaInit), NULL);
/* 	gtk_signal_connect(GTK_OBJECT(PaletteArea), "button_press_event", */
/* 				GTK_SIGNAL_FUNC(printStatus), statusBar); */
	gtk_signal_connect(GTK_OBJECT(PaletteArea), "motion_notify_event",
				GTK_SIGNAL_FUNC(printStatus), statusBar);

	gtk_signal_connect(GTK_OBJECT(scaleAdjustment), "value_changed",
				GTK_SIGNAL_FUNC(resizeWidget), WaveArea);
	gtk_signal_connect(GTK_OBJECT(scaleAdjustment), "changed",
				GTK_SIGNAL_FUNC(resizeWidget), WaveArea);

	gtk_signal_connect(GTK_OBJECT(fftAdjustment), "value_changed",
				GTK_SIGNAL_FUNC(adjustFFT), NULL);
	gtk_signal_connect(GTK_OBJECT(fftAdjustment), "changed",
				GTK_SIGNAL_FUNC(adjustFFT), NULL);
  return DisplayWindow;
}

GtkWidget*
create_fileSelection (void)
{
  GtkWidget *fileSelection;
  GtkWidget *ok_button;
  GtkWidget *cancel_button;

  fileSelection = gtk_file_selection_new (_("Datei auswählen"));
  gtk_widget_set_name (fileSelection, "fileSelection");
  gtk_object_set_data (GTK_OBJECT (fileSelection), "fileSelection", fileSelection);
  gtk_container_set_border_width (GTK_CONTAINER (fileSelection), 10);
  GTK_WINDOW (fileSelection)->type = GTK_WINDOW_DIALOG;
  gtk_window_set_position (GTK_WINDOW (fileSelection), GTK_WIN_POS_MOUSE);

  ok_button = GTK_FILE_SELECTION (fileSelection)->ok_button;
  gtk_widget_set_name (ok_button, "ok_button");
  gtk_object_set_data (GTK_OBJECT (fileSelection), "ok_button", ok_button);
  gtk_widget_show (ok_button);
  GTK_WIDGET_SET_FLAGS (ok_button, GTK_CAN_DEFAULT);

  cancel_button = GTK_FILE_SELECTION (fileSelection)->cancel_button;
  gtk_widget_set_name (cancel_button, "cancel_button");
  gtk_object_set_data (GTK_OBJECT (fileSelection), "cancel_button", cancel_button);
  gtk_widget_show (cancel_button);
  GTK_WIDGET_SET_FLAGS (cancel_button, GTK_CAN_DEFAULT);

  gtk_signal_connect (GTK_OBJECT (ok_button), "clicked",
                      GTK_SIGNAL_FUNC (on_ok_button_clicked),
                      NULL);
  gtk_signal_connect_object (GTK_OBJECT (cancel_button), "clicked",
                             GTK_SIGNAL_FUNC (gtk_widget_destroy),
                             GTK_OBJECT (fileSelection));

  gtk_widget_grab_default (ok_button);
  return fileSelection;
}

GtkWidget*
create_About_SonaSound (void)
{
  GtkWidget *About_SonaSound;
  GtkWidget *dialog_vbox1;
  GtkWidget *scrolledwindow1;
  GtkWidget *dialogText;
  GtkWidget *dialog_action_area1;
  GtkWidget *OK;


  About_SonaSound = gtk_dialog_new ();
  gtk_widget_set_name (About_SonaSound, "About_SonaSound");
  gtk_object_set_data (GTK_OBJECT (About_SonaSound), "About_SonaSound", About_SonaSound);
  gtk_window_set_title (GTK_WINDOW (About_SonaSound), _("About SonaSound"));
  gtk_window_set_position (GTK_WINDOW (About_SonaSound), GTK_WIN_POS_CENTER);
  gtk_window_set_policy (GTK_WINDOW (About_SonaSound), TRUE, TRUE, TRUE);

  dialog_vbox1 = GTK_DIALOG (About_SonaSound)->vbox;
  gtk_widget_set_name (dialog_vbox1, "dialog_vbox1");
  gtk_object_set_data (GTK_OBJECT (About_SonaSound), "dialog_vbox1", dialog_vbox1);
  gtk_widget_show (dialog_vbox1);

  scrolledwindow1 = gtk_scrolled_window_new (NULL, NULL);
  gtk_widget_set_name (scrolledwindow1, "scrolledwindow1");
  gtk_widget_ref (scrolledwindow1);
  gtk_object_set_data_full (GTK_OBJECT (About_SonaSound), "scrolledwindow1", scrolledwindow1,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_widget_set_usize(GTK_WIDGET(scrolledwindow1), 480, 300);
  gtk_widget_show (scrolledwindow1);
  gtk_box_pack_start (GTK_BOX (dialog_vbox1), scrolledwindow1, TRUE, TRUE, 0);
  gtk_scrolled_window_set_policy (GTK_SCROLLED_WINDOW (scrolledwindow1), GTK_POLICY_NEVER, GTK_POLICY_ALWAYS);

  dialogText = gtk_text_new (NULL, NULL);
  gtk_widget_set_name (dialogText, "dialogText");
  gtk_widget_ref (dialogText);
  gtk_object_set_data_full (GTK_OBJECT (About_SonaSound), "dialogText", dialogText,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (dialogText);
  gtk_container_add (GTK_CONTAINER (scrolledwindow1), dialogText);
  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL,
                   _("\
This is a Master's thesis at the institute for\n\
communications research at the TU Berlin:\n\
\n\
http://www.kgw.tu-berlin.de/KW/\n\
\n\
Feel free to download the newest version from:\n\
\n\
http://www.sonasound.de or http://sonasound.sf.net\n\
\n\
Copyright (C) 2002-2003 Niklas Werner <niklas@niklaswerner.de>\n\
\n\
This program is free software; you can redistribute it and/or modify\n\
it under the terms of the GNU General Public License as published by\n\
the Free Software Foundation; either version 2 of the License, or\n\
(at your option) any later version.\n\
\n\
This program is distributed in the hope that it will be useful,\n\
but WITHOUT ANY WARRANTY; without even the implied warranty of\n\
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n\
GNU General Public License for more details.\n\
\n\
You should have received a copy of the GNU General Public License\n\
along with this program; if not, write to the Free Software\n\
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.\n\
									 "), -1);

  dialog_action_area1 = GTK_DIALOG (About_SonaSound)->action_area;
  gtk_widget_set_name (dialog_action_area1, "dialog_action_area1");
  gtk_object_set_data (GTK_OBJECT (About_SonaSound), "dialog_action_area1", dialog_action_area1);
  gtk_widget_show (dialog_action_area1);
  gtk_container_set_border_width (GTK_CONTAINER (dialog_action_area1), 10);

  OK = gtk_button_new_with_label (_("OK"));
  gtk_widget_set_name (OK, "OK");
  gtk_widget_ref (OK);
  gtk_object_set_data_full (GTK_OBJECT (About_SonaSound), "OK", OK,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (OK);
  gtk_box_pack_start (GTK_BOX (dialog_action_area1), OK, FALSE, FALSE, 0);
  gtk_container_set_border_width (GTK_CONTAINER (OK), 1);
  gtk_button_set_relief (GTK_BUTTON (OK), GTK_RELIEF_HALF);

  gtk_signal_connect_object (GTK_OBJECT (OK), "clicked",
                      GTK_SIGNAL_FUNC (gtk_widget_destroy),
                      GTK_OBJECT(About_SonaSound));

  GTK_WIDGET_SET_FLAGS (OK, GTK_CAN_DEFAULT);
  gtk_widget_grab_default (OK);

  return About_SonaSound;
}

GtkWidget*
createTutorial (void)
{
  GtkWidget *tutorial;
  GtkWidget *dialog_vbox1;
  GtkWidget *scrolledwindow1;
  GtkWidget *dialogText;
  GtkWidget *dialog_action_area1;
  GtkWidget *OK;
	GString *tutorialTextIntro;
	GString *tutorialTextCaveats;
	GString *tutorialTextMain;
	GString *tutorialTextOutro;
	GString *tutorialTextSep;

	tutorialTextSep = g_string_new(_("\
\n\
######################################################################\n\
\n\
"));

	tutorialTextIntro = g_string_new(_("\
                                     SonaSound\n\
\n\
\n\
- Switching from Soundfile to Soundcard Input:\n\
\t+ Click \"Close Devices\" to activate the Togglebutton \"Use Soundfile\"\n\
\t+ Click \"Open Devices\" to reactivate the transport controls\n\
\n\
-------------------------------------------------------------------------------\n\
\n\
- What it does:\n\
\n\
  * Check for OpenGL-support, set up windows, measure fastest FFT-method or \n\
	  read it from wisdom-file\n\
  * start Audio-Engine in new thread and calculate FFT or LPC from the given\n\
	  Buffer-Size (Windows are applied to either the number of samples in one \n\
		Buffer or to the number of samples corresponding to the number of \n\
		FFT-points, whatever is smaller)\n\
  * Draw WaveForm, FFT-View and one Sonogram-strip every time a new audio-buffer\n\
	  arrives. Shift Sonogram one pixel to the left and discard the leftmost strip\n\
  * LPC: autoregressive calculation of the filter-coefficients followed by\n\
	  frequency response with half the number of FFT-points \n\
	"));

	tutorialTextCaveats = g_string_new(_("\
- Caveats:\n\
\n\
  * LPC-calculation takes quite a long time, be careful on slow \n\
	  (less than 700MHz) systems.\n\
  * memmove is slow on MacOsX: Don't choose too large a buffer.\n\
  * Textures under Linux are limited to sizes of powers of two, so making the \n\
	  sonogram-view too large for your graphics card (typically larger than \n\
		512 by 512 or 1024 by 1024) may give you a white window and lots of \n\
		error-messages.\n\
 -> Solution: make the window smaller! This is an OpenGL-limitation \n\
		(More exactly a MESA-limitation) and doesn't apply on MacOsX, \n\
		because there I'm using a different way of Blitting\n\
  * Resizing the window when playing garbles the sonogram-view, \n\
	  because there is no way of knowing what happens inside the graphics-card-memory.\n\
  * Large values (>=512) for the number of LPC-coefficients together \n\
	  with large values for the number of FFT-points (>=4096) slows the display \n\
		down to unusable.\n\
 -> Solution: choose a larger buffer (then the redrawing doesn't happen that often)\n\
		or a smaller number of FFT-points.\n\
  * The calculation of the waveform takes a long time, because of the large amount\n\
	  of data to be processed.\n\
 -> Solution: choose a smaller range of seconds to be displayed or a larger buffer.\n\
  * Switching from File-input to Soundcard-input requires closing the sounddevice\n\
 -> Solution: For performance reasons I have to reallocate lots of buffers.\n\
		so no soultion ATM.\n\
  * On PPC-computers stopping the playing results in an endless repeating of the\n\
	  last buffer.\n\
 -> Solution: This seems to be a portaudio version18-patch bug. \n\
		Close the device or restart the playing.\n\
"));

	tutorialTextMain = g_string_new(_("\
- What the Parameters mean:\n\
\n\
  In (brackets) are the command line pendants.\n\
	\n\
 +SamplingRate (-s) Set the Samplingrate to specified float in Hz\n\
  Only adjustable when the sound devices are closed.\n\
\n\
 +FFT Size (-f oder -a) Set the number (-a) of FFT-Points (integer) \n\
  the toggle-button next to it means rounding the figure to the next\n\
	power of two (-f)\n\
\n\
 +Buffer Size (-p) Set the Audio-buffer size to specified integer\n\
  Only adjustable when the sound devices are closed.\n\
  the toggle-button next to it means rounding the figure to the next\n\
	power of two\n\
\n\
 +Window Type (-w) Choose the windowing function\n\
  On the commandline you have to specify the following digits:\n\
	\n\
  * 0:Hamming\n\
\n\
  * 1:Hanning (default)\n\
\n\
  * 2:Blackman\n\
\n\
  * 3:Bartlett\n\
\n\
  * 4:Kaiser\n\
\n\
  * 5:Rectangular\n\
\n\
 +Display Type (-d) Choose between linear (i) or logarithmic (g) frequency scaling\n\
\n\
 +Spectrum Type (-g) Switch between LPC (l) or FFT (f) for spectrum generation.\n\
\n\
 +LPC Size (-l) Number (integer) of LPC-coefficients to calculate\n\
\n\
 +Reference A (-b) Frequency in Hz for the reference-A used for displaying the \n\
  staff-lines\n\
\n\
 +Waveform Range (-S) Maximum number of seconds to keep in Waveform-buffer.\n\
  this is not accessible via the GUi, because the implications on the program \n\
	are too great to allow this while running (the value is clamped to the range\n\
	1-20 sec)\n\
\n\
 +AudioIn Device (-D) Choose Audio-device to be used for input (numbered from\n\
  0)\n\
  Only adjustable when the sound devices are closed.\n\
	\n\
 +AudioOut Device (-O) same as AudioIn for output\n\
  Only adjustable when the sound devices are closed.\n\
\n\
 +Analysis Presets Choice of several predefined Settings for certain kinds of\n\
  signals\n\
  Only adjustable when the sound devices are closed.\n\
	\n\
 +Overlap (-o) Switch overlapping (50%) of analysis-buffer on or off\n\
\n\
 +HighPass (-H) Switch the highpass-filter (1st order) on or off to clean the\n\
  spectral display. On the commandline you can specify the value of the coefficient\n\
	in the range (0.001, 1.0). Default Value is 0.5\n\
	\n\
 +Draw Staff-Lines (-n) Switch Staff-lines on or off. \n\
\n\
 +Use Soundfile (-F) Use a file for analysing instead of soundcard input\n\
  Only adjustable when the sound devices are closed.\n\
\n\
 +Help (-h) displays basic help for command-line-options\n\
\n\
-The Buttons\n\
\n\
 +The functions  not available are grayed out. Try stopping or closing devices\n\
  to access buttons you cannot reach.\n\
\n\
 +Draw:  Draw the current buffer once.\n\
\n\
 +Start:  Start the Audio-engine\n\
\n\
 +Stop:  Stop the Audio-engine\n\
\n\
 +Reset File:  Start playing the Sounfile from the beginning\n\
\n\
 +Start Drawing:  Start displaying the buffers in Sync to the audio\n\
\n\
 +Stop Drawing:  Stoppe displaying the buffers\n\
\n\
 +Start All:  Start audio and Drawing (KeypadEnter)\n\
\n\
 +Stop All:  Stop audio and Drawing (Keypad 0)\n\
\n\
 +Close Devices:  Close the audio-devices to allow for second instance of the\n\
 SonaSound or another program to use the device. Or for switching between different devices.\n\
\n\
 +Open Devices: Open chosen Audio-device. When using a Soundfile for input\n\
 only the output-channels will be openen and vv.\n\
"));

	tutorialTextOutro = g_string_new(_("\
-The (rudimentary) menu\n\
\n\
 +File->Open: Open Soundfile\n\
\n\
 +File->Quit: Quit SonaSound\n\
\n\
 +Help->About: Show info about SonaSound\n\
\n\
 +Help->Tutorial: Whre do you think you are?\n\
\n\
\n\
Copyright (C) 2002-2003 Niklas Werner <niklas@niklaswerner.de>\n\
"));

  tutorial = gtk_dialog_new ();
  gtk_widget_set_name (tutorial, "SonaSoundTutorial");
  gtk_object_set_data (GTK_OBJECT (tutorial), "tutorial", tutorial);
  gtk_window_set_title (GTK_WINDOW (tutorial), _("tutorial"));
  gtk_window_set_position (GTK_WINDOW (tutorial), GTK_WIN_POS_CENTER);
  gtk_window_set_policy (GTK_WINDOW (tutorial), TRUE, TRUE, TRUE);

  dialog_vbox1 = GTK_DIALOG (tutorial)->vbox;
  gtk_widget_set_name (dialog_vbox1, "dialog_vbox1");
  gtk_object_set_data (GTK_OBJECT (tutorial), "dialog_vbox1", dialog_vbox1);
  gtk_widget_show (dialog_vbox1);

  scrolledwindow1 = gtk_scrolled_window_new (NULL, NULL);
  gtk_widget_set_name (scrolledwindow1, "scrolledwindow1");
  gtk_widget_ref (scrolledwindow1);
  gtk_object_set_data_full (GTK_OBJECT (tutorial), "scrolledwindow1", scrolledwindow1,
                            (GtkDestroyNotify) gtk_widget_unref);
	gtk_widget_set_usize(GTK_WIDGET(scrolledwindow1), 600, 600);
  gtk_widget_show (scrolledwindow1);
  gtk_box_pack_start (GTK_BOX (dialog_vbox1), scrolledwindow1, TRUE, TRUE, 0);
  gtk_scrolled_window_set_policy (GTK_SCROLLED_WINDOW (scrolledwindow1), GTK_POLICY_NEVER, GTK_POLICY_ALWAYS);

  dialogText = gtk_text_new (NULL, NULL);
  gtk_widget_set_name (dialogText, "dialogText");
  gtk_widget_ref (dialogText);
  gtk_object_set_data_full (GTK_OBJECT (tutorial), "dialogText", dialogText,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (dialogText);
  gtk_container_add (GTK_CONTAINER (scrolledwindow1), dialogText);

  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL, tutorialTextIntro->str, -1);
  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL, tutorialTextSep->str, -1);
  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL, tutorialTextCaveats->str, -1);
  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL, tutorialTextSep->str, -1);
  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL, tutorialTextMain->str, -1);
  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL, tutorialTextSep->str, -1);
  gtk_text_insert (GTK_TEXT (dialogText), NULL, NULL, NULL, tutorialTextOutro->str, -1);

  dialog_action_area1 = GTK_DIALOG (tutorial)->action_area;
  gtk_widget_set_name (dialog_action_area1, "dialog_action_area1");
  gtk_object_set_data (GTK_OBJECT (tutorial), "dialog_action_area1", dialog_action_area1);
  gtk_widget_show (dialog_action_area1);
  gtk_container_set_border_width (GTK_CONTAINER (dialog_action_area1), 10);

  OK = gtk_button_new_with_label (_("OK"));
  gtk_widget_set_name (OK, "OK");
  gtk_widget_ref (OK);
  gtk_object_set_data_full (GTK_OBJECT (tutorial), "OK", OK,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (OK);
  gtk_box_pack_start (GTK_BOX (dialog_action_area1), OK, FALSE, FALSE, 0);
  gtk_container_set_border_width (GTK_CONTAINER (OK), 1);
  gtk_button_set_relief (GTK_BUTTON (OK), GTK_RELIEF_HALF);

  gtk_signal_connect_object (GTK_OBJECT (OK), "clicked",
                      GTK_SIGNAL_FUNC (gtk_widget_destroy),
                      GTK_OBJECT(tutorial));

  GTK_WIDGET_SET_FLAGS (OK, GTK_CAN_DEFAULT);
  gtk_widget_grab_default (OK);

	g_string_free(tutorialTextIntro, TRUE);
	g_string_free(tutorialTextCaveats, TRUE);
	g_string_free(tutorialTextMain, TRUE);
	g_string_free(tutorialTextOutro, TRUE);
	g_string_free(tutorialTextSep, TRUE);

  return tutorial;
}

