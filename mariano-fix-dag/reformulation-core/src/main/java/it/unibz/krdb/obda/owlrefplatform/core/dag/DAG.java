package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubPropertyAxiomImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAG implements Cloneable {

	private final Logger log = LoggerFactory.getLogger(DAG.class);

	public Map<Description, Description> equi_mappings = new HashMap<Description, Description>();

	private final Map<Description, DAGNode> allnodes;

	private final OntologyFactory descFactory = new OntologyFactoryImpl();

	public DAG() {
		allnodes = new HashMap<Description, DAGNode>();
	}

	/**
	 * Build the DAG from the ontology
	 * 
	 * @param ontology
	 *            ontology that contain TBox assertions for the DAG
	 */
	public DAG(Ontology ontology) {

		int rolenodes = ontology.getRoles().size() * 2;

		int classnodes = ontology.getConcepts().size() + rolenodes * 2;

		allnodes = new HashMap<Description, DAGNode>(
				(rolenodes + classnodes) * 2);

		// classes.put(thingConcept, thing);

		for (Predicate conceptp : ontology.getConcepts()) {
			ClassDescription concept = descFactory.createClass(conceptp);
			DAGNode node = new DAGNode(concept);

			// if (!concept.equals(thingConcept)) {
			// addParent(node, thing);
			// classes.put(concept, node);

			allnodes.put(concept, node);

			// }
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		for (Predicate rolep : ontology.getRoles()) {
			Property role = descFactory.createProperty(rolep);
			DAGNode rolenode = new DAGNode(role);

			Property roleInv = descFactory.createProperty(role.getPredicate(),
					!role.isInverse());
			DAGNode rolenodeinv = new DAGNode(roleInv);

			PropertySomeRestriction existsRole = descFactory
					.getPropertySomeRestriction(role.getPredicate(),
							role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory
					.getPropertySomeRestriction(role.getPredicate(),
							!role.isInverse());
			DAGNode existsNode = new DAGNode(existsRole);
			DAGNode existsNodeInv = new DAGNode(existsRoleInv);

			allnodes.put(role, rolenode);
			allnodes.put(existsRole, existsNode);
			allnodes.put(existsRoleInv, existsNodeInv);
			allnodes.put(roleInv, rolenodeinv);

		}

		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();

				addClassEdge(parent, child);
			} else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();

				// This adds the direct edge and the inverse, e.g., R ISA S and
				// R- ISA S-,
				// R- ISA S and R ISA S-
				addRoleEdge(parent, child);
			}
		}
		// clean();
	}

	public DAG clone() {

		/*
		 * clone first all nodes, including those metioned only in the
		 * equi_mappings
		 */
		DAG clonedag = new DAG();

		for (Description key : allnodes.keySet()) {
			clonedag.allnodes.put(key, allnodes.get(key).clone());
		}

		clonedag.equi_mappings.putAll(equi_mappings);

		/*
		 * The the pointers (equivalents, children, parents, descendants, etc)
		 * of all the clones of each node still point to the original nodes, we
		 * need to redirect the pointers to the clones.
		 */
		for (DAGNode clonenode : clonedag.allnodes.values()) {

			Set<DAGNode> newlinks = new LinkedHashSet<DAGNode>();
			for (DAGNode oldnode : clonenode.getChildren()) {
				DAGNode newnode = clonedag.allnodes.get(oldnode
						.getDescription());
				newlinks.add(newnode);
			}
			clonenode.getChildren().clear();
			clonenode.getChildren().addAll(newlinks);

			newlinks.clear();
			for (DAGNode oldnode : clonenode.getParents()) {
				DAGNode newnode = clonedag.allnodes.get(oldnode
						.getDescription());
				newlinks.add(newnode);
			}
			clonenode.getParents().clear();
			clonenode.getParents().addAll(newlinks);

			newlinks.clear();
			for (DAGNode oldnode : clonenode.getDescendants()) {
				DAGNode newnode = clonedag.allnodes.get(oldnode
						.getDescription());
				newlinks.add(newnode);

			}
			clonenode.getDescendants().clear();
			clonenode.getDescendants().addAll(newlinks);

			newlinks.clear();
			for (DAGNode oldnode : clonenode.getEquivalents()) {

				/***
				 * Here we use the clone since, if there are nodes in the
				 * equivalence list, these are only put there by the removeCycls
				 * method, which also removes teh node from the allnodes map.
				 * Since the only reference to the nodes in the equivalence
				 * lists of each node are in this equivalence lists, it is safe
				 * to just clone them.
				 */
				DAGNode newnode = oldnode.clone();
				// DAGNode newnode = clonedag.allnodes.get(oldnode
				// .getDescription());

				newlinks.add(newnode);
			}
			clonenode.getEquivalents().clear();
			clonenode.getEquivalents().addAll(newlinks);
		}

		return clonedag;

	}

	/***
	 * Adds an edges between two nodes, adding pointers to the parent and child
	 * lists of both.. If the two nodes are equal (a self-loop) the method will
	 * not do anything.
	 * 
	 * @param child
	 * @param parent
	 */
	public void addParent(DAGNode child, DAGNode parent) {
		if (child.getDescription().equals(parent.getDescription())) {
			return;
		}
		child.getParents().add(parent);
		parent.getChildren().add(child);
	}

	public void removeParent(DAGNode childnode, DAGNode parentnode) {
		childnode.getParents().remove(parentnode);
		parentnode.getChildren().remove(childnode);
	}

	/**
	 * The methods setups the equivalent relations between n1 and n2. Note that
	 * this doesn't guarantee that nodes that are equivalent to n2 and n1 are
	 * also set. For that purpose see {@see addAllEquivalences}.
	 * 
	 * @param node1
	 * @param node2
	 */
	public void addEquivalence(DAGNode node1, DAGNode node2) {
		if (node1.equals(node2))
			return;

		node1.getEquivalents().remove(node2);
		node1.getEquivalents().add(node2);

		node2.getEquivalents().remove(node1);
		node2.getEquivalents().add(node1);
	}

	/***
	 * Adds an edge between the nodes for two classes. The method takes care to
	 * get the correct nodes for the descriptions, if they are not found, it
	 * will create them.
	 * 
	 */
	public void addClassEdge(ClassDescription parent, ClassDescription child) {

		DAGNode parentNode;
		if (allnodes.containsKey(parent)) {
			parentNode = allnodes.get(parent);
		} else {
			parentNode = new DAGNode(parent);
			allnodes.put(parent, parentNode);
		}
		DAGNode childNode;
		if (allnodes.containsKey(child)) {
			childNode = allnodes.get(child);
		} else {
			childNode = new DAGNode(child);
			allnodes.put(child, childNode);
		}
		addParent(childNode, parentNode);

	}

	/***
	 * Adds role edge directly, without considering extra 'logical' edges. The
	 * method takes care to get the correct nodes for the descriptions, if they
	 * are not found, it will create them.
	 * 
	 * @param parent
	 * @param child
	 */
	public void addRoleEdgeSingle(Property parent, Property child) {
		DAGNode parentNode = allnodes.get(parent);
		if (parentNode == null) {
			parentNode = new DAGNode(parent);
			allnodes.put(parent, parentNode);
		}

		DAGNode childNode = allnodes.get(child);
		if (childNode == null) {
			childNode = new DAGNode(child);

			allnodes.put(parent, parentNode);
		}
		addParent(childNode, parentNode);
	}

	/***
	 * Adds a role edge C ISA P, and the edge that is its logical consequences
	 * e.g., the one one between the inverses C- ISA P-, EC ISA EP, EC- ISA EP-
	 * This method is usefull to create DAGs that contain all the logical
	 * consequences of a DL-Lite TBox.
	 * 
	 * @param parent
	 * @param child
	 */
	public void addRoleEdge(Property parent, Property child) {
		/* direct edge */
		addRoleEdgeSingle(parent, child);

		/* domain and range edges */

		ClassDescription existsParent = descFactory.getPropertySomeRestriction(
				parent.getPredicate(), parent.isInverse());

		ClassDescription existChild = descFactory.getPropertySomeRestriction(
				child.getPredicate(), child.isInverse());

		addClassEdge(existsParent, existChild);

		ClassDescription existsParentinv = descFactory
				.getPropertySomeRestriction(parent.getPredicate(),
						!parent.isInverse());

		ClassDescription existChildinv = descFactory
				.getPropertySomeRestriction(child.getPredicate(),
						!child.isInverse());

		addClassEdge(existsParentinv, existChildinv);

		/* inverse edge */
		addRoleEdgeSingle(
				descFactory.createProperty(parent.getPredicate(),
						!parent.isInverse()),
				descFactory.createProperty(child.getPredicate(),
						!child.isInverse()));

	}

	public void clean() {
		equi_mappings.clear();
		/*
		 * First we remove all cycles in roles, not that while doing so we might
		 * also need to colapse some nodes in the class hierarchy, i.e., those
		 * for \exists R and \exists R-
		 */
		removeCycles();
		computeTransitiveReduct();

		// DAGOperations.removeCycles(classes, equi_mappings, this);
		// DAGOperations.computeTransitiveReduct(classes);

	}

	public void computeTransitiveReduct() {
		buildDescendants();

		LinkedList<Edge> redundantEdges = new LinkedList<Edge>();
		for (DAGNode node : allnodes.values()) {
			for (DAGNode child : node.getChildren()) {
				for (DAGNode child_desc : child.getDescendants()) {
					if (child_desc == null)
						redundantEdges.add(new Edge(node, child_desc));
				}
			}
		}
		for (Edge edge : redundantEdges) {
			DAGNode from = edge.getLeft();
			DAGNode to = edge.getRight();

			from.getChildren().remove(to);
			to.getParents().remove(from);
		}
	}

	/**
	 * Calculate the descendants for all nodes in the given DAG
	 * 
	 * @param dagnodes
	 *            a DAG
	 * @return Map from uri to the Set of their descendants
	 */
	public void buildDescendants() {

		for (DAGNode nodes : allnodes.values())
			nodes.getDescendants().clear();

		Queue<DAGNode> stack = new LinkedList<DAGNode>();

		stack.addAll(getLeafNodes());

		if (stack.isEmpty() && !allnodes.values().isEmpty()) {
			log.error("Can not build descendants for graph with cycles");
		}

		while (!stack.isEmpty()) {
			DAGNode cur_el = stack.remove();

			for (DAGNode eq_node : cur_el.getEquivalents()) {
				if (!cur_el.getDescendants().contains(eq_node)) {
					cur_el.getDescendants().add(eq_node);
				}
			}

			for (DAGNode par_node : cur_el.getParents()) {

				// add child to descendants list
				if (!par_node.getDescendants().contains(cur_el)) {
					par_node.getDescendants().add(cur_el);
				}

				// add child children to descendants list
				for (DAGNode cur_el_descendant : cur_el.getDescendants()) {
					if (!par_node.getDescendants().contains(cur_el_descendant)) {
						par_node.getDescendants().add(cur_el_descendant);
					}
				}
				stack.add(par_node);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (DAGNode node : allnodes.values()) {
			res.append(node);
			res.append("\n");
		}
		return res.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (this.getClass() != other.getClass())
			return false;

		DAG otherDAG = (DAG) other;
		return this.allnodes.equals(otherDAG.allnodes);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result += 37 * result + this.allnodes.hashCode();
		return result;
	}

	/***
	 * Returns the all the nodes for class descriptions.
	 * 
	 * @return
	 */
	public Set<DAGNode> getClasses() {
		LinkedHashSet<DAGNode> classes = new LinkedHashSet<DAGNode>();
		for (DAGNode node : allnodes.values()) {
			if (node.getDescription() instanceof ClassDescription)
				classes.add(node);
		}
		return Collections.unmodifiableSet(classes);
	}

	/***
	 * Returns the all the nodes for property descriptions.
	 * 
	 * @return
	 */
	public Set<DAGNode> getRoles() {
		LinkedHashSet<DAGNode> properties = new LinkedHashSet<DAGNode>();
		for (DAGNode node : allnodes.values()) {
			if (node.getDescription() instanceof Property)
				properties.add(node);
		}
		return Collections.unmodifiableSet(properties);
	}

	/***
	 * Returns the node that is associated to the description in this DAG.
	 * 
	 * @param desc
	 * @return
	 */
	public DAGNode get(Description desc) {
		return allnodes.get(desc);
	}

	/***
	 * Returns the node associated to this description. It doesnt take into
	 * account equivalences.
	 * 
	 * @param description
	 * @return
	 */
	public DAGNode getNode(Description description) {
		DAGNode node = allnodes.get(description);
		if (node == null) {
			node = allnodes.get(equi_mappings.get(description));
		}
		return node;
	}

	public Map<Description, DAGNode> getAllnodes() {
		return Collections.unmodifiableMap(allnodes);
	}

	/***
	 * Removes a node N taking care to keep the transitive isa relation between
	 * all the rest of the nodes. That is, all the parents of N node become
	 * parents of the children of N, and all the children of N become children
	 * of the parents of N.
	 * 
	 * The node will also be removed from the list of nodes of the DAG.
	 * 
	 * Note, the descendants of each node are not updated by this operation. If
	 * a DAG has been modified with this function it is necessary to call
	 * buildDescendants() once more to recompute the relation.
	 * 
	 * @param node
	 */
	public void removeNode(DAGNode node) {
		Collection<DAGNode> parents = new LinkedList<DAGNode>(node.getParents());
		Collection<DAGNode> children = new LinkedList<DAGNode>(
				node.getChildren());

		for (DAGNode parent : parents) {
			removeParent(node, parent);
		}
		for (DAGNode child : children) {
			removeParent(child, node);
		}

		for (DAGNode parent : parents) {
			for (DAGNode child : children) {
				addParent(child, parent);
			}
		}
		allnodes.remove(node.getDescription());
	}

	/***
	 * Return all nodes with no parents, i.e., the roots of the DAG.
	 * 
	 * @return
	 */
	public Set<DAGNode> getRootNodes() {
		Set<DAGNode> roots = new LinkedHashSet<DAGNode>();
		for (DAGNode candiate : allnodes.values()) {
			if (candiate.getParents().isEmpty())
				roots.add(candiate);
		}
		return Collections.unmodifiableSet(roots);
	}

	/***
	 * Return all nodes with no parents, i.e., the roots of the DAG.
	 * 
	 * @return
	 */
	public Set<DAGNode> getLeafNodes() {
		Set<DAGNode> leafs = new LinkedHashSet<DAGNode>();
		for (DAGNode candiate : allnodes.values()) {
			if (candiate.getChildren().isEmpty())
				leafs.add(candiate);
		}
		return Collections.unmodifiableSet(leafs);
	}

	public Iterator<Edge> getTransitiveEdgeIterator() {
		return null;
	}

	/***
	 * Takes a DAG with nodes general concept and role descriptions and
	 * transforms it into one with nodes only for named concepts and roles,
	 * while keeping the original isa hierarchy.
	 * 
	 * Internally, the method will clone the original DAG and will work only on
	 * the clone.
	 * 
	 * It will start from leaf nodes and go up, replacing
	 * parents/child/descendent references to remove all nodes general
	 * descriptions.
	 * 
	 * Important, this method can only work in DAGs. Any cycles in the graph
	 * will generate inconsistent behaivor. Be sure to call removeCycles before
	 * you call this method.
	 * 
	 * @param dag
	 * @return
	 */
	public DAG filterPureISA() {
		DAG clone = this.clone();

		Queue<DAGNode> queue = new LinkedList<DAGNode>();
		queue.addAll(clone.getLeafNodes());

		while (!queue.isEmpty()) {
			DAGNode currentnode = queue.poll();

			queue.addAll(currentnode.getParents());

			/*
			 * Checking if the description corresponds to a named class or
			 * property, if so, we jsut advance
			 */
			Description desc = currentnode.getDescription();
			if (desc instanceof OClass)
				continue;
			if (desc instanceof Property) {
				Property prop = (Property) desc;
				if (!prop.isInverse())
					continue;
			}

			/*
			 * The description is a complex description, we need to remove the
			 * node.
			 */

			clone.removeNode(currentnode);
		}

		return clone;
	}

	public void removeCycles() {
		LinkedList<DAGNode> roles = new LinkedList<DAGNode>(getRoles());
		Collections.sort(roles, new DescriptionNodeComparator());

		removeCycles(roles);

		LinkedList<DAGNode> classes = new LinkedList<DAGNode>(getClasses());

		Collections.sort(classes, new DescriptionNodeComparator());
		removeCycles(classes);

	}

	public class DescriptionNodeComparator implements
			java.util.Comparator<DAGNode> {
		@Override
		public int compare(DAGNode arg0, DAGNode arg1) {

			if (arg0.getDescription() instanceof Property
					&& arg1.getDescription() instanceof Property) {
				Property d0 = (Property) arg0.getDescription();
				Property d1 = (Property) arg1.getDescription();

				if (!d0.isInverse() && d1.isInverse())
					return -1;
				else if (d0.isInverse() && !d1.isInverse())
					return 1;
				else
					return 0;
			} else if (arg0.getDescription() instanceof ClassDescription
					&& arg1.getDescription() instanceof ClassDescription) {
				if (arg0.getDescription() instanceof OClass
						&& !(arg1.getDescription() instanceof OClass))
					return -1;
				else if (arg1.getDescription() instanceof OClass
						&& !(arg0.getDescription() instanceof OClass))
					return 1;
				else
					return 0;
			} else
				return 0;
		}
	}

	public void removeCycles(Collection<DAGNode> nodes) {

		// Finding the cycles (strongly connected components)
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		CycleCollector cc = new CycleCollector();
		ArrayList<ArrayList<DAGNode>> sccs = cc.scc(nodes);

		/*
		 * A set with all the nodes that have been proceesed as participating in
		 * an equivalence cycle. If a component contains any of these nodes, the
		 * component should be ignored, since a cycle involving the same nodes
		 * or nodes for inverse descriptions has already been processed.
		 */
		Set<DAGNode> processedNodes = new HashSet<DAGNode>();

		for (ArrayList<DAGNode> component : sccs) {
			Collections.sort(component, new DescriptionNodeComparator());
			/*
			 * Avoiding processing nodes two times
			 */
			// boolean ignore = false;
			// for (DAGNode node : component) {
			// if (processedNodes.contains(node)) {
			// ignore = true;
			// break;
			// }
			// }
			// if (ignore) {
			// System.out.println("Ignoring");
			// continue;
			// }

			DAGNode cycleheadNode = component.get(0);
			DAGNode cycleheadinverseNode = null;
			DAGNode cycleheaddomainNode = null;
			DAGNode cycleheadrangeNode = null;

			if (cycleheadNode.getDescription() instanceof Property) {

				Property prop = (Property) cycleheadNode.getDescription();

				Property inverse = fac.createProperty(prop.getPredicate(),
						!prop.isInverse());
				PropertySomeRestriction domain = fac
						.createPropertySomeRestriction(prop.getPredicate(),
								prop.isInverse());
				PropertySomeRestriction range = fac
						.createPropertySomeRestriction(prop.getPredicate(),
								!prop.isInverse());

				cycleheadinverseNode = this.getNode(inverse);
				cycleheaddomainNode = this.getNode(domain);
				cycleheadrangeNode = this.getNode(range);
			}

			/*
			 * putting a cyclehead that is a named concept or named role
			 */
			// if (component.size() > 1
			// && cycleheadNode.getDescription() instanceof
			// PropertySomeRestriction) {
			//
			// for (int i = 1; i < component.size(); i++) {
			// if (component.get(i).getDescription() instanceof OClass) {
			// DAGNode tmp = component.get(i);
			// component.set(i, cycleheadNode);
			// component.set(0, tmp);
			// cycleheadNode = tmp;
			// break;
			// }
			// }
			// }

			if (component.size() > 0
					&& cycleheadNode.getDescription() instanceof Property
					&& ((Property) cycleheadNode.getDescription()).isInverse()) {
				for (int i = 1; i < component.size(); i++) {
					if (component.get(i).getDescription() instanceof Property
							&& !((Property) component.get(i).getDescription())
									.isInverse()) {
						DAGNode tmp = component.get(i);
						component.set(i, cycleheadNode);
						component.set(0, tmp);
						cycleheadNode = tmp;

						Property prop = (Property) cycleheadNode
								.getDescription();

						Property inverse = fac.createProperty(
								prop.getPredicate(), !prop.isInverse());
						PropertySomeRestriction domain = fac
								.createPropertySomeRestriction(
										prop.getPredicate(), prop.isInverse());
						PropertySomeRestriction range = fac
								.createPropertySomeRestriction(
										prop.getPredicate(), !prop.isInverse());

						cycleheadinverseNode = this.getNode(inverse);
						cycleheaddomainNode = this.getNode(domain);
						cycleheadrangeNode = this.getNode(range);

						break;
					}
				}
			}
			processedNodes.add(cycleheadNode);

			if (cycleheadinverseNode != null) {
				processedNodes.add(cycleheadinverseNode);
				processedNodes.add(cycleheaddomainNode);
				processedNodes.add(cycleheadrangeNode);
			}

			/*
			 * Collapsing the cycle (the nodes in the component)
			 */
			for (int i = 1; i < component.size(); i++) {

				DAGNode equivnode = component.get(i);

				replaceNode(equivnode, cycleheadNode);

				/***
				 * Setting up the equivalence map
				 */

				/*
				 * Removing all the nodes that have been collapsed
				 */

				allnodes.remove(equivnode.getDescription());

				setupEquivalence(cycleheadNode, equivnode);

				processedNodes.add(equivnode);

				if (false && cycleheadinverseNode != null) {
					/*
					 * we are dealing with properties, so we need to also
					 * collapse the inverses and existentials.
					 */
					Property equiprop = (Property) equivnode.getDescription();

					DAGNode equivinverseNode = this.getNode(fac.createProperty(
							equiprop.getPredicate(), !equiprop.isInverse()));
					DAGNode equivDomainNode = this.getNode(fac
							.createPropertySomeRestriction(
									equiprop.getPredicate(),
									equiprop.isInverse()));
					DAGNode equivRangeNode = this.getNode(fac
							.createPropertySomeRestriction(
									equiprop.getPredicate(),
									!equiprop.isInverse()));

					/*
					 * Doing the inverses
					 */
					replaceNode(equivinverseNode, cycleheadinverseNode);
					/*
					 * Doing the domain
					 */
					replaceNode(equivDomainNode, cycleheaddomainNode);

					/*
					 * Collapsing the range
					 */
					replaceNode(equivRangeNode, cycleheadrangeNode);

					/*
					 * Setting up equivalences and removing the replaced nodes
					 */
					setupEquivalence(cycleheadinverseNode, equivinverseNode);
					setupEquivalence(cycleheaddomainNode, equivDomainNode);
					setupEquivalence(cycleheadrangeNode, equivRangeNode);

					processedNodes.add(equivinverseNode);
					processedNodes.add(equivDomainNode);
					processedNodes.add(equivRangeNode);

				}

			}
		}
	}

	/***
	 * Setups an equivalence link from node1 into node2 and removes n2 from the
	 * DAG.
	 */
	public void setupEquivalence(DAGNode n1, DAGNode n2) {
		allnodes.remove(n2.getDescription());

		equi_mappings.put(n2.getDescription(), n1.getDescription());
		n1.getEquivalents().add(n2);
	}

	/***
	 * Removes the equivalence pointer to the Node N that is currently setup for
	 * description D. And removes the node for D, from N.equivalences.
	 * 
	 * This efectively removes any traces of a node that was collapsed by cycle
	 * elimination.
	 * 
	 * @param d
	 * @param node
	 */
	public void removeEquivalence(Description d) {
		DAGNode mainnode = allnodes.get(equi_mappings.get(d));
		DAGNode nodeD = null;
		for (DAGNode n : mainnode.getEquivalents()) {
			if (n.getDescription().equals(d)) {
				nodeD = n;
				break;
			}
		}
		mainnode.getEquivalents().remove(nodeD);
		equi_mappings.remove(d);
	}

	/***
	 * An implementation of the connected components algorithm for detection of
	 * cycles in graphs.
	 * 
	 */
	private class CycleCollector {
		private int index = 0;
		private ArrayList<DAGNode> stack;
		private ArrayList<ArrayList<DAGNode>> SCC;

		private Map<DAGNode, Integer> t_idx;
		private Map<DAGNode, Integer> t_low_idx;

		private ArrayList<ArrayList<DAGNode>> scc(Collection<DAGNode> list) {
			stack = new ArrayList<DAGNode>();
			SCC = new ArrayList<ArrayList<DAGNode>>();
			t_idx = new HashMap<DAGNode, Integer>();
			t_low_idx = new HashMap<DAGNode, Integer>();
			for (DAGNode node : list) {
				if (t_idx.get(node) == null) {
					strongconnect(node);
				}
			}
			return SCC;
		}

		private void strongconnect(DAGNode v) {
			t_idx.put(v, index);
			t_low_idx.put(v, index);

			index++;
			stack.add(0, v);
			for (DAGNode child : v.getChildren()) {
				if (t_idx.get(child) == null) {
					strongconnect(child);
					t_low_idx.put(v,
							Math.min(t_low_idx.get(v), t_low_idx.get(child)));
				} else if (stack.contains(child)) {
					t_low_idx.put(v,
							Math.min(t_low_idx.get(v), t_idx.get(child)));
				}
			}
			if (t_low_idx.get(v).equals(t_idx.get(v))) {
				DAGNode n;
				ArrayList<DAGNode> component = new ArrayList<DAGNode>();
				do {
					n = stack.remove(0);
					component.add(n);
				} while (!n.equals(v));
				SCC.add(component);
			}
		}
	}

	/***
	 * Removes all references to oldnode from the child/parent list of the
	 * oldnode, and replaces them for references to "newnode"
	 * 
	 * @param oldnode
	 * @param newnode
	 */
	public void replaceNode(DAGNode oldnode, DAGNode newnode) {
		for (DAGNode parent : new LinkedList<DAGNode>(oldnode.getParents())) {
			removeParent(oldnode, parent);
			addParent(newnode, parent);
		}

		for (DAGNode childchild : new LinkedList<DAGNode>(oldnode.getChildren())) {
			removeParent(childchild, oldnode);
			addParent(childchild, newnode);
		}
	}

	/***
	 * SEMANTIC INDEX FUNCTIONS
	 */

	/***
	 * Indexes each node in a depth first way, as defined by the semantic index
	 * technique. Note, indexing should be donde only for DAGs contaiing the
	 * hierarchies between named classes and properties, not with general DAGs.
	 */
	public void index() {

		int indexcounter[] = { 1 };

		Set<DAGNode> roots = getRootNodes();

		Set<DAGNode> namedroles = new LinkedHashSet<DAGNode>();
		for (DAGNode node : getRootNodes()) {
			if (node.getDescription() instanceof Property) {

				Property p = (Property) node.getDescription();
				if (!p.isInverse())
					namedroles.add(node);
			}
		}

		for (DAGNode node : namedroles) {
			indexNode(node, indexcounter);
		}

		Set<DAGNode> namedclasses = new LinkedHashSet<DAGNode>();
		for (DAGNode node : getRootNodes()) {
			if (node.getDescription() instanceof OClass) {
				namedclasses.add(node);
			}
		}
		for (DAGNode node : namedclasses) {
			indexNode(node, indexcounter);
		}

		for (DAGNode node : namedclasses) {
			mergeRangeNode(node);
		}
		for (DAGNode node : namedroles) {
			mergeRangeNode(node);
		}
	}

	private void mergeRangeNode(DAGNode node) {

		for (DAGNode ch : node.getChildren()) {
			if (ch != node) {
				mergeRangeNode(ch);
				node.getRange().addRange(ch.getRange());
			}

		}
	}

	private void indexNode(DAGNode node, int[] index_counter) {

		if (node.getIndex() == SemanticIndexRange.NULL_INDEX) {
			node.setIndex(index_counter[0]);
			node.setRange(new SemanticIndexRange(index_counter[0],
					index_counter[0]));
			index_counter[0]++;
		} else {
			return;
		}

		for (DAGNode ch : node.getChildren()) {
			if (ch != node) {
				indexNode(ch, index_counter);
			}
		}
	}

}
