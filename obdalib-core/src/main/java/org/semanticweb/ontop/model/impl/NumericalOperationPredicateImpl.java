/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.model.impl;

import org.semanticweb.ontop.model.NumericalOperationPredicate;

public class NumericalOperationPredicateImpl extends PredicateImpl implements NumericalOperationPredicate {

	private static final long serialVersionUID = 1L;

	protected NumericalOperationPredicateImpl(String name, int arity) {
		super(name, arity, null);
	}
	
	protected NumericalOperationPredicateImpl(String name, int arity, COL_TYPE[] types) {
		super(name, arity, types);
	}

	@Override
	public NumericalOperationPredicate clone() {
		return this;
	}
}
