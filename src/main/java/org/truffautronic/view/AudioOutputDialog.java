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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.truffautronic.controller.I18N;
import org.truffautronic.model.AudioOutput;
import org.truffautronic.model.MixerControl;
import org.truffautronic.model.Scenario;

public class AudioOutputDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private List<MixerEditPanel> mixerEdits = new ArrayList<MixerEditPanel>();

	public AudioOutputDialog(final Scenario scenario, Frame frame) {
		super(frame);
		setTitle(I18N.translate("dialog.title.audio_output"));

		JPanel contentPanel = new JPanel();
		add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		// Audio buffer
		JPanel miscPanel = new JPanel(new FlowLayout());
		contentPanel.add(miscPanel);
		Integer[] latencies = new Integer[] { 50, 70, 100, 120, 140, 160, 180,
				200, 250, 300, 400, 500, 700, 1000 };
		JLabel latencyLabel = new JLabel(I18N.translate("dialog.buffer_ms"));
		miscPanel.add(latencyLabel);
		final JComboBox<Integer> latencyComboBox = new JComboBox<Integer>(
				latencies);
		latencyLabel.setLabelFor(latencyComboBox);
		miscPanel.add(latencyComboBox);
		miscPanel.add(new JLabel("ms"));
		latencyComboBox
				.setSelectedItem(scenario.getAudioParams().getBufferMs());
		// Mixer control parameters
		for (MixerControl mixerControl : scenario.getMixerControls()) {
			MixerEditPanel mixerEdit = new MixerEditPanel(scenario,
					mixerControl);
			mixerEdits.add(mixerEdit);
			contentPanel.add(mixerEdit);
		}

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		add(buttonsPanel, BorderLayout.SOUTH);
		JButton okButton = new JButton(I18N.translate("button.ok"));
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (MixerEditPanel mixerEdit : mixerEdits) {
					mixerEdit.commit();
				}
				scenario.getAudioParams().setBufferMs(
						(Integer) latencyComboBox.getSelectedItem());
				setVisible(false);
			}
		});
		JButton cancelButton = new JButton(I18N.translate("button.cancel"));
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		setResizable(false);
		setModal(true);
		pack();
	}

	private class MixerEditPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private MixerControl mixerControl;
		private JComboBox<AudioOutput> audioOutCombo;

		public MixerEditPanel(Scenario scenario, MixerControl mixerControl) {
			this.mixerControl = mixerControl;
			add(new JLabel(mixerControl.getName()));
			audioOutCombo = new JComboBox<AudioOutput>(scenario
					.getAudioOutputs().toArray(new AudioOutput[0]));
			add(audioOutCombo);
			audioOutCombo.setSelectedItem(mixerControl.getAudioOutput());
		}

		public void commit() {
			mixerControl.setAudioOutput((AudioOutput) audioOutCombo
					.getSelectedItem());
		}

	}

}
