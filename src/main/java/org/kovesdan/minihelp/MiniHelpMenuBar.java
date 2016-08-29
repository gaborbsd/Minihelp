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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

class MiniHelpMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;

	public MiniHelpMenuBar(MiniHelp mainApp) {
		JMenu fileMenu = new JMenu(Messages.get("File"));
		fileMenu.setMnemonic(Messages.mnemonic("File Mnemonic", "F"));

		JMenuItem filePrintMenu = new JMenuItem(Messages.get("Print"));
		filePrintMenu.addActionListener(e -> mainApp.print());
		fileMenu.add(filePrintMenu);
		Messages.accelerator(filePrintMenu, "File Print Acc", "control P");
		filePrintMenu.setMnemonic(Messages.mnemonic("Print Mnemonic", "P"));
		
		JMenuItem filePageSetupMenu = new JMenuItem(Messages.get("Page Setup"));
		filePageSetupMenu.addActionListener(e -> mainApp.pageSetup());
		fileMenu.add(filePageSetupMenu);
		
		JMenuItem fileCloseMenu = new JMenuItem(Messages.get("Close"));
		fileCloseMenu.addActionListener(e -> mainApp.setVisible(false));
		fileMenu.add(fileCloseMenu);
		Messages.accelerator(fileCloseMenu, "File Close Acc", "control W");
		fileCloseMenu.setMnemonic(Messages.mnemonic("Close Mnemonic", "C"));
		
		this.add(fileMenu);
	}
}
