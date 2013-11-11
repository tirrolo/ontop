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
import org.semanticweb.ontop.ontology.DisjointObjectPropertyAxiom;

public class DisjointObjectPropertyAxiomImpl extends DisjointPropertyAxiomImpl implements DisjointObjectPropertyAxiom {

	private static final long serialVersionUID = 8438290081979472614L;

	DisjointObjectPropertyAxiomImpl(Predicate p1, Predicate p2) {
	//	if (p1.isObjectProperty() && p2.isObjectProperty())
			super(p1, p2);
	}


}
