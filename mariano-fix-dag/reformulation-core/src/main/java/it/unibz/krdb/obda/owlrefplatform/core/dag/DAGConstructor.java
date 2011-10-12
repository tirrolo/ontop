package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DAGConstructor {

	private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl
			.getInstance();
	private static final OntologyFactory descFactory = new OntologyFactoryImpl();

	public static DAG getDAG(Ontology ontology) {
		return new DAG(ontology);
	}

	public static DAG getSigma(Ontology ontology) {

		Ontology sigma = descFactory.createOntology(URI.create(""));
		sigma.addConcepts(ontology.getConcepts());
		sigma.addRoles(ontology.getRoles());
		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl inclusion = (SubClassAxiomImpl) assertion;
				Description parent = inclusion.getSuper();
				Description child = inclusion.getSub();
				if (parent instanceof PropertySomeRestriction) {
					continue;
				}
			}

			sigma.addAssertion(assertion);
		}

		sigma.saturate();
		return getDAG(sigma);
	}
	
	public static Ontology getSigmaOntology(Ontology ontology) {
		DAG dag = new DAG(ontology);
		return getSigmaOntology(dag);
	}

	public static Ontology getSigmaOntology(DAG dag) {

		Ontology sigma = descFactory.createOntology(URI.create("sigma"));

		DAGEdgeIterator edgeiterator = new DAGEdgeIterator(dag);
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		while (edgeiterator.hasNext()) {
			Edge edge = edgeiterator.next();
			if (edge.getLeft().getDescription() instanceof ClassDescription) {
				ClassDescription sub = (ClassDescription) edge.getLeft()
						.getDescription();
				ClassDescription superp = (ClassDescription) edge.getRight()
						.getDescription();
				if (superp instanceof PropertySomeRestriction)
					continue;

				Axiom ax = fac.createSubClassAxiom(sub, superp);
				sigma.addEntities(ax.getReferencedEntities());
				sigma.addAssertion(ax);
			} else {
				Property sub = (Property) edge.getLeft().getDescription();
				Property superp = (Property) edge.getRight().getDescription();

				Axiom ax = fac.createSubPropertyAxiom(sub, superp);
				sigma.addEntities(ax.getReferencedEntities());

				sigma.addAssertion(ax);
			}
		}

		return sigma;
	}



	

}
