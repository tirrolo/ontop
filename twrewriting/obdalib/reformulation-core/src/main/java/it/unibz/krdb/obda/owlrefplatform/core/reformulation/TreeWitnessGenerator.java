package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.OClass;

public class TreeWitnessGenerator {
	private PredicatePosition position;
	private OClass filler;
	
	public TreeWitnessGenerator(PredicatePosition position) {
		this.position = position;
		this.filler = null;
	}
	
	public TreeWitnessGenerator(PredicatePosition position, OClass filler) {
		this.position = position;
		this.filler = null;
	}
	
	public PredicatePosition getPredicatePosition() {
		return position;
	}
	
	public Predicate getPredicate() {
		return position.getPredicate();
	}
	
	public boolean isInverse() {
		return position.getPosition() == PredicatePosition.INVERSE;
	}
	
	public OClass getFiller() {
		return filler;
	}
	
	public String toString() {
		return "" + position + "." + ((filler == null) ? "T" : filler.toString());
	}
	
	public int hashCode() {
		return position.hashCode() ^ ((filler == null) ? 0 : filler.hashCode());
	}

	public boolean equals(Object obj) {
		if (obj instanceof TreeWitnessGenerator) {
			TreeWitnessGenerator other = (TreeWitnessGenerator)obj;
			if ((this.hashCode() != other.hashCode()) || !this.position.equals(other.position))
				return false;
			if (this.filler == other.filler) 
				return true;
			if (this.filler != null) 
				return this.filler.equals(other.filler);
		}
		return false;
	}
}
