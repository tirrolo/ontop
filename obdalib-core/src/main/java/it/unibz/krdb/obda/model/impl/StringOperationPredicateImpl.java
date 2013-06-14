package it.unibz.krdb.obda.model.impl;

import com.hp.hpl.jena.iri.IRI;

import it.unibz.krdb.obda.model.StringOperationPredicate;


public class StringOperationPredicateImpl extends PredicateImpl implements StringOperationPredicate{

	private static final long serialVersionUID = -3576055015957514916L;

	public StringOperationPredicateImpl(IRI iri)
	{
		super(iri, 1, null);
	}
	
	public StringOperationPredicateImpl(IRI iri, int arity)
	{
		super(iri, arity, null);
	}

}
