package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeWitnessQueryGraph {

	private List<Term> variables; 	
	private List<Variable> quantifiedVariables;
	private List<Edge> edges;
	private boolean noFreeTerms;
	
	public TreeWitnessQueryGraph(CQIE cqie) {
		noFreeTerms = true;
		Set<Term> variablesSet = new HashSet<Term>();	
		Map<Term, Set<Atom> > loops = new HashMap<Term, Set<Atom> >();
		Map<TermPair, Edge> pairs = new HashMap<TermPair, Edge>();
		edges = new ArrayList<Edge>(cqie.getBody().size());
		
		for (Atom a: cqie.getBody()) {
			if (a.getArity() == 2 && !a.getTerm(0).equals(a.getTerm(1))) {
				Term t0 = a.getTerm(0);
				if (t0 instanceof Variable)
					variablesSet.add(t0);
				else
					noFreeTerms = false;
				Term t1 = a.getTerm(1);
				if (t1 instanceof Variable)
					variablesSet.add(t1);
				else
					noFreeTerms = false;
				
				TermPair pair = new TermPair(t0, t1);
				if (!pairs.containsKey(pair)) {
					Edge e = new Edge(t0, t1, a);
					edges.add(e);
					pairs.put(pair, e);
				}
				else
					pairs.get(pair).addAtom(a);			
			}
			else // if ((a.getArity() == 1) || terms are equal)
			{
				Term key = a.getTerm(0);
				if (key instanceof Variable)
					variablesSet.add((Variable)key);
				else
					noFreeTerms = false;
				
				if (!loops.containsKey(key)) {
					Set<Atom> atoms = new HashSet<Atom>();
					atoms.add(a);
					loops.put(key, atoms);
				}
				else
					loops.get(key).add(a);			
					
			}
		}	
		for (Edge e : edges) {
			e.addAllAtoms(loops.get(e.getTerm0()));
			e.addAllAtoms(loops.get(e.getTerm1()));
		}
		variables = new ArrayList<Term>(variablesSet);
		quantifiedVariables = new ArrayList<Variable>(variables.size());
		
		for (Term v : variables) 
			if (!cqie.getHead().getTerms().contains(v))
				quantifiedVariables.add((Variable)v);
		
		noFreeTerms = noFreeTerms && (cqie.getHead().getTerms().size() == 0);
	}
	
	public boolean hasNoFreeTerms() {
		return noFreeTerms;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public Set<Edge> getIncidentEdges(Term t) {
		// used only for quantified variables
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
	
	static class Edge {
		private Term t0, t1;
		private Set<Atom> atoms;
		
		public Edge(Term t0, Term t1, Atom a) {
			this.t0 = t0;
			this.t1 = t1;
			this.atoms = new HashSet<Atom>();
			atoms.add(a);
		}

		public Term getTerm0() {
			return t0;
		}
		
		public Term getTerm1() {
			return t1;
		}
	
		void addAtom(Atom a) {
			atoms.add(a);
		}

		void addAllAtoms(Set<Atom> set) {
			if (set != null)
				atoms.addAll(set);
		}
		
		public Set<Atom> getAtoms() {
			return atoms;
		}

		@Override
		public String toString() {
			return "edge: {" + t0 + ", " + t1 + "}" + atoms;
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
