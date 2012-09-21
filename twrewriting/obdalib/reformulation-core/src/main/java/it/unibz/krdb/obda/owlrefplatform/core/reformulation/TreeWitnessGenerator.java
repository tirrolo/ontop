package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TreeWitnessGenerator {
	private PropertySomeClassRestriction some;
	private Set<BasicClassDescription> concepts = new HashSet<BasicClassDescription>();
	private PropertySomeRestriction existsRinv;
	private Property property;

	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	public TreeWitnessGenerator(PropertySomeClassRestriction some) {
		this.some = some;
	}

	public void addConcept(BasicClassDescription con) {
		concepts.add(con);
	}
	
	public void addAllConcepts(Set<BasicClassDescription> cons) {
		concepts.addAll(cons);
	}
	
	public Set<BasicClassDescription> getConcepts() {
		return concepts;
	}
	
	public OClass getFiller() {
		return some.getFiller();
	}
	
	public PropertySomeRestriction getRoleEndType() {
		if (existsRinv == null)
			existsRinv = ontFactory.createPropertySomeRestriction(some.getPredicate(), !some.isInverse());	
		return existsRinv; 	
	}
	
	public Property getProperty() {
		if (property == null)
			property = ontFactory.createProperty(some.getPredicate(), some.isInverse());
		return property;
	}
	
	@Override
	public int hashCode() {
		return some.hashCode();
	}
	
	@Override 
	public String toString() {
		return "tw-generator" + some.toString();
	}
	
	@Override 
	public boolean equals(Object other) {
		if (other instanceof TreeWitnessGenerator)
			return some.equals(((TreeWitnessGenerator)other).some);
		return false;
	}
}
