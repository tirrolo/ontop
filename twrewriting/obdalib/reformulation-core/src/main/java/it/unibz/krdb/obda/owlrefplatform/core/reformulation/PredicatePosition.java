package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Predicate;

public class PredicatePosition implements Cloneable {
	
	private Predicate predicate;
	private int position;
	
	public static final int DIRECT = 1;
	public static final int INVERSE = 0;
	
	public PredicatePosition(Predicate predicate, int position) {
		this.predicate = predicate;
		this.position = position;
	}
	
	public boolean isInverseOf(PredicatePosition pos) {
		// handles only binary predicates
		return (this.predicate.equals(pos.predicate) && (this.position != pos.position));
	}

	public PredicatePosition getInverse() {
		return new PredicatePosition(predicate, 1-position);
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public int getPosition() {
		return position;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof PredicatePosition) {
			PredicatePosition other = (PredicatePosition)obj;
			return (this.hashCode() == other.hashCode()) && 
				    (this.predicate.equals(other.predicate) && 
						   (this.position == other.position));
		}
		return false;
	}
	
	public int hashCode() {
		return predicate.hashCode() ^ position;
	}
	
	public String toString() {
		return predicate  + ((position == INVERSE) ? "-" : "");
	}
}
