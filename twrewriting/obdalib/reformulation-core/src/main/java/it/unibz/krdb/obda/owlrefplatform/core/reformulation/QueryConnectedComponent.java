package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * QueryConnectedComponent represents a connected component of a CQ
 * 
 * keeps track of variables (both quantified and free) and edges
 * 
 * a connected component can either be degenerate (if it has no proper edges, i.e., just a loop)
 * 
 * @author Roman Kontchakov
 *
 */

public class QueryConnectedComponent {

	private List<Term> variables; 	
	private Set<Variable> quantifiedVariables;   // set for more efficient .containsAll check
	private List<Term> freeVariables;
	
	private List<Edge> edges;  // a connect component contains a list of edges 
	
	private boolean noFreeTerms; // no free variables and no constants 
	                             // if true the component can be mapped onto the anonymous part of the canonical model
	private boolean isDegenerate; // contains just a single term (and no proper edges)

	
	/**
	 * constructor: it is private as instances created only by the static method getConnectedComponents
	 * 
	 * @param edges: a list of edges in the connected component
	 * @param terms: terms that are coveted by the edges
	 * @param headTerms: terms of the head of the query, which is used to determine whether a variable is free of quantified
	 */
	
	private QueryConnectedComponent(List<Edge> edges, Set<Term> terms, Set<Term> headTerms) {
		this.edges = edges;
		this.isDegenerate = (terms.size() == 1);

		variables = new ArrayList<Term>(terms.size());
		quantifiedVariables = new HashSet<Variable>(terms.size());
		freeVariables = new ArrayList<Term>(terms.size());
		noFreeTerms = true;
		
		for (Term t : terms) {
			if (t instanceof Variable) {
				variables.add(t);
				if (!headTerms.contains(t))
					quantifiedVariables.add((Variable)t);
				else {
					freeVariables.add(t);
					noFreeTerms = false;
				}
			}
			else
				noFreeTerms = false; // not a variable -- better definition?
		}
	}

	/**
	 * getConnectedComponents creates a list of connected components of a given CQ
	 * 
	 * @param cqie: CQ to be split into connected components 
	 * @return list of connected components
	 */
	
	public static List<QueryConnectedComponent> getConnectedComponents(CQIE cqie) {
		List<QueryConnectedComponent> ccs = new LinkedList<QueryConnectedComponent>();

		// collect all edges and loops 
		//      an edge is a binary predicate P(t, t') with t \ne t'
		// 		a loop is either a unary predicate A(t) or a binary predicate P(t,t)
		Map<TermPair, Edge> pairs = new HashMap<TermPair, Edge>();
		Map<Term, Set<Atom>> loops = new HashMap<Term, Set<Atom> >();
		
		for (Atom a: cqie.getBody()) {
			Term t0 = a.getTerm(0);				
			if (a.getArity() == 2 && !t0.equals(a.getTerm(1))) {
				TermPair pair = new TermPair(t0, a.getTerm(1));
				Edge edge =  pairs.get(pair); 
				if (edge == null) {
					edge = new Edge(pair);
					pairs.put(pair, edge);
				}
				edge.addBAtom(a);			
			}
			else // if ((a.getArity() == 1) || terms are equal)
			{
				Set<Atom> loop = loops.get(t0);
				if (loop == null) {
					loop = new HashSet<Atom>();
					loops.put(t0, loop);
				}
				loop.add(a);
			}
		}	
		for (Edge e : pairs.values())
			e.setLAtoms(loops.get(e.getTerm0()), loops.get(e.getTerm1()));
		
		Set<Term> headTerms = new HashSet<Term>(cqie.getHead().getTerms());
		
		// form the list of connected components from the list of edges
		while (!pairs.isEmpty()) {
			List<Edge> edgesInCC = new ArrayList<Edge>(cqie.getBody().size());
			Set<Term> terms = new HashSet<Term>();
			Iterator<Entry<TermPair,Edge>> i = pairs.entrySet().iterator();

			// add the first available edge to the current CC
			Edge edge0 = i.next().getValue();
			edgesInCC.add(edge0);
			terms.add(edge0.getTerm0());
			terms.add(edge0.getTerm1());
			i.remove();
			
			// expand the current CC by adding all edges that are have at least one of the terms in them
			boolean expanded = false;
			do {
				expanded = false;
				i = pairs.entrySet().iterator();
				while (i.hasNext()) {
					Edge edge = i.next().getValue();
					Term t0 = edge.getTerm0();
					Term t1 = edge.getTerm1();
					if (terms.contains(t0)) {
						edgesInCC.add(edge);
						expanded = true;
						terms.add(t1); // the other term is already there
						i.remove();
					}
					else if (terms.contains(t1)) {
						edgesInCC.add(edge);
						expanded = true;
						terms.add(t0); // the other term is already there
						i.remove();
					}
				}
			} while (expanded);
			
			// remove the loops that are covered by the edges in CC
			for (Term t : terms) 
				loops.remove(t); 

			ccs.add(new QueryConnectedComponent(edgesInCC, terms, headTerms));			
		}
		
		// create degenerate connected components for all remaining loops (which are disconnected from anything else)
		for (Entry<Term, Set<Atom>> loop : loops.entrySet()) {
			Term t = loop.getKey();
			List<Edge> edgesInCC = new ArrayList<Edge>(1);
			Edge e = new Edge(new TermPair(t, t));
			e.setLAtoms(loop.getValue(), loop.getValue());
			edgesInCC.add(e);
			ccs.add(new QueryConnectedComponent(edgesInCC, Collections.singleton(t), headTerms));
		}
		
		return ccs;
	}
	
	/**
	 * boolean isDenenerate() 
	 * 
	 * @return true if the component is degenerate (has no proper edges with two distinct terms)
	 */
	
	public boolean isDegenerate() {
		return isDegenerate;
	}
	
	/**
	 * boolean hasNoFreeTerms()
	 * 
	 * @return true if all terms of the connected component are existentially quantified variables
	 */
	
	public boolean hasNoFreeTerms() {
		return noFreeTerms;
	}
	
	/**
	 * List<Edge> getEdges()
	 * 
	 * @return the list of edges in the connected component
	 */
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	/**
	 * List<Term> getVariables()
	 * 
	 * @return the list of variables in the connected components
	 */
	
	public List<Term> getVariables() {
		return variables;		
	}

	/**
	 * Set<Variable> getQuantifiedVariables()
	 * 
	 * @return the set (for efficient membership) of existentially quantified variables
	 */
	
	public Set<Variable> getQuantifiedVariables() {
		return quantifiedVariables;		
	}
	
	/**
	 * List<Term> getFreeVariables()
	 * 
	 * @return the list of free variables in the connected component
	 */
	
	public List<Term> getFreeVariables() {
		return freeVariables;
	}
	
	/**
	 * Edge: class representing edges of connected components
	 * 
	 * an edge is characterized by a pair of terms and a set of atoms involving only those terms
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	static class Edge {
		private TermPair terms;
		private Set<Atom> bAtoms;
		private Set<Atom> l0Atoms, l1Atoms;
		
		public Edge(TermPair terms) {
			this.terms = terms;
			this.bAtoms = new HashSet<Atom>();
			this.l0Atoms = null;
			this.l1Atoms = null;
		}

		public Term getTerm(int v) {
			return (v == 0) ? terms.t0 : terms.t1;
		}
		
		public Term getTerm0() {
			return terms.t0;
		}
		
		public Term getTerm1() {
			return terms.t1;
		}
	
		public Set<Atom> getBAtoms() {
			return bAtoms;
		}
		
		public Set<Atom> getLAtoms(int v) {
			return (v == 0) ? l0Atoms : l1Atoms;
		}
		public Set<Atom> getL0Atoms() {
			return l0Atoms;
		}
		public Set<Atom> getL1Atoms() {
			return l1Atoms;
		}

		private void addBAtom(Atom a) {
			bAtoms.add(a);
		}

		private void setLAtoms(Set<Atom> l0Atoms, Set<Atom> l1Atoms) {
			this.l0Atoms = (l0Atoms == null) ? Collections.EMPTY_SET : l0Atoms;
			this.l1Atoms = (l1Atoms == null) ? Collections.EMPTY_SET : l1Atoms;
		}
		
		@Override
		public String toString() {
			return "edge: {" + terms.t0 + ", " + terms.t1 + "}" + bAtoms + l0Atoms + l1Atoms;
		}
		
		@Override 
		public boolean equals(Object o) {
			if (o instanceof Edge) 
				return terms.equals(((Edge)o).terms);
			return false;
		}
		
		@Override
		public int hashCode() {
			return terms.hashCode();
		}
	}
	
	/**
	 * TermPair: a simple abstraction of *unordered* pair of terms (i.e., {t1, t2} and {t2, t1} are equal)
	 * 
	 * @author Roman Kontchakov
	 *
	 */
	
	private static class TermPair {
		private Term t0, t1;

		public TermPair(Term t0, Term t1) {
			this.t0 = t0;
			this.t1 = t1;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			
			if (o instanceof TermPair) {
				TermPair other = (TermPair) o;
				if (this.t0.equals(other.t0) 
						&& this.t1.equals(other.t1))
					return true;
				if (this.t0.equals(other.t1) 
						&& this.t1.equals(other.t0))
					return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "term pair: {" + t0 + ", " + t1 + "}";
		}
		
		@Override
		public int hashCode() {
			return t0.hashCode() ^ t1.hashCode();
		}
	}	
}
