
package org.semanticweb.ontop.reformulation.semindex.tests;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.Set;

import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.PropertySomeRestriction;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.DAG;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.GraphBuilderImpl;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.GraphImpl;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import junit.framework.TestCase;


public class DAGChainTest extends TestCase {

	SemanticIndexHelper						helper				= new SemanticIndexHelper();

	private static final OBDADataFactory	predicateFactory	= OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory	descFactory			= new OntologyFactoryImpl();

	public void test_simple_isa() {
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getPredicate("a", 1);
		Predicate b = predicateFactory.getPredicate("b", 1);
		Predicate c = predicateFactory.getPredicate("c", 1);

		OClass ac = descFactory.createClass(a);
		OClass bc = descFactory.createClass(b);
		OClass cc = descFactory.createClass(c);

		ontology.addConcept(ac.getPredicate());
		ontology.addConcept(bc.getPredicate());
		ontology.addConcept(cc.getPredicate());

		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(bc, ac));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, bc));

		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(ontology, false);
		DAG res = reasoner.getDAG();
		reasoner.getChainDAG();

		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(bc, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(cc, false)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		assertTrue(reasoner.getDescendants(bc, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(bc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);
	}

	public void test_exists_simple() {
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getClassPredicate("a");
		Predicate r = predicateFactory.getObjectPropertyPredicate("r");
		Predicate c = predicateFactory.getClassPredicate("c");
		OClass ac = descFactory.createClass(a);
		PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
		PropertySomeRestriction ier = descFactory.getPropertySomeRestriction(r, true);
		OClass cc = descFactory.createClass(c);

		ontology.addConcept(ac.getPredicate());
		ontology.addConcept(cc.getPredicate());

		ontology.addRole(er.getPredicate());
		ontology.addRole(ier.getPredicate());
		
		System.out.println(er);
		System.out.println(ac);
		System.out.println(cc);
		System.out.println(ier);

		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, ier));
		
		//generate Graph
		GraphBuilderImpl change= new GraphBuilderImpl(ontology);
		
		GraphImpl res = (GraphImpl) change.getGraph();

		
		
//		for (Description nodes: res.vertexSet()) {
//			System.out.println("---- " + nodes);
//		}
		
		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(res);
		reasoner.getChainDAG();

		
		
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(er, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(ier, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(cc, false)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 3);

		assertTrue(reasoner.getDescendants(er, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(er, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);

		assertTrue(reasoner.getDescendants(ier, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ier, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);
	}

	public void test_exists_complex() {

		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getPredicate("a", 1);
		Predicate r = predicateFactory.getPredicate("r", 2);
		Predicate c = predicateFactory.getPredicate("c", 1);
		Predicate b = predicateFactory.getPredicate("b", 1);
		Predicate d = predicateFactory.getPredicate("d", 1);

		OClass ac = descFactory.createClass(a);
		PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
		PropertySomeRestriction ier = descFactory.getPropertySomeRestriction(r, true);
		OClass cc = descFactory.createClass(c);
		OClass bc = descFactory.createClass(b);
		OClass dc = descFactory.createClass(d);

		ontology.addConcept(ac.getPredicate());

		ontology.addConcept(cc.getPredicate());
		ontology.addConcept(bc.getPredicate());
		ontology.addConcept(dc.getPredicate());

		ontology.addRole(er.getPredicate());
		ontology.addRole(ier.getPredicate());

		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, ier));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(bc, er));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(ier, dc));

		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(ontology, false);
		reasoner.getChainDAG();

		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(er, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(ier, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(cc, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(bc, false)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 4);

		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(er, false)));
		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(ier, false)));
		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(cc, false)));
		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(bc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(dc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 4);

		assertTrue(reasoner.getDescendants(er, false).contains(reasoner.getEquivalences(bc, false)));
		assertTrue(reasoner.getDescendants(er, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(er, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		assertTrue(reasoner.getDescendants(ier, false).contains(reasoner.getEquivalences(bc, false)));
		assertTrue(reasoner.getDescendants(ier, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ier, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(bc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);

	}

	

}
