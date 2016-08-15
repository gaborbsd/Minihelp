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

import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.kovesdan.minihelp.xml.IndexEntry;
import org.kovesdan.minihelp.xml.IndexItem;
import org.kovesdan.minihelp.xml.TOCItem;

class MiniHelpSearch extends JPanel implements FocusListener {
	private static final long serialVersionUID = 1L;
	private JList<LinkInfo> resultList;
	private List<IndexItem> index;
	private List<TOCItem> tocItems;
	private MiniHelpIndexListModel<LinkInfo> resultModel;
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	private boolean caseSensitive = false;
	private boolean wholeWords = false;
	private TreeSet<LinkInfo> resultSet = new TreeSet<>();
	private JTextField searchField;

	class SearchAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			search(searchField.getText());
		}
	}

	private boolean matches(String string, String keyword) {
		if (!caseSensitive) {
			keyword = keyword.toLowerCase();
			string = string.toLowerCase();
		}

		if (wholeWords) {
			String pattern = ".*\\b" + keyword + "\\b.*";
			return string.matches(pattern);
		} else {
			return string.contains(keyword);
		}
	}

	private void searchIndexItem(IndexItem item, String keyword) {
		if (matches(item.getText(), keyword))
			resultSet.add(new LinkInfo(item.getText().trim(), item.getTarget()));

		for (IndexItem i : item.getIndexItems())
			searchIndexItem(i, keyword);
		for (IndexEntry e : item.getIndexEntries())
			searchIndexEntry(e, keyword);
	}

	private void searchIndexEntry(IndexEntry entry, String keyword) {
		if (matches(entry.getText(), keyword))
			resultSet.add(new LinkInfo(entry.getText(), entry.getTarget()));
	}

	private void searchTOCItem(TOCItem item, String keyword) {
		if (matches(item.getText(), keyword))
			resultSet.add(new LinkInfo(item.getText(), item.getTarget()));
		for (TOCItem i : item.getTOCItems())
			searchTOCItem(i, keyword);
	}

	private void search(String keyword) {
		resultSet.clear();
		for (IndexItem i : index)
			searchIndexItem(i, keyword);
		for (TOCItem i : tocItems)
			searchTOCItem(i, keyword);
		resultModel.setData(new ArrayList<>(resultSet));
		resultList.clearSelection();
	}
	
	public void initSearch(String keyword) {
		searchField.setText(keyword);
		search(keyword);
	}

	private void updateHtmlPane(MiniHelp mainApp) {
		int index = resultList.getSelectionModel().getMinSelectionIndex();
		if (index < 0)
			return;
		mainApp.displayPageForTarget(resultModel.getElementAt(index).getTarget());
	}

	public MiniHelpSearch(List<IndexItem> index, List<TOCItem> contents, MiniHelp mainApp) {
		super(new GridBagLayout());
		this.index = index;
		this.tocItems = contents;

		searchField = new JTextField();
		String enter = "ENTER";
		KeyStroke enterKeystroke = KeyStroke.getKeyStroke(enter);
		searchField.getInputMap().put(enterKeystroke, enter);
		searchField.getActionMap().put(enter, new SearchAction());

		JCheckBox caseSensitiveCheckBox = new JCheckBox("Case sensitive");
		caseSensitiveCheckBox.setMnemonic(KeyEvent.VK_C);
		caseSensitiveCheckBox.addActionListener(e -> caseSensitive = !caseSensitive);
		JCheckBox wholeWordCheckBox = new JCheckBox("Whole word");
		wholeWordCheckBox.addActionListener(e -> wholeWords = !wholeWords);
		wholeWordCheckBox.setMnemonic(KeyEvent.VK_W);
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(e -> search(searchField.getText()));

		JPanel searchFormPanel = new JPanel();
		GroupLayout searchFormLayout = new GroupLayout(searchFormPanel);
		searchFormLayout.setHorizontalGroup(searchFormLayout.createSequentialGroup()
				.addGroup(searchFormLayout.createParallelGroup()
						.addComponent(searchField).addGroup(searchFormLayout.createSequentialGroup()
								.addComponent(caseSensitiveCheckBox).addComponent(wholeWordCheckBox)))
				.addComponent(searchButton));
		searchFormLayout
				.setVerticalGroup(searchFormLayout.createSequentialGroup()
						.addGroup(searchFormLayout.createParallelGroup().addComponent(searchField)
								.addComponent(searchButton))
						.addGroup(searchFormLayout.createParallelGroup().addComponent(caseSensitiveCheckBox)
								.addComponent(wholeWordCheckBox)));
		searchFormPanel.setLayout(searchFormLayout);

		resultModel = new MiniHelpIndexListModel<>(Collections.emptyList());
		resultList = new JList<>(resultModel);
		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane resultScroller = new JScrollPane(resultList);
		resultList.addListSelectionListener(e -> updateHtmlPane(mainApp));
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
