/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.ObjectConstant;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.UnaryAssertion;

public class ClassAssertionImpl implements ClassAssertion, UnaryAssertion {

	private static final long serialVersionUID = 5689712345023046811L;

	private ObjectConstant object = null;

	private Predicate concept = null;

	ClassAssertionImpl(Predicate concept, ObjectConstant object) {
		this.object = object;
		this.concept = concept;
	}

	@Override
	public ObjectConstant getObject() {
		return object;
	}

	@Override
	public Predicate getConcept() {
		return concept;
	}

	public String toString() {
		return concept.toString() + "(" + object.toString() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(concept);
		return res;
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public Constant getValue() {
		return getObject();
	}

	@Override
	public Predicate getPredicate() {
		return concept;
	}
}
