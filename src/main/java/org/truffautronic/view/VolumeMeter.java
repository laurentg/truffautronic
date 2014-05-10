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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

public class VolumeMeter extends JComponent {
	private static final long serialVersionUID = 1L;

	private float vol = 1.0f;

	public VolumeMeter() {
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		this.setPreferredSize(new Dimension(10, Short.MAX_VALUE));
	}

	public void setValue(float vol) {
		this.vol = vol;
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		int height = getHeight();

		float volGreen = vol;
		float volYellow = vol;
		if (volGreen > 1.0f)
			volGreen = 1.0f;
		if (volYellow > 1.25f)
			volYellow = 1.25f;

		int yRed = Math.round(height * vol / 1.5f);
		int yGreen = Math.round(height * volGreen / 1.5f);
		int yYellow = Math.round(height * volYellow / 1.5f);

		// Black Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height - yRed);
		// Green bar
		g.setColor(Color.GREEN);
		g.fillRect(0, height - yGreen, width, yGreen);
		// Yellow bar
		if (yYellow != yGreen) {
			g.setColor(Color.YELLOW);
			g.fillRect(0, height - yYellow, width, yYellow - yGreen);
		}
		// Red bar
		if (yRed != yYellow) {
			g.setColor(Color.RED);
			g.fillRect(0, height - yRed, width, yRed - yYellow);
		}
	}
}
