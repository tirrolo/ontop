/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.obda.codec.xml.query.XMLReader;
import inf.unibz.it.obda.codec.xml.query.XMLRenderer;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerTreeModel;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryGroupTreeElement;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryTreeElement;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.TreeElement;

import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// import edu.stanford.smi.protegex.owl.model.OWLModel;

/***
 * Controller for the query manager
 */
public class QueryController {

	private static QueryController			instance	= null;
	private Vector<TreeElement>				collection	= null;
	private Vector<QueryControllerListener>	listeners	= null;
	private QueryControllerTreeModel		treemodel	= null;

	public QueryController() {

		collection = new Vector<TreeElement>();
		listeners = new Vector<QueryControllerListener>();
		this.treemodel = new QueryControllerTreeModel(this);
		addListener(treemodel);
	}

	public QueryControllerTreeModel getTreeModel() {
		return treemodel;
	}

	public void addListener(QueryControllerListener listener) {
		if (listeners.contains(listener))
			return;
		listeners.add(listener);
	}

	public void removeListener(QueryControllerListener listener) {
		listeners.remove(listener);
	}

	//TODO remove static method, no more static Controllers
	/***
	 * @deprecated
	 */
//	public static QueryController getInstance() {
//		// if (model==null)
//		// return new QueryController();
//		// if (instances == null) {
//		// instances = new HashMap<OWLModel, QueryController>();
//		// }
//		// QueryController cinstance = instances.get(model);
//		if (instance == null) {
//			instance = new QueryController();
//			// instances.put(model, cinstance);
//		}
//		return instance;
//	}

	public void createGroup(String group_name) {

		if (getElementPosition(group_name) == -1) {
			QueryGroupTreeElement group = new QueryGroupTreeElement(group_name);
			collection.add(group);
			fireElementAdded(group);
		} else {

			System.out.println("Group already exists!");
		}
	}

	public void removeGroup(String group_name) {
		for (Iterator<TreeElement> iterator = collection.iterator(); iterator.hasNext();) {
			TreeElement element = (TreeElement) iterator.next();
			if (element instanceof QueryGroupTreeElement) {
				QueryGroupTreeElement group = (QueryGroupTreeElement) element;
				if (group.getID().equals(group_name)) {
					collection.remove(group);
					fireElementRemoved(group);
					return;
				}
			}
		}

	}

	public void addQuery(String querystr, String id) {

		if (getElementPosition(id) == -1) {
			QueryTreeElement query = new QueryTreeElement(id);
			query.setQuery(querystr);
			collection.add(query);
			fireElementAdded(query);
		} else {

			System.out.println("Query already exists!");
		}
	}

	public void removeAllQueriesAndGroups() {
		Vector<TreeElement> elements = getElements();

		for (TreeElement treeElement : elements) {
			fireElementRemoved(treeElement);
		}
		collection.removeAllElements();
	}

	public void addQuery(String querystr, String id, String groupid) {

		if (getElementPosition(id) == -1) {
			QueryTreeElement query = new QueryTreeElement(id);
			query.setQuery(querystr);
			QueryGroupTreeElement group = getGroup(groupid);
			group.addQuery(query);
			fireElementAdded(query, group);
		} else {

			System.out.println("Query already exists!");
		}
	}

	public void removeQuery(String id) {
		int index = getElementPosition(id);
		TreeElement element = (TreeElement) collection.get(index);
		if (element instanceof QueryTreeElement) {
			collection.remove(index);
			fireElementRemoved(element);
			return;
		} else {
			QueryGroupTreeElement group = (QueryGroupTreeElement) collection.get(index);
			QueryTreeElement query = group.removeQuery(id);
			fireElementRemoved(query, group);
			return;
		}

	}

	public void fromDOM(Element idconstraints) {
		NodeList xml_elements = idconstraints.getChildNodes();
		XMLReader xml_reader = new XMLReader();

		for (int i = 0; i < xml_elements.getLength(); i++) {
			Node node = xml_elements.item(i);
			if (node instanceof Element) {
				Element element = (Element) xml_elements.item(i);
				if (element.getNodeName().equals("Query")) {
					QueryTreeElement query = xml_reader.readQuery(element);
					addQuery(query.getQuery(), query.getID());
				} else if ((element.getNodeName().equals("QueryGroup"))) {
					QueryGroupTreeElement group = xml_reader.readQueryGroup(element);
					createGroup(group.getID());
					Vector<QueryTreeElement> queries = group.getQueries();
					for (QueryTreeElement query : queries) {
						addQuery(query.getQuery(), query.getID(), group.getID());
					}
				}
			}
		}
	}

	public Element toDOM(Element parent) {
		Document doc = parent.getOwnerDocument();
		XMLRenderer xmlrendrer = new XMLRenderer();
		Element savedqueries = doc.createElement("SavedQueries");
		for (TreeElement element : collection) {
			Element xmlconstraint = xmlrendrer.render(savedqueries, element);
			savedqueries.appendChild(xmlconstraint);
		}
		return savedqueries;
	}

	public QueryGroupTreeElement getGroup(String groupid) {
		int index = getElementPosition(groupid);
		if (index == -1)
			return null;
		QueryGroupTreeElement group = (QueryGroupTreeElement) collection.get(index);
		return group;
	}

	/***************************************************************************
	 * Returns the index of the element in the vector. If its is a query and the
	 * query is found inside a query group. The position of the group is
	 * returned instead.
	 * 
	 * @param element_id
	 * @return
	 */
	public int getElementPosition(String element_id) {
		int index = -1;

		for (int i = 0; i < collection.size(); i++) {
			TreeElement element = (TreeElement) collection.get(i);

			if (element.getID().equals(element_id)) {
				index = i;
				break;
			}

			if (element instanceof QueryTreeElement) {
				QueryTreeElement query = (QueryTreeElement) element;
				if (query.getID().equals(element_id)) {
					index = i;
					break;
				}
			} else {

				/***************************************************************
				 * Searching inside the group.
				 */
				QueryGroupTreeElement group = (QueryGroupTreeElement) element;
				{
					Vector<QueryTreeElement> queries_ingroup = group.getQueries();
					for (QueryTreeElement query : queries_ingroup) {
						if (query.getID().equals(element_id)) {
							index = i;
							break;
						}
					}
				}
			}
		}
		return index;
	}

	public Vector<TreeElement> getElements() {
		return this.collection;
	}

	public Vector<QueryGroupTreeElement> getGroups() {
		Vector<QueryGroupTreeElement> groups = new Vector<QueryGroupTreeElement>();
		for (TreeElement element : collection) {
			if (element instanceof QueryGroupTreeElement) {
				groups.add((QueryGroupTreeElement) element);
			}
		}
		return groups;
	}

	public void fireElementAdded(TreeElement element) {
		for (QueryControllerListener listener : listeners) {
			listener.elementAdded(element);
		}
	}

	public void fireElementAdded(QueryTreeElement query, QueryGroupTreeElement group) {
		for (QueryControllerListener listener : listeners) {
			listener.elementAdded(query, group);
		}
	}

	public void fireElementRemoved(TreeElement element) {
		for (QueryControllerListener listener : listeners) {
			listener.elementRemoved(element);
		}
	}

	public void fireElementRemoved(QueryTreeElement query, QueryGroupTreeElement group) {
		for (QueryControllerListener listener : listeners) {
			listener.elementRemoved(query, group);
		}
	}

}
