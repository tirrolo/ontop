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
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryGroupTreeElement;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryTreeElement;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.TreeElement;

public interface QueryControllerListener {
	public void elementAdded(TreeElement element);
	public void elementAdded(QueryTreeElement query, QueryGroupTreeElement group);
	public void elementRemoved(TreeElement element);
	public void elementRemoved(QueryTreeElement query, QueryGroupTreeElement group);
}
