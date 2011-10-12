package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DAGNode implements Cloneable {

	/***
	 * The description associated to this node
	 */
	private final Description description;

	/***
	 * The list of ranges associated to this node. Can be equal to
	 * DAG.NULL_RANGE;
	 */
	private SemanticIndexRange range = SemanticIndexRange.NULL_RANGE;

	/***
	 * The index for this node, may be DAG.NUL_INDEX
	 */
	private int index = SemanticIndexRange.NULL_INDEX;

	/***
	 * The set of parents, children, descendants and equivalents for this node.
	 * The sets keep the original order in which the nodes where added.
	 */
	private Set<DAGNode> parents = new LinkedHashSet<DAGNode>();
	private Set<DAGNode> children = new LinkedHashSet<DAGNode>();
	private Set<DAGNode> descendants = new LinkedHashSet<DAGNode>();
	private Set<DAGNode> equivalents = new LinkedHashSet<DAGNode>();

	/*
	 * The string representation of this node
	 */
	String string = "";

	/*
	 * The current value of the hash.
	 */
	int hashcode = 0;

	/*
	 * Flags to trigger the computation of hash or string.
	 */
	boolean hashNeedsUpdate = true;

	boolean stringNeedsUpdate = true;

	public DAGNode(Description description) {
		this.description = description;
		computeHash();
		computeString();
	}

	/***
	 * Does a deep copy of this node, except for the list of equivalents,
	 * children, descendants and parents. In those cases, the lists are new, but
	 * the reference are still to the original objects that are not cloned by
	 * this method.
	 */
	@Override
	public DAGNode clone() {
		DAGNode clone = new DAGNode(this.description.clone());

		Set<DAGNode> list = null;

		list = clone.getChildren();
		for (DAGNode node : children) {
			list.add(node);
		}

		list = clone.getParents();
		for (DAGNode node : parents) {
			list.add(node);
		}

		list = clone.getDescendants();
		for (DAGNode node : descendants) {
			list.add(node);
		}

		list = clone.getEquivalents();
		for (DAGNode node : equivalents) {
			list.add(node);
		}

		clone.index = index;
		clone.range = range.clone();

		return clone;
	}

	/***
	 * The hash of a node is based solely on the hash of the description of the
	 * node.
	 */
	private void computeHash() {
		if (!hashNeedsUpdate)
			return;

		hashcode = description != null ? description.hashCode() : 0;

		hashNeedsUpdate = false;
	}

	private void computeString() {
		if (!stringNeedsUpdate)
			return;
		StringBuilder bf = new StringBuilder();
		bf.append("N{");
		if (description instanceof PropertySomeRestriction) {
			bf.append("E");
			bf.append(((PropertySomeRestriction) description).getPredicate()
					.getName().getFragment());
			if (((PropertySomeRestriction) description).isInverse())
				bf.append("^-");
		}

		if (description instanceof OClass) {
			bf.append(((OClass) description).getPredicate().getName()
					.getFragment());
		}
		if (description instanceof Property) {
			bf.append(((Property) description).getPredicate().getName()
					.getFragment());
			if (((Property) description).isInverse()) {
				bf.append("^-");
			}
		}

		bf.append(",R=");
		bf.append(range);
		bf.append(",I=");
		bf.append(index);
		bf.append('}');

		string = bf.toString();
		stringNeedsUpdate = false;
	}

	/***
	 * Node equality is defined by the class description.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (this.getClass() != other.getClass())
			return false;

		DAGNode otherNode = (DAGNode) other;
		return this.description.equals(otherNode.description);
	}

	/***
	 * Optimized so that strings are only computed once, or when there has been
	 * a change in the node.
	 */
	@Override
	public String toString() {
		computeString();
		return string;
	}

	/***
	 * The hash code is overriden so that two nodes have the same hashcode only
	 * if they are equal, as defined by the equals() function.
	 * 
	 * Computation of the hash is done at creation time, or at request if there
	 * was a change in the object.
	 */
	@Override
	public int hashCode() {
		computeHash();
		return hashcode;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
		hashNeedsUpdate = true;
		stringNeedsUpdate = true;
	}

	public Set<DAGNode> getParents() {
		return parents;
	}

	public void setRange(SemanticIndexRange range) {
		this.range = range;
		hashNeedsUpdate = true;
		stringNeedsUpdate = true;
	}

	public SemanticIndexRange getRange() {
		return this.range;

	}

	public Set<DAGNode> getChildren() {
		return children;
	}

	public Set<DAGNode> getEquivalents() {
		return equivalents;
	}

	public void setDescendants(Set<DAGNode> descendans) {
		this.descendants = descendans;
	}

	public Set<DAGNode> getDescendants() {
		return descendants;
	}

	public void setChildren(Set<DAGNode> children) {
		this.children = children;
		hashNeedsUpdate = true;
		stringNeedsUpdate = true;
	}

	public void setParents(Set<DAGNode> parents) {
		this.parents = parents;
		hashNeedsUpdate = true;
		stringNeedsUpdate = true;
	}

	public Description getDescription() {
		return description;
	}
}
