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

public abstract class OutputMixer implements Comparable<OutputMixer> {

	public interface Listener {
		public void volumeChanged(OutputMixer outputMixer);
	}

	// Saved parameters
	private String name;
	private AudioOutput audioOutput;

	// Transient parameters
	private transient List<Listener> listeners = new ArrayList<Listener>();

	public OutputMixer() {
	}

	public OutputMixer(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract float getVolume();

	protected void updateVolume() {
		for (Listener listener : listeners) {
			listener.volumeChanged(this);
		}
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	public AudioOutput getAudioOutput() {
		return audioOutput;
	}

	public void setAudioOutput(AudioOutput audioOutput) {
		this.audioOutput = audioOutput;
	}

	@Override
	public int compareTo(OutputMixer o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public String toString() {
		return name;
	}
}
