/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.ontology.impl;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.PropertyFunctionalAxiom;

public class PropertyFunctionalAxiomImpl implements PropertyFunctionalAxiom{

	private static final long serialVersionUID = 6020134666314925589L;
	
	private Property role = null;
	
	PropertyFunctionalAxiomImpl(Property role) {
		this.role = role;
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		return Collections.singleton(role.getPredicate());
	}	
}
