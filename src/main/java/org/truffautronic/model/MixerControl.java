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

import java.util.ArrayList;
import java.util.List;

public class MixerControl {

	private String name;
	private float volume;
	private float mixer;
	private AudioOutput audioOutput;
	private List<OutputMixer> outputMixers;

	public MixerControl(String name) {
		this.name = name;
		this.volume = 1.0f;
		this.mixer = 1.0f;
		outputMixers = new ArrayList<OutputMixer>();
		outputMixers.add(new OutputMixer(name) {
			@Override
			public float getVolume() {
				return MixerControl.this.volume;
			}
		});
		outputMixers.add(new OutputMixer(name + "1") {
			@Override
			public float getVolume() {
				return MixerControl.this.volume * MixerControl.this.mixer;
			}
		});
		outputMixers.add(new OutputMixer(name + "2") {
			@Override
			public float getVolume() {
				return MixerControl.this.volume
						* (1.0f - MixerControl.this.mixer);
			}
		});
	}

	public String getName() {
		return name;
	}

	public void setAudioOutput(AudioOutput audioOutput) {
		this.audioOutput = audioOutput;
		for (OutputMixer outputMixer : outputMixers) {
			outputMixer.setAudioOutput(audioOutput);
		}
	}

	public AudioOutput getAudioOutput() {
		return audioOutput;
	}

	private void updateVolume() {
		for (OutputMixer outputMixer : outputMixers) {
			outputMixer.updateVolume();
		}
	}

	public void setVolume(float volume) {
		this.volume = volume;
		updateVolume();
	}

	public float getVolume() {
		return volume;
	}

	public void setMixer(float mixer) {
		this.mixer = mixer;
		updateVolume();
	}

	public float getMixer() {
		return mixer;
	}

	public List<OutputMixer> getOutputMixers() {
		return outputMixers;
	}
}
