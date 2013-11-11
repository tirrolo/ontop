/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.core.abox;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.DataPropertyAssertion;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.ObjectPropertyAssertion;
import org.semanticweb.ontop.owlrefplatform.core.abox.NTripleAssertionIterator;

import junit.framework.TestCase;

public class NTripleAssertionIteratorTest extends TestCase {
	public void testIteratorTest() throws IOException {
		File testFile = new File("src/test/resources/test/lubm-data.n3");
		URI fileURI = testFile.toURI();
		NTripleAssertionIterator iterator = new NTripleAssertionIterator(fileURI, new HashMap<Predicate, Description>());
		
		int typeCount = 0;
		int objPropCount = 0;
		int datPropCount = 0;
		
		while (iterator.hasNext()) {
			Assertion ass = iterator.next();
			if (ass instanceof ClassAssertion) {
				typeCount +=1;
			} else if (ass instanceof DataPropertyAssertion) {
				datPropCount +=1;
			} else if (ass instanceof ObjectPropertyAssertion) {
				objPropCount +=1;
			} 
		}
		
		assertEquals(2, typeCount);
		assertEquals(3, datPropCount);
		assertEquals(6, objPropCount);
		
	}
}
