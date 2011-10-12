package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGEdgeIterator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.Edge;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.coode.obo.parser.IsATagValueHandler;

/***
 * An optimizer that will eliminate equivalences implied by the ontology,
 * simplifying the vocabulary of the ontology. This allows to reduce the number
 * of inferences implied by the onotlogy and eliminating redundancy. The output
 * is two components: a "equivalence map" that functional mapping from class
 * (property) expression to class (property) that can be used to retrieve the
 * class (property) of the optimized ontology that should be used instead of one
 * class (property) that has been removed; a TBox T' that has a simpler
 * vocabulary.
 * 
 * 
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class EquivalenceTBoxOptimizer {

	private Ontology optimalTBox = null;
	private Map<Predicate, Description> equivalenceMap;
	private Ontology tbox;

	private final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public EquivalenceTBoxOptimizer(Ontology tbox) {
		this.tbox = tbox;
		equivalenceMap = new HashMap<Predicate, Description>();
	}

	/***
	 * Optimize will compute the implied hierachy of the given ontology and use
	 * DAGConstructor utilities to remove any cycles (compute equivalence
	 * classes). Then for each equivalent set (of classes/roles) it will keep
	 * only one representative and replace reference to any other node in the
	 * equivalence set with reference to the representative. The equivalences
	 * will be kept in an equivalence map, that relates classes/properties with
	 * its equivalent. Note that the equivalent of a class can only be another
	 * class, an the equivalent of a property can be another property, or the
	 * inverse of a property.
	 * 
	 * The resulting ontology will only
	 */
	public void optimize() {
		DAG impliedDAG = DAGConstructor.getDAG(tbox);
		impliedDAG.clean();

		try {
			GraphGenerator.dumpISA(impliedDAG, "isa-indexed");
		} catch (Exception e) {
			System.out.println("imposible");
		}

		/*
		 * Processing all properties
		 */

		Collection<Description> props = new HashSet<Description>(impliedDAG
				.getAllnodes().keySet());
		HashSet<Property> removedNodes = new HashSet<Property>();

		for (Description desc : props) {
			if (!(desc instanceof Property))
				continue;

			if (removedNodes.contains((Property) desc))
				continue;

			Property nonRedundantProperty = (Property) desc;
			Property nonRedundantInverseProperty = ofac.createProperty(
					nonRedundantProperty.getPredicate(),
					!nonRedundantProperty.isInverse());

			DAGNode nonRedndantNode = impliedDAG.get(nonRedundantProperty);
			// note, this could be null
			DAGNode nonRedundandInvNode = impliedDAG
					.get(nonRedundantInverseProperty);

			Collection<DAGNode> equivalents = new LinkedList<DAGNode>(nonRedndantNode.getEquivalents());

			for (DAGNode redundantNode : equivalents) {

				if (removedNodes.contains(redundantNode)) {
					continue;
				}

				/*
				 * each of the equivalents is redundant, we need to elimitate
				 * them and their inverses, and we need to set pointers in the
				 * equivalence map to the substitue node for them (the current
				 * description)
				 */

				/*
				 * Setting up the equivalence map
				 */
				Property equivalentProperty = (Property) redundantNode
						.getDescription();

				if (equivalentProperty.isInverse()) {
					/*
					 * We need to invert the equivalent and the good property
					 */
					Property inverseProp = ofac.createProperty(
							nonRedundantProperty.getPredicate(),
							!nonRedundantProperty.isInverse());
					equivalenceMap.put(equivalentProperty.getPredicate(),
							inverseProp);
				} else {
					equivalenceMap.put(equivalentProperty.getPredicate(),
							nonRedundantProperty);
				}

				/*
				 * Eliminating the redundant nodes
				 */

				impliedDAG.removeEquivalence(redundantNode.getDescription());
				impliedDAG.removeNode(redundantNode);
				
				
				DAGNode redundandInvNode = impliedDAG.getNode(ofac.createProperty(
						equivalentProperty.getPredicate(),
						!equivalentProperty.isInverse()));
				impliedDAG.removeEquivalence(redundantNode.getDescription());
				

				if (redundandInvNode != null) {
					impliedDAG.removeNode(redundandInvNode);
				} else {
//					/*
//					 * The inverse of the redundant node is null, this means
//					 * that its only accesible through an equivalence relation.
//					 * We need to get the main node for this relation and
//					 * replace that node, stablish the proper equivalences too.
//					 */
//					DAGNode redundantInvNodeEq = impliedDAG.getNode();
//					
				}

//				removedNodes.add((Property) redundantNode.getDescription());

//				nonRedndantNode.getEquivalents().remove(redundantNode);
//				nonRedundandInvNode.getEquivalents().remove(redundandInvNode);

			}
			// We clear all equivalents!
			nonRedndantNode.getDescendants().removeAll(equivalents);
		}

		/*
		 * Replacing any \exists R for \exists S, in case R was replaced by S
		 * due to an equivalence
		 */

		for (Property prop : removedNodes) {

			Predicate redundantProp = prop.getPredicate();
			Property equivalent = (Property) equivalenceMap.get(redundantProp);

			/* first for the domain */

			DAGNode directRedundantNode = impliedDAG.getNode(ofac
					.getPropertySomeRestriction(redundantProp, false));
			DAGNode equivalentNode = impliedDAG.getNode(ofac
					.getPropertySomeRestriction(equivalent.getPredicate(),
							equivalent.isInverse()));

			/***
			 * modified
			 */
			// equivalentNode.getEquivalents().remove(directRedundantNode);

			// impliedDAG.getClasses().remove(directRedundantNode);
			// impliedDAG.getAllnodes().remove(directRedundantNode);

			// mofieid impliedDAG.removeNode(directRedundantNode);

			for (DAGNode child : directRedundantNode.getChildren()) {
				if (!equivalentNode.getChildren().contains(child)) {
					equivalentNode.getChildren().add(child);
				}
			}

			for (DAGNode parent : directRedundantNode.getParents()) {
				if (!equivalentNode.getParents().contains(parent)) {
					equivalentNode.getParents().add(parent);
				}
			}

			equivalentNode.getDescendants().remove(directRedundantNode);

			/* Now for the range */

			directRedundantNode = impliedDAG.getNode(ofac
					.getPropertySomeRestriction(redundantProp, true));
			equivalentNode = impliedDAG.getNode(ofac
					.getPropertySomeRestriction(equivalent.getPredicate(),
							!equivalent.isInverse()));

			/***
			 * modified
			 */
			// equivalentNode.getEquivalents().remove(directRedundantNode);
			// impliedDAG.getClasses().remove(directRedundantNode);
			// impliedDAG.getAllnodes().remove(directRedundantNode);

			// original impliedDAG.removeNode(directRedundantNode);

			for (DAGNode child : directRedundantNode.getChildren()) {
				if (!equivalentNode.getChildren().contains(child)) {
					equivalentNode.getChildren().add(child);
				}
			}

			for (DAGNode parent : directRedundantNode.getParents()) {
				if (!equivalentNode.getParents().contains(parent)) {
					equivalentNode.getParents().add(parent);
				}
			}

			equivalentNode.getDescendants().remove(directRedundantNode);

			// TODO we need todo something with the references to this node in
			// the set "descenddants" of all the ancestors of this node,
			// however, we dont have an ancestors set...
		}

		/*
		 * Processing all classes
		 */
		Collection<DAGNode> classNodes = impliedDAG.getClasses();
		for (DAGNode classNode : classNodes) {
			if (!(classNode.getDescription() instanceof OClass))
				continue;
			OClass classDescription = (OClass) classNode.getDescription();

			Collection<DAGNode> redundantClasses = new LinkedList<DAGNode>();
			Collection<DAGNode> replacements = new HashSet<DAGNode>();

			for (DAGNode equivalentNode : classNode.getEquivalents()) {
				Description descEquivalent = equivalentNode.getDescription();
				if (descEquivalent instanceof OClass) {
					/*
					 * Its a named class, we need to remove it
					 */
					OClass equiClass = (OClass) descEquivalent;
					equivalenceMap.put(equiClass.getPredicate(),
							classDescription);
					redundantClasses.add(equivalentNode);
				} else {

					/*
					 * Its an \exists R, we need to make sure that it references
					 * to the proper vocabulary
					 */
					PropertySomeRestriction existsR = (PropertySomeRestriction) descEquivalent;
					Property prop = ofac.createProperty(existsR.getPredicate());
					Property equiProp = (Property) equivalenceMap.get(prop);
					if (equiProp == null)
						continue;

					/*
					 * This \exists R indeed referes to a property that was
					 * removed in the previous step and replace for some other
					 * property S. we need to eliminate the \exists R, and add
					 * an \exists S
					 */
					PropertySomeRestriction replacement = ofac
							.createPropertySomeRestriction(
									equiProp.getPredicate(),
									equiProp.isInverse());
					redundantClasses.add(equivalentNode);

					// WARNING this will delete all indexes!
					replacements.add(new DAGNode(replacement));
				}
			}

			classNode.getEquivalents().removeAll(redundantClasses);
			for (DAGNode replacement : replacements)
				classNode.getEquivalents().add(replacement);

		}

		/*
		 * Done with the simplificatino of the vocabulary, now we create the
		 * optimized ontology
		 */

		optimalTBox = ofac.createOntology();
		DAGEdgeIterator it = new DAGEdgeIterator(impliedDAG);
		while (it.hasNext()) {
			Edge edge = it.next();
			Axiom axiom = null;
			/*
			 * Creating subClassOf or subPropertyOf axioms
			 */
			if (edge.getLeft().getDescription() instanceof ClassDescription) {
				axiom = ofac.createSubClassAxiom((ClassDescription) edge
						.getLeft().getDescription(), (ClassDescription) edge
						.getRight().getDescription());
			} else {
				axiom = ofac.createSubPropertyAxiom((Property) edge.getLeft()
						.getDescription(), (Property) edge.getRight()
						.getDescription());
			}
			optimalTBox.addEntities(axiom.getReferencedEntities());
			optimalTBox.addAssertion(axiom);
		}

		/*
		 * Last, we add references to all the vocabulary of the previous TBox
		 */
		Set<Predicate> extraVocabulary = new HashSet<Predicate>();
		extraVocabulary.addAll(tbox.getVocabulary());
		extraVocabulary.removeAll(equivalenceMap.keySet());
		optimalTBox.addEntities(extraVocabulary);

	}

	public Ontology getOptimalTBox() {
		return this.optimalTBox;
	}

	public Map<Predicate, Description> getEquivalenceMap() {
		return this.equivalenceMap;
	}
}
