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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.truffautronic.controller.I18N;

public class ColorDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private Color color;

	public ColorDialog(Frame frame, Color color) {
		super(frame);
		this.color = color;
		setTitle(I18N.translate("dialog.title.color_picker"));

		final JColorChooser colorChooser = new JColorChooser(color);
		add(colorChooser, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		add(buttonsPanel, BorderLayout.SOUTH);
		JButton okButton = new JButton(I18N.translate("button.ok"));
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ColorDialog.this.color = colorChooser.getColor();
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

	public Color getColor() {
		return color;
	}
}
