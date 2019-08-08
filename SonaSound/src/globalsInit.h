/*
 * $Id: globalsInit.h,v 1.15 2003/03/17 14:03:35 niklaswerner Exp $
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

gfloat
myLog(gfloat value);

void
globalsInit(int argc, char *argv[]);

void
logScaleInit(GtkWidget *widget);

void
printUsage (char *argv[]);

GList*
getDeviceInfo(GList *liste, guint8 inOut);

GList*
getSamplingRate(GList *liste);

void
printSummaryString(void);

void
windowNameListInit(void);

void
genNoteTable(void);
