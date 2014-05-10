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
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.truffautronic.controller.I18N;

public class SplashDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public SplashDialog(Frame frame) {
		super(frame);
		setTitle(I18N.translate("application.name"));

		// Image
		add(new JLabel(ViewUtils.loadIcon("truffautronic.png")),
				BorderLayout.CENTER);

		// Info and close button
		JPanel infoPanel = new JPanel();
		add(infoPanel, BorderLayout.SOUTH);
		JLabel copyright = new JLabel(I18N.translate("dialog.splash.infos"));
		infoPanel.add(copyright, BorderLayout.CENTER);
		JButton closeButton = new JButton(I18N.translate("button.ok"));
		infoPanel.add(closeButton, BorderLayout.EAST);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SplashDialog.this.setVisible(false);
			}
		});

		// Dialog
		setResizable(false);
		setModal(true);
		pack();
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2
				- getWidth() / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height) / 2
						- getHeight() / 2);
	}
}
