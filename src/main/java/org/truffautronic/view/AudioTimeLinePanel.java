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

package org.truffautronic.view;

import java.awt.GridLayout;

import javax.swing.JPanel;

public class AudioTimeLinePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private TimeCursorPanel cursorPanel;
	private WaveformPanel waveformPanel;

	public AudioTimeLinePanel(CueView cueView) {
		setLayout(new GridLayout(0, 1));
		cursorPanel = new TimeCursorPanel(cueView);
		waveformPanel = new WaveformPanel();
		add(waveformPanel);
		add(cursorPanel);
	}

	public WaveformPanel getWaveformPanel() {
		return waveformPanel;
	}

	public TimeCursorPanel getCursorPanel() {
		return cursorPanel;
	}
}
