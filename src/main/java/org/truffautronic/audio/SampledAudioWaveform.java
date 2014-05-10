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

package org.truffautronic.audio;

public class SampledAudioWaveform implements AudioWaveform {

	private int[] values;

	public SampledAudioWaveform(int[] samples) {
		values = new int[MAX_SAMPLES];
		long j1 = 0;
		for (int i = 0; i < MAX_SAMPLES; i++) {
			int n = 0;
			int sum = 0;
			long j2 = ((long) i + 1) * samples.length / MAX_SAMPLES;
			for (long j = j1; j < j2; j++) {
				int val = samples[(int) j];
				if (val < 0) {
					val = -val;
				}
				sum += val;
				n++;
			}
			values[i] = n > 0 ? sum / n : 0;
			j1 = j2 - 2;
			if (j1 < 0)
				j1 = 0;
		}
	}

	public float getAmplitude(int index) {
		return (float) values[index] / 22000;
	}
}
