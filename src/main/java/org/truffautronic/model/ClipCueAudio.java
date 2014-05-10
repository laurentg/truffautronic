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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import org.truffautronic.audio.LowLatencyClip;

public class ClipCueAudio implements CueAudio {

	private Listener listener;
	private LowLatencyClip clip;
	private FloatControl volControl;
	private FloatControl panControl;
	private VolumeManager volumeManager;
	private long startMs, endMs;

	public ClipCueAudio(AudioFormat audioFormat, byte[] audioData,
			AudioOutput audioOutput, AudioParams audioParams,
			VolumeManager volumeManager) {
		try {
			clip = new LowLatencyClip(audioOutput.getMixer(), volumeManager,
					audioParams.getBufferMs());
			this.volumeManager = volumeManager;
			clip.open(audioFormat, audioData, 0, audioData.length);
			startMs = clipPositionMs(0);
			endMs = clipPositionMs(Integer.MAX_VALUE);
			volControl = (FloatControl) clip
					.getControl(FloatControl.Type.MASTER_GAIN);
			panControl = null;
			if (clip.isControlSupported(FloatControl.Type.PAN)) {
				panControl = (FloatControl) clip
						.getControl(FloatControl.Type.PAN);
			} else if (clip.isControlSupported(FloatControl.Type.BALANCE)) {
				panControl = (FloatControl) clip
						.getControl(FloatControl.Type.BALANCE);
			}
			clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					if (event.getType() == LineEvent.Type.CLOSE) {
						listener.audioFinished(ClipCueAudio.this);
					}
				}
			});
			updateVolume();
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	@Override
	public void start() {
		synchronized (this) {
			clip.start();
		}
	}

	@Override
	public void pauseResume(boolean pause) {
		synchronized (this) {
			if (pause) {
				if (clip.isRunning())
					clip.stop();
			} else {
				if (!clip.isRunning())
					clip.start();
			}
		}
	}

	@Override
	public void stop() {
		synchronized (this) {
			if (clip.isRunning())
				clip.stop();
			clip.close();
		}
	}

	@Override
	public void destroy() {
		synchronized (this) {
			if (clip.isRunning())
				clip.stop();
			clip.flush();
			clip.close();
		}
	}

	@Override
	public void updateVolume() {
		float volumeMultiplier = volumeManager.getEffectiveVolume();
		if (volumeMultiplier < 0.001f)
			volumeMultiplier = 0.001f; // Log(0) is -inf
		float gaindB = (float) (20.0f * Math.log(volumeMultiplier));
		float minGain = volControl.getMinimum();
		float maxGain = volControl.getMaximum();
		if (gaindB < minGain)
			gaindB = minGain;
		if (gaindB > maxGain)
			gaindB = maxGain;
		synchronized (volControl) {
			volControl.setValue(gaindB);
		}
		if (panControl != null) {
			float pan = volumeManager.getBalance();
			// Assume pan is symetrical
			float maxPan = panControl.getMaximum();
			synchronized (panControl) {
				panControl.setValue(pan * maxPan);
			}
		}
	}

	@Override
	public long getElapsedTimeMs() {
		return clip.getMicrosecondPosition() / 1000;
	}

	@Override
	public void setStartMs(long startMs) {
		this.startMs = clipPositionMs(startMs);
		clip.setStartMs(this.startMs);
	}

	@Override
	public void setEndMs(long endMs) {
		this.endMs = clipPositionMs(endMs);
		clip.setEndMs(this.endMs);
	}

	private long clipPositionMs(long posMs) {
		long maxMs = clip.getMicrosecondLength() / 1000L;
		if (posMs > maxMs) {
			posMs = maxMs;
		}
		if (posMs < 0)
			posMs = 0;
		return posMs;
	}

	@Override
	public void setLoop(boolean loop) {
		clip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
	}
}
