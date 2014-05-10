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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scene implements Comparable<Scene> {

	private String name;
	private List<Cue> cues = new ArrayList<Cue>();

	public Scene() {
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<Cue> getCues() {
		return Collections.unmodifiableList(cues);
	}

	public void setCues(List<Cue> cues) {
		this.cues = cues;
	}

	public void addCue(Cue cue) {
		cues.add(cue);
	}

	public void removeCue(Cue cue) {
		cue.destroy();
		cues.remove(cue);
	}

	@Override
	public int compareTo(Scene o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public String toString() {
		return name;
	}

	private Object readResolve() throws IOException {
		if (cues == null)
			cues = new ArrayList<Cue>();
		return this;
	}

}
