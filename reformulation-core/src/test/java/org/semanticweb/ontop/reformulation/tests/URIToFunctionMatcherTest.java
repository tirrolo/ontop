/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.reformulation.tests;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.URIToFunctionMatcher;

import junit.framework.TestCase;

public class URIToFunctionMatcherTest extends TestCase {

	URIToFunctionMatcher matcher;
	
	
	public void setUp() throws Exception {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		List<Term> variables = new LinkedList<Term>();
		variables.add(fac.getVariable("x"));
		variables.add(fac.getVariable("y"));
		
		OBDADataFactory pfac = OBDADataFactoryImpl.getInstance();
		Predicate p = pfac.getPredicate("http://www.obda.com/onto#individual", 2);
		
		Term fterm = fac.getFunction(p, variables);
		
		Map<String,Function> termList = new HashMap<String, Function>();
		termList.put(p.getName().toString(), (Function)fterm);
		
		matcher = new URIToFunctionMatcher(termList);	
	}
	
	public void testMatchURI() {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		Function matchedTerm = matcher.getPossibleFunctionalTermMatch(fac.getConstantURI("http://www.obda.com/onto#individual-mariano-rodriguez"));
		assertTrue(matchedTerm != null);
		assertTrue(matchedTerm.toString(), matchedTerm.getFunctionSymbol().toString().equals("http://www.obda.com/onto#individual"));
		assertTrue(matchedTerm.toString(), matchedTerm.getTerms().get(0) instanceof ValueConstant);
		assertTrue(matchedTerm.toString(), matchedTerm.getTerms().get(1) instanceof ValueConstant);
		assertTrue(matchedTerm.getTerms().get(0).toString(), ((ValueConstant) matchedTerm.getTerms().get(0)).getValue().equals("mariano"));
		assertTrue(matchedTerm.getTerms().get(1).toString(), ((ValueConstant) matchedTerm.getTerms().get(1)).getValue().equals("rodriguez"));
	}

}
