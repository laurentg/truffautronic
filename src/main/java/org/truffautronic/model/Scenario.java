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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class Scenario implements Serializable {
	private static final long serialVersionUID = 1L;

	private transient List<AudioOutput> audioOutputs = new ArrayList<AudioOutput>();
	private transient OutputMixer defaultOutputMixer;
	private transient Map<String, OutputMixer> outputMixers = new HashMap<String, OutputMixer>();
	private transient List<MixerControl> mixerControls = new ArrayList<MixerControl>();
	private AudioParams audioParams = new AudioParams();
	private List<Scene> scenes = new ArrayList<Scene>();

	public Scenario() {
		// Get audio outputs
		for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
			Mixer mixer = AudioSystem.getMixer(mixerInfo);
			AudioOutput audioOutput = new AudioOutput();
			audioOutput.setMixer(mixer);
			audioOutputs.add(audioOutput);
		}
		if (audioOutputs.size() == 0)
			throw new RuntimeException("No mixer available on this system!");

		// TODO Set this parametrable
		MixerControl A = new MixerControl("A");
		A.setAudioOutput(audioOutputs.get(0));
		mixerControls.add(A);
		MixerControl B = new MixerControl("B");
		B.setAudioOutput(audioOutputs.get(0));
		mixerControls.add(B);
		MixerControl C = new MixerControl("C");
		C.setAudioOutput(audioOutputs.get(0));
		mixerControls.add(C);
		// Create volume mixer from controls
		for (MixerControl mixerControl : mixerControls) {
			for (OutputMixer outputMixer : mixerControl.getOutputMixers()) {
				outputMixers.put(outputMixer.getName(), outputMixer);
				if (defaultOutputMixer == null)
					defaultOutputMixer = outputMixer;
			}
		}
	}

	public OutputMixer getOutputMixer(String mixerName) {
		OutputMixer mixer = outputMixers.get(mixerName);
		if (mixer != null)
			return mixer;
		return defaultOutputMixer;
	}

	public OutputMixer getDefaultOutputMixer() {
		return defaultOutputMixer;
	}

	public List<OutputMixer> getOutputMixers() {
		List<OutputMixer> retval = new ArrayList<OutputMixer>(
				outputMixers.values());
		Collections.sort(retval);
		return retval;
	}

	public List<MixerControl> getMixerControls() {
		return mixerControls;
	}

	public void addScene(Scene scene) {
		scenes.add(scene);
	}

	public void removeScene(Scene scene) {
		scenes.remove(scene);
	}

	public Scene getCueScene(Cue cue) {
		// This is not really efficient, use with parsimony
		for (Scene scene : getScenes()) {
			if (scene.getCues().contains(cue))
				return scene;
		}
		return null;
	}

	public List<Scene> getScenes() {
		return Collections.unmodifiableList(scenes);
	}

	public void setScenes(List<Scene> scenes) {
		this.scenes = scenes;
	}

	public List<AudioOutput> getAudioOutputs() {
		return Collections.unmodifiableList(audioOutputs);
	}

	public AudioParams getAudioParams() {
		return audioParams;
	}

	public void setAudioParams(AudioParams audioParams) {
		this.audioParams = audioParams;
	}

	private Object readResolve() throws IOException {
		if (audioParams == null)
			audioParams = new AudioParams();
		return this;
	}
}
