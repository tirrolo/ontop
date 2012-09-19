package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.BooleanOperationPredicateImpl;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Edge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFolding {
	private Set<Property> properties; 
	private Set<Atom> rootAtoms; 
	private Set<Term> roots; 
	private Set<Atom> internalRootAtoms; 
	private Set<Term> internalRoots;
	private Set<Term> internalDomain;
	private boolean status = true;

	private static final Logger log = LoggerFactory.getLogger(QueryFolding.class);
	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
		
	public QueryFolding() {
		properties = new HashSet<Property>(); 
		rootAtoms = new HashSet<Atom>(); 
		roots = new HashSet<Term>(); 
		internalRootAtoms = new HashSet<Atom>(); 
		internalRoots = new HashSet<Term>();
		internalDomain = new HashSet<Term>();
	}
	
	public QueryFolding(TreeWitness tw) {
		properties = new HashSet<Property>(); 
		rootAtoms = new HashSet<Atom>(); 
		roots = new HashSet<Term>(); 
		internalRootAtoms = new HashSet<Atom>(tw.getRootAtoms()); 
		internalRoots = new HashSet<Term>(tw.getRoots());
		internalDomain = new HashSet<Term>(tw.getDomain());
	}

	public QueryFolding(QueryFolding qf) {
		properties = new HashSet<Property>(qf.properties); 
		rootAtoms = new HashSet<Atom>(qf.rootAtoms); 
		roots = new HashSet<Term>(qf.roots); 
		internalRootAtoms = new HashSet<Atom>(qf.internalRootAtoms); 
		internalRoots = new HashSet<Term>(qf.internalRoots);
		internalDomain = new HashSet<Term>(qf.internalDomain);
		status = qf.status;
	}

	
	public QueryFolding extend(TreeWitness tw) {
		QueryFolding c = new QueryFolding(this);
		c.internalRoots.addAll(tw.getRoots());
		c.internalDomain.addAll(tw.getDomain());
		c.internalRootAtoms.addAll(tw.getRootAtoms());
		return c;
	}
	
	public void extend(Term root, Set<Atom> bAtoms, Set<Atom> rootLoop, Set<Atom> intRootLoop) {
		for (Atom a: intRootLoop)
			if (a.getArity() == 2) {
				log.debug("        NO LOOPS ALLOWED IN INTERNAL TERMS: " + a);
				status = false;
				return; 
			}
		
		internalRootAtoms.addAll(intRootLoop);
		roots.add(root);
		rootAtoms.addAll(rootLoop);

		for (Atom a : bAtoms) {
			if (a.getPredicate() instanceof BooleanOperationPredicateImpl) {
				log.debug("        NO BOOLEAN OPERATION PREDICATES ALLOWED IN PROPERTIES: " + a);
				status = false;
				return;
			}
			log.debug("FOLDING PROPERTY: " + a.getPredicate().getClass());
			// TODO: CACHE THESE
			if (root.equals(a.getTerm(0))) // internalRoots.contains
				properties.add(ontFactory.createProperty(a.getPredicate(), false)); 
			else 
				properties.add(ontFactory.createProperty(a.getPredicate(), true)); 
		}	
	}
	
	public boolean canBeFolded(Term t, QueryConnectedComponent cc) {
		properties.clear();
		rootAtoms.clear();
		roots.clear();
		internalRootAtoms.clear();
		internalDomain = Collections.singleton(t);
		status = true;
		
		for (Edge edge : cc.getEdges()) {
			if (t.equals(edge.getTerm0()))
				extend(edge.getTerm1(), edge.getBAtoms(),  edge.getL1Atoms(), edge.getL0Atoms());
			else if (t.equals(edge.getTerm1()))
				extend(edge.getTerm0(), edge.getBAtoms(),  edge.getL0Atoms(), edge.getL1Atoms());
			
			if (!status)
				return false;
		}

		// TODO: EXTEND ROOT ATOMS BY ALL-ROOT EDGES
		
		log.debug("  PROPERTIES " + properties);
		log.debug("  ENDTYPE " + internalRootAtoms);
		log.debug("  ROOTTYPE " + rootAtoms);
		
		return true;
	}
	
	public Set<Property> getProperties() {
		return properties;
	}
	
	public boolean isValid() {
		return status;
	}
	
	public boolean hasRoot() {
		return (roots.size() != 0);
	}
	
	public boolean canBeAttachedToAnInternalRoot(Term t0, Term t1) {
		return internalRoots.contains(t0) && !internalDomain.contains(t1) && !roots.contains(t1);
	}
	
	public Set<Atom> getInternalRootAtoms() {
		return internalRootAtoms;
	}
	
	private Set<Term> domain;
	
	public TreeWitness getTreeWitness(TreeWitnessGenerator g, Set<Variable> quantifiedVariables) {
		if (domain == null) {
			domain = new HashSet<Term>(internalDomain);
			domain.addAll(roots);
		}
		return new TreeWitness(g, roots, quantifiedVariables.containsAll(roots), rootAtoms, domain); 	
	}
}
