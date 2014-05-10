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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.truffautronic.audio.AudioStreamWaveformBuilder;
import org.truffautronic.audio.AudioWaveform;

public class ClipCueAudioFactory implements CueAudioFactory {

	// Saved parameters
	private File audioFile;

	// Transient parameters
	private transient List<AudioWaveform> waveforms;
	private transient AudioFormat audioFormat;
	private transient byte[] audioData;
	private transient Duration duration;

	public ClipCueAudioFactory() {
	}

	public File getAudioFile() {
		return audioFile;
	}

	public void setAudioFile(File audioFile) {
		this.audioFile = audioFile;
		waveforms = null;
		audioData = null;
		audioFormat = null;
		duration = null;
		try {
			loadAudio();
		} catch (Exception e) {
			this.audioFile = null;
		}
	}

	private void loadAudio() throws IOException, UnsupportedAudioFileException {
		byte[] fileData = loadFile(audioFile);
		AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(new ByteArrayInputStream(fileData));
		audioFormat = audioInputStream.getFormat();
		// Make sure format is 16 bits PCM signed.
		if (!audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
				|| audioFormat.getSampleSizeInBits() != 16
				|| audioFormat.isBigEndian()) {
			// Force PCM_signed, little endian, 16 bits sample size
			audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					audioFormat.getSampleRate(), 16, audioFormat.getChannels(),
					audioFormat.getFrameSize(), audioFormat.getFrameRate(),
					false);
			audioInputStream = AudioSystem.getAudioInputStream(audioFormat,
					audioInputStream);
			// Normally a no-op, just to make sure:
			audioFormat = audioInputStream.getFormat();
		}

		long frameLen = audioInputStream.getFrameLength();
		long byteLen = frameLen * audioFormat.getFrameSize();
		audioData = new byte[(int) byteLen];
		int byteRead = audioInputStream.read(audioData);
		if (byteRead != byteLen)
			throw new EOFException();
		duration = new Duration(Math.round(audioInputStream.getFrameLength()
				* 1000.0f / audioFormat.getSampleRate()));
		waveforms = new AudioStreamWaveformBuilder(audioFormat, audioData)
				.getWaveforms();
		audioInputStream.close();
	}

	private byte[] loadFile(File file) throws IOException {
		if (file.length() > (long) Integer.MAX_VALUE)
			throw new IOException("File '" + file + "' too large: "
					+ file.length() + " bytes.");
		InputStream is = new FileInputStream(file);
		byte[] retval = new byte[(int) file.length()];
		int read = is.read(retval);
		is.close();
		if (read != retval.length)
			throw new EOFException();
		return retval;
	}

	@Override
	public CueAudio createCueAudio(AudioOutput audioOutput,
			AudioParams audioParams, VolumeManager volumeManager) {
		return new ClipCueAudio(audioFormat, audioData, audioOutput,
				audioParams, volumeManager);
	}

	@Override
	public Duration getDuration() {
		return duration;
	}

	@Override
	public List<AudioWaveform> getWaveforms() {
		return waveforms;
	}

	private Object readResolve() throws IOException {
		if (audioFile != null) {
			setAudioFile(audioFile);
		}
		return this;
	}
}
