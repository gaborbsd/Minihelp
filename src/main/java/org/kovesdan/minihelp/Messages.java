/*
 * Copyright (c) 2016 Gábor Kövesdán
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.kovesdan.minihelp;

import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class Messages {
	private static ResourceBundle bundle;

	static {
		Locale locale = Locale.getDefault();
		bundle = ResourceBundle.getBundle("MiniHelp", locale);
		if (bundle == null) {
			locale = Locale.ENGLISH;
			bundle = ResourceBundle.getBundle("MiniHelp", locale);
		}
	}

	public static void setLocale(Locale locale) {
		ResourceBundle bundleToSet = ResourceBundle.getBundle("MiniHelp", locale);
		if (bundleToSet != null)
			bundle = bundleToSet;
	}

	public static String get(String key) {
		try {
			String val = bundle.getString(key);
			return val;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static String get(String key, String def) {
		try {
			String val = bundle.getString(key);
			return val;
		} catch (MissingResourceException e) {
			return def;
		}
	}

	public static int mnemonic(String key, String def) {
		String val = get(key, def);
		KeyStroke keyStroke = KeyStroke.getKeyStroke(val);
		return (keyStroke == null) ? '.' : keyStroke.getKeyCode();
	}

	public static void keyBoardAction(JComponent component, ActionListener action, String key, String def, int cond) {
		KeyStroke keyStroke = KeyStroke.getKeyStroke(get(key, def));
		if (keyStroke != null)
			component.registerKeyboardAction(action, keyStroke, cond);
	}

	public static void accelerator(JMenuItem menu, String key, String def) {
		KeyStroke keyStroke = KeyStroke.getKeyStroke(get(key, def));
		if (keyStroke != null)
			menu.setAccelerator(keyStroke);
	}
}
