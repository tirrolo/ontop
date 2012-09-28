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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessSet {
	private Set<TreeWitness> tws = new HashSet<TreeWitness>();
	private QueryConnectedComponent cc;
	private TreeWitnessReasonerLite reasoner;
	private PropertiesCache propertiesCache; 

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessSet.class);
	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	private TreeWitnessSet(QueryConnectedComponent cc, TreeWitnessReasonerLite reasoner) {
		this.cc = cc;
		this.reasoner = reasoner;
	}
	
	public Collection<TreeWitness> getTWs(QueryConnectedComponent.Edge edge) {
		Collection<TreeWitness> m = new LinkedList<TreeWitness>();
		for (TreeWitness tw : tws)
			if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
				m.add(tw);
			}			
		return m;
	}
	
	public Collection<TreeWitness> getTWs() {
		return tws;
	}
	
	public static TreeWitnessSet getTreeWitnesses(QueryConnectedComponent cc, TreeWitnessReasonerLite reasoner) {		
		TreeWitnessSet treewitnesses = new TreeWitnessSet(cc, reasoner);
		
		if (!cc.isDegenerate())
		{
			List<TreeWitness> delta = new LinkedList<TreeWitness>();
			
			treewitnesses.propertiesCache = new PropertiesCache(reasoner);
			QueryFolding qf = new QueryFolding();
			
			for (Term v : cc.getQuantifiedVariables()) {
				log.debug("QUANTIFIED VARIABLE " + v); 			
				if (qf.canBeFolded(v, cc, treewitnesses.propertiesCache))
					treewitnesses.addAllTreeWitnesses(new QueryFolding(qf), delta);
			}		
			
			List<TreeWitness> working = new LinkedList<TreeWitness>();
			while (true) {
				for (TreeWitness tw : delta)
					if (treewitnesses.addTreeWitness(tw)) // (treewitnesses.add(tw))
						working.add(tw);		

				if (working.isEmpty()) 
					break;
				
				delta.clear(); 
				for (TreeWitness tw : working) 
					if (tw.allRootsQuantified()) 
						treewitnesses.saturateTreeWitnesses(delta, new QueryFolding(tw)); 					
				working.clear();
			}				
		}
		log.debug("TREE WITNESSES FOUND: " + treewitnesses.tws);
		for (TreeWitness tw : treewitnesses.tws) 
			log.debug(" " + tw);
				
		return treewitnesses;
	}

	private void saturateTreeWitnesses(List<TreeWitness> delta, QueryFolding qf) { 
		boolean saturated = true; 
		
		for (QueryConnectedComponent.Edge edge : cc.getEdges()) { 
			if (qf.canBeAttachedToAnInternalRoot(edge.getTerm0(), edge.getTerm1())) {
				log.debug("EDGE " + edge + " IS ADJACENT TO THE TREE WITNESS " + qf); 

				saturated = false; 

				for (TreeWitness tw : tws)  
					if (tw.allRootsQuantified() && 
							tw.getRoots().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
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
				
				for (TreeWitness tw : tws)  
					if (tw.allRootsQuantified() && 
							tw.getRoots().contains(edge.getTerm1()) && tw.getDomain().contains(edge.getTerm0())) {
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

		if (saturated && qf.hasRoot())  
			addAllTreeWitnesses(qf, delta);
	}
	 
	private void addAllTreeWitnesses(QueryFolding qf, List<TreeWitness> delta) {

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
			
			TreeWitness tw = qf.getTreeWitness(g, cc.getQuantifiedVariables()); 
			log.debug("TREE WITNESS: " + tw);
			delta.add(tw); 
		}
	}
	
	private boolean addTreeWitness(TreeWitness tw0) {
		TreeWitnessGenerator twg0 = tw0.getGenerator();
		
		Iterator<TreeWitness> i = tws.iterator();
		while (i.hasNext()) {
			TreeWitness tw = i.next();
			if (tw.getDomain().equals(tw0.getDomain()) && tw.getRoots().equals(tw0.getRoots())) {
				TreeWitnessGenerator twg = tw.getGenerator();
				if (reasoner.isSubsumed(twg0, twg)) {
						log.debug("SUBSUMED: " + tw0 + " BY " + tw);
						return false;
					}
				else if (reasoner.isSubsumed(twg, twg0)) {
						log.debug("SUBSUMED: " + tw + " BY " + tw0);
						i.remove();
					}
			}
		}
		tws.add(tw0);
		return true;
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
			for (TreeWitness tw : tws) 
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
