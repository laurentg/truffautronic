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

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneLayout;

import org.truffautronic.model.AudioCue;
import org.truffautronic.model.Cue;
import org.truffautronic.model.Scenario;
import org.truffautronic.model.Scene;

public class CueListView extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	private Scenario scenario;

	public CueListView(Scenario scenario) {
		this.scenario = scenario;
		rebuildTabs();
	}

	public void rebuildTabs() {
		while (getTabCount() > 0)
			removeTabAt(0);
		for (Scene scene : scenario.getScenes()) {
			addTab(scene.getName(), buildTab(scene));
		}
	}

	private Component buildTab(Scene scene) {
		JScrollPane scrollPane = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setLayout(new ScrollPaneLayout());
		JPanel listPanel = new JPanel();
		scrollPane.setViewportView(listPanel);
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

		for (Cue cue : scene.getCues()) {
			// TODO Use a factory
			if (cue instanceof AudioCue) {
				CueView cueView = new CueView(scenario, (AudioCue) cue, this);
				listPanel.add(cueView);
			} else {
				// Should not happen
				throw new RuntimeException("Unknown cue type: "
						+ cue.getClass());
			}
		}
		return scrollPane;
	}

	public Scene getCurrentScene() {
		int tabIndex = getSelectedIndex();
		if (tabIndex < 0)
			return null;
		return scenario.getScenes().get(tabIndex);
	}

	private int getSceneTabIndex(Scene scene) {
		return scenario.getScenes().indexOf(scene);
	}

	public void rename(Scene scene) {
		setTitleAt(getSceneTabIndex(scene), scene.getName());
	}

	public void refresh(Scene scene) {
		setComponentAt(getSceneTabIndex(scene), buildTab(scene));
	}

	public void newScene(Scene scene) {
		addTab(scene.getName(), buildTab(scene));
	}

	public void removeScene(Scene scene) {
		removeTabAt(getSceneTabIndex(scene));
	}

	public void deleteCue(Cue cue) {
		Scene scene = scenario.getCueScene(cue);
		if (scene != null) {
			scene.removeCue(cue);
			refresh(scene);
		}
	}

	public void moveCue(Cue cue, Scene newScene) {
		Scene oldScene = scenario.getCueScene(cue);
		oldScene.removeCue(cue);
		newScene.addCue(cue);
		refresh(oldScene);
		refresh(newScene);
	}
}
