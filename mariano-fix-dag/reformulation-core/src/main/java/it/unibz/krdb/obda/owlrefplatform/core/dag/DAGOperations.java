package it.unibz.krdb.obda.owlrefplatform.core.dag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAGOperations {

	private static final Logger log = LoggerFactory
			.getLogger(DAGOperations.class);

	// /**
	// * Adds two edges between child and parent. The first edge is a child
	// * hasParent edge, the second edge is a parent hasChild edge. The method
	// * guarantees no duplicate edges in any single node, e.g., no node will
	// have
	// * the edge A hasParent B two times.
	// *
	// * @param childnode
	// * @param parentnode
	// */
	// public static void addParentEdge(DAGNode childnode, DAGNode parentnode) {
	//
	// if (childnode.equals(parentnode)) {
	// return;
	// }
	//
	// childnode.getParents().add(parentnode);
	// parentnode.getChildren().add(childnode);
	// }
	//
	// /**
	// * Removes the hasParent and hasChild edges between the two nodes, if they
	// * exist. The method also guarantees that if as a result of this operation
	// * the Node's children or parents list becomes empty, then they will be
	// * assigned NULL. This is done in order to gurantee that at any time, if a
	// * Node has no children or parents, then the correspondent collections
	// will
	// * be NULL.
	// *
	// * @param childnode
	// * @param parentnode
	// */
	// private static void removeParentEdge(DAGNode childnode, DAGNode
	// parentnode) {
	// childnode.getParents().remove(parentnode);
	// parentnode.getChildren().remove(childnode);
	// }

}
