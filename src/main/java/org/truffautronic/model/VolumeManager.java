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
import java.util.HashMap;
import java.util.Map;

public class VolumeManager implements OutputMixer.Listener {

	public interface Listener {
		public void volumeChanged(VolumeManager volumeManager);
	}

	// Saved parameters
	private OutputMixer outputMixer;
	private Duration fadeInDuration;
	private Duration fadeOutDuration;
	private float volume = 1.0f;
	private float balance = 0.0f;

	// Transient parameters
	private transient Map<String, Listener> listeners;

	public VolumeManager() {
	}

	public void setListener(String key, Listener listener) {
		if (listeners == null)
			listeners = new HashMap<String, Listener>();
		listeners.put(key, listener);
	}

	public void removeListener(String key) {
		listeners.remove(key);
	}

	public void setOutputMixer(OutputMixer outputMixer) {
		if (this.outputMixer != null)
			this.outputMixer.removeListener(this);
		this.outputMixer = outputMixer;
		this.outputMixer.addListener(this);
		signalListeners();
	}

	public OutputMixer getOutputMixer() {
		return outputMixer;
	}

	public void setFadeInDuration(Duration fadeInDuration) {
		this.fadeInDuration = fadeInDuration;
	}

	public Duration getFadeInDuration() {
		if (fadeInDuration == null)
			return Duration.ZERO;
		return fadeInDuration;
	}

	public void setFadeOutDuration(Duration fadeOutDuration) {
		this.fadeOutDuration = fadeOutDuration;
	}

	public Duration getFadeOutDuration() {
		if (fadeOutDuration == null)
			return Duration.ZERO;
		return fadeOutDuration;
	}

	public void setCueVolume(float cueVolume) {
		if (cueVolume > 2.0f)
			cueVolume = 2.0f;
		if (cueVolume < 0.0f)
			cueVolume = 0.0f;
		this.volume = cueVolume;
		signalListeners();
	}

	public float getCueVolume() {
		return volume;
	}

	public float getEffectiveVolume() {
		return outputMixer.getVolume() * volume;
	}

	public float getTimeDependentMultiplier(long positionMs, long totalLenMs) {
		float k = 1.0f;
		if (getFadeInDuration().getMs() > 0
				&& positionMs <= getFadeInDuration().getMs()) {
			k = positionMs * 1.0f / fadeInDuration.getMs();
		}
		if (getFadeOutDuration().getMs() > 0
				&& positionMs >= totalLenMs - getFadeOutDuration().getMs()) {
			float k2 = (totalLenMs - positionMs) * 1.0f
					/ getFadeOutDuration().getMs();
			if (k2 < k)
				k = k2;
		}
		return k;
	}

	public void setBalance(float balance) {
		if (balance > 1.0f)
			balance = 1.0f;
		if (balance < -1.0f)
			balance = -1.0f;
		this.balance = balance;
	}

	public float getBalance() {
		return balance;
	}

	public void destroy() {
		if (outputMixer != null)
			outputMixer.removeListener(this);
	}

	@Override
	public void volumeChanged(OutputMixer outputMixer) {
		signalListeners();
	}

	private void signalListeners() {
		if (listeners != null) {
			for (Listener listener : listeners.values()) {
				listener.volumeChanged(this);
			}
		}
	}

	private Object readResolve() throws IOException {
		setOutputMixer(outputMixer);
		return this;
	}
}
