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

package org.truffautronic;

import java.io.File;
import java.util.Locale;

import javax.swing.UIManager;

import org.truffautronic.controller.AppController;
import org.truffautronic.controller.I18N;

public class Main {

	public static void main(String[] args) throws Exception {

		I18N.setLocale(Locale.getDefault());

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		AppController appController = new AppController();
		if (args.length >= 1) {
			appController.openInitialFile(new File(args[0]));
		}
		appController.run();
	}
}
