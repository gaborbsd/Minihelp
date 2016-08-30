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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.kovesdan.minihelp.xml.Configuration;
import org.kovesdan.minihelp.xml.DocumentMapping;
import org.kovesdan.minihelp.xml.IndexItem;
import org.kovesdan.minihelp.xml.TOCItem;

/**
 * The window class of the help viewer.
 * 
 * @author Gábor Kövesdán
 */
public class MiniHelp extends JFrame implements HyperlinkListener {
	private static final long serialVersionUID = 1L;

	private static final String ERROR_PAGE_HEADER = "<html><head>" + "<title>Error loading page</title></head>"
			+ "<body><h1>Error loading page</h1><p>";
	private static final String ERROR_PAGE_FOOTER = "</p></body></html>";

	protected String homeID = null;
	protected String currentTarget = null;
	protected Map<String, URL> mappedContent = new HashMap<>();
	protected List<TOCItem> tableOfContents = new ArrayList<>();
	protected List<IndexItem> indexes = new ArrayList<>();
	protected JTextPane htmlPane = new JTextPane();
	protected HistoryManager<String> history = new HistoryManager<>();
	protected MiniHelpToolbar toolbar;
	protected JPanel contentsPanel;
	protected MiniHelpSearch searchPanel;
	protected MiniHelpIndex indexPanel;
	protected MiniHelpContents contentsTree;
	protected JTabbedPane navPane;
	protected PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
	protected PrinterJob printerJob = PrinterJob.getPrinterJob();
	
	public MiniHelp(String mainTitle, boolean showIndexTab, boolean showSearchTab) {
		super(mainTitle);
		JPanel leftPanel = new JPanel(new GridLayout(1, 1));
		JPanel rightPanel = new JPanel(new GridLayout(1, 1));
		JSplitPane helpWindowPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		this.add(helpWindowPane, BorderLayout.CENTER);

		navPane = new JTabbedPane();
		contentsPanel = new JPanel(new GridLayout(1, 1));
		contentsTree = new MiniHelpContents(tableOfContents, this);
		contentsPanel.add(contentsTree);

		navPane.addTab(Messages.get("Contents"), contentsPanel);
		navPane.setMnemonicAt(0, Messages.mnemonic("Content Mnemonic", "T"));
		Messages.keyBoardAction(getRootPane(), e -> navPane.setSelectedIndex(0), "TOC Tab Key", "control T",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		indexPanel = new MiniHelpIndex(indexes, this);
		searchPanel = new MiniHelpSearch(mappedContent, indexes, tableOfContents, this);
		if (showIndexTab)
			enableIndexPanel();
		if (showSearchTab)
			enableSearchPanel();
		leftPanel.add(navPane);

		Messages.keyBoardAction(getRootPane(), e -> back(), "Back Key 1", "alt LEFT",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		Messages.keyBoardAction(getRootPane(), e -> back(), "Back Key 2", "BACKSPACE",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		Messages.keyBoardAction(getRootPane(), e -> forward(), "Forward Key 1", "alt RIGHT",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		Messages.keyBoardAction(getRootPane(), e -> forward(), "Forward Key 2", "shift BACKSPACE",
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		htmlPane.setContentType("text/html");
		HTMLEditorKit editorKit = new HTMLEditorKit();
		htmlPane.setEditorKit(editorKit);
		htmlPane.addHyperlinkListener(this);
		htmlPane.setEditable(false);
		htmlPane.setComponentPopupMenu(createMenu());
		htmlPane.addMouseWheelListener(e -> {
			if (e.isMetaDown() || e.isControlDown()) {
				int n = e.getWheelRotation();
				if (n < 0)
					while (n != 0) {
						increaseFont();
						n++;
					}
				else
					while (n != 0) {
						decreaseFont();
						n--;
					}
				e.consume();
			}
		});

		rightPanel.add(htmlPane);

		this.setJMenuBar(new MiniHelpMenuBar(this));
		
		toolbar = new MiniHelpToolbar(this);
		this.add(toolbar, BorderLayout.NORTH);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setMinimumSize(new Dimension(screenSize.width * 10 / 24, screenSize.height * 10 / 24));
		this.setSize(screenSize.width * 2 / 3, screenSize.height * 2 / 3);
		this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
				screenSize.height / 2 - this.getSize().height / 2);
	}
	
	/**
	 * 
	 * Constructs the help window, which can later be displayes by calling
	 * setVisible(true).
	 * 
	 * @param configuration
	 *            the {@link Configuration} object that contains the parsed
	 *            configuration data.
	 * @param baseUri
	 *            the {@link URI}, where relative paths of the documents are
	 *            resolved.
	 * @param showIndexTab
	 *            whether to show the index tab. The default behavior is to only
	 *            show the index tab when index terms are defined in the
	 *            configuration.
	 * @param showSearchTab
	 *            whether to show the search tab. The default is
	 *            <code>true</code>.
	 * @throws HeadlessException
	 */
	public MiniHelp(String mainTitle, Configuration configuration, URI baseUri, boolean showIndexTab,
			boolean showSearchTab) throws HeadlessException {
		this(mainTitle, showIndexTab, showSearchTab);
		addHelpset(configuration, baseUri);
	}

	/**
	 * Constructs the help window, which can later be displayes by calling
	 * setVisible(true).
	 * 
	 * @param configuration
	 *            the {@link Configuration} object that contains the parsed
	 *            configuration data.
	 * @param baseUri
	 *            the {@link URI}, where relative paths of the documents are
	 *            resolved.
	 * @throws HeadlessException
	 */
	public MiniHelp(String mainTitle, Configuration configuration, URI baseUri) throws HeadlessException {
		this(mainTitle, configuration, baseUri, configuration.getIndexItems().isEmpty() ? false : true, true);
	}
	
	/**
	 * Constructs the help window, which can later be displayes by calling
	 * setVisible(true).
	 * 
	 * @param configuration
	 *            the {@link Configuration} object that contains the parsed
	 *            configuration data.
	 * @param baseUri
	 *            the {@link URI}, where relative paths of the documents are
	 *            resolved.
	 * @throws HeadlessException
	 */
	public MiniHelp(Configuration configuration, URI baseUri) throws HeadlessException {
		this(configuration.getTitle(), configuration, baseUri, configuration.getIndexItems().isEmpty() ? false : true,
				true);
	}

	public void addHelpset(Configuration configuration, URI baseUri) {
		// store document mapping
		for (DocumentMapping m : configuration.getDocumentMappings()) {
			try {
				URL url = baseUri.resolve(m.getUrl()).toURL();
				mappedContent.put(m.getTarget(), url);
			} catch (MalformedURLException e1) {
				// TODO: warning; not mapping malformed URLs
			}
		}

		// default mapping for documents that are not explicitly mapped
		for (TOCItem i : configuration.getTOCItems()) {
			mapTOCItem(i, baseUri);
		}
		map(configuration.getHomeID(), baseUri);
		
		// copy TOC
		TOCItem rootTOC = new TOCItem();
		rootTOC.setText(configuration.getTitle());
		rootTOC.setTarget(configuration.getHomeID());
		rootTOC.getTOCItems().addAll(configuration.getTOCItems());
		tableOfContents.add(rootTOC);
		
		// merge indexes
		for (IndexItem i : configuration.getIndexItems())
			mergeIndexInto(indexes, i);
		
		contentsTree.updateModel();
		indexPanel.updateModel();
		if (homeID == null)
			homeID = configuration.getHomeID();
		displayHomePage();
	}
	
	public void enableIndexPanel() {
		int idx = navPane.indexOfComponent(indexPanel);
		if (idx != -1)
			return;

		navPane.addTab(Messages.get("Index"), indexPanel);
		idx = navPane.indexOfComponent(indexPanel);
		navPane.setMnemonicAt(idx, Messages.mnemonic("Index Mnemonic", "I"));
		Messages.keyBoardAction(getRootPane(), e -> showIndexPanel(), "Index Tab Key", "control I",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	public void disableIndexPanel() {
		int idx = navPane.indexOfComponent(indexPanel);
		if (idx == -1)
			return;
		navPane.remove(indexPanel);
		Messages.keyBoardAction(getRootPane(), e -> {}, "Index Tab Key", "control I",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	public void enableSearchPanel() {
		int idx = navPane.indexOfComponent(searchPanel);
		if (idx != -1)
			return;

		navPane.addTab(Messages.get("Search"), searchPanel);
		idx = navPane.indexOfComponent(searchPanel);
		navPane.setMnemonicAt(idx, Messages.mnemonic("Search Mnemonic", "S"));
		Messages.keyBoardAction(getRootPane(), e -> showSearchPanel(), "Search Tab Key", "control S",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public void disableSearchPanel() {
		int idx = navPane.indexOfComponent(searchPanel);
		if (idx == -1)
			return;
		navPane.remove(searchPanel);
		Messages.keyBoardAction(getRootPane(), e -> {}, "Search Tab Key", "control S",
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	private void map(String target, URI baseUri) {
		if (!mappedContent.containsKey(target)) {
			try {
				URL url = baseUri.resolve(target + ".html").toURL();
				File file = new File(url.getFile());
				if (file.isFile())
					mappedContent.put(target, url);
			} catch (MalformedURLException e1) {
				// TODO: warning; not mapping malformed URLs
			}
		}
	}
	
	private void mapTOCItem(TOCItem i, URI baseUri) {
		map(i.getTarget(), baseUri);
		for (TOCItem i2 : i.getTOCItems())
			mapTOCItem(i2, baseUri);
	}
	
	private void mergeIndexInto(List<IndexItem> list, IndexItem item) {
		Optional<IndexItem> same = indexes.stream().filter(i -> i.getText().equals(item.getText())).findFirst();
		if (same.isPresent()) {
			for (IndexItem i : item.getIndexItems())
				mergeIndexInto(same.get().getIndexItems(), i);
			same.get().getIndexEntries().addAll(item.getIndexEntries());
		} else {
			indexes.add(item);
		}
	}
	
	protected JPopupMenu createMenu() {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem backMenu = new JMenuItem(Messages.get("Back"));
		backMenu.addActionListener(e -> back());
		menu.add(backMenu);

		JMenuItem forwardMenu = new JMenuItem(Messages.get("Forward"));
		forwardMenu.addActionListener(e -> forward());
		menu.add(forwardMenu);
		
		menu.addSeparator();
		
		JMenuItem selectAllMenu = new JMenuItem(Messages.get("Select All"));
		selectAllMenu.addActionListener(e -> htmlPane.selectAll());
		menu.add(selectAllMenu);
		
		JMenuItem copyMenu = new JMenuItem(new DefaultEditorKit.CopyAction());
		copyMenu.setText(Messages.get("Copy"));
		menu.add(copyMenu);
		
		JMenuItem searchForTerm = new JMenuItem(Messages.get("Search for Selected Text"));
		searchForTerm.addActionListener(e -> {
			searchPanel.search(htmlPane.getSelectedText(), true, false, false);
			navPane.setSelectedComponent(searchPanel);
		});
		menu.add(searchForTerm);
		
		PopupMenuListener listener = new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				backMenu.setEnabled(history.isBackActive());
				forwardMenu.setEnabled(history.isForwardActive());
				copyMenu.setEnabled(htmlPane.getSelectedText() != null);
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		};
		menu.addPopupMenuListener(listener);

		return menu;
	}
	
	public void increaseFont() {
	    MutableAttributeSet attrs = htmlPane.getInputAttributes();
	    int size = StyleConstants.getFontSize(attrs);
	    StyleConstants.setFontSize(attrs, size * 3 / 2);
	    StyledDocument doc = htmlPane.getStyledDocument();
	    doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
	}
	
	public void decreaseFont() {
	    MutableAttributeSet attrs = htmlPane.getInputAttributes();
	    int size = StyleConstants.getFontSize(attrs);
	    StyleConstants.setFontSize(attrs, size * 2 / 3);
	    StyledDocument doc = htmlPane.getStyledDocument();
	    doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
	}
	
	public void search(String keyword, boolean fullText, boolean caseSensitive, boolean wholeWords) {
		searchPanel.search(keyword, fullText, caseSensitive, wholeWords);
	}
	
	private void displayPageForUrlNoHistory(String url) {
		try {
			htmlPane.setPage(url);
		} catch (IOException e) {
			e.printStackTrace();
			StringBuffer sb = new StringBuffer(ERROR_PAGE_HEADER);
			sb.append(e.getMessage());
			sb.append(ERROR_PAGE_FOOTER);
			htmlPane.setText(sb.toString());
		}
	}

	protected void back() {
		if (history.isBackActive()) {
			displayPageForUrlNoHistory(history.back());
			toolbar.updateActiveButtons();
		}
	}
	
	protected void forward() {
		if (history.isForwardActive()) {
			displayPageForUrlNoHistory(history.forward());
			toolbar.updateActiveButtons();
		}
	}

	protected void displayPageForUrl(URL url) {
		try {
			history.navigatedTo(url.toString());
			htmlPane.setPage(url.toString());
			removeHighlights();
			toolbar.updateActiveButtons();
		} catch (IOException e) {
			e.printStackTrace();
			StringBuffer sb = new StringBuffer(ERROR_PAGE_HEADER);
			sb.append(e.getMessage());
			sb.append(ERROR_PAGE_FOOTER);
			htmlPane.setText(sb.toString());
		}
	}

	public void displayPageForTarget(String target) {
		currentTarget = target;
		URL url = mappedContent.get(target);
		displayPageForUrl(url);
	}
	
	public void displayHomePage() {
		if (homeID != null)
			displayPageForTarget(homeID);
	}
	
	public URL getCurrentURL() {
		try {
			return new URL(history.current());
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public String getCurrentTarget() {
		return currentTarget;
	}
	
	public void showSearchPanel() {
		int idx = navPane.indexOfComponent(searchPanel);
		if (idx != -1)
			return;
		navPane.setSelectedIndex(navPane.indexOfComponent(searchPanel));
	}
	
	public void showIndexPanel() {
		int idx = navPane.indexOfComponent(indexPanel);
		if (idx != -1)
			return;
		navPane.setSelectedIndex(navPane.indexOfComponent(indexPanel));
	}
	
	public void showTOCPanel() {
		navPane.setSelectedIndex(navPane.indexOfComponent(contentsPanel));
	}
	
	public void print() {
		try {
			htmlPane.print(null, null, true, printerJob.getPrintService(), attr, true);
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}

	public void pageSetup() {
		printerJob.pageDialog(attr);
	}
	
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			displayPageForUrl(e.getURL());
	}

	public void highlight(Pattern pattern) {
		removeHighlights();
		try {
			HighlightPainter highlightPainter = new MiniHelpHighlightPainter(Color.YELLOW);
			Highlighter highlighter = htmlPane.getHighlighter();
			Document doc = htmlPane.getDocument();
			Matcher matcher = pattern.matcher(doc.getText(0, doc.getLength()));

			while (matcher.find())
				highlighter.addHighlight(matcher.start(), matcher.end(), highlightPainter);

		} catch (BadLocationException e) {
		}
	}

	public void removeHighlights() {
		Highlighter highlighter = htmlPane.getHighlighter();
		for (Highlight h : highlighter.getHighlights())
			if (h.getPainter() instanceof MiniHelpHighlightPainter)
				highlighter.removeHighlight(h);
	}
}
