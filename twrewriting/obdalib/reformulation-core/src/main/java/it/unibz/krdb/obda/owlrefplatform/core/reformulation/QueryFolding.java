package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Edge;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessSet.PropertiesCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFolding {
	private Set<Property> properties; 
	private Set<Atom> rootAtoms; 
	private Set<Term> roots; 
	//private Set<Atom> internalRootAtoms; 
	private Set<BasicClassDescription> internalRootConcepts;
	private Set<Term> internalRoots;
	private Set<Term> internalDomain;
	private List<TreeWitness> interior;
	private boolean status = true;

	private static final Logger log = LoggerFactory.getLogger(QueryFolding.class);
		
	public QueryFolding() {
		properties = new HashSet<Property>(); 
		rootAtoms = new HashSet<Atom>(); 
		roots = new HashSet<Term>(); 
		internalRootConcepts = null; 
		internalRoots = new HashSet<Term>();
		internalDomain = new HashSet<Term>();
		interior = Collections.EMPTY_LIST; // in-place QueryFolding for one-step TreeWitnesses, 
		                                   //             which have no interior TreeWitnesses
	}
	
	public QueryFolding(TreeWitness tw) {
		properties = new HashSet<Property>(); 
		rootAtoms = new HashSet<Atom>(); 
		roots = new HashSet<Term>(); 
		internalRootConcepts = (tw.getRootConcepts() == null) ? 
											null : new HashSet<BasicClassDescription>(tw.getRootConcepts()); 
		internalRoots = new HashSet<Term>(tw.getRoots());
		internalDomain = new HashSet<Term>(tw.getDomain());
		interior = new LinkedList<TreeWitness>();
		interior.add(tw);
	}

	public QueryFolding(QueryFolding qf) {
		properties = new HashSet<Property>(qf.properties); 
		rootAtoms = new HashSet<Atom>(qf.rootAtoms); 
		roots = new HashSet<Term>(qf.roots); 
		internalRootConcepts = (qf.internalRootConcepts == null) ?  
												null : new HashSet<BasicClassDescription>(qf.internalRootConcepts); 
		internalRoots = new HashSet<Term>(qf.internalRoots);
		internalDomain = new HashSet<Term>(qf.internalDomain);
		interior = new LinkedList<TreeWitness>(qf.interior);
		status = qf.status;
	}

	
	public QueryFolding extend(TreeWitness tw) {
		QueryFolding c = new QueryFolding(this);
		c.internalRoots.addAll(tw.getRoots());
		c.internalDomain.addAll(tw.getDomain());
		c.interior.add(tw);
		c.addToInternalRootConcepts(tw.getRootConcepts());
		return c;
	}
	
	private boolean addToInternalRootConcepts(Set<BasicClassDescription> subc) {
		if (subc == null)
			return true;
					
		if ((subc != null) && subc.isEmpty()) {
			status = false;
			return false;
		}
		
		if (internalRootConcepts == null)
			internalRootConcepts = new HashSet<BasicClassDescription>(subc);
		else
			internalRootConcepts.retainAll(subc);
		
		if (internalRootConcepts.isEmpty()) 
			status = false;
		
		return status;
	}
	
	public void extend(Term root, Set<Property> props, Set<Atom> rootLoop, Set<BasicClassDescription> intRootLoop) {
		assert(status);
		
//		for (Atom a: intRootLoop) {
//			if (a.getArity() == 2) {
//				log.debug("        NO LOOPS ALLOWED IN INTERNAL TERMS: " + a);
//				status = false;
//				return; 
//			}
//		}
		if (!addToInternalRootConcepts(intRootLoop))
			return;
		
		//internalRootAtoms.addAll(intRootLoop);
		roots.add(root);
		rootAtoms.addAll(rootLoop);

		if (properties.isEmpty()) // first edge
			properties.addAll(props);
		else
			properties.retainAll(props);
		
		if (properties.isEmpty()) 
			status = false;			
	}
	
	public boolean canBeFolded(Term t, QueryConnectedComponent cc, PropertiesCache propertiesCache) {
		properties.clear();
		rootAtoms.clear();
		roots.clear();
		internalRootConcepts = null; 
		internalDomain = Collections.singleton(t);
		terms = null;
		status = true;
		
		for (Edge edge : cc.getEdges()) {
			propertiesCache.setLoopAtoms(edge.getTerm1(), edge.getL1Atoms());
			propertiesCache.setLoopAtoms(edge.getTerm0(), edge.getL0Atoms());
			
			if (t.equals(edge.getTerm0()))
				extend(edge.getTerm1(), propertiesCache.getEdgeProperties(edge, edge.getTerm1()),  
										edge.getL1Atoms(), propertiesCache.getTermConcepts(edge.getTerm0()));
			else if (t.equals(edge.getTerm1()))
				extend(edge.getTerm0(), propertiesCache.getEdgeProperties(edge, edge.getTerm0()),  
										edge.getL0Atoms(), propertiesCache.getTermConcepts(edge.getTerm1()));
			
			if (!status)
				return false;
		}

		// TODO: EXTEND ROOT ATOMS BY ALL-ROOT EDGES
		
		log.debug("  PROPERTIES " + properties);
		log.debug("  ENDTYPE " + internalRootConcepts);
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
	
	public Set<BasicClassDescription> getInternalRootConcepts() {
		return internalRootConcepts;
	}
	
	public Collection<TreeWitness> getInteriorTreeWitnesses() {
		return interior;
	}
	
	private TreeWitness.TermCover terms;
	
	public TreeWitness.TermCover getTerms() {
		if (terms == null) {
			Set<Term> domain = new HashSet<Term>(internalDomain);
			domain.addAll(roots);
			terms = new TreeWitness.TermCover(domain, roots);
		}
		return terms;
	}
	
	public TreeWitness getTreeWitness(Collection<TreeWitnessGenerator> twg, Set<Variable> quantifiedVariables) {
		return new TreeWitness(twg, getTerms(), quantifiedVariables.containsAll(roots), rootAtoms); 	
	}
}
