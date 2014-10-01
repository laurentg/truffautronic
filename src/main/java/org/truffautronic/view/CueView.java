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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.truffautronic.controller.I18N;
import org.truffautronic.model.AudioCue;
import org.truffautronic.model.Duration;
import org.truffautronic.model.OutputMixer;
import org.truffautronic.model.Scenario;
import org.truffautronic.model.Scene;
import org.truffautronic.model.VolumeManager;
import org.truffautronic.model.VolumeManager.Listener;

public class CueView extends JPanel implements AudioCue.Listener {
	private static final long serialVersionUID = 1L;

	private Scenario scenario;
	private AudioCue cue;
	private AudioTimeLinePanel timePanel;
	private JPanel headerPanel;
	private JLabel nameLabel;
	private JLabel descLabel;
	private JLabel audioDescLabel;
	private CueListView cueListView;

	public CueView(Scenario scenario, AudioCue aCue, CueListView cueListView) {
		super(new BorderLayout());

		this.scenario = scenario;
		this.cue = aCue;
		this.cue.setListener(this);
		this.cueListView = cueListView;

		// Root panel
		setMinimumSize(new Dimension(0, 160));
		setPreferredSize(new Dimension(-1, 160));
		setMaximumSize(new Dimension(Short.MAX_VALUE, 160));
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		// Header
		FlowLayout headerLayout = new FlowLayout();
		headerLayout.setVgap(0);
		headerLayout.setAlignment(FlowLayout.LEADING);
		headerPanel = new JPanel(headerLayout);
		add(headerPanel, BorderLayout.NORTH);
		if (cue.getColor() != null) {
			try {
				headerPanel.setBackground(new Color(Integer.parseInt(
						cue.getColor(), 16)));
			} catch (NumberFormatException e) {
				cue.setColor(null);
			}
		}
		nameLabel = new JLabel(cue.getName());
		headerPanel.add(nameLabel);
		nameLabel.setFont(ViewUtils.BIG_FONT);
		descLabel = new JLabel(cue.getDescription());
		headerPanel.add(descLabel);
		descLabel.setFont(ViewUtils.NORMAL_FONT);
		audioDescLabel = new JLabel(cue.getAudioDescription());
		headerPanel.add(Box.createHorizontalGlue());
		headerPanel.add(audioDescLabel);
		audioDescLabel.setFont(ViewUtils.NORMAL_FONT);

		// Mouse listener for popup
		headerPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				displayMenu(e.getComponent(), e.getX(), e.getY());
			}
		});

		// Left panel
		JPanel leftPanel = new JPanel(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		leftPanel.add(Box.createHorizontalStrut(5), BorderLayout.CENTER);

		// Sliders panel
		JPanel sliderPanel = new JPanel();
		leftPanel.add(sliderPanel, BorderLayout.EAST);
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));

		// Volume control
		JSlider volumeControl = new JSlider(JSlider.VERTICAL, 0, 150,
				Math.round(cue.getVolumeManager().getCueVolume() * 100.0f));
		sliderPanel.add(volumeControl);
		volumeControl.setMajorTickSpacing(50);
		volumeControl.setMinorTickSpacing(10);
		volumeControl.setPaintTicks(true);
		volumeControl.setPaintLabels(true);
		volumeControl.setFont(ViewUtils.SMALL_FONT);
		volumeControl.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				cue.setVolume(source.getValue() / 100.0f);
			}
		});

		// Pan (balance) control
		JSlider balanceControl = new JSlider(JSlider.VERTICAL, -100, 100,
				Math.round(cue.getVolumeManager().getBalance() * 100.0f));
		sliderPanel.add(balanceControl);
		balanceControl.setMajorTickSpacing(50);
		balanceControl.setMinorTickSpacing(10);
		balanceControl.setPaintTicks(true);
		balanceControl.setPaintLabels(true);
		balanceControl.setFont(ViewUtils.SMALL_FONT);
		Dictionary<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		JLabel left = new JLabel("L");
		left.setFont(ViewUtils.SMALL_FONT);
		labelTable.put(-100, left);
		JLabel center = new JLabel("0");
		center.setFont(ViewUtils.SMALL_FONT);
		labelTable.put(0, center);
		JLabel right = new JLabel("R");
		right.setFont(ViewUtils.SMALL_FONT);
		labelTable.put(100, right);
		balanceControl.setLabelTable(labelTable);
		balanceControl.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				cue.setBalance(source.getValue() / 100.0f);
			}
		});

		// Volume meter
		final VolumeMeter volMeter = new VolumeMeter();
		volMeter.setValue(cue.getVolumeManager().getEffectiveVolume());
		sliderPanel.add(volMeter);
		cue.getVolumeManager().setListener("view", new Listener() {
			@Override
			public void volumeChanged(VolumeManager volumeManager) {
				volMeter.setValue(cue.getVolumeManager().getEffectiveVolume());
			}
		});

		// Control panel
		JPanel controlPanel = new JPanel();
		leftPanel.add(controlPanel, BorderLayout.WEST);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		// Buttons row panel
		JPanel buttonsPanel = new JPanel();
		controlPanel.add(buttonsPanel, BorderLayout.WEST);
		FlowLayout buttonsLayout = new FlowLayout(FlowLayout.LEFT);
		buttonsPanel.setLayout(buttonsLayout);
		buttonsLayout.setHgap(0);
		Insets buttonInsets = new Insets(0, 4, 0, 4);
		// Pause/resum
		final JToggleButton pauseResumeButton = new JToggleButton(
				ViewUtils.loadIcon("24x24/pause.png"));
		pauseResumeButton.setMargin(buttonInsets);
		pauseResumeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pauseResumeButton.setSelected(cue.pauseResume());
			}
		});
		// Start button
		JButton startButton = new JButton(ViewUtils.loadIcon("24x24/start.png"));
		startButton.setMargin(buttonInsets);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pauseResumeButton.setSelected(false);
				cue.start(CueView.this.scenario.getAudioParams());
			}
		});
		// Stop button
		JButton stopButton = new JButton(ViewUtils.loadIcon("24x24/stop.png"));
		stopButton.setMargin(buttonInsets);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pauseResumeButton.setSelected(false);
				cue.stop();
			}
		});
		// Loop button
		final JToggleButton loopButton = new JToggleButton(
				ViewUtils.loadIcon("24x24/repeat.png"));
		loopButton.setMargin(buttonInsets);
		loopButton.setSelected(cue.isLoop());
		loopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cue.setLoop(loopButton.isSelected());
			}
		});

		// Add buttons, in order
		buttonsPanel.add(startButton);
		buttonsPanel.add(pauseResumeButton);
		buttonsPanel.add(stopButton);
		buttonsPanel.add(loopButton);

		// Output selector
		JPanel outputPanel = new JPanel();
		controlPanel.add(outputPanel);
		outputPanel.setLayout(new FlowLayout());
		JLabel mixerLabel = new JLabel("MX");
		mixerLabel.setFont(ViewUtils.NORMAL_FONT);
		outputPanel.add(mixerLabel);
		final JComboBox<OutputMixer> mixerCombo = new JComboBox<OutputMixer>(
				scenario.getOutputMixers().toArray(new OutputMixer[0]));
		mixerCombo.setFont(ViewUtils.BIG_FONT);
		outputPanel.add(mixerCombo);
		mixerCombo.setSelectedItem(cue.getVolumeManager().getOutputMixer());
		mixerCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VolumeManager volManager = cue.getVolumeManager();
				volManager.setOutputMixer((OutputMixer) mixerCombo
						.getSelectedItem());
				volMeter.setValue(volManager.getEffectiveVolume());
			}
		});

		// Wave form, cursor
		timePanel = new AudioTimeLinePanel(this);
		add(timePanel, BorderLayout.CENTER);
		initTimePanel();
	}

	private void initTimePanel() {
		Duration duration = cue.getTotalTime();
		WaveformPanel waveformPanel = timePanel.getWaveformPanel();
		waveformPanel.setWaveforms(cue.getWaveforms());
		waveformPanel.setTimeLabels(cue.getTimeLabels());
		waveformPanel.setTotalTimeMs(duration == null ? 0 : duration.getMs());
		TimeCursorPanel timeCursor = timePanel.getCursorPanel();
		timeCursor.setTotalTimeMs(duration == null ? 0 : duration.getMs());
		timeCursor.setRangeMs(cue.getStart().getMs(), cue.getEnd().getMs());
		timeCursor.setFadeMs(
				cue.getVolumeManager().getFadeInDuration().getMs(), cue
						.getVolumeManager().getFadeOutDuration().getMs());
	}

	@Override
	public void audioStarted() {
		cueListView.updatePlaying(scenario.getCueScene(cue));
	}

	@Override
	public void audioFinished() {
		cueListView.updatePlaying(scenario.getCueScene(cue));
	}

	@Override
	public void updatePlayPositionsMs(long[] positionsMs) {
		timePanel.getCursorPanel().setCursorsMs(positionsMs);
	}

	public void setRangeMs(long startMs, long endMs) {
		cue.setStart(new Duration(startMs));
		cue.setEnd(new Duration(endMs));
	}

	public void setFadeInOutMs(long fadeInMs, long fadeOutMs) {
		cue.getVolumeManager().setFadeInDuration(new Duration(fadeInMs));
		cue.getVolumeManager().setFadeOutDuration(new Duration(fadeOutMs));
	}

	public AudioCue getAudioCue() {
		return cue;
	}

	private void displayMenu(Component invoker, int x, int y) {
		final JPopupMenu popupMenu = new JPopupMenu();
		// Rename cue
		JMenuItem editCueNameMenuItem = new JMenuItem(
				I18N.translate("menu.cue.editname"),
				ViewUtils.loadIcon("16x16/rename.png"));
		editCueNameMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newName = JOptionPane.showInputDialog(CueView.this,
						I18N.translate("dialog.cue.newname"), cue.getName());
				if (newName != null) {
					cue.setName(newName);
					nameLabel.setText(newName);
				}
			}
		});
		popupMenu.add(editCueNameMenuItem);
		// Edit description
		JMenuItem editCueDescMenuItem = new JMenuItem(
				I18N.translate("menu.cue.editdesc"),
				ViewUtils.loadIcon("16x16/rename.png"));
		editCueDescMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newDesc = JOptionPane.showInputDialog(CueView.this,
						I18N.translate("dialog.cue.newdesc"),
						cue.getDescription());
				if (newDesc != null) {
					cue.setDescription(newDesc);
					descLabel.setText(newDesc);
				}
			}
		});
		popupMenu.add(editCueDescMenuItem);
		// Load audio file
		JMenuItem loadAudioFileMenuItem = new JMenuItem(
				I18N.translate("menu.cue.loadaudio"),
				ViewUtils.loadIcon("16x16/audio.png"));
		loadAudioFileMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				File cwd = Paths.get("").toAbsolutePath().toFile();
				fileChooser.setCurrentDirectory(cwd);
				int retval = fileChooser.showOpenDialog(CueView.this);
				if (retval == JFileChooser.APPROVE_OPTION) {
					cue.stop();
					cue.setAudioFile(fileChooser.getSelectedFile());
					audioDescLabel.setText(cue.getAudioDescription());
					initTimePanel();
				}
			}
		});
		popupMenu.add(loadAudioFileMenuItem);
		// Change color
		JMenuItem colorFileMenuItem = new JMenuItem(
				I18N.translate("menu.cue.color"),
				ViewUtils.loadIcon("16x16/color.png"));
		colorFileMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = Color.WHITE;
				try {
					color = new Color(Integer.parseInt(cue.getColor(), 16));
				} catch (NumberFormatException ex) {
				}
				ColorDialog colorDialog = new ColorDialog(null, color);
				colorDialog.setVisible(true);
				color = colorDialog.getColor();
				cue.setColor(Integer.toHexString(color.getRGB() & 0x00FFFFFF));
				headerPanel.setBackground(color);
			}
		});
		popupMenu.add(colorFileMenuItem);
		// Move cue to another scene
		JMenu moveMenuItem = new JMenu(I18N.translate("menu.cue.move"));
		moveMenuItem.setIcon(ViewUtils.loadIcon("16x16/move.png"));
		boolean moveOk = false;
		final Scene oldScene = scenario.getCueScene(cue);
		for (final Scene newScene : scenario.getScenes()) {
			if (newScene.equals(oldScene))
				continue;
			moveOk = true;
			JMenuItem moveScene = new JMenuItem(newScene.getName());
			moveMenuItem.add(moveScene);
			moveScene.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cueListView.moveCue(cue, newScene);
					cueListView.updatePlaying(oldScene);
					cueListView.updatePlaying(newScene);
				}
			});
		}
		if (moveOk)
			popupMenu.add(moveMenuItem);
		// Delete cue
		JMenuItem deleteMenuItem = new JMenuItem(
				I18N.translate("menu.cue.delete"),
				ViewUtils.loadIcon("16x16/delete.png"));
		deleteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(CueView.this,
						I18N.translate("dialog.confirm_cue_deletion"),
						I18N.translate("dialog.title.confirm"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
					cue.stop();
					cueListView.deleteCue(cue);
				}
			}
		});
		popupMenu.add(deleteMenuItem);

		popupMenu.show(invoker, x, y);
	}
}
