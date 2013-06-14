package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.IRIOperationPredicate;

import com.hp.hpl.jena.iri.IRI;

public class IRIOperationPredicateImpl extends PredicateImpl implements IRIOperationPredicate {

	private static final long serialVersionUID = -3120856969056026217L;

	public IRIOperationPredicateImpl(IRI name) {
		super(name, 1, null);
	}

	
}
