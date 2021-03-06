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

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.kovesdan.minihelp.xml.IndexEntry;
import org.kovesdan.minihelp.xml.IndexItem;
import org.kovesdan.minihelp.xml.TOCItem;

class MiniHelpSearch extends JPanel implements FocusListener {
	private static final long serialVersionUID = 1L;
	private JList<LinkInfo> resultList;
	private List<IndexItem> index;
	private List<TOCItem> tocItems;
	private Map<String, URL> mappedContent;
	private MiniHelpIndexListModel<LinkInfo> resultModel;
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	private boolean caseSensitive = false;
	private JCheckBox caseSensitiveCheckBox;
	private boolean wholeWords = false;
	private JCheckBox wholeWordCheckBox;
	private boolean fullText = false;
	private JCheckBox fullTextCheckBox;
	private boolean regex = false;
	private JCheckBox regexCheckBox;
	private TreeSet<LinkInfo> resultSet = new TreeSet<>();
	private JTextField searchField;
	private JProgressBar searchProgressBar;
	private JButton searchButton;
	private Pattern pattern;


	class SearchAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			initSearch(searchField.getText());
		}
	}
	
	class SearchTask extends SwingWorker<Void, Void> {

		@Override
		public Void doInBackground() {
			int progress = 0;
			progress += index.size();
			progress += tocItems.size();
			if (fullText)
				progress += mappedContent.size();
			final int pm = progress;
			SwingUtilities.invokeLater(() -> searchProgressBar.setMaximum(pm));

			progress = 0;
			for (IndexItem i : index) {
				searchIndexItem(i, searchField.getText());
				progress++;
				final int fp = progress;
				SwingUtilities.invokeLater(() -> searchProgressBar.setValue(fp));
			}
			for (TOCItem i : tocItems) {
				searchTOCItem(i, searchField.getText());
				progress++;
				final int fp = progress;
				SwingUtilities.invokeLater(() -> searchProgressBar.setValue(fp));
			}
			if (fullText)
				for (Entry<String, URL> e : mappedContent.entrySet()) {
					searchDocument(e.getKey(), e.getValue(), searchField.getText());
					progress++;
					final int fp = progress;
					SwingUtilities.invokeLater(() -> searchProgressBar.setValue(fp));
				}

			return null;
		}
 
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            searchProgressBar.setValue(0);
            searchButton.setEnabled(true);
			searchField.setEditable(true);
			searchField.setEnabled(true);
            setCursor(null);
    		resultModel.setData(new ArrayList<>(resultSet));
    		resultList.clearSelection();
        }
    }

	private boolean matches(String string) {
		if (pattern == null)
			return false;
		return pattern.matcher(string).find();
	}
	
	private void compilePattern(String keyword) {
		int flags = 0;
		if (!caseSensitive)
			flags += Pattern.CASE_INSENSITIVE;
		
		if (!regex && !wholeWords)
			flags += Pattern.LITERAL;
		
		String pat = keyword;
		if (wholeWords) {
			if (!keyword.startsWith(".*\\b"))
				pat = ".*\\b" + pat;
			if (!keyword.endsWith("\\b.*"))
				pat = pat + "\\b.*";
		} 

		pattern = Pattern.compile(pat, flags);
	}

	private void searchIndexItem(IndexItem item, String keyword) {
		if (matches(item.getText()) && item.getTarget() != null)
			resultSet.add(new LinkInfo(item.getText().trim(), item.getTarget()));

		for (IndexItem i : item.getIndexItems())
			searchIndexItem(i, keyword);
		for (IndexEntry e : item.getIndexEntries())
			searchIndexEntry(e, keyword);
	}

	private void searchIndexEntry(IndexEntry entry, String keyword) {
		if (matches(entry.getText()))
			resultSet.add(new LinkInfo(entry.getText(), entry.getTarget()));
	}

	private void searchTOCItem(TOCItem item, String keyword) {
		if (matches(item.getText()))
			resultSet.add(new LinkInfo(item.getText(), item.getTarget()));
		for (TOCItem i : item.getTOCItems())
			searchTOCItem(i, keyword);
	}
	
	private void searchDocument(String target, URL url, String keyword) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
			boolean matches = false;

			String line;
			while ((line = in.readLine()) != null) {
				if (matches(line.replaceAll("<[^>]+>", ""))) {
					matches = true;
					break;
				}
			}
			if (matches)
				resultSet.add(new LinkInfo(new File(url.getFile()).getName(), target));
		} catch (IOException e) {
		}
	}
	
	private void initSearch(String keyword) {
		searchField.setText(keyword);
		resultSet.clear();
		searchButton.setEnabled(false);
		searchField.setEditable(false);
		searchField.setEnabled(false);
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	compilePattern(searchField.getText());
    	SearchTask searchTask = new SearchTask();
    	searchTask.execute();
	}
	
	public void search(String keyword, boolean fullText, boolean caseSensitive, boolean wholeWords) {
		this.fullText = fullText;
		fullTextCheckBox.setSelected(fullText);
		this.caseSensitive = caseSensitive;
		caseSensitiveCheckBox.setSelected(caseSensitive);
		this.wholeWords = wholeWords;
		wholeWordCheckBox.setSelected(wholeWords);
		initSearch(keyword);
	}

	private void updateHtmlPane(MiniHelp mainApp) {
		int index = resultList.getSelectionModel().getMinSelectionIndex();
		if (index < 0)
			return;
		mainApp.displayPageForTarget(resultModel.getElementAt(index).getTarget());
	}

	public MiniHelpSearch(Map<String, URL> mappedContent, List<IndexItem> index, List<TOCItem> contents,
			MiniHelp mainApp) {
		super(new GridBagLayout());
		this.mappedContent = mappedContent;
		this.index = index;
		this.tocItems = contents;

		searchField = new JTextField();
		String enter = "ENTER";
		KeyStroke enterKeystroke = KeyStroke.getKeyStroke(enter);
		searchField.getInputMap().put(enterKeystroke, enter);
		searchField.getActionMap().put(enter, new SearchAction());

		caseSensitiveCheckBox = new JCheckBox(Messages.get("Case sensitive"));
		caseSensitiveCheckBox.setMnemonic(Messages.mnemonic("Case sensitive Mnemonic", "C"));
		caseSensitiveCheckBox.addItemListener(e -> caseSensitive = e.getStateChange() == ItemEvent.SELECTED);
		wholeWordCheckBox = new JCheckBox(Messages.get("Whole word"));
		wholeWordCheckBox.addItemListener(e -> {
			wholeWords = e.getStateChange() == ItemEvent.SELECTED;
			if (wholeWords) {
				regex = true;
				regexCheckBox.setSelected(true);
			}
		});
		wholeWordCheckBox.setMnemonic(Messages.mnemonic("Whole word Mnemonic", "W"));
		fullTextCheckBox = new JCheckBox(Messages.get("Search in documents"));
		fullTextCheckBox.addItemListener(e -> fullText = e.getStateChange() == ItemEvent.SELECTED);
		fullTextCheckBox.setMnemonic(Messages.mnemonic("Search in documents Mnemonic", "D"));
		regexCheckBox = new JCheckBox(Messages.get("Regular expression"));
		regexCheckBox.addItemListener(e -> {
			regex = e.getStateChange() == ItemEvent.SELECTED;
			if (!regex) {
				wholeWords = false;
				wholeWordCheckBox.setSelected(false);
			}
		});
		regexCheckBox.setMnemonic(Messages.mnemonic("Regular expression Mnemonic", "R"));
		searchButton = new JButton(Messages.get("Search"));
		searchButton.addActionListener(e -> initSearch(searchField.getText()));
		searchProgressBar = new JProgressBar();
		searchProgressBar.setValue(0);
		searchProgressBar.setStringPainted(true);

		JPanel searchFormPanel = new JPanel();
		GroupLayout searchFormLayout = new GroupLayout(searchFormPanel);
		searchFormLayout.setHorizontalGroup(searchFormLayout.createParallelGroup()
				.addGroup(searchFormLayout.createSequentialGroup()
				.addGroup(searchFormLayout.createParallelGroup()
						.addComponent(searchField)
						.addGroup(searchFormLayout.createSequentialGroup()
								.addGroup(searchFormLayout.createParallelGroup()
										.addComponent(caseSensitiveCheckBox)
										.addComponent(fullTextCheckBox))
								.addGroup(searchFormLayout.createParallelGroup()
										.addComponent(wholeWordCheckBox)
										.addComponent(regexCheckBox))))
				.addComponent(searchButton))
				.addComponent(searchProgressBar));
		searchFormLayout
				.setVerticalGroup(searchFormLayout.createSequentialGroup()
						.addGroup(searchFormLayout.createParallelGroup().addComponent(searchField)
								.addComponent(searchButton))
						.addGroup(searchFormLayout.createParallelGroup()
								.addComponent(caseSensitiveCheckBox)
								.addComponent(wholeWordCheckBox))
						.addGroup(searchFormLayout.createParallelGroup()
								.addComponent(fullTextCheckBox)
								.addComponent(regexCheckBox))
						.addComponent(searchProgressBar));
		searchFormPanel.setLayout(searchFormLayout);

		resultModel = new MiniHelpIndexListModel<>(Collections.emptyList());
		resultList = new JList<>(resultModel);
		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane resultScroller = new JScrollPane(resultList);
		resultList.addListSelectionListener(e -> {
			updateHtmlPane(mainApp);
			mainApp.highlight(pattern);
		});
		resultList.addFocusListener(new FocusGainedListener(() -> updateHtmlPane(mainApp)));

		GridBagConstraints cons = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.PAGE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		add(searchFormPanel, cons);
		cons.gridy = 1;
		cons.weighty = 12;
		cons.anchor = GridBagConstraints.PAGE_END;
		cons.fill = GridBagConstraints.BOTH;
		add(resultScroller, cons);
		addFocusListener(this);
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		searchField.requestFocusInWindow();
	}

	@Override
	public void focusLost(FocusEvent arg0) {
	}
}
