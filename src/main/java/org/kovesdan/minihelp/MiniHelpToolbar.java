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

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

class MiniHelpToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private MiniHelp mainApp;
	private JButton backButton;
	private JButton forwardButton;
	
	public MiniHelpToolbar(MiniHelp mainApp) {
		this.mainApp = mainApp;
		
		backButton = new JButton();
		backButton.addActionListener(e -> mainApp.back());
		initButton(backButton, "back.png", Messages.get("Back"), Messages.get("Previous Page"));
		add(backButton);
		
		forwardButton = new JButton();
		forwardButton.addActionListener(e -> mainApp.forward());
		initButton(forwardButton, "forward.png", Messages.get("Forward"), Messages.get("Next Page"));
		add(forwardButton);
		
		addSeparator();
		
		JButton printButton = new JButton();
		printButton.addActionListener(e -> mainApp.print());
		initButton(printButton, "print.png", Messages.get("Print"), Messages.get("Print"));
		add(printButton);
		
		JButton pageSetupButton = new JButton();
		pageSetupButton.addActionListener(e -> mainApp.pageSetup());
		initButton(pageSetupButton, "pagesetup.png", Messages.get("Page Setup"), Messages.get("Page Setup"));
		add(pageSetupButton);
		
		addSeparator();
		
		JButton decFontButton = new JButton();
		decFontButton.addActionListener(e -> mainApp.decreaseFont());
		initButton(decFontButton, "dec.png", Messages.get("Decrease Font"), Messages.get("Decrease Font Size"));
		add(decFontButton);
		
		JButton incFontButton = new JButton();
		incFontButton.addActionListener(e -> mainApp.increaseFont());
		initButton(incFontButton, "inc.png", Messages.get("Increase Font"), Messages.get("Increase Font Size"));
		add(incFontButton);
		
		updateActiveButtons();
	}
	
	private void initButton(JButton button, String imgName, String altText, String tooltip) {
		ClassLoader classLoader = getClass().getClassLoader();
		URL imgUrl = classLoader.getResource(imgName);
		if (imgUrl != null)
			button.setIcon(new ImageIcon(imgUrl, altText));
		else
			button.setText(altText);
		button.setToolTipText(tooltip);
	}
	
	public void updateActiveButtons() {
		backButton.setEnabled(mainApp.history.isBackActive());
		forwardButton.setEnabled(mainApp.history.isForwardActive());
	}
}
