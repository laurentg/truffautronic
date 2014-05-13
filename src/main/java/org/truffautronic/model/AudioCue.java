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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.truffautronic.audio.AudioWaveform;

public class AudioCue implements Cue, CueAudio.Listener, VolumeManager.Listener {

	private final static int UPDATE_INTERVAL_MS = 200;

	public interface Listener {
		public void updatePlayPositionsMs(long[] positionsMs);
	}

	// Saved parameters
	private String name;
	private String description;
	private String color;
	private Duration start;
	private Duration end;
	private boolean loop = false;
	private VolumeManager volumeManager;
	private CueAudioFactory cueAudioFactory;
	private TimeLabelsBundle timeLabels;

	// Transient parameters
	private transient List<CueAudio> playingList = new ArrayList<CueAudio>();
	private transient Listener listener;
	private transient boolean paused;
	private transient Timer timer = new Timer();

	public AudioCue() {
		this.volumeManager = new VolumeManager();
		volumeManager.setListener("cue", this);
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public void destroy() {
		stop();
		volumeManager.destroy();
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setCueAudioFactory(CueAudioFactory cueAudioFactory) {
		this.cueAudioFactory = cueAudioFactory;
	}

	public void setAudioFile(File audioFile) {
		// TODO Make this more generic
		if (cueAudioFactory instanceof ClipCueAudioFactory) {
			ClipCueAudioFactory clipCueAudioFactory = (ClipCueAudioFactory) cueAudioFactory;
			clipCueAudioFactory.setAudioFile(audioFile);
			setStart(Duration.ZERO);
			setEnd(clipCueAudioFactory.getDuration());
		}
	}

	public void setOutputMixer(OutputMixer outputMixer) {
		volumeManager.setOutputMixer(outputMixer);
	}

	public void start(AudioParams audioParams) {
		if (paused) {
			pauseResume();
		}
		CueAudio cueAudio = cueAudioFactory.createCueAudio(volumeManager
				.getOutputMixer().getAudioOutput(), audioParams, volumeManager);
		if (cueAudio == null)
			return;
		cueAudio.setListener(this);
		cueAudio.setStartMs(start.getMs());
		cueAudio.setEndMs(end.getMs());
		cueAudio.setLoop(loop);
		cueAudio.start();
		synchronized (this) {
			if (playingList.isEmpty()) {
				updatePlaying();
			}
			playingList.add(cueAudio);
			System.out.println("Starting a new cue: " + name);
		}
	}

	public boolean pauseResume() {
		// Pause/resume all playing
		synchronized (this) {
			for (CueAudio cueAudio : playingList) {
				cueAudio.pauseResume(!paused);
			}
			paused = !paused;
		}
		return paused;
	}

	public void stop() {
		// Stop all playing
		synchronized (this) {
			for (CueAudio cueAudio : playingList) {
				// The cueAudio will be removed from the playing list
				// by the listener
				cueAudio.stop();
			}
			paused = false;
		}
	}

	public float getVolume() {
		return volumeManager.getCueVolume();
	}

	public void setVolume(float volume) {
		volumeManager.setCueVolume(volume);
	}

	public void setBalance(float balance) {
		volumeManager.setBalance(balance);
	}

	public Duration getStart() {
		if (start == null)
			start = Duration.ZERO;
		return start;
	}
	
	public Duration getEnd() {
		if (end == null)
			end = cueAudioFactory.getDuration();
		return end;
	}

	public boolean isLoop() {
		return loop;
	}

	public void setStart(Duration start) {
		this.start = start;
		synchronized (this) {
			for (CueAudio cueAudio : playingList) {
				cueAudio.setStartMs(start.getMs());
			}
		}
	}

	public void setEnd(Duration end) {
		this.end = end;
		synchronized (this) {
			for (CueAudio cueAudio : playingList) {
				cueAudio.setEndMs(end.getMs());
			}
		}
	}

	public VolumeManager getVolumeManager() {
		return volumeManager;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
		synchronized (this) {
			for (CueAudio cueAudio : playingList) {
				cueAudio.setLoop(loop);
			}
		}
	}

	public List<AudioWaveform> getWaveforms() {
		List<AudioWaveform> waveforms = cueAudioFactory.getWaveforms();
		return waveforms;
	}

	public Duration getTotalTime() {
		return cueAudioFactory.getDuration();
	}

	private void updatePlaying() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				synchronized (this) {
					long[] positionsMs = new long[playingList.size()];
					for (int i = 0; i < positionsMs.length; i++) {
						CueAudio audio = playingList.get(i);
						positionsMs[i] = audio.getElapsedTimeMs();
					}
					if (listener != null)
						listener.updatePlayPositionsMs(positionsMs);
					if (!playingList.isEmpty())
						updatePlaying();
				}
			}
		}, UPDATE_INTERVAL_MS);
	}

	@Override
	public void audioFinished(CueAudio cueAudio) {
		synchronized (this) {
			System.out.println("Cue finished: " + name);
			cueAudio.destroy();
			playingList.remove(cueAudio);
		}
	}

	@Override
	public void volumeChanged(VolumeManager volumeManager) {
		synchronized (this) {
			for (CueAudio cueAudio : playingList) {
				cueAudio.updateVolume();
			}
		}
	}

	private Object readResolve() throws IOException {
		if (playingList == null)
			playingList = new ArrayList<CueAudio>();
		if (timer == null)
			timer = new Timer();
		if (volumeManager == null)
			volumeManager = new VolumeManager();
		volumeManager.setListener("cue", this);
		return this;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getAudioDescription() {
		return cueAudioFactory.getAudioDescription();
	}
	
	public TimeLabelsBundle getTimeLabels() {
		if (timeLabels == null)
			timeLabels = new TimeLabelsBundle();
		return timeLabels;
	}
}
