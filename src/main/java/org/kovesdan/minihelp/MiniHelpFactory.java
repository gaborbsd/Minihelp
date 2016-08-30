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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.kovesdan.minihelp.xml.Configuration;

class Helpset {
	Configuration configuration;
	URI baseUri;

	public Helpset(Configuration configuration, URI baseUri) {
		super();
		this.configuration = configuration;
		this.baseUri = baseUri;
	}
}

public class MiniHelpFactory {
	private Locale locale = Locale.getDefault();
	private String mainTitle = "Documentation";
	private boolean showIndexTab = true;
	private boolean showSearchTab = true;
	private List<Helpset> helpsets = new ArrayList<>();

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setMainTitle(String mainTitle) {
		this.mainTitle = mainTitle;
	}

	public void setShowIndexTab(boolean showIndexTab) {
		this.showIndexTab = showIndexTab;
	}

	public void setShowSearchTab(boolean showSearchTab) {
		this.showSearchTab = showSearchTab;
	}

	public void addHelpset(Configuration configuration, URI baseUri) {
		helpsets.add(new Helpset(configuration, baseUri));
	}

	public MiniHelp getMiniHelp() {
		Messages.setLocale(locale);
		MiniHelp help = new MiniHelp(mainTitle, showIndexTab, showSearchTab);
		for (Helpset hs : helpsets)
			help.addHelpset(hs.configuration, hs.baseUri);
		return help;
	}
}
