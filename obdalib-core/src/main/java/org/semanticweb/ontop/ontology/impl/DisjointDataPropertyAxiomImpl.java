/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.ontology.impl;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.DisjointDataPropertyAxiom;

public class DisjointDataPropertyAxiomImpl extends DisjointPropertyAxiomImpl implements DisjointDataPropertyAxiom{

	private static final long serialVersionUID = 2049346032304523558L;

	DisjointDataPropertyAxiomImpl(Predicate p1, Predicate p2) {
	//	if (p1.isDataProperty() && p2.isDataProperty())
		super(p1, p2);
		
	}

}
