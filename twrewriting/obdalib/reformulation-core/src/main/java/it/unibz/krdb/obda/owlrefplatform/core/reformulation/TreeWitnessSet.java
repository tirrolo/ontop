package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.BooleanOperationPredicateImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Edge;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessSet {
	private Map<TreeWitness.TermCover, TreeWitness> tws = new HashMap<TreeWitness.TermCover, TreeWitness>();
	private QueryConnectedComponent cc;
	private TreeWitnessReasonerLite reasoner;
	private PropertiesCache propertiesCache; 
	private List<TreeWitness> mergeable;

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessSet.class);
	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	private TreeWitnessSet(QueryConnectedComponent cc, TreeWitnessReasonerLite reasoner) {
		this.cc = cc;
		this.reasoner = reasoner;
	}
	
	public Collection<TreeWitness> getTWs(QueryConnectedComponent.Edge edge) {
		Collection<TreeWitness> m = new LinkedList<TreeWitness>();
		for (Map.Entry<TreeWitness.TermCover, TreeWitness> tw : tws.entrySet())
			if (tw.getKey().getDomain().contains(edge.getTerm0()) && tw.getKey().getDomain().contains(edge.getTerm1())) {
				m.add(tw.getValue());
			}			
		return m;
	}
	
	public Collection<TreeWitness> getTWs() {
		return tws.values();
	}
	
	public static TreeWitnessSet getTreeWitnesses(QueryConnectedComponent cc, TreeWitnessReasonerLite reasoner) {		
		TreeWitnessSet treewitnesses = new TreeWitnessSet(cc, reasoner);
		
		if (!cc.isDegenerate())
			treewitnesses.computeTreeWitnesses();
				
		return treewitnesses;
	}

	private void computeTreeWitnesses() {
		Map<TreeWitness.TermCover, TreeWitness> delta = new HashMap<TreeWitness.TermCover, TreeWitness>();
		
		propertiesCache = new PropertiesCache(reasoner);
		QueryFolding qf = new QueryFolding();
		
		for (Term v : cc.getQuantifiedVariables()) {
			log.debug("QUANTIFIED VARIABLE " + v); 			
			if (qf.canBeFolded(v, cc, propertiesCache)) {
				// delta cannot contain duplicates by construction
				Set<TreeWitnessGenerator> twg = getTreeWitnessGenerators(qf); 
				if (twg != null) { // does not make sense to cache negatives because they will never re-occur
					// copy the query folding
					TreeWitness tw = new QueryFolding(qf).getTreeWitness(twg, cc.getQuantifiedVariables()); 
					delta.put(tw.getTerms(), tw);
				}
			}
		}		
		
		mergeable = new LinkedList<TreeWitness>();
		Queue<TreeWitness> working = new LinkedList<TreeWitness>();
		while (true) {
			for (Map.Entry<TreeWitness.TermCover, TreeWitness> e : delta.entrySet()) {
				TreeWitness tw = e.getValue();
				if (tw == null) {
					// keep negative cache
					tws.put(e.getKey(), null);
				}
				else {
					TreeWitness tw0 = tws.get(e.getKey());
					if (tw0 == null) {
						tws.put(tw.getTerms(), tw);
						if (tw.allRootsQuantified())  {
							working.add(tw);			
							mergeable.add(tw);
						}
					}
					else
						assert (tw.getGenerators().equals(tw0.getGenerators()));
				}
			}

			if (working.isEmpty()) 
				break;
			
			delta.clear(); 
			while (!working.isEmpty()) {
				TreeWitness tw = working.poll(); 
				saturateTreeWitnesses(delta, new QueryFolding(tw)); 					
			}
		}				

		log.debug("TREE WITNESSES FOUND: " + tws.size());
		Iterator<Map.Entry<TreeWitness.TermCover, TreeWitness>> i = tws.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<TreeWitness.TermCover, TreeWitness> e = i.next();
			if (e.getValue() == null) {
				log.debug("REMOVE NEGATIVE CACHE: " + e.getKey());
				i.remove();	
			}
			else
				log.debug(" " + e.getValue());
		}
		
	}
	
	private void saturateTreeWitnesses(Map<TreeWitness.TermCover, TreeWitness> delta, QueryFolding qf) { 
		boolean saturated = true; 
		
		for (QueryConnectedComponent.Edge edge : cc.getEdges()) { 
			if (qf.canBeAttachedToAnInternalRoot(edge.getTerm0(), edge.getTerm1())) {
				log.debug("EDGE " + edge + " IS ADJACENT TO THE TREE WITNESS " + qf); 

				saturated = false; 

				for (TreeWitness tw : mergeable)  // (tw != null) && tw.allRootsQuantified() && 
					if (tw.getRoots().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
						log.debug("    ATTACHING A TREE WITNESS " + tw);
						saturateTreeWitnesses(delta, qf.extend(tw)); 
					} 
				
				QueryFolding qf2 = new QueryFolding(qf);
				qf2.extend(edge.getTerm1(), propertiesCache.getEdgeProperties(edge, edge.getTerm1()),  edge.getL1Atoms(), edge.getL0Atoms());
				if (qf2.isValid()) {
					log.debug("    ATTACHING A HANDLE " + edge);
					saturateTreeWitnesses(delta, qf2);  
				}	
			} 
			else if (qf.canBeAttachedToAnInternalRoot(edge.getTerm1(),edge.getTerm0())) { 
				log.debug("EDGE " + edge + " IS ADJACENT TO THE TREE WITNESS " + qf); 
				
				saturated = false; 
				
				for (TreeWitness tw : mergeable)  // (tw != null) && tw.allRootsQuantified() && 
					if (tw.getRoots().contains(edge.getTerm1()) && tw.getDomain().contains(edge.getTerm0())) {
						log.debug("    ATTACHING A TREE WITNESS " + tw);
						saturateTreeWitnesses(delta, qf.extend(tw)); 
					} 

				QueryFolding qf2 = new QueryFolding(qf);
				qf2.extend(edge.getTerm0(), propertiesCache.getEdgeProperties(edge, edge.getTerm0()),  edge.getL0Atoms(), edge.getL1Atoms());
				if (qf2.isValid()) {
					log.debug("    ATTACHING A HANDLE " + edge);
					saturateTreeWitnesses(delta, qf2);  
				}	
			} 
		}

		if (saturated && qf.hasRoot())  {
			if (tws.containsKey(qf.getTerms()) || delta.containsKey(qf.getTerms())) {
				log.debug("DUPLICATE " + qf.getTerms());
				return;
			}
			Set<TreeWitnessGenerator> twg = getTreeWitnessGenerators(qf); 
			if (twg != null) {
				TreeWitness tw = qf.getTreeWitness(twg, cc.getQuantifiedVariables()); 
				delta.put(tw.getTerms(), tw);
			}
			else
				delta.put(qf.getTerms(), null); // cache negative
		}
	}
	
	// can return null if there are no applicable generators!
	
	private Set<TreeWitnessGenerator> getTreeWitnessGenerators(QueryFolding qf) {
		Set<TreeWitnessGenerator> twg = null;
		
		log.debug("CHECKING WHETHER THE FOLDING " + qf + " CAN BE GENERATED: "); 
		for (TreeWitnessGenerator g : reasoner.getGenerators()) {
			log.debug("      CHECKING " + g);		
			if (qf.getProperties().contains(g.getProperty())) 
				log.debug("        PROPERTIES ARE FINE: " + qf.getProperties() + " FOR " + g.getProperty());
			else {
				log.debug("        PROPERTIES ARE TOO SPECIFIC: " + qf.getProperties() + " FOR " + g.getProperty());
				continue;
			}
			
			if (!isGenerated(g, qf.getInternalRootAtoms()))
				continue;
			
			if (twg == null) 
				twg = new HashSet<TreeWitnessGenerator>();
			twg.add(g);
			log.debug("TREE WITNESS GENERATOR: " + g);
		}
		return twg;
	}
	
	private boolean isGenerated(TreeWitnessGenerator g, Set<Atom> endtype) {
		for (Atom a : endtype) {
			 if (a.getArity() != 1)
				 return false;        // binary predicates R(x,x) cannot be matched to the anonymous part
			 
			 BasicClassDescription con = ontFactory.createClass(a.getPredicate());
			 if (!reasoner.getSubConcepts(con).contains(g.getFiller()) && 
					 !reasoner.getSubConcepts(con).contains(g.getRoleEndType())) {
				 log.debug("        ENDTYPE TOO SPECIFIC: " + con + " FOR " + g.getFiller() + " AND " + g.getRoleEndType());
				 return false;
			 }
			 else
				 log.debug("        ENDTYPE IS FINE: " + con + " FOR " + g.getFiller());
		}
		return true;
	}

	static class PropertiesCache {
		private Map<Edge, Set<Property>> prop0 = new HashMap<Edge, Set<Property>>();
		private Map<Edge, Set<Property>> prop1 = new HashMap<Edge, Set<Property>>();

		private TreeWitnessReasonerLite reasoner;
		
		private PropertiesCache(TreeWitnessReasonerLite reasoner) {
			this.reasoner = reasoner;
		}
		
		public Set<Property> getEdgeProperties(Edge edge, Term root) {
			Map<Edge, Set<Property>> props = edge.getTerm0().equals(root) ? prop0 : prop1;
			Set<Property> properties = props.get(edge);
			
			if (properties == null) {
				properties = new HashSet<Property>();
				for (Atom a : edge.getBAtoms()) {
					log.debug("EDGE " + edge + " HAS PROPERTY " + a);
					if (a.getPredicate() instanceof BooleanOperationPredicateImpl) {
						log.debug("        NO BOOLEAN OPERATION PREDICATES ALLOWED IN PROPERTIES ");
						properties.clear();
					}
					else {
						Property p = ontFactory.createProperty(a.getPredicate(), !root.equals(a.getTerm(0)));
						if (properties.isEmpty()) // first atom
							properties.addAll(reasoner.getSubProperties(p));
						else
							properties.retainAll(reasoner.getSubProperties(p));
					}
					if (properties.isEmpty())
						break;
				}				
				props.put(edge, properties);
			}

			return properties;
		}
	}

	public Set<TreeWitnessGenerator> getGeneratorsOfDetachedCC() {		
		Set<TreeWitnessGenerator> generators = new HashSet<TreeWitnessGenerator>();
		
		if (cc.isDegenerate()) { // do not remove the curly brackets -- dangling else otherwise
			for (TreeWitnessGenerator some : reasoner.getGenerators())
				if (isGenerated(some, cc.getEdges().get(0).getL0Atoms())) 
					generators.add(some);					
		} else {
			for (TreeWitness tw : tws.values()) 
				if (tw.getDomain().containsAll(cc.getVariables())) {
					log.debug("TREE WITNESS " + tw + " COVERS THE QUERY");
					for (TreeWitnessGenerator some : reasoner.getGenerators())
						if (isGenerated(some, tw.getRootAtoms())) // generator itself?
							generators.add(some);
				}
		}
		
		boolean saturated = true;
		do {
			List<TreeWitnessGenerator> delta = new LinkedList<TreeWitnessGenerator>();
			for (TreeWitnessGenerator gen : generators)
				for (BasicClassDescription g : gen.getConcepts()) {
					for (TreeWitnessGenerator some : reasoner.getGenerators()) {
						if (reasoner.getSubConcepts(g).contains(some.getFiller()) || 
								reasoner.getSubConcepts(g).contains(some.getRoleEndType())) {
							saturated = false;
							delta.add(some);
						}		 		
					}
				}
			saturated = !generators.addAll(delta);
		} while (!saturated);						
		
		return generators;
	}
	
	
}
