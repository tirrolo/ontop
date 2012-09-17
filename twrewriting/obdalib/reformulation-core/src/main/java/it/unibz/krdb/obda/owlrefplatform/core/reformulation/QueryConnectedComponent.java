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

public class QueryConnectedComponent {

	private List<Term> variables; 	
	private List<Variable> quantifiedVariables;
	private List<Term> freeVariables;
	
	private List<Edge> edges;  // a connect component contains a list of edges 
	
	private boolean noFreeTerms; // no free variables and no constants 
	                             // if true the component can be mapped onto the anonymous part of the canonical model
	private boolean isDegenerate; // contains just a single term (and no proper edges)

	private QueryConnectedComponent(List<Edge> edges, Set<Term> terms, Set<Term> headTerms) {
		this.edges = edges;
		this.isDegenerate = (terms.size() == 1);

		variables = new ArrayList<Term>(terms.size());
		quantifiedVariables = new ArrayList<Variable>(terms.size());
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

	public static List<QueryConnectedComponent> getQueryConnectedComponents(CQIE cqie) {
		List<QueryConnectedComponent> ccs = new LinkedList<QueryConnectedComponent>();

		// collect all edges and loop 
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
					edge = new Edge(pair, a);
					pairs.put(pair, edge);
				}
				else
					edge.addAtom(a);			
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
		// extend atom lists for edges by the loop atoms of both ends 
		for (Edge e : pairs.values()) {
			e.addAllAtoms(loops.get(e.getTerm0()));
			e.addAllAtoms(loops.get(e.getTerm1()));
		}
		
		Set<Term> headTerms = new HashSet<Term>(cqie.getHead().getTerms());
		
		// form the set of connected components from edges
		while (!pairs.isEmpty()) {
			List<Edge> edgesInCC = new ArrayList<Edge>(cqie.getBody().size());
			Set<Term> terms = new HashSet<Term>();
			Iterator<Entry<TermPair,Edge>> i = pairs.entrySet().iterator();

			// add an edge to the current CC
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
						terms.add(t1);
						i.remove();
					}
					else if (terms.contains(t1)) {
						edgesInCC.add(edge);
						expanded = true;
						terms.add(t0);
						i.remove();
					}
				}
			} while (expanded);
			
			// remove the respective loops
			for (Term t : terms) 
				loops.remove(t); 

			ccs.add(new QueryConnectedComponent(edgesInCC, terms, headTerms));			
		}
		
		// convert disconnected loops into degenerate connected components
		for (Entry<Term, Set<Atom>> loop : loops.entrySet()) {
			Term t = loop.getKey();
			List<Edge> edgesInCC = new ArrayList<Edge>(1);
			edgesInCC.add(new Edge(new TermPair(t, t), loop.getValue()));
			ccs.add(new QueryConnectedComponent(edgesInCC, Collections.singleton(t), headTerms));
		}
		
		return ccs;
	}
	
	public boolean isDegenerate() {
		return isDegenerate;
	}
	
	
	public boolean hasNoFreeTerms() {
		return noFreeTerms;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public Set<Edge> getIncidentEdges(Term t) {
		// used only for quantified variables t (not arbitrary terms)
		Set<Edge> inc = new HashSet<Edge>(edges.size());
		for (Edge e : edges) {
			if (e.getTerm0().equals(t) || e.getTerm1().equals(t))
				inc.add(e);
		}
		return inc;
	}
	
	public List<Term> getVariables() {
		return variables;		
	}

	public List<Variable> getQuantifiedVariables() {
		return quantifiedVariables;		
	}
	
	public List<Term> getFreeVariables() {
		return freeVariables;
	}
	
	static class Edge {
		private TermPair terms;
		private Set<Atom> atoms;
		
		public Edge(TermPair terms, Atom a) {
			this.terms = terms;
			this.atoms = new HashSet<Atom>();
			atoms.add(a);
		}
		
		public Edge(TermPair terms, Set<Atom> atoms) {
			this.terms = terms;
			this.atoms = atoms;
		}

		public Term getTerm0() {
			return terms.t0;
		}
		
		public Term getTerm1() {
			return terms.t1;
		}
	
		private void addAtom(Atom a) {
			atoms.add(a);
		}

		private void addAllAtoms(Set<Atom> set) {
			if (set != null)
				atoms.addAll(set);
		}
		
		public Set<Atom> getAtoms() {
			return atoms;
		}

		@Override
		public String toString() {
			return "edge: {" + terms.t0 + ", " + terms.t1 + "}" + atoms;
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
