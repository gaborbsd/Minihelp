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

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.kovesdan.minihelp.xml.IndexEntry;
import org.kovesdan.minihelp.xml.IndexItem;

class MiniHelpIndex extends JPanel {
	private static final long serialVersionUID = 1L;
	private JList<IndexItem> indexList;
	private JList<LinkInfo> resultList;
	private MiniHelpIndexListModel<LinkInfo> resultModel;
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	private List<IndexItem> list = new ArrayList<>();

	private void flattenList(List<IndexItem> index, int level) {
		Collections.sort(index, (o1, o2) -> o1.getText().compareTo(o2.getText()));
		for (IndexItem item : index) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < level; i++)
				sb.append("  ");
			sb.append(item.getText());
			IndexItem copy = new IndexItem();
			copy.setText(sb.toString());
			copy.setTarget(item.getTarget());
			list.add(copy);
			flattenList(item.getIndexItems(), level + 1);
		}
	}

	private void updateHtmlPaneWithIndexterm(MiniHelp mainApp) {
		int index = indexList.getSelectionModel().getMinSelectionIndex();
		if (index < 0)
			return;
		resultList.clearSelection();
		IndexItem item = indexList.getModel().getElementAt(index);
		List<LinkInfo> results = new ArrayList<>();
		if (item.getTarget() != null)
			mainApp.displayPageForTarget(item.getTarget());
		for (IndexEntry entry : item.getIndexEntries())
			results.add(new LinkInfo(entry.getText(), entry.getTarget()));
		Collections.sort(results, (e1, e2) -> e1.getLabel().compareTo(e2.getLabel()));
		resultModel.setData(results);
	}

	private void updateHtmlPaneWithIndexentry(MiniHelp mainApp) {
		int index = resultList.getSelectionModel().getMinSelectionIndex();
		if (index < 0)
			return;
		mainApp.displayPageForTarget(resultModel.getElementAt(index).getTarget());
	}

	public MiniHelpIndex(List<IndexItem> index, MiniHelp mainApp) {
		super(new GridLayout(2, 1));
		flattenList(index, 0);

		ListModel<IndexItem> listModel = new MiniHelpIndexListModel<>(list);
		indexList = new JList<>(listModel);
		indexList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScroller = new JScrollPane(indexList);
		indexList.setCellRenderer(new ListCellRenderer<IndexItem>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends IndexItem> list, IndexItem value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				renderer.setText(value.getText());
				return renderer;
			}
		});
		indexList.addListSelectionListener(e -> updateHtmlPaneWithIndexterm(mainApp));
		indexList.addFocusListener(new FocusGainedListener(() -> updateHtmlPaneWithIndexterm(mainApp)));
		add(listScroller);

		resultModel = new MiniHelpIndexListModel<>(Collections.emptyList());
		resultList = new JList<>(resultModel);
		JScrollPane resultScroller = new JScrollPane(resultList);
		resultList.addListSelectionListener(e -> updateHtmlPaneWithIndexentry(mainApp));
		resultList.addFocusListener(new FocusGainedListener(() -> updateHtmlPaneWithIndexentry(mainApp)));
		add(resultScroller);
	}

}
