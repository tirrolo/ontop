package org.semanaticweb.ontop.reformulation.tests;

/*
 * #%L
 * ontop-reformulation-core
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


import org.semanaticweb.ontop.model.OBDADataFactory;
import org.semanaticweb.ontop.model.Predicate;
import org.semanaticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanaticweb.ontop.ontology.OClass;
import org.semanaticweb.ontop.ontology.Ontology;
import org.semanaticweb.ontop.ontology.OntologyFactory;
import org.semanaticweb.ontop.ontology.PropertySomeRestriction;
import org.semanaticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanaticweb.ontop.owlrefplatform.core.dag.DAG;
import org.semanaticweb.ontop.owlrefplatform.core.dag.DAGConstructor;
import org.semanaticweb.ontop.owlrefplatform.core.dagjgrapht.DAGImpl;
import org.semanaticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

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

        
       
        Ontology ontologySigma =  TBoxReasonerImpl.getSigma(ontology);
        TBoxReasonerImpl sigma= new TBoxReasonerImpl(ontologySigma, false);
        DAGImpl res= sigma.getDAG();

        assertTrue(sigma.getDescendants(ac, false).contains(sigma.getEquivalences(er, false)));

        assertEquals(1, sigma.getDescendants(ac, false).size());

        assertEquals(0, sigma.getDescendants(er, false).size());

        assertEquals(0, sigma.getDescendants(cc, false).size());

    }
}
