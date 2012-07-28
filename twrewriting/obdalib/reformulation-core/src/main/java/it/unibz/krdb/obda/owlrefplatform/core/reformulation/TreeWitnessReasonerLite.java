package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
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
	private Map<ClassDescription, HashSet<ClassDescription>> subconcepts; 
	private Map<Property, HashSet<Property>> subproperties; 

	private Map<PropertySomeClassRestriction, ArrayList<ClassDescription>> generatingAxioms;

	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessReasonerLite.class);	

	public static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");	
	
	public void setTBox(Ontology ontology) {

		this.tbox = ontology;
		log.debug("SET ONTOLOGY " + ontology);
		// collect generating axioms
		generatingAxioms = new HashMap<PropertySomeClassRestriction, ArrayList<ClassDescription>>();
		subconcepts = new HashMap<ClassDescription, HashSet<ClassDescription>>();
		subproperties = new HashMap<Property, HashSet<Property>>();
		
		log.debug("AXIOMS");
		for (Axiom ax : tbox.getAssertions()) {
			if (ax instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl sax = (SubClassAxiomImpl) ax;
				log.debug("CI AXIOM: " + sax);
				ClassDescription superConcept = getConceptFromClassDescription(sax.getSuper());
				if (superConcept instanceof PropertySomeClassRestriction) {
					if (!generatingAxioms.containsKey(superConcept)) {
						ArrayList<ClassDescription> e = new ArrayList<ClassDescription>();
						e.add(getConceptFromClassDescription(sax.getSub()));
						generatingAxioms.put((PropertySomeClassRestriction)superConcept, e);
					}
					else
						generatingAxioms.get(superConcept).add(getConceptFromClassDescription(sax.getSub())); 
				}
				
				addSubConcept(getConceptFromClassDescription(sax.getSub()), superConcept);
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
		
		for (Object k : subconcepts.keySet())
			log.debug("SUBCONCEPTS OF " + k + " are " + subconcepts.get(k));
		
		for (Property prop : subproperties.keySet()) 
			for (Property subproperty : subproperties.get(prop)) {
				addSubConcept(ontFactory.createPropertySomeClassRestriction(subproperty.getPredicate(), subproperty.isInverse(), owlThing), 
						ontFactory.createPropertySomeClassRestriction(prop.getPredicate(), prop.isInverse(), owlThing));
			}
		
		boolean changed = false;
		do {
			changed = false;
			for (ClassDescription o1 : subconcepts.keySet())
				for (ClassDescription o2 : subconcepts.keySet())
					if (subconcepts.get(o2).contains(o1)) {
						if (subconcepts.get(o2).addAll(subconcepts.get(o1))) {
							log.debug("ALL " + o2 + " ARE EXTENDED WITH ALL " + o1);
							changed = true;
						}
					}
		} while (changed);
		
		for (ClassDescription k : subconcepts.keySet())
			log.debug("SATURATED SUBCONCEPTS OF " + k + " are " + subconcepts.get(k));
	}

	private static ClassDescription getConceptFromClassDescription(ClassDescription c) {
		//log.debug("CONCEPT TYPE: " + c.getClass());
		if (c instanceof PropertySomeClassRestriction) {
			PropertySomeClassRestriction some = (PropertySomeClassRestriction) c;
			log.debug("  SOME CONCEPT " + some.getPredicate() + (some.isInverse() ? "^-" : "") + ", FILLER " + some.getFiller());
			//return new TreeWitnessGenerator
			//		(ontFactory.createProperty(some.getPredicate(), some.isInverse()), some.getFiller());
			return some;
		} 
		else if (c instanceof PropertySomeRestriction) {
			PropertySomeRestriction some = (PropertySomeRestriction) c;
			log.debug("  SOME " + some.getPredicate() + (some.isInverse() ? "^-" : "") + ", FILLER TOP");
			//return new TreeWitnessGenerator
			//		(ontFactory.createProperty(some.getPredicate(), some.isInverse()));
			return ontFactory.createPropertySomeClassRestriction(some.getPredicate(), some.isInverse(), owlThing);
		} 
		else if (c instanceof OClass) {
			OClass oc = (OClass)c;
			log.debug("  CONCEPT " + oc);
			return oc;
		}
		log.debug("UNKNONW TYPE: " + c);
		return null; 
	}
		
	
	private void addSubConcept(ClassDescription subConcept, ClassDescription superConcept) {
		if (!subconcepts.containsKey(superConcept)) {
			HashSet<ClassDescription> set = new HashSet<ClassDescription>();
			set.add(subConcept);
			set.add(superConcept);
			subconcepts.put(superConcept, set);
		}
		else
			subconcepts.get(superConcept).add(subConcept);		
	}
	
	public Set<Property> getSubProperties(Property prop) {
		return (!subproperties.containsKey(prop)) ? Collections.singleton(prop) : subproperties.get(prop);
	}
	
	public Set<ClassDescription> getSubConcepts(ClassDescription con) {
		return (!subconcepts.containsKey(con)) ? Collections.singleton(con) : subconcepts.get(con);
	}

	public Set<PropertySomeClassRestriction> getGenerators() {
		return generatingAxioms.keySet();
	}
	
	public List<ClassDescription> getConceptsForGenerator(PropertySomeClassRestriction e) {
		return generatingAxioms.get(e);
	}
	
	public Set<Predicate> getRoles() {
		return tbox.getRoles();
	}
}
