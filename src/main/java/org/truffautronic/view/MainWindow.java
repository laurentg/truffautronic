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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.truffautronic.controller.I18N;
import org.truffautronic.controller.ScenarioIO;
import org.truffautronic.model.AudioCue;
import org.truffautronic.model.ClipCueAudioFactory;
import org.truffautronic.model.Cue;
import org.truffautronic.model.MixerControl;
import org.truffautronic.model.Scenario;
import org.truffautronic.model.Scene;

public class MainWindow {

	public interface Listener {

		public void newProject();

		public void open(File file);

		public void save(File file);

		public void quit();
	}

	private Scenario scenario;
	private File currentFile;
	private byte[] lastSavedChecksum;
	private Listener listener;
	private JFrame rootFrame;
	private CueListView cueListView;
	private File cwd = null;

	public MainWindow(Listener listener) {
		this.listener = listener;
		rootFrame = new JFrame(I18N.translate("application.name"));
		rootFrame.setJMenuBar(buildMenuBar());
		rootFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		rootFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		rootFrame.setIconImage(ViewUtils.loadIcon("appicon.png").getImage());
		rootFrame.pack();
		rootFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		rootFrame.setVisible(true);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SplashDialog(rootFrame).setVisible(true);
			}
		});
	}

	public void setScenario(final Scenario scenario) {
		this.scenario = scenario;
		this.lastSavedChecksum = new ScenarioIO().checksum(scenario);
		rootFrame.getContentPane().removeAll();
		// Cue list
		cueListView = new CueListView(scenario);
		rootFrame.getContentPane().add(cueListView, BorderLayout.CENTER);

		// Mixer controls
		JPanel mixerPanel = new JPanel();
		rootFrame.getContentPane().add(mixerPanel, BorderLayout.WEST);
		mixerPanel.setLayout(new BoxLayout(mixerPanel, BoxLayout.Y_AXIS));
		for (MixerControl mixerControl : scenario.getMixerControls()) {
			mixerPanel.add(new MixerControlView(scenario, mixerControl));
		}

		rootFrame.pack();
	}

	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		// File menu
		JMenu fileMenu = new JMenu(I18N.translate("menu.file"));
		menuBar.add(fileMenu);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem newMenuItem = new JMenuItem(I18N.translate("menu.file.new"),
				ViewUtils.loadIcon("16x16/newproject.png"));
		newMenuItem.setMnemonic(KeyEvent.VK_N);
		fileMenu.add(newMenuItem);
		newMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newScenario();
			}
		});
		JMenuItem openMenuItem = new JMenuItem(
				I18N.translate("menu.file.open"),
				ViewUtils.loadIcon("16x16/open.png"));
		openMenuItem.setMnemonic(KeyEvent.VK_L);
		fileMenu.add(openMenuItem);
		openMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openScenario();
			}
		});
		JMenuItem saveMenuItem = new JMenuItem(
				I18N.translate("menu.file.save"),
				ViewUtils.loadIcon("16x16/save.png"));
		saveMenuItem.setMnemonic(KeyEvent.VK_S);
		fileMenu.add(saveMenuItem);
		saveMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveScenario(false);
			}
		});
		JMenuItem saveAsMenuItem = new JMenuItem(
				I18N.translate("menu.file.saveas"),
				ViewUtils.loadIcon("16x16/save.png"));
		fileMenu.add(saveAsMenuItem);
		saveAsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveScenario(true);
			}
		});
		fileMenu.add(new JSeparator());
		JMenuItem quitMenuItem = new JMenuItem(
				I18N.translate("menu.file.quit"),
				ViewUtils.loadIcon("16x16/quit.png"));
		quitMenuItem.setMnemonic(KeyEvent.VK_Q);
		fileMenu.add(quitMenuItem);
		quitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});

		// Audio menu
		JMenu audioMenu = new JMenu(I18N.translate("menu.audio"));
		menuBar.add(audioMenu);
		audioMenu.setMnemonic(KeyEvent.VK_A);
		JMenuItem mapOutputMenuItem = new JMenuItem(
				I18N.translate("menu.audio.map_output"),
				ViewUtils.loadIcon("16x16/newproject.png"));
		mapOutputMenuItem.setMnemonic(KeyEvent.VK_M);
		audioMenu.add(mapOutputMenuItem);
		mapOutputMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AudioOutputDialog(scenario, rootFrame).setVisible(true);
			}
		});

		// Scene menu
		JMenu scenesMenu = new JMenu(I18N.translate("menu.scenes"));
		menuBar.add(scenesMenu);
		scenesMenu.setMnemonic(KeyEvent.VK_S);
		JMenuItem createNewSceneMenuItem = new JMenuItem(
				I18N.translate("menu.scenes.add"),
				ViewUtils.loadIcon("16x16/newtab.png"));
		createNewSceneMenuItem.setMnemonic(KeyEvent.VK_A);
		scenesMenu.add(createNewSceneMenuItem);
		createNewSceneMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewScene();
			}
		});
		JMenuItem reorderScenesMenuItem = new JMenuItem(
				I18N.translate("menu.scenes.reorder"),
				ViewUtils.loadIcon("16x16/reorder.png"));
		reorderScenesMenuItem.setMnemonic(KeyEvent.VK_O);
		scenesMenu.add(reorderScenesMenuItem);
		reorderScenesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reorderScenes();
			}
		});
		JMenuItem renameSceneMenuItem = new JMenuItem(
				I18N.translate("menu.scenes.rename"),
				ViewUtils.loadIcon("16x16/rename.png"));
		renameSceneMenuItem.setMnemonic(KeyEvent.VK_R);
		scenesMenu.add(renameSceneMenuItem);
		renameSceneMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameScene();
			}
		});
		JMenuItem deleteSceneMenuItem = new JMenuItem(
				I18N.translate("menu.scenes.delete"),
				ViewUtils.loadIcon("16x16/delete.png"));
		deleteSceneMenuItem.setMnemonic(KeyEvent.VK_D);
		scenesMenu.add(deleteSceneMenuItem);
		deleteSceneMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteScene();
			}
		});

		// Cues menu
		JMenu cuesMenu = new JMenu(I18N.translate("menu.cues"));
		menuBar.add(cuesMenu);
		cuesMenu.setMnemonic(KeyEvent.VK_C);
		JMenuItem addNewCueMenuItem = new JMenuItem(
				I18N.translate("menu.cues.add"),
				ViewUtils.loadIcon("16x16/new.png"));
		addNewCueMenuItem.setMnemonic(KeyEvent.VK_A);
		cuesMenu.add(addNewCueMenuItem);
		addNewCueMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewCue();
			}
		});
		JMenuItem reorderCuesMenuItem = new JMenuItem(
				I18N.translate("menu.cues.reorder"),
				ViewUtils.loadIcon("16x16/reorder.png"));
		reorderCuesMenuItem.setMnemonic(KeyEvent.VK_O);
		cuesMenu.add(reorderCuesMenuItem);
		reorderCuesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reorderCues();
			}
		});

		// Help menu
		JMenu helpMenu = new JMenu(I18N.translate("menu.help"));
		menuBar.add(helpMenu);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem aboutMenuItem = new JMenuItem(
				I18N.translate("menu.help.about"),
				ViewUtils.loadIcon("16x16/about.png"));
		aboutMenuItem.setMnemonic(KeyEvent.VK_A);
		helpMenu.add(aboutMenuItem);
		aboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SplashDialog(rootFrame).setVisible(true);
			}
		});

		return menuBar;
	}

	private void newScenario() {
		if (!askForSaving())
			return;
		setCurrentFile(null);
		listener.newProject();
	}

	public void openFile(File file) {
		setCurrentFile(file);
		cwd = getParentPath(currentFile);
		listener.open(currentFile);
	}

	private void openScenario() {
		if (!askForSaving())
			return;
		JFileChooser fileChooser = new JFileChooser();
		if (cwd == null)
			cwd = getParentPath(currentFile);
		fileChooser.setCurrentDirectory(cwd);
		int retval = fileChooser.showOpenDialog(rootFrame);
		if (retval != JFileChooser.APPROVE_OPTION) {
			return;
		}
		setCurrentFile(fileChooser.getSelectedFile());
		cwd = getParentPath(currentFile);
		listener.open(currentFile);
	}

	private boolean saveScenario(boolean askFilename) {
		if (currentFile == null || askFilename) {
			JFileChooser fileChooser = new JFileChooser();
			if (cwd == null)
				cwd = getParentPath(new File("."));
			fileChooser.setCurrentDirectory(cwd);
			int retval = fileChooser.showSaveDialog(rootFrame);
			if (retval != JFileChooser.APPROVE_OPTION) {
				return false;
			}
			setCurrentFile(fileChooser.getSelectedFile());
			cwd = getParentPath(currentFile);
		}
		listener.save(currentFile);
		this.lastSavedChecksum = new ScenarioIO().checksum(scenario);
		return true;
	}

	private void quit() {
		if (!askForSaving())
			return;
		listener.quit();
	}

	private boolean askForSaving() {
		byte[] currentChecksum = new ScenarioIO().checksum(scenario);
		if (Arrays.equals(lastSavedChecksum, currentChecksum))
			return true;
		int result = JOptionPane.showConfirmDialog(rootFrame,
				I18N.translate("dialog.do_you_want_to_save"),
				I18N.translate("dialog.title.confirm"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == JOptionPane.CANCEL_OPTION)
			return false;
		if (result == JOptionPane.YES_OPTION) {
			return saveScenario(false);
		}
		return true;
	}

	private void addNewScene() {
		String sceneName = JOptionPane.showInputDialog(
				rootFrame,
				I18N.translate("dialog.new_scene_name"),
				I18N.translate("model.default_scene_name",
						(cueListView.getTabCount() + 1)));
		if (sceneName == null)
			return;
		Scene scene = new Scene();
		scene.setName(sceneName);
		scenario.addScene(scene);
		cueListView.newScene(scene);
	}

	private void deleteScene() {
		Scene scene = cueListView.getCurrentScene();
		if (scene == null) {
			return;
		}
		if (scenario.getScenes().size() == 1) {
			showError(I18N.translate("error.cant_delete_last_scene"));
			return;
		}
		if (scene.getCues().size() > 0) {
			showError(I18N.translate("error.cant_delete_non_empty_scene"));
			return;
		}
		// No confirmation, empty scene is just a name.
		cueListView.removeScene(scene);
		scenario.removeScene(scene);
	}

	private void addNewCue() {
		Scene scene = cueListView.getCurrentScene();
		if (scene == null) {
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		if (cwd == null)
			cwd = Paths.get("").toAbsolutePath().toFile();
		fileChooser.setCurrentDirectory(cwd);
		int retval = fileChooser.showOpenDialog(rootFrame);
		if (retval != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File audioFile = fileChooser.getSelectedFile();
		AudioCue audioCue = new AudioCue();
		String cueName = audioFile.getName();
		if (cueName.contains("."))
			cueName = cueName.substring(0, cueName.lastIndexOf('.'));
		audioCue.setName(cueName);
		ClipCueAudioFactory clipCueAudioFactory = new ClipCueAudioFactory();
		clipCueAudioFactory.setAudioFile(audioFile);
		if (clipCueAudioFactory.getAudioFile() == null) {
			showError(I18N.translate("error.cant_load_audiodata"));
			return;
		}
		audioCue.getVolumeManager().setOutputMixer(
				scenario.getDefaultOutputMixer());
		audioCue.setCueAudioFactory(clipCueAudioFactory);
		scene.addCue(audioCue);
		cueListView.refresh(scene);
	}

	private void reorderScenes() {
		if (scenario.getScenes().size() < 2) {
			showError(I18N.translate("error.need_two_scenes_to_reorder"));
			return;
		}
		ReorderItemsDialog<Scene> dialog = new ReorderItemsDialog<Scene>(
				I18N.translate("dialog.title.reorder_scenes"), rootFrame,
				scenario.getScenes());
		List<Scene> scenes = dialog.reorder();
		if (scenes != null) {
			scenario.setScenes(scenes);
			cueListView.rebuildTabs();
		}
	}

	private void renameScene() {
		Scene scene = cueListView.getCurrentScene();
		if (scene == null) {
			return;
		}
		String newName = JOptionPane.showInputDialog(rootFrame,
				I18N.translate("dialog.rename_scene"), scene.getName());
		if (newName != null) {
			newName = newName.trim();
			if (!newName.isEmpty()) {
				scene.setName(newName);
				cueListView.rename(scene);
			}
		}
	}

	private void reorderCues() {
		Scene scene = cueListView.getCurrentScene();
		if (scene == null) {
			return;
		}
		if (scene.getCues().size() < 2) {
			showError(I18N.translate("error.need_two_cues_to_reorder"));
			return;
		}
		ReorderItemsDialog<Cue> dialog = new ReorderItemsDialog<Cue>(
				I18N.translate("dialog.title.reorder_cues", scene.getName()),
				rootFrame, scene.getCues());
		List<Cue> cues = dialog.reorder();
		if (cues != null) {
			scene.setCues(cues);
			cueListView.refresh(scene);
		}
	}

	public void showError(String message) {
		JOptionPane
				.showMessageDialog(rootFrame, message,
						I18N.translate("dialog.title.error"),
						JOptionPane.ERROR_MESSAGE);
	}

	private File getParentPath(File file) {
		try {
			return file.getCanonicalFile().getParentFile();
		} catch (IOException e) {
			return file.getAbsoluteFile().getParentFile();
		}
	}

	private void setCurrentFile(File file) {
		this.currentFile = file;
		if (this.currentFile == null)
			rootFrame.setTitle("Truffautronic");
		else
			try {
				rootFrame.setTitle("Truffautronic - "
						+ file.getCanonicalPath().toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

}
