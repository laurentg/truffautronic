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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.truffautronic.model.VolumeManager;

public class LowLatencyClip implements Clip, LineListener {

	private SourceDataLine dataLine;
	private volatile int framePosition;
	private volatile int flushedFrames;
	private byte[] audioData;
	private int audioDataOffset;
	private int audioDataLen;
	private Thread thread;
	private volatile boolean active;
	private AudioFormat format;
	private Mixer mixer;
	private int bufferMs;
	private int frameStart;
	private int frameEnd;
	private boolean loop = false;
	private VolumeManager volumeManager;

	public LowLatencyClip(Mixer mixer, VolumeManager volumeManager, int bufferMs) {
		this.mixer = mixer;
		this.volumeManager = volumeManager;
		this.bufferMs = bufferMs;
	}

	private long framesToMs(int frames) {
		return Math.round(frames * 1000.0 / format.getSampleRate());
	}

	private int msToFrames(long ms) {
		return Math.round(ms * format.getSampleRate() / 1000.0f);
	}

	@Override
	public void update(LineEvent le) {
	}

	@Override
	public void loop(int count) {
		if (count != LOOP_CONTINUOUSLY && count != 0)
			throw new IllegalArgumentException(
					"Support 0 or LOOP_CONTINUOUSLY only.");
		loop = count == LOOP_CONTINUOUSLY;
	}

	@Override
	public void setLoopPoints(int start, int end) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMicrosecondPosition(long microseconds) {
		framePosition = msToFrames(microseconds / 1000);
	}

	@Override
	public long getMicrosecondPosition() {
		return framesToMs(getFramePosition()) * 1000;
	}

	@Override
	public long getMicrosecondLength() {
		return framesToMs(getFrameLength()) * 1000;
	}

	@Override
	public void setFramePosition(int frames) {
		framePosition = frames;
	}

	@Override
	public int getFramePosition() {
		return framePosition;
	}

	@Override
	public int getFrameLength() {
		return audioData.length / format.getFrameSize();
	}

	@Override
	public void open(AudioInputStream stream) throws IOException,
			LineUnavailableException {
		format = stream.getFormat();
		long frameLen = stream.getFrameLength();
		long byteLen = frameLen * format.getFrameSize();
		byte[] data = new byte[(int) byteLen];
		int byteRead = stream.read(data);
		if (byteRead != byteLen)
			throw new EOFException();
		init(format, data, 0, data.length);
		stream.close();
	}

	@Override
	public void open(AudioFormat format, byte[] data, int offset, int len)
			throws LineUnavailableException {
		init(format, data, offset, len);
	}

	private void init(AudioFormat format, byte[] data, int offset,
			int bufferSize) throws LineUnavailableException {

		if (!format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
				|| format.getSampleSizeInBits() != 16 || format.isBigEndian()) {
			throw new IllegalArgumentException(
					"Format should be 16 bits PCM signed, little endian!");
		}

		this.format = format;
		audioData = data;
		audioDataOffset = offset;
		audioDataLen = bufferSize;

		frameStart = 0;
		framePosition = frameStart;
		frameEnd = audioDataLen / format.getFrameSize();
		Line.Info info = new Line.Info(SourceDataLine.class);
		dataLine = (SourceDataLine) mixer.getLine(info);
		int bufSize = Math.round(bufferMs * format.getFrameRate()
				* format.getFrameSize() / 1000.0f);
		if (bufSize > audioDataLen)
			bufSize = audioDataLen;
		bufSize = bufSize / format.getFrameSize() * format.getFrameSize();
		dataLine.open(format, bufSize);
	}

	public void setStartMs(long startMs) {
		int frameStart = msToFrames(startMs);
		if (frameStart < 0)
			frameStart = 0;
		if (framePosition == this.frameStart)
			framePosition = frameStart;
		this.frameStart = frameStart;
	}

	public void setEndMs(long endMs) {
		int frameEnd = msToFrames(endMs);
		if (frameEnd > audioDataLen / format.getFrameSize())
			frameEnd = audioDataLen / format.getFrameSize();
		this.frameEnd = frameEnd;
	}

	@Override
	public float getLevel() {
		return dataLine.getLevel();
	}

	@Override
	public long getLongFramePosition() {
		return framePosition;
	}

	@Override
	public int available() {
		return 0;
	}

	@Override
	public int getBufferSize() {
		return dataLine.getBufferSize();
	}

	@Override
	public AudioFormat getFormat() {
		return format;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isRunning() {
		return dataLine.isRunning();
	}

	@Override
	public boolean isOpen() {
		return dataLine.isOpen();
	}

	@Override
	public void stop() {
		active = false;
		// Estimate number of flushed bytes
		int flushedBytes = dataLine.getBufferSize() - dataLine.available();
		flushedFrames = flushedBytes / format.getFrameSize();
		dataLine.flush();
		dataLine.stop();
		if (thread != null) {
			try {
				active = false;
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}

	private class ClipLoop implements Runnable {
		@Override
		public void run() {
			active = true;
			dataLine.start();
			int bufSize = dataLine.getBufferSize();
			int frameSize = format.getFrameSize();
			while (framePosition < frameEnd && active) {
				int len = audioDataLen - framePosition * frameSize;
				if (len > bufSize)
					len = bufSize;
				if (len > (frameEnd - framePosition) * frameSize)
					len = (frameEnd - framePosition) * frameSize;
				long totalLenMs = framesToMs(frameEnd - frameStart);
				long positionMs1 = framesToMs(framePosition - frameStart);
				long positionMs2 = framesToMs(framePosition + len / frameSize
						- frameStart);
				float k1 = volumeManager.getTimeDependentMultiplier(
						positionMs1, totalLenMs);
				float k2 = volumeManager.getTimeDependentMultiplier(
						positionMs2, totalLenMs);
				int offset = framePosition * frameSize + audioDataOffset;
				byte[] pcm = scalePcm(audioData, offset, len, k1, k2);
				int written = dataLine.write(pcm, 0, pcm.length);
				framePosition += written / frameSize;
				if (active && loop && framePosition >= frameEnd) {
					framePosition = frameStart;
				}
			}
			if (flushedFrames > 0) {
				framePosition -= flushedFrames;
				if (framePosition < frameStart)
					framePosition = frameStart;
				flushedFrames = 0;
			}
			active = false;
			if (framePosition >= frameEnd) {
				dataLine.drain();
				dataLine.stop();
				dataLine.close();
			} else {
				dataLine.stop();
			}
			thread = null;
		}

		private byte[] scalePcm(byte[] pcmData1, int offset, int len, float k1,
				float k2) {
			byte[] pcmData2 = new byte[len];
			ByteBuffer bb1 = ByteBuffer.wrap(pcmData1, offset, len);
			ByteBuffer bb2 = ByteBuffer.wrap(pcmData2);
			bb1.order(ByteOrder.LITTLE_ENDIAN);
			bb2.order(ByteOrder.LITTLE_ENDIAN);
			int nChannels = format.getChannels();
			int i = 0;
			int n = len * 8 / format.getSampleSizeInBits()
					/ format.getChannels();
			while (bb1.hasRemaining()) {
				for (int j = 0; j < nChannels; j++) {
					float x = i * 1.0f / n;
					float k = k1 * (1.0f - x) + k2 * x;
					short val1 = bb1.getShort();
					short val2 = (short) (val1 * k);
					bb2.putShort(val2);
				}
				i++;
			}
			return pcmData2;
		}
	}

	@Override
	public void start() {
		thread = new Thread(new ClipLoop());
		thread.setPriority(thread.getThreadGroup().getMaxPriority());
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void flush() {
		dataLine.flush();
	}

	@Override
	public void drain() {
		dataLine.drain();
	}

	@Override
	public void removeLineListener(LineListener listener) {
		dataLine.removeLineListener(listener);
	}

	@Override
	public void addLineListener(LineListener listener) {
		dataLine.addLineListener(listener);
	}

	@Override
	public Control getControl(Control.Type control) {
		return dataLine.getControl(control);
	}

	@Override
	public Control[] getControls() {
		return dataLine.getControls();
	}

	@Override
	public boolean isControlSupported(Control.Type control) {
		return dataLine.isControlSupported(control);
	}

	@Override
	public void close() {
		dataLine.close();
	}

	@Override
	public void open() throws LineUnavailableException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Line.Info getLineInfo() {
		return dataLine.getLineInfo();
	}
}