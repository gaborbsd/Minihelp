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

import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.kovesdan.minihelp.xml.TOCItem;

class MiniHelpContents extends JTree implements TreeSelectionListener {
	private static final long serialVersionUID = 1L;

	private MiniHelp mainApp;
	private List<TOCItem> tableOfContents;

	private void createLeaves(DefaultMutableTreeNode parent, List<TOCItem> items) {
		for (TOCItem i : items) {
			DefaultMutableTreeNode entry = (i.getTarget() != null && !i.getTarget().isEmpty())
					? new DefaultMutableTreeNode(new LinkInfo(i.getText(), i.getTarget()))
					: new DefaultMutableTreeNode(i.getText());
			parent.add(entry);
			if (i.getTOCItems() != null && !i.getTOCItems().isEmpty())
				createLeaves(entry, i.getTOCItems());
		}
	}

	public MiniHelpContents(List<TOCItem> tableOfContents, MiniHelp mainApp) {
		this.mainApp = mainApp;
		this.tableOfContents = tableOfContents;
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		updateModel();
	}
	
	public void updateModel() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Documentation");
		DefaultTreeModel model = new DefaultTreeModel(top);
		this.setModel(model);
		setShowsRootHandles(true);
		createLeaves(top, tableOfContents);
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();

		if (node == null)
			return;

		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof LinkInfo) {
			LinkInfo linkInfo = (LinkInfo) nodeInfo;
			mainApp.displayPageForTarget(linkInfo.getTarget());
		}
	}
}
