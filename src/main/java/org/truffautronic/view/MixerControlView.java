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
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.truffautronic.model.MixerControl;
import org.truffautronic.model.Scenario;

public class MixerControlView extends JPanel {
	private static final long serialVersionUID = 1L;

	private MixerControl mixerControl;

	public MixerControlView(Scenario scenario, MixerControl mixerControl) {
		super();
		this.mixerControl = mixerControl;

		// Root panel
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		// Header
		FlowLayout headerLayout = new FlowLayout();
		headerLayout.setAlignment(FlowLayout.LEADING);
		JPanel headerPanel = new JPanel(headerLayout);
		add(headerPanel, BorderLayout.NORTH);
		JLabel nameLabel = new JLabel(mixerControl.getName());
		headerPanel.add(nameLabel);
		nameLabel.setFont(ViewUtils.BIG_FONT);

		// Sliders
		JPanel sliderPanel = new JPanel();
		add(sliderPanel, BorderLayout.CENTER);
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));

		// Mix slider
		JSlider mixSlider = new JSlider(JSlider.VERTICAL, 0, 100,
				Math.round(mixerControl.getMixer() * 100.0f));
		sliderPanel.add(mixSlider);
		mixSlider.setMajorTickSpacing(50);
		mixSlider.setMinorTickSpacing(10);
		mixSlider.setPaintTicks(true);
		mixSlider.setPaintLabels(true);
		mixSlider.setFont(ViewUtils.SMALL_FONT);
		Dictionary<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		JLabel one = new JLabel(mixerControl.getName() + "1");
		one.setFont(ViewUtils.SMALL_FONT);
		labelTable.put(100, one);
		JLabel two = new JLabel(mixerControl.getName() + "2");
		two.setFont(ViewUtils.SMALL_FONT);
		labelTable.put(0, two);
		mixSlider.setLabelTable(labelTable);
		mixSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				MixerControlView.this.mixerControl.setMixer(source.getValue() / 100.0f);
			}
		});

		// Volume slider
		JSlider volSlider = new JSlider(JSlider.VERTICAL, 0, 150,
				Math.round(mixerControl.getVolume() * 100.0f));
		sliderPanel.add(volSlider);
		volSlider.setMajorTickSpacing(50);
		volSlider.setMinorTickSpacing(10);
		volSlider.setPaintTicks(true);
		volSlider.setPaintLabels(true);
		volSlider.setFont(ViewUtils.SMALL_FONT);
		volSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				MixerControlView.this.mixerControl.setVolume(source.getValue() / 100.0f);
			}
		});
	}
}
