/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.gui.swing.querycontroller.tree;

import inf.unibz.it.obda.api.controller.QueryController;
import inf.unibz.it.obda.api.controller.QueryControllerListener;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class QueryControllerTreeModel extends DefaultTreeModel implements QueryControllerListener {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5182895959682699380L;
	private QueryController	qc	= null;

	public QueryControllerTreeModel(QueryController qc) {
		super(new DefaultMutableTreeNode(""));
		this.qc = qc;
//		QueryController.getInstance();
		
//		controller.addListener(this);
	}

	/***************************************************************************
	 * Takes all the existing nodes and constructs the tree.
	 */
	public void init() {
		
		Vector<TreeElement> elements = qc.getElements();
		if (elements.size() > 0) {
			for (TreeElement treeElement : elements) {
				if (treeElement instanceof QueryGroupTreeElement) {
					QueryGroupTreeElement group = (QueryGroupTreeElement) treeElement;
					Vector<QueryTreeElement> queries = group.getQueries();
					for (QueryTreeElement query : queries) {
						insertNodeInto(query, (DefaultMutableTreeNode) group, group.getChildCount());
					}
					insertNodeInto(group, (DefaultMutableTreeNode) root, root.getChildCount());
				} else {
					QueryTreeElement query = (QueryTreeElement) treeElement;
					insertNodeInto(query, (DefaultMutableTreeNode) root, root.getChildCount());
				}
			}
			nodeStructureChanged(root);
		}
	}

	public void elementAdded(TreeElement element) {
		insertNodeInto(element, (DefaultMutableTreeNode) root, root.getChildCount());
	}

	public void elementRemoved(TreeElement element) {
		removeNodeFromParent(element);
	}

	public void elementAdded(QueryTreeElement query, QueryGroupTreeElement group) {
		insertNodeInto(query, (DefaultMutableTreeNode) group, group.getChildCount());
	}

	public void elementRemoved(QueryTreeElement query, QueryGroupTreeElement group) {
		removeNodeFromParent(query);
	}

}
