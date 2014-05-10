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

package org.truffautronic.controller;

import java.io.File;

import org.truffautronic.model.Scenario;
import org.truffautronic.model.Scene;
import org.truffautronic.view.MainWindow;

public class AppController implements MainWindow.Listener {

	private Scenario scenario;
	private MainWindow mainWindow;

	public AppController() {
		scenario = createEmptyScenario();
		mainWindow = new MainWindow(this);
		mainWindow.setScenario(scenario);
	}

	public void run() {
	}

	@Override
	public void newProject() {
		scenario = createEmptyScenario();
		mainWindow.setScenario(scenario);
	}

	public void openInitialFile(File file) {
		mainWindow.openFile(file);
	}
	
	@Override
	public void open(File file) {
		ScenarioIO io = new ScenarioIO();
		try {
			Scenario newScenario = io.load(file);
			scenario = newScenario;
			mainWindow.setScenario(newScenario);
		} catch (Exception e) {
			e.printStackTrace();
			showError(I18N.translate("error.cant_load", e));
		}
	}

	@Override
	public void save(File file) {
		ScenarioIO io = new ScenarioIO();
		try {
			io.save(scenario, file);
		} catch (Exception e) {
			e.printStackTrace();
			showError(I18N.translate("error.cant_save", e));
		}
	}

	@Override
	public void quit() {
		System.exit(0);
	}

	private Scenario createEmptyScenario() {
		Scenario scenario = new Scenario();
		Scene defaultScene = new Scene();
		defaultScene.setName(I18N.translate("model.default_scene_name", 1));
		scenario.addScene(defaultScene);
		return scenario;
	}

	public void showError(String message) {
		mainWindow.showError(message);
	}
}
