/*
 * $Id: helpers.c,v 1.7 2003/03/28 18:13:58 niklaswerner Exp $
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
 * implement some often called functions a bit faster
 */

#include "sonasound.h"
#include "helpers.h"

/* Thanks to music-dsp mailing-list */
inline unsigned nextPowerOfTwo (unsigned n)
{
  unsigned prevN;
  for (prevN = n; n &= n-1; prevN = n)
	;
  return (prevN * 2);
}

inline unsigned prevPowerOfTwo (unsigned n)
{
  unsigned prevN;
  for (prevN = n; n &= n-1; prevN = n)
	;
  return (prevN);
}

inline gfloat fastLog2 (gfloat val)
{
  int *exp_ptr;
  int x;
  int log_2;

  g_assert (val > 0);
/* 	if(val <= 0.0) val = 1.0; */
	exp_ptr = (int *) (&val);
	x = *exp_ptr;
	log_2 = ((x >> 23) & 255) - 128;
	x &= ~(255 << 23);
	x += 127 << 23;
	*exp_ptr = x;

	return (val + log_2);
}

