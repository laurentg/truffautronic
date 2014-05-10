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

package org.truffautronic.controller;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {

	private static ResourceBundle resourceBundle;

	public static void setLocale(Locale locale) {
		init(locale);
	}

	private static void init(Locale locale) {
		resourceBundle = ResourceBundle.getBundle("Truffautronic", locale);
	}

	public static String translate(String key, Object... params) {
		String translated = translate(key);
		return MessageFormat.format(translated, params);
	}

	public static String translate(String key) {
		if (resourceBundle == null)
			init(Locale.getDefault());
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			e.printStackTrace();
			return "***" + key;
		}
	}
}
