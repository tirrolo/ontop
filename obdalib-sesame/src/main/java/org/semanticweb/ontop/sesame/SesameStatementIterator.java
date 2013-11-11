/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.sesame;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openrdf.model.Statement;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;

/***
 * An iterator that will dynamically construct ABox assertions for the given
 * predicate based on the results of executing the mappings for the predicate in
 * each data source.
 * 
 */
public class SesameStatementIterator implements Iterator<Statement> {
	private Iterator<Assertion> iterator;	
	

	public SesameStatementIterator(Iterator<Assertion> it) {
		this.iterator = it;
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public Statement next() {
		Assertion assertion = iterator.next();
		Statement individual = new SesameStatement(assertion);
		return individual;
	}

	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}
	
	
}
