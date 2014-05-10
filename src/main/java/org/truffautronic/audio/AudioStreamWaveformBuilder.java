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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

public class AudioStreamWaveformBuilder {

	protected int sampleMax = 0;
	protected int sampleMin = 0;
	protected double biggestSample;
	private int frameSize;
	private int sampleSizeInBits;
	private int numBytesPerSample;
	private int numChannels;

	private List<SampledAudioWaveform> waveforms = new ArrayList<SampledAudioWaveform>();

	public AudioStreamWaveformBuilder(AudioFormat format, byte[] audioData)
			throws IOException {

		// Make sure format is PCM signed.
		if (!format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
				|| format.getSampleSizeInBits() != 16 || format.isBigEndian()) {
			throw new IllegalArgumentException(
					"Format should be 16 bits PCM signed, little endian!");
		}
		frameSize = format.getFrameSize();
		sampleSizeInBits = format.getSampleSizeInBits();
		numBytesPerSample = sampleSizeInBits / 8;
		numChannels = frameSize / numBytesPerSample;
		if (sampleSizeInBits != 16) {
			throw new IllegalArgumentException("16 bits data only");
		}
		int[][] samples = convertRawData(audioData);
		for (int channel = 0; channel < numChannels; channel++) {
			waveforms.add(new SampledAudioWaveform(samples[channel]));
		}
	}

	protected int[][] convertRawData(byte[] rawData) {
		int[][] retval = new int[numChannels][rawData.length
				/ (2 * numChannels)];
		int i = 0;
		for (int t = 0; t < rawData.length;) {
			for (int channel = 0; channel < numChannels; channel++) {
				int l = (int) rawData[t];
				t++;
				int h = (int) rawData[t];
				t++;
				retval[channel][i] = (h << 8) + (l & 0xff);
			}
			i++;
		}
		return retval;
	}

	public List<AudioWaveform> getWaveforms() {
		return new ArrayList<AudioWaveform>(waveforms);
	}

}
