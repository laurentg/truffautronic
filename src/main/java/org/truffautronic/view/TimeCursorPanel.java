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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;

import org.truffautronic.controller.I18N;
import org.truffautronic.model.Duration;

public class TimeCursorPanel extends JComponent {
	private static final long serialVersionUID = 1L;

	private long totalLenMs;
	private long[] positionsMs;
	private long startMs, endMs;
	private long fadeInMs, fadeOutMs;
	private long cursorMs = -1;
	private CueView cueView;

	class Popup extends JPopupMenu {
		private static final long serialVersionUID = 1L;
		private JMenuItem startHereItem, endHereItem;
		private JMenuItem endFadeInItem, startFadeOutItem;
		private JMenuItem resetFadeInItem, resetFadeOutItem;

		public Popup() {
			JMenuItem cursorPosition = new JMenuItem(
					new Duration(cursorMs).toString());
			cursorPosition.setEnabled(false);
			add(cursorPosition);
			add(new JSeparator());
			startHereItem = new JMenuItem(
					I18N.translate("menu.cue.start_here"),
					ViewUtils.loadIcon("16x16/starthere.png"));
			startHereItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					startMs = cursorMs;
					cueView.setRangeMs(startMs, endMs);
					TimeCursorPanel.this.repaint();
				}
			});
			if (cursorMs < endMs)
				add(startHereItem);
			endHereItem = new JMenuItem(I18N.translate("menu.cue.end_here"),
					ViewUtils.loadIcon("16x16/endhere.png"));
			endHereItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					endMs = cursorMs;
					cueView.setRangeMs(startMs, endMs);
					TimeCursorPanel.this.repaint();
				}
			});
			if (cursorMs > startMs)
				add(endHereItem);
			endFadeInItem = new JMenuItem(
					I18N.translate("menu.cue.end_fadein_here"),
					ViewUtils.loadIcon("16x16/endfadein.png"));
			endFadeInItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fadeInMs = cursorMs - startMs;
					cueView.setFadeInOutMs(fadeInMs, fadeOutMs);
					TimeCursorPanel.this.repaint();
				}
			});
			if (cursorMs > startMs && cursorMs < endMs)
				add(endFadeInItem);
			resetFadeInItem = new JMenuItem(
					I18N.translate("menu.cue.reset_fadein"));
			resetFadeInItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fadeInMs = 0;
					cueView.setFadeInOutMs(fadeInMs, fadeOutMs);
					TimeCursorPanel.this.repaint();
				}
			});
			add(resetFadeInItem);
			startFadeOutItem = new JMenuItem(
					I18N.translate("menu.cue.start_fadeout_here"),
					ViewUtils.loadIcon("16x16/startfadeout.png"));
			startFadeOutItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fadeOutMs = endMs - cursorMs;
					cueView.setFadeInOutMs(fadeInMs, fadeOutMs);
					TimeCursorPanel.this.repaint();
				}
			});
			if (cursorMs >= startMs && cursorMs <= endMs)
				add(startFadeOutItem);
			resetFadeOutItem = new JMenuItem(
					I18N.translate("menu.cue.reset_fadeout"));
			resetFadeOutItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fadeOutMs = 0;
					cueView.setFadeInOutMs(fadeInMs, fadeOutMs);
					TimeCursorPanel.this.repaint();
				}
			});
			add(resetFadeOutItem);
		}
	}

	public TimeCursorPanel(CueView cueView) {
		this.cueView = cueView;
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		totalLenMs = 1;
		this.positionsMs = new long[0];
		setMinimumSize(new Dimension(0, 10));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (totalLenMs == 0)
					return; // No cue, no clic
				cursorMs = e.getX() * totalLenMs / getWidth();
				Popup menu = new Popup();
				menu.show(e.getComponent(), e.getX(), e.getY());
				repaint();
			}
		});
	}

	public void setTotalTimeMs(long totalLenMs) {
		this.totalLenMs = totalLenMs;
		if (endMs > totalLenMs)
			endMs = totalLenMs;
	}

	public void setCursorsMs(long[] positionsMs) {
		this.positionsMs = positionsMs;
		repaint();
	}

	public void setRangeMs(long startMs, long endMs) {
		if (startMs < 0)
			startMs = 0;
		if (startMs > totalLenMs)
			startMs = totalLenMs;
		if (endMs < 0)
			endMs = 0;
		if (endMs > totalLenMs)
			endMs = totalLenMs;
		if (startMs > endMs)
			startMs = endMs;
		this.startMs = startMs;
		this.endMs = endMs;
		repaint();
	}

	public void setFadeMs(long fadeInMs, long fadeOutMs) {
		this.fadeInMs = fadeInMs;
		this.fadeOutMs = fadeOutMs;
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		// Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		if (totalLenMs == 0)
			return; // No cue probably, or zero len
		// Clip range + Volume envelope
		g.setColor(Color.GRAY);
		int startX = (int) (startMs * width / totalLenMs);
		int fadeInX = (int) ((startMs + fadeInMs) * width / totalLenMs);
		int[] xs = new int[] { 0, 0, startX, fadeInX };
		int[] ys = new int[] { 0, height, height, 0 };
		g.fillPolygon(xs, ys, 4);
		int endX = (int) (endMs * width / totalLenMs);
		int fadeOutX = (int) ((endMs - fadeOutMs) * width / totalLenMs);
		xs = new int[] { width, width, endX, fadeOutX };
		ys = new int[] { 0, height, height, 0 };
		g.fillPolygon(xs, ys, 4);
		// Playing cursor
		g.setColor(Color.YELLOW);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		for (long positionMs : positionsMs) {
			int x = (int) (positionMs * width / totalLenMs);
			g.drawLine(x, 0, x, height);
		}
		g.setColor(Color.GREEN);
		g.setFont(ViewUtils.BIG_FONT);
		g.drawString(new Duration(totalLenMs).toString(), 2, height - 4);
		// User cursor
		if (cursorMs >= 0) {
			g.setColor(Color.GREEN);
			int xc = (int) (cursorMs * width / totalLenMs);
			g.drawLine(xc, 0, xc, height);
		}
	}
}
