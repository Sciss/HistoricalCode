/*
 * $Id: helpers.h,v 1.5 2003/03/28 18:13:58 niklaswerner Exp $
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

unsigned nextPowerOfTwo (unsigned n);

unsigned prevPowerOfTwo (unsigned n);

gfloat fastLog2 (gfloat val);

#ifdef INTELA

static int truncate(float flt);

#elif defined POWERPCaa

static int truncate(float x);

#endif
