/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.reformulation.tests;


import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.PropertySomeRestriction;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.dag.DAG;
import org.semanticweb.ontop.owlrefplatform.core.dag.DAGConstructor;

import junit.framework.TestCase;

public class SigmaTest extends TestCase {

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = new OntologyFactoryImpl();

    public void test_exists_simple() {
        Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

        Predicate a = predicateFactory.getPredicate("a", 1);
        Predicate c = predicateFactory.getPredicate("c", 1);
        Predicate r = predicateFactory.getPredicate("r", 2);
        OClass ac = descFactory.createClass(a);
        OClass cc = descFactory.createClass(c);
        PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
        ontology.addConcept(ac.getPredicate());
        ontology.addConcept(cc.getPredicate());
        ontology.addRole(er.getPredicate());

        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
        ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, er));

        DAG res = DAGConstructor.getSigma(ontology);
        res.clean();

        assertTrue(res.getClassNode(ac).getDescendants().contains(res.getClassNode(er)));

        assertEquals(1, res.getClassNode(ac).getDescendants().size());

        assertEquals(0, res.getClassNode(er).getDescendants().size());

        assertEquals(0, res.getClassNode(cc).getDescendants().size());

    }
}
