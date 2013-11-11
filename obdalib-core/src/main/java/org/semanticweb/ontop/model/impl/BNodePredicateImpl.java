/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.model.impl;

import org.semanticweb.ontop.model.BNodePredicate;

public class BNodePredicateImpl extends PredicateImpl implements BNodePredicate {

	private static final long serialVersionUID = -1546325236776439443L;

	public BNodePredicateImpl(int arity) {
		// TODO: BAD CODE! Predicate shouldn't store the arity and the type.
		super(OBDAVocabulary.QUEST_BNODE, arity, null);
	}

	@Override
	public BNodePredicateImpl clone() {
		return this;
	}
}
