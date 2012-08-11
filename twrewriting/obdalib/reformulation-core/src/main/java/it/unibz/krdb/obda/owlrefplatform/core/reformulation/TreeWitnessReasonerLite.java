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
import java.util.Collection;
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
	private Map<BasicClassDescription, Set<BasicClassDescription>> subconcepts; 
	private Map<Property, Set<Property>> subproperties; 

	// caching OClasses and Properties 
	private Map<Predicate, Set<BasicClassDescription>> predicateSubconcepts;
	private Map<Predicate, Set<Property>> predicateSubproperties;
	
	private Map<PropertySomeClassRestriction, TreeWitnessGenerator> generators;

	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessReasonerLite.class);	

	public static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");	
	
	public void setTBox(Ontology ontology) {

		this.tbox = ontology;
		log.debug("SET ONTOLOGY " + ontology);
		// collect generating axioms
		generators = new HashMap<PropertySomeClassRestriction, TreeWitnessGenerator>();
		subconcepts = new HashMap<BasicClassDescription, Set<BasicClassDescription>>();
		subproperties = new HashMap<Property, Set<Property>>();
		
		predicateSubconcepts = new HashMap<Predicate, Set<BasicClassDescription>>();
		predicateSubproperties = new HashMap<Predicate, Set<Property>>();
		
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
					Set<Property> set = new HashSet<Property>(2);
					set.add(superProperty);
					set.add(subProperty);
					subproperties.put(superProperty, set);
					Set<Property> setInverse = new HashSet<Property>(2);
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
			Set<Property> sp = subproperties.keySet();
			for (Property p : sp)
				log.debug("DECLARED SUBPROPERTIES OF " + p + " ARE " + subproperties.get(p));

			boolean changed = false;
			do {
				changed = false;
				for (Property o1 : sp) {
					Set<Property> so1 = null;
					for (Property o2 : sp) {
						if (o2 == o1)
							continue;
						Set<Property> so2 = subproperties.get(o2);
						if (so2.contains(o1)) {
							if (so1 == null)
								so1 = subproperties.get(o1);
							if (so2.addAll(so1)) {
								log.debug("ALL " + o2 + " ARE EXTENDED WITH ALL " + o1);
								changed = true;
							}
						}
					}
				}
			} while (changed);
			
			for (Property p : sp)
				log.debug("SATURATED SUBPROPERTY OF " + p + " ARE " + subproperties.get(p));
		}
	
		// SATURATE CONCEPTS HIERARCHY
		{
			for (BasicClassDescription k : subconcepts.keySet())
				log.debug("DECLARED SUBCONCEPTS OF " + k + " ARE " + subconcepts.get(k));
	
			for (Property prop : subproperties.keySet()) 
				for (Property subproperty : subproperties.get(prop)) 
					addSubConcept(ontFactory.createPropertySomeRestriction(subproperty.getPredicate(), subproperty.isInverse()), 
							ontFactory.createPropertySomeRestriction(prop.getPredicate(), prop.isInverse()));
			
			Set<BasicClassDescription> sc = subconcepts.keySet();
			boolean changed = false;
			do {
				changed = false;
				for (BasicClassDescription o1 : sc) {
					Set<BasicClassDescription> so1 = null;
					for (BasicClassDescription o2 : sc) {
						if (o2 == o1)
							continue;
						Set<BasicClassDescription> so2 = subconcepts.get(o2);
						if (so2.contains(o1)) {
							if (so1 == null)
								so1 = subconcepts.get(o1);
							if (so2.addAll(so1)) {
								log.debug("ALL " + o2 + " ARE EXTENDED WITH ALL " + o1);
								changed = true;
							}
						}
					}
				}
			} while (changed);
			
			for (BasicClassDescription k : sc)
				log.debug("SATURATED SUBCONCEPTS OF " + k + " are " + subconcepts.get(k));
		}
		
		// TODO: SATURATE GENERATING AXIOMS
	}
	
	private void addSubConcept(BasicClassDescription subConcept, BasicClassDescription superConcept) {
		Set<BasicClassDescription> set = subconcepts.get(superConcept);
		if (set == null) {
			set = new HashSet<BasicClassDescription>();
			set.add(superConcept);
			subconcepts.put(superConcept, set);
		}
		set.add(subConcept);		
	}

	private void addGeneratingConceptAxiom(BasicClassDescription subConcept, PropertySomeClassRestriction superConcept) {
		TreeWitnessGenerator twg = generators.get(superConcept);
		if (twg == null) {
			twg = new TreeWitnessGenerator(superConcept);			
			generators.put(superConcept, twg);
		}
		twg.addConcept(subConcept);
		log.debug("GENERATING CI: " + subConcept + " <= " + superConcept);
	}
	
	public Set<Property> getSubProperties(Predicate pred) {
		Set<Property> s = predicateSubproperties.get(pred);
		if (s == null) {
			s = getSubProperties(ontFactory.createProperty(pred));
			predicateSubproperties.put(pred, s);
		}
		return s;
	}
	
	public Set<Property> getSubProperties(Property prop) {
		Set<Property> s = subproperties.get(prop);
		if (s == null) {
			s = Collections.singleton(prop);
			subproperties.put(prop, s);
		}
		return s;
	}
	
	public Set<BasicClassDescription> getSubConcepts(Predicate pred) {
		Set<BasicClassDescription> s = predicateSubconcepts.get(pred);
		if (s == null) {
			s = getSubConcepts(ontFactory.createClass(pred));
			predicateSubconcepts.put(pred, s);
		}
		return s;
	}
	
	public Set<BasicClassDescription> getSubConcepts(BasicClassDescription con) {
		Set<BasicClassDescription> s = subconcepts.get(con);
		if (s == null) {
			s = Collections.singleton(con);
			subconcepts.put(con, s);
		}
		return s;
	}

	public Collection<TreeWitnessGenerator> getGenerators() {
		return generators.values();
	}
}
