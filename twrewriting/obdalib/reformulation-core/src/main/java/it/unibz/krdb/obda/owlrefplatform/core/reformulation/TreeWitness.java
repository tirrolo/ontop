package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.ontology.BasicClassDescription;

/**
 * TreeWitness: universal tree witnesses as in the KR 2012 paper
 *     each tree witness is determined by its domain, root terms and a set of \exists R.B concept 
 *           that generate a tree in the TBox canonical model to embed the tree witness part of the query
 *           
 *           roots are the terms that are mapped to the root of that tree
 *           
 *           the "tree witness part of the query" consists of all atoms in the query 
 *                       with terms in the tw domain and at least one of the terms not being a tw root
 *                       
 *     each instance also stores those atoms of the query with all terms among the tw roots
 *      
 *     this information is enough to produce the tree witness formula tw_f 
 *     
 * @author Roman Kontchakov
 *
 */

public class TreeWitness {
	private TermCover terms;
	
	private Set<Atom> rootAtoms; // atoms of the query that contain only the roots of the tree witness
	                            // these atoms must hold true for this tree witness to be realised
	private Collection<TreeWitnessGenerator> gens; // the \exists R.B concepts that realise the tree witness 
	                                          // in the canonical model of the TBox
	private boolean allRootsQuantified; // all the roots are quantified variables
	
	private Set<BasicClassDescription> rootConcepts;
	
	private List<List<Atom>> twfs;  // tw-formula: disjunction of conjunctions of atoms

	public TreeWitness(Collection<TreeWitnessGenerator> gens, TermCover terms, boolean allRootsQuantified, Set<Atom> rootAtoms) {
		this.gens = gens;
		this.terms = terms;
		this.allRootsQuantified = allRootsQuantified;
		this.rootAtoms = rootAtoms;
		//this.domain = domain; // new HashSet<Term>(roots); domain.addAll(nonroots);
	}
	
	public void setFormula(List<List<Atom>> twfs) {
		this.twfs = twfs;
	}
	
	public List<List<Atom>> getFormula() {
		return twfs;
	}
	
	public void setRootConcepts(Set<BasicClassDescription> rootConcepts) {
		this.rootConcepts = rootConcepts;
	}

	public Set<BasicClassDescription> getRootConcepts() {
		return rootConcepts;
	}
	
	/**
	 * Set<Term> getRoots()
	 * 
	 * @return set of roots of the tree witness
	 */
	public Set<Term> getRoots() {
		return terms.getRoots();
	}
	
	/**
	 * boolean allRootsQuantified()
	 * 
	 * @return true if all root terms are quantified variables 
	 */
	public boolean allRootsQuantified() {
		return allRootsQuantified;
	}
	
	/**
	 * Set<Term> getDomain()
	 * 
	 * @return the domain (set of terms) of the tree witness
	 */
	
	public Set<Term> getDomain() {
		return terms.getDomain();
	}
	
	public TermCover getTerms() {
		return terms;
	}
	
	/**
	 * Set<TreeWitnessGenerator> getGenerator()
	 * 
	 * @return the tree witness generators \exists R.B
	 */
	
	public Collection<TreeWitnessGenerator> getGenerators() {
		return gens;
	}
	
	
	/**
	 * Set<Atom> getRootAtoms()
	 * 
	 * @return query atoms with all terms among the roots of tree witness
	 */
	
	public Set<Atom> getRootAtoms() {
		return rootAtoms;
	}
	
	@Override
	public String toString() {
		return "tree witness generated by " + gens + "\n    with domain " + terms + " and root atoms " + rootAtoms;
	}

/*	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TreeWitness) {
			TreeWitness other = (TreeWitness)obj;
			return this.gens.equals(other.gens) &&
					this.terms.equals(other.terms) && 
					this.rootAtoms.equals(other.rootAtoms);			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return gens.hashCode() ^ terms.hashCode() ^ rootAtoms.hashCode(); 
	}
*/
	
	public static class TermCover {
		private Set<Term> domain; // terms that are covered by the tree witness
		private Set<Term> roots;   // terms that are mapped onto the root of the tree witness
		
		public TermCover(Set<Term> domain, Set<Term> roots) {
			this.domain = domain;
			this.roots = roots;
		}
		
		public Set<Term> getDomain() {
			return domain;
		}
		
		public Set<Term> getRoots() {
			return roots;
		}
		
		@Override
		public String toString() {
			return "tree witness domain " + domain + " with roots " + roots;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TermCover) {
				TermCover other = (TermCover)obj;
				return this.roots.equals(other.roots) && 
					   this.domain.equals(other.domain);			
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return roots.hashCode() ^ domain.hashCode(); 
		}
	}

}