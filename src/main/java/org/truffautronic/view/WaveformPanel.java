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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

import org.truffautronic.audio.AudioWaveform;
import org.truffautronic.model.Duration;
import org.truffautronic.model.TimeLabel;
import org.truffautronic.model.TimeLabelsBundle;

public class WaveformPanel extends JComponent {
	private static final long serialVersionUID = 1L;

	private long totalTimeMs;
	private List<AudioWaveform> waveforms;
	private TimeLabelsBundle timeLabels;

	public WaveformPanel() {
		super();
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	public void setWaveforms(List<AudioWaveform> waveforms) {
		this.waveforms = waveforms;
		repaint();
	}

	public void setTimeLabels(TimeLabelsBundle timeLabels) {
		this.timeLabels = timeLabels;
		repaint();
	}

	public void setTotalTimeMs(long totalTimeMs) {
		this.totalTimeMs = totalTimeMs;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (waveforms == null) {
			// No data
			g.setFont(ViewUtils.BIG_FONT);
			g.setColor(Color.RED);
			g.drawString("NO AUDIO DATA", 4, getHeight() - 4);
			return;
		}
		int width = getWidth();
		int height = getHeight();
		// Ticks
		if (totalTimeMs > 0) {
			long tickMs1 = Duration.round(totalTimeMs * 50 / width).getMs();
			long tickMs2 = Duration.round(totalTimeMs * 10 / width).getMs();
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(ViewUtils.SMALL_FONT);
			long ms = tickMs2;
			while (ms < totalTimeMs) {
				int x = (int) (ms * width / totalTimeMs);
				g.drawLine(x, 0, x, height);
				ms += tickMs2;
			}
			g.setColor(Color.DARK_GRAY);
			ms = tickMs1;
			while (ms < totalTimeMs) {
				int x = (int) (ms * width / totalTimeMs);
				g.drawLine(x, 0, x, height);
				g.drawString(new Duration(ms).toString(), x + 2, 10);
				ms += tickMs1;
			}
		}
		// Waveform
		int nChannels = waveforms.size();
		for (int channel = 0; channel < nChannels; channel++) {
			AudioWaveform waveform = waveforms.get(channel);
			int height2 = (getHeight() - 4) / nChannels;
			int yBase = channel * height2;
			int ox = 0;
			int oy = 0;
			Color fill = new Color(0xffaaaa);
			for (int x = 0; x < width; x++) {
				int sample = x * AudioWaveform.MAX_SAMPLES / width;
				int y = height2
						- Math.round(waveform.getAmplitude(sample) * height2);
				g.setColor(fill);
				g.drawLine(x, yBase + height2, x, yBase + y);
				g.setColor(Color.RED);
				g.drawLine(ox, yBase + oy, x, yBase + y);
				oy = y;
				ox = x;
			}
			g.setColor(Color.BLUE);
			g.drawLine(0, yBase + height2, width, yBase + height2);
		}
		// Labels
		int lastXEnd = Integer.MIN_VALUE;
		g.setFont(ViewUtils.NORMAL_FONT);
		int sh = g.getFontMetrics().getHeight();
		int y = sh + 10;
		for (TimeLabel timeLabel : timeLabels.getLabels()) {
			int x = (int) (timeLabel.getPosition().getMs() * width / totalTimeMs);
			int sw = g.getFontMetrics().stringWidth(timeLabel.getName());
			if (x > lastXEnd + 2) {
				y = sh + 10;
			} else {
				y = y + sh;
				if (y > height)
					y = sh + 10;
			}
			g.setColor(new Color(0.5f, 0.5f, 1.0f, 0.5f));
			g.fillRect(x, y - sh + 4, sw + 4, sh - 2);
			g.setColor(Color.BLACK);
			g.drawLine(x, height, x, y - sh + 4);
			g.drawString(timeLabel.getName(), x + 2, y);
			lastXEnd = x + sw;
		}
	}
}
