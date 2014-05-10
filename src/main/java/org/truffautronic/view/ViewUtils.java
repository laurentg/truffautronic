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

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ViewUtils {

	// Create fonts
	public static Font BIG_FONT = new Font(Font.DIALOG, Font.BOLD, 14);
	public static Font NORMAL_FONT = new Font(Font.DIALOG, Font.PLAIN, 10);
	public static Font SMALL_FONT = new Font(Font.DIALOG, Font.PLAIN, 8);

	public static ImageIcon loadIcon(String imageFile) {
		InputStream istream = ViewUtils.class.getClassLoader().getResourceAsStream(
				imageFile);
		if (istream == null)
			throw new RuntimeException("Missing image: " + imageFile);
		try {
			return new ImageIcon(ImageIO.read(istream));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
