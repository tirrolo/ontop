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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessReasonerLite {
	private Ontology tbox;

	// reflexive and transitive closure of the relations
	private Map<BasicClassDescription, Set<BasicClassDescription>> subconcepts; 
	private Map<TreeWitnessGenerator, Set<BasicClassDescription>> twgSubconcepts; 
	private Map<Property, Set<Property>> subproperties; 

	// caching OClasses and Properties 
	private Map<Predicate, Set<BasicClassDescription>> predicateSubconcepts;
	private Map<Predicate, Set<Property>> predicateSubproperties;
	
	private Collection<TreeWitnessGenerator> generators;

	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessReasonerLite.class);	

	public static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");	
	
	public void setTBox(Ontology ontology) {

		this.tbox = ontology;
		log.debug("SET ONTOLOGY " + ontology);

		Map<PropertySomeClassRestriction, TreeWitnessGenerator> gens = new HashMap<PropertySomeClassRestriction, TreeWitnessGenerator>();
		subconcepts = new HashMap<BasicClassDescription, Set<BasicClassDescription>>();
		twgSubconcepts = new HashMap<TreeWitnessGenerator, Set<BasicClassDescription>>();
		subproperties = new HashMap<Property, Set<Property>>();
		
		predicateSubconcepts = new HashMap<Predicate, Set<BasicClassDescription>>();
		predicateSubproperties = new HashMap<Predicate, Set<Property>>();
		
		// COLLECT GENERATING CONCEPTS (together with their declared subclasses)
		// COLLECT SUB-CONCEPT AND SUB-PROPERTY RELATIONS
		log.debug("AXIOMS");
		for (Axiom ax : tbox.getAssertions()) {
			if (ax instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl sax = (SubClassAxiomImpl) ax;
				log.debug("CI AXIOM: " + sax);
				BasicClassDescription subConcept = (BasicClassDescription)sax.getSub();
				ClassDescription superConcept = sax.getSuper();
				if (superConcept instanceof PropertySomeClassRestriction) {
					addGeneratingConceptAxiom(gens, subConcept, (PropertySomeClassRestriction)superConcept);
				}
				else if (superConcept instanceof PropertySomeRestriction) {
					PropertySomeRestriction some = (PropertySomeRestriction)superConcept;
					PropertySomeClassRestriction genConcept = ontFactory.createPropertySomeClassRestriction(some.getPredicate(), some.isInverse(), owlThing);
					addGeneratingConceptAxiom(gens, subConcept, genConcept);
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
			for (Map.Entry<Property, Set<Property>> p : subproperties.entrySet())
				log.debug("DECLARED SUBPROPERTIES OF " + p.getKey() + " ARE " + p.getValue());

			graphTransitiveClosure(subproperties);
			
			for (Map.Entry<Property, Set<Property>> p : subproperties.entrySet())
				log.debug("SATURATED SUBPROPERTY OF " + p.getKey() + " ARE " + p.getValue());
		}
	
		// SATURATE CONCEPTS HIERARCHY
		{
			for (Map.Entry<BasicClassDescription, Set<BasicClassDescription>> k : subconcepts.entrySet())
				log.debug("DECLARED SUBCONCEPTS OF " + k.getKey() + " ARE " + k.getValue());
	
			// ADD INCLUSIONS BETWEEN EXISTENTIALS OF SUB-PROPERTIES
			for (Map.Entry<Property, Set<Property>> prop : subproperties.entrySet()) 
				for (Property subproperty : prop.getValue()) 
					addSubConcept(ontFactory.createPropertySomeRestriction(subproperty.getPredicate(), subproperty.isInverse()), 
							ontFactory.createPropertySomeRestriction(prop.getKey().getPredicate(), prop.getKey().isInverse()));

			graphTransitiveClosure(subconcepts);
			
			for (Map.Entry<BasicClassDescription, Set<BasicClassDescription>> k : subconcepts.entrySet())
				log.debug("SATURATED SUBCONCEPTS OF " +  k.getKey() + " ARE " + k.getValue());
		}
			
		generators = gens.values();
		
		/*
		// SATURATE GENERATING AXIOMS
		for (TreeWitnessGenerator twg0 : generatorsSet) {
			for (TreeWitnessGenerator twg1 : generatorsSet) {
				// check whether twg1 subsumes twg0
				if (getSubConcepts(twg1.getFiller()).contains(twg0.getFiller()) && 
						getSubProperties(twg1.getProperty()).contains(twg0.getProperty()))
					twg1.addAllConcepts(twg0.getConcepts()); 
			}
		}
		*/
	}
	
	private static <T> void graphTransitiveClosure(Map<T, Set<T>> graph) {
		log.debug("COMPUTING TRANSITIVE CLOSURE");
		Queue<T> useForExtension = new LinkedList<T>(graph.keySet());
		while (!useForExtension.isEmpty()) {
			T o1key = useForExtension.poll();
			log.debug("   USE FOR EXTENSION: " + o1key);
			Set<T> o1value = null;
			for (Map.Entry<T, Set<T>> o2 : graph.entrySet()) {
				if (o2.getKey() == o1key)
					continue;
				if (o2.getValue().contains(o1key)) {
					if (o1value == null)
						o1value = graph.get(o1key);
					if (o2.getValue().addAll(o1value)) {
						useForExtension.add(o2.getKey());
						log.debug("ALL " + o2.getKey() + " ARE EXTENDED WITH ALL " + o1key);
					}
				}
			}
		}		
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

	private void addGeneratingConceptAxiom(Map<PropertySomeClassRestriction, TreeWitnessGenerator> generators, BasicClassDescription subConcept, PropertySomeClassRestriction superConcept) {
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

	public Set<BasicClassDescription> getSubConcepts(TreeWitnessGenerator twg) {
		Set<BasicClassDescription> s = twgSubconcepts.get(twg);
		if (s == null) {
			s = new HashSet<BasicClassDescription>();
			for (BasicClassDescription con : twg.getConcepts())
				s.addAll(getSubConcepts(con));
			twgSubconcepts.put(twg, s);
		}
		return s;
	}
	
	public Collection<TreeWitnessGenerator> getGenerators() {
		return generators;
	}
	
	public Collection<BasicClassDescription> getMaximalBasicConcepts(Collection<TreeWitnessGenerator> gens) {
		Set<BasicClassDescription> cons = new HashSet<BasicClassDescription>();
		for (TreeWitnessGenerator twg : gens) {
			cons.addAll(twg.getConcepts());
		}
		
		if (cons.size() > 1) {
			log.debug("MORE THAN ONE GEN CON: " + cons);
		}
		// TODO: select only maximal ones
		
		return cons;
	}
}
