package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessReasonerLite {
	private Ontology tbox;

	// reflexive and transitive closure of the relations
	private Map<BasicClassDescription, HashSet<BasicClassDescription>> subconcepts; 
	private Map<Property, HashSet<Property>> subproperties; 

	private Map<PropertySomeClassRestriction, ArrayList<BasicClassDescription>> generatingAxioms;

	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessReasonerLite.class);	

	public static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");	
	
	public void setTBox(Ontology ontology) {

		this.tbox = ontology;
		log.debug("SET ONTOLOGY " + ontology);
		// collect generating axioms
		generatingAxioms = new HashMap<PropertySomeClassRestriction, ArrayList<BasicClassDescription>>();
		subconcepts = new HashMap<BasicClassDescription, HashSet<BasicClassDescription>>();
		subproperties = new HashMap<Property, HashSet<Property>>();
		
		log.debug("AXIOMS");
		for (Axiom ax : tbox.getAssertions()) {
			if (ax instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl sax = (SubClassAxiomImpl) ax;
				log.debug("CI AXIOM: " + sax);
				BasicClassDescription subConcept = (BasicClassDescription)sax.getSub();
				ClassDescription superConcept = sax.getSuper();
				if (superConcept instanceof PropertySomeClassRestriction) {
					addGeneratingConceptAxiom(subConcept, (PropertySomeClassRestriction)superConcept);
				}
				else if (superConcept instanceof PropertySomeRestriction) {
					PropertySomeRestriction some = (PropertySomeRestriction)superConcept;
					PropertySomeClassRestriction genConcept = ontFactory.createPropertySomeClassRestriction(some.getPredicate(), some.isInverse(), owlThing);;
					addGeneratingConceptAxiom(subConcept, genConcept);
					addSubConcept(subConcept, some);
				}
				else 
					addSubConcept(subConcept, (BasicClassDescription)superConcept);
			} 
			else if (ax instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl sax = (SubPropertyAxiomImpl) ax;
				log.debug("RI AXIOM: " + sax);
				Property superProperty = sax.getSuper();
				Property subProperty = sax.getSub();
				Property superInverseProperty = ontFactory.createProperty(superProperty.getPredicate(), !superProperty.isInverse());
				Property subInverseProperty = ontFactory.createProperty(subProperty.getPredicate(), !subProperty.isInverse());
				if (!subproperties.containsKey(superProperty)) {
					HashSet<Property> set = new HashSet<Property>();
					set.add(superProperty);
					set.add(subProperty);
					subproperties.put(superProperty, set);
					HashSet<Property> setInverse = new HashSet<Property>();
					setInverse.add(superInverseProperty);
					setInverse.add(subInverseProperty);
					subproperties.put(superInverseProperty, setInverse);
				}
				else {
					subproperties.get(superProperty).add(subProperty);
					subproperties.get(superInverseProperty).add(subInverseProperty);
				}
			}
			else
				log.debug("UNKNOWN AXIOM TYPE:" + ax);
		}

		// SATURATE PROPERTIES HIERARCHY
		{
			for (Property p : subproperties.keySet())
				log.debug("SUBPROPERTIES OF " + p + " ARE " + subproperties.get(p));

			boolean changed = false;
			do {
				changed = false;
				for (Property o1 : subproperties.keySet())
					for (Property o2 : subproperties.keySet())
						if (subproperties.get(o2).contains(o1)) {
							if (subproperties.get(o2).addAll(subproperties.get(o1))) {
								log.debug("ALL " + o2 + " ARE EXTENDED WITH ALL " + o1);
								changed = true;
							}
						}
			} while (changed);
			
			for (Property p : subproperties.keySet())
				log.debug("SATURATED SUBPROPERTY OF " + p + " ARE " + subproperties.get(p));
		}
	
		// SATURATE CONCEPTS HIERARCHY
		{
			for (BasicClassDescription k : subconcepts.keySet())
				log.debug("SUBCONCEPTS OF " + k + " ARE " + subconcepts.get(k));
	
			for (Property prop : subproperties.keySet()) 
				for (Property subproperty : subproperties.get(prop)) 
					addSubConcept(ontFactory.createPropertySomeRestriction(subproperty.getPredicate(), subproperty.isInverse()), 
							ontFactory.createPropertySomeRestriction(prop.getPredicate(), prop.isInverse()));
			
			boolean changed = false;
			do {
				changed = false;
				for (BasicClassDescription o1 : subconcepts.keySet())
					for (BasicClassDescription o2 : subconcepts.keySet())
						if (subconcepts.get(o2).contains(o1)) {
							if (subconcepts.get(o2).addAll(subconcepts.get(o1))) {
								log.debug("ALL " + o2 + " ARE EXTENDED WITH ALL " + o1);
								changed = true;
							}
						}
			} while (changed);
			
			for (BasicClassDescription k : subconcepts.keySet())
				log.debug("SATURATED SUBCONCEPTS OF " + k + " are " + subconcepts.get(k));
		}
		
		// TODO: SATURATE GENERATING AXIOMS
	}
	
	private void addSubConcept(BasicClassDescription subConcept, BasicClassDescription superConcept) {
		if (!subconcepts.containsKey(superConcept)) {
			HashSet<BasicClassDescription> set = new HashSet<BasicClassDescription>();
			set.add(subConcept);
			set.add(superConcept);
			subconcepts.put(superConcept, set);
		}
		else
			subconcepts.get(superConcept).add(subConcept);		
	}

	private void addGeneratingConceptAxiom(BasicClassDescription subConcept, PropertySomeClassRestriction superConcept) {
		if (!generatingAxioms.containsKey(superConcept)) {
			ArrayList<BasicClassDescription> e = new ArrayList<BasicClassDescription>();
			e.add(subConcept);
			generatingAxioms.put(superConcept, e);
		}
		else
			generatingAxioms.get(superConcept).add(subConcept); 		
	}
	
	public Set<Property> getSubProperties(Property prop) {
		return (!subproperties.containsKey(prop)) ? Collections.singleton(prop) : subproperties.get(prop);
	}
	
	public Set<BasicClassDescription> getSubConcepts(BasicClassDescription con) {
		return (!subconcepts.containsKey(con)) ? Collections.singleton(con) : subconcepts.get(con);
	}

	public Set<PropertySomeClassRestriction> getGenerators() {
		return generatingAxioms.keySet();
	}
	
	public List<BasicClassDescription> getConceptsForGenerator(PropertySomeClassRestriction gencon) {
		return generatingAxioms.get(gencon);
	}
}
