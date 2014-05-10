/*
 * Truffautronic
 * Copyright (c) 2014 Laurent GRÉGOIRE <laurent.gregoire@gmail.com>
 * & Lycée Général et Technologique François Truffaut - Challans
 *
 * Truffautronic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Truffautronic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Truffautronic.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.truffautronic.model;

public class Duration {

	private long durationMs;

	public static final Duration ZERO = new Duration(0);

	public Duration(long ms) {
		this.durationMs = ms;
	}

	public Duration(String duration) throws NumberFormatException {
		String[] elems = duration.split(":");
		if (elems.length >= 1) {
			Double sec = Double.valueOf(elems[elems.length - 1]);
			durationMs = Math.round(sec * 1000);
		}
		if (elems.length >= 2) {
			Integer min = Integer.valueOf(elems[elems.length - 2]);
			durationMs += min * 60 * 1000;
		}
		if (elems.length >= 3) {
			Integer hour = Integer.valueOf(elems[elems.length - 3]);
			durationMs += hour * 60 * 60 * 1000;
		}
	}

	public long getMs() {
		return durationMs;
	}

	public String toString() {
		long x = durationMs;
		long ms = x % 1000L;
		x /= 1000L;
		long sec = x % 60L;
		x /= 60L;
		long min = x % 60L;
		x /= 60L;
		long hour = x;
		if (hour != 0) {
			if (ms == 0)
				return String.format("%d:%02d:%02d", hour, min, sec);
			else
				return String.format("%d:%02d:%02d.%03d", hour, min, sec, ms);
		}
		if (min != 0) {
			if (ms == 0)
				return String.format("%d:%02d", min, sec);
			else
				return String.format("%d:%02d.%03d", min, sec, ms);
		}
		if (ms == 0)
			return String.format("%d.0", sec);
		else
			return String.format("%d.%03d", sec, ms);
	}

	private static final long[] ALIGNMENTS = { 1, 2, 5, 10, 20, 50, 100, 200,
			500, 1000, 2000, 5000, 10000, 20000, 30000, 60000, 120000, 300000,
			600000, 1800000, 3600000 };

	public static Duration round(long durationMs) {
		for (long align : ALIGNMENTS) {
			if (align > durationMs) {
				return new Duration(align);
			}
		}
		return new Duration(ALIGNMENTS[ALIGNMENTS.length - 1]);
	}
}
