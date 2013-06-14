package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.URIPredicate;

public class URIPredicateImpl extends PredicateImpl implements URIPredicate {

	private static final long serialVersionUID = 1L;

	public URIPredicateImpl() {
		// TODO: BAD CODE! Predicate shouldn't store the arity and the type.
		super(OBDADataFactoryImpl.getIRI(OBDAVocabulary.QUEST_URI), 1, null);
	}
	
	@Override
	public URIPredicateImpl clone() {
		return this;
	}
}
