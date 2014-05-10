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

public interface CueAudio {

	public interface Listener {

		public void audioFinished(CueAudio cueAudio);
	}

	public void setListener(Listener listener);

	public void start();

	public void pauseResume(boolean pause);

	public void stop();

	public void destroy();

	public void updateVolume();

	public void setStartMs(long startMs);

	public void setEndMs(long endMs);

	public void setLoop(boolean loop);

	public long getElapsedTimeMs();
}
