/*
 * $Id: glDraw.h,v 1.11 2003/04/03 19:07:43 niklaswerner Exp $
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
 *
 */

gint
glAreaDrawSync				(int sig);

gint
glAreaInit						(GtkWidget *widget);

gint
glAreaDrawWave						(GtkWidget *widget, GdkEventMotion *event);

gint
glAreaDrawFFT						(GtkWidget *widget, GdkEventMotion *event);

gint
glAreaDrawPalette						(GtkWidget *widget, GdkEventMotion *event);

gint
glAreaDrawSonogram						(GtkWidget *widget, GdkEventMotion *event);

gint
glAreaReshape					(GtkWidget *widget, GdkEventConfigure *event);

gint
glAreaDrawGrid				(areaName name, GtkWidget *widget);
