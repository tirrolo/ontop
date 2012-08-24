package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.utils.ListListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/***
 * The implentation of an Atom. This implementation is aware of changes in the
 * list of terms. Any call to a content changing method in the list of terms
 * will force the atom to invalidate the current hash and string values and
 * recompute them in the next calls to hashCode or toString.
 * 
 * The implementation will also listen to changes in the list of terms of any
 * functional term inside the atom.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class PredicateAtomImpl extends FunctionalTermImpl implements Atom,
		ListListener, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 265506245551670210L;

	protected PredicateAtomImpl(Predicate functor, List<NewLiteral> terms) {
		super(functor, terms);
	}

	@Override
	public PredicateAtomImpl clone() {
		List<NewLiteral> v = new ArrayList<NewLiteral>(terms.size() + 10);
		Iterator<NewLiteral> it = terms.iterator();
		while (it.hasNext()) {
			v.add(it.next().clone());
		}
		PredicateAtomImpl clone = new PredicateAtomImpl(this.functor, v);
		clone.identifier = identifier;
		clone.rehash = rehash;
		return clone;
	}

}
