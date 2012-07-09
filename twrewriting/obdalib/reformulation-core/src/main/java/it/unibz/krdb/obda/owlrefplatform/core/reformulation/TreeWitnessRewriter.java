package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessRewriter implements QueryRewriter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Ontology tbox;
	private Set<TreeWitnessGenerator> generators;

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	@Override
	public void setTBox(Ontology ontology) {
		this.tbox = ontology;
		log.debug("SET ONTOLOGY " + ontology);
		// collect generating axioms
		generators = new HashSet<TreeWitnessGenerator>();
		log.debug("AXIOMS");
		for (Axiom ax : tbox.getAssertions()) {
			if (ax instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl sax = (SubClassAxiomImpl) ax;
				log.debug("AXIOM: " + sax);
				ClassDescription sc = sax.getSuper();
				log.debug("SC TYPE: " + sc.getClass());
				if (sc instanceof PropertySomeClassRestriction) {
					PropertySomeClassRestriction some = (PropertySomeClassRestriction) sc;
					log.debug("property " + some.getPredicate() + ", filler " + some.getFiller() + " from " + sax);
					generators.add(new TreeWitnessGenerator
							(new PredicatePosition(some.getPredicate(),
									some.isInverse() ? PredicatePosition.INVERSE : PredicatePosition.DIRECT), some.getFiller()));
				} 
				else if (sc instanceof PropertySomeRestriction) {
					PropertySomeRestriction some = (PropertySomeRestriction) sc;
					log.debug("property " + some.getPredicate() + ", filler TOP" + " from " + sax);
					generators.add(new TreeWitnessGenerator
							(new PredicatePosition(some.getPredicate(),
									some.isInverse() ? PredicatePosition.INVERSE : PredicatePosition.DIRECT)));
				} 
				else  { // if (sc instanceof OClass)
					log.debug("SUBCLASS OF " + sc + ": " + sax + ((sc instanceof OClass) ? "" : " UNKNOWN TYPE"));
				}
			} 
			else
				log.debug(ax.toString());
		}
	}

	@Override
	public void setCBox(Ontology sigma) {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	private static CQIE getCQIE(Atom head, Atom body) {
		List<Atom> b = new LinkedList<Atom>();
		b.add(body);
		return fac.getCQIE(head, b);
	}

	private static CQIE getCQIE(Atom head, Atom body1, Atom body2) {
		List<Atom> b = new LinkedList<Atom>();
		b.add(body1);
		b.add(body2);
		return fac.getCQIE(head, b);
	}

	private static Atom getAtom(URI name, List<Term> terms) {
		return fac.getAtom(fac.getPredicate(name, terms.size()), terms);
	}

	private static Atom getAtom(URI name, Term term) {
		List<Term> terms = new LinkedList<Term>();
		terms.add(term);
		return fac.getAtom(fac.getClassPredicate(name), terms);
	}

	private static Atom getAtom(URI name, Term term1, Term term2) {
		List<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		return fac.getAtom(fac.getObjectPropertyPredicate(name), terms);
	}

	private static Atom getRoleAtom(TreeWitnessGenerator p, Term t1, Term t2) 
	{
		if (!p.isInverse())
			return getAtom(p.getPredicate().getName(), t1, t2);
		else
			return getAtom(p.getPredicate().getName(), t2, t1);
	}

	private static class AdjacentTermsPair {
		private List<Term> terms;
		private URI name;

		public AdjacentTermsPair(List<Term> terms, URI name) {
			this.terms = terms;
			this.name = name;
		}

		public URI getName() {
			return name;
		}
		
		public List<Term> getTerms() {
			return terms;
		}
		
		public boolean equals(Object o) {
			if (o instanceof AdjacentTermsPair) {
				AdjacentTermsPair other = (AdjacentTermsPair) o;
				if (this.terms.get(0).equals(other.terms.get(0)) 
						&& this.terms.get(1).equals(other.terms.get(1)))
					return true;
				if (this.terms.get(0).equals(other.terms.get(1)) 
						&& this.terms.get(1).equals(other.terms.get(0)))
					return true;
			}
			return false;
		}

		public String toString() {
			return "term pair: {" + terms.get(0) + ", " + terms.get(1) + "}";
		}
		
		public int hashCode() {
			return terms.get(0).hashCode() ^ terms.get(1).hashCode();
		}
	}

	private void rewriteCQ(CQIE cqie, DatalogProgram output) throws URISyntaxException {

		int pairIndex = 0;
		Map<AdjacentTermsPair, Set<Atom> > edges = new HashMap<AdjacentTermsPair, Set<Atom> >();
		Map<Term, Set<Atom> > loops = new HashMap<Term, Set<Atom> >();
		
		for (Atom a: cqie.getBody()) {
			if (a.getArity() == 2 && !a.getTerm(0).equals(a.getTerm(1))) {
				AdjacentTermsPair pair = new AdjacentTermsPair(a.getTerms(), getQName(a.getPredicate().getName(), ++pairIndex));
				if (!edges.containsKey(pair)) {
					Set<Atom> atoms = new HashSet<Atom>();
					atoms.add(a);
					edges.put(pair, atoms);
				}
				else
					edges.get(pair).add(a);			
			}
			else // if ((a.getArity() == 1) || terms are equal)
			{
				Term key = a.getTerm(0);
				if (!loops.containsKey(key)) {
					Set<Atom> atoms = new HashSet<Atom>();
					atoms.add(a);
					loops.put(key, atoms);
				}
				else
					loops.get(key).add(a);			
					
			}
		}
		
		Set<TreeWitness> tws = getReducedSetOfTreeWitnesses(cqie);	
		
		{
			List<Atom> mainbody = new ArrayList<Atom>(edges.size());
			for (AdjacentTermsPair pair : edges.keySet())
				mainbody.add(getAtom(pair.getName(), pair.getTerms()));
		
			// if no binary predicates -- TO BE REWRITTEN
			if (mainbody.size() == 0)
				for (Atom a : cqie.getBody())
					mainbody.add(getAtom(getExtName(a.getPredicate().getName()), a.getTerms()));

			output.appendRule(fac.getCQIE(cqie.getHead(), mainbody));
		}

		for (AdjacentTermsPair edge : edges.keySet()) {
			List<Atom> atoms = new ArrayList<Atom>(edges.get(edge));
			Set<Atom> arg0 = loops.get(edge.getTerms().get(0));
			if (arg0 != null)
				atoms.addAll(arg0);
			Set<Atom> arg1 = loops.get(edge.getTerms().get(1));
			if (arg1 != null)
				atoms.addAll(arg1);
			
			List<Atom> extAtoms = new ArrayList<Atom>(atoms.size());
			for (Atom aa : atoms)
				extAtoms.add(getAtom(getExtName(aa.getPredicate().getName()), aa.getTerms()));

			output.appendRule(fac.getCQIE(getAtom(edge.getName(),edge.getTerms()), extAtoms));
					
			for (TreeWitness tw : tws)
				if (tw.getDomain().containsAll(edge.getTerms())) {
					// TREE WITNESS FORMULAS
					List<Atom> twf = new LinkedList<Atom>();
					List<Term> roots = new LinkedList<Term>(tw.getRoots());
					Term r0 = roots.get(0);
					twf.add(getAtom(getExtName(tw.getGenerator()), r0));
					for (Term rt : roots)
						if (!rt.equals(roots.get(0)))
							twf.add(fac.getEQAtom(rt, r0));
					for (Atom c : tw.getRootType()) {
						// TODO: REWRITE
						twf.add(getAtom(getExtName(c.getPredicate().getName()), r0));
					}
					output.appendRule(fac.getCQIE(getAtom(edge.getName(), edge.getTerms()), twf));
				}
				
		}

		Set<TreeWitnessGenerator> gen = new HashSet<TreeWitnessGenerator>();
		for (TreeWitness tw : tws)
			gen.add(tw.getGenerator());
		
		log.debug("\nEXT PREDICATES");
		for (CQIE ext : getExtPredicates(cqie, gen))
			output.appendRule(ext);
	}
	
	@Override
	public OBDAQuery rewrite(OBDAQuery input) throws OBDAException {
		DatalogProgram dp = (DatalogProgram) input;
		DatalogProgram output = fac.getDatalogProgram(); // (cqie.getHead().getPredicate());

		for (CQIE cqie : dp.getRules()) {
			try {
				rewriteCQ(cqie, output);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		log.debug("REWRITTEN PROGRAM\n" + output);			
		DatalogProgram simplified = DatalogQueryServices.simplify(output,dp.getRules().get(0).getHead().getPredicate());
		log.debug("SIMPLIFIED PROGRAM\n" + simplified);
		DatalogProgram flattenned = DatalogQueryServices.flatten(simplified,dp.getRules().get(0).getHead().getPredicate());
		log.debug("FLATTENED PROGRAM\n" + flattenned);
		return flattenned;
	}

	private List<CQIE> getExtPredicates(CQIE cqie, Set<TreeWitnessGenerator> gencon) throws URISyntaxException {																															

		List<CQIE> list = new LinkedList<CQIE>();
		
		Set<Predicate> exts = new HashSet<Predicate>();
		for (Atom a : cqie.getBody())
			exts.add(a.getPredicate());

		// GENERATING CONCEPTS

		for (TreeWitnessGenerator some : gencon) {
			log.debug("GEN CON EXT: " + gencon);
			URI uri = getExtName(some);
			Term x = fac.getVariable("x");

			for (Predicate c : tbox.getConcepts())
				if (isSubsumed(c, some))
				{
					log.debug("EXT COMPUTE SUBCLASSES: " + c);
					list.add(getCQIE(getAtom(uri, x), getAtom(c.getName(), x)));
				}

			{
				Term w = fac.getVariable("w");
				Atom ra = getRoleAtom(some, x, w);
				exts.add(ra.getPredicate());
				ra.setPredicate(fac.getObjectPropertyPredicate(getExtName(ra.getPredicate().getName())));
				log.debug("ROLE ATOM: " + ra);
				// COMM
				// if(r.isSubsumed(f.getOWLObjectSomeValuesFrom(some.getProperty(),
				// f.getOWLThing()), some))
				list.add(getCQIE(getAtom(uri, x), ra));
			}
			/*
			 * for (OWLObjectPropertyExpression p: getProperties()) if
			 * (!p.isOWLBottomObjectProperty()) {
			 * if(isSubsumed(f.getOWLObjectSomeValuesFrom(p, f.getOWLThing()),
			 * some)) { list.add(getCQIE(new Atom(uri, x), getRoleAtom(p, x,
			 * w))); continue; }
			 * 
			 * for (OWLClassExpression c: getClasses()) if (!c.isOWLNothing() &&
			 * isSubsumed(f.getOWLObjectSomeValuesFrom(p, c), some))
			 * list.add(getCQIE(new Atom(uri, x), getRoleAtom(p, x, w), new
			 * Atom(new URI(c.asOWLClass().getIRI().toString()), w))); }
			 */
		}

		
		 for (Predicate pred : exts) { 
			 if (pred.getArity() == 1) { 
				 URI ext = getExtName(pred.getName()); 
				 // OWLClass ac = f.getOWLClass(pred.getIRI()); 
				 Term x = fac.getVariable("x");
				 //list.add(getCQIE(new Atom(ext, x), new Atom(a.getPredicate().getName(), x)));
		  
				 //for (OWLClass c: r.getClasses()) 
				//	 if (!c.isOWLNothing() && r.isSubsumed(c, ac)) 
						 list.add(getCQIE(getAtom(ext, x), getAtom(pred.getName(), x)));
		  
				 //for (OWLObjectPropertyExpression p: r.getProperties()) 
				//	 if (!p.isOWLBottomObjectProperty() && r.isSubsumed(f.getOWLObjectSomeValuesFrom(p, f.getOWLThing()), ac))
				//		 list.add(getCQIE(getAtom(ext, x), getRoleAtom(p, x, fac.getVariable("w")))); 
			 } 
			 else if (pred.getArity() == 2) { 
				 URI ext = getExtName(pred.getName()); 
				 // OWLObjectProperty pa = f.getOWLObjectProperty(pred.getIRI()); 
				 Term x = fac.getVariable("x");
				 Term y = fac.getVariable("y"); 
				 //list.add(getCQIE(new Atom(ext, x, y), new Atom(a.getPredicate().getName(), x, y)));
		  
				 //for (OWLObjectPropertyExpression p: r.getProperties()) 
				//	 if (!p.isOWLBottomObjectProperty() && r.isSubsumed(p, pa)) {
						 //OWLObjectPropertyExpression pi = p.getInverseProperty().getSimplified(); 
						 //if (!pi.isAnonymous() && isSubsumed(pa, pi.getInverseProperty())) { 
						 // 	System.out.println("INVERSE " + pi + " OF " + a.getPredicate() + ": NO EXTRA RULE GENERATED"); 
						 // 	continue; 
						 //}
						 list.add(getCQIE(getAtom(ext, x, y), getAtom(pred.getName(), x, y))); 
					//} 
			 } 
		 }
		 return list;
	}

	
	
	
	private static URI getExtName(URI name) throws URISyntaxException {
		return new URI(name.getScheme(), name.getSchemeSpecificPart(), "EXT_" + name.getFragment());
	}

	private static URI getExtName(TreeWitnessGenerator some) throws URISyntaxException {
		URI property = some.getPredicate().getName();
		String fillerName = (some.getFiller() != null) ? some.getFiller().getPredicate().getName().getFragment() : "T";
		return new URI(property.getScheme(), property.getSchemeSpecificPart(), "EXT_" + property.getFragment()
				+ (some.isInverse() ? "_I_" : "_") + fillerName);
	}

	private static URI getQName(URI name, int pos) throws URISyntaxException {
		return new URI(name.getScheme(), name.getSchemeSpecificPart(), "Q_" + pos + "_" + name.getFragment());
	}

	private Set<TreeWitness> getReducedSetOfTreeWitnesses(CQIE cqie) {
		Set<TreeWitness> treewitnesses = getTreeWitnesses(cqie);

		Set<TreeWitness> subtws = new HashSet<TreeWitness>();
		for (TreeWitness tw : treewitnesses) {
			boolean subsumed = false;
			for (TreeWitness tw1 : treewitnesses)
				if (!tw.equals(tw1) && tw.getDomain().equals(tw1.getDomain()) && tw.getRoots().equals(tw1.getRoots()))
				// COMM if
				// (reasoner.isEntailed(f.getOWLSubClassOfAxiom(tw.getGenerator(),
				// tw1.getGenerator())))
				{
					System.out.println("SUBSUMED: " + tw + " BY " + tw1);
					subsumed = true;
					// break;
				}
			if (!subsumed)
				subtws.add(tw);
		}
		return subtws;
	}

	private Set<TreeWitness> getTreeWitnesses(CQIE cqie) {
		Set<Variable> quantifiedVariables = new HashSet<Variable>();

		for (Atom a : cqie.getBody()) 
			for (Term t : a.getTerms()) {
				if ((t instanceof Variable) && !cqie.getHead().getTerms().contains(t))
					quantifiedVariables.add((Variable)t);
			}
		log.debug("QUANTIFIED VARIABLES: " + quantifiedVariables);
		
		Set<TreeWitness> treewitnesses = new HashSet<TreeWitness>();
		
		for (Term v: quantifiedVariables) {
			log.debug("VARIABLE " + v); 
			List<PredicatePosition> edges = new LinkedList<PredicatePosition>(); 
			Set<Atom> endtype = new HashSet<Atom>(); 
			Set<Term> roots = new HashSet<Term>(); 
			
			for (Atom a : cqie.getBody()) { 
				if (((a.getArity() == 1) && a.getTerm(0).equals(v)) ||
					((a.getArity() == 2) && a.getTerm(0).equals(v) && a.getTerm(1).equals(v))) {
					endtype.add(a); 
				}
				else if ((a.getArity() == 2) && a.getTerm(1).equals(v)) {
					edges.add(new PredicatePosition(a.getPredicate(), PredicatePosition.DIRECT));
					roots.add(a.getTerm(0)); 
				} 
				else if ((a.getArity() == 2) && a.getTerm(0).equals(v)) {
					edges.add(new PredicatePosition(a.getPredicate(), PredicatePosition.INVERSE)); 
					roots.add(a.getTerm(1)); 
				} 
			} 
			log.debug("  EDGES " + edges);
			log.debug("  ENDTYPE " + endtype);
		 
			for (TreeWitnessGenerator g: generators) {
				if (isTreeWitness(g, roots, edges, endtype)) { 
					TreeWitness tw = new TreeWitness(g, roots, getRootType(cqie, roots), Collections.singleton(v)); 
					log.debug("TREE WITNESS: " + tw);
					treewitnesses.add(tw); 
				} 
			} 
		}
		 
		 /* Set<TreeWitness> delta = new HashSet<TreeWitness>(); do for
		 * (TreeWitness tw: treewitnesses) { Set<TreeWitness> twa = new
		 * HashSet<TreeWitness>(); twa.add(tw);
		 * saturateTreeWitnesses(treewitnesses, delta, new HashSet<Term>(), new
		 * LinkedList<OWLObjectPropertyExpression>(), twa); } while
		 * (treewitnesses.addAll(delta));
		 */
		return treewitnesses;
	}

	/*
	 * COMM private void saturateTreeWitnesses(CQIE cqie, Set<TreeWitness>
	 * treewitnesses, Set<TreeWitness> delta, Set<Term> roots,
	 * List<OWLObjectPropertyExpression> edges, Set<TreeWitness> tws) { boolean
	 * saturated = true; for (Atom a: cqie.getBody()) { if (a.getArity() == 2) {
	 * for (TreeWitness tw: tws) { Term r = null; Term nonr = null;
	 * OWLObjectPropertyExpression edge = null; if
	 * (tw.getRoots().contains(a.getTerm(0)) &&
	 * !tw.getDomain().contains(a.getTerm(1)) && !roots.contains(a.getTerm(1)))
	 * { r = a.getTerm(0); nonr = a.getTerm(1); edge =
	 * f.getOWLObjectProperty(IRI.create(a.getPredicate().getName().toString()))
	 * .getInverseProperty(); } else if (tw.getRoots().contains(a.getTerm(1)) &&
	 * !tw.getDomain().contains(a.getTerm(0)) && !roots.contains(a.getTerm(0)))
	 * { r = a.getTerm(1); nonr = a.getTerm(0); edge =
	 * f.getOWLObjectProperty(IRI
	 * .create(a.getPredicate().getName().toString())); } else continue;
	 * 
	 * System.out.println("ATOM " + a + " IS ADJACENT TO THE TREE WITNESS " +
	 * tw); saturated = false; for (TreeWitness twa: treewitnesses) { if
	 * (twa.getRoots().contains(r) && tw.getDomain().contains(nonr)) {
	 * Set<TreeWitness> tws2 = new HashSet<TreeWitness>(tws); tws2.add(twa);
	 * System.out.println("    ATTACHING THE TREE WITNESS " + twa);
	 * saturateTreeWitnesses(treewitnesses, delta, roots, edges, tws2); } }
	 * Set<Term> roots2 = new HashSet<Term>(roots); roots2.add(nonr);
	 * List<OWLObjectPropertyExpression> edges2 = new
	 * LinkedList<OWLObjectPropertyExpression>(edges); edges2.add(edge);
	 * System.out.println("    ATTACHING THE HANDLE " + edge);
	 * saturateTreeWitnesses(treewitnesses, delta, roots2, edges2, tws); } } }
	 * if (saturated) { System.out.println("CHEKCING WHETHER THE ROOTS " + roots
	 * + " WITH EDGES " + edges + " CAN BE ATTACHED TO THE FOLLOWING: "); for
	 * (TreeWitness tw: tws) System.out.println(tw);
	 * 
	 * // collect the type of the root Set<ClassDescription> endtype = new
	 * HashSet<ClassDescription>(); Set<Term> nonroots = new HashSet<Term>();
	 * boolean nonrootsbound = true; for (TreeWitness tw: tws) {
	 * endtype.add(tw.getGenerator()); nonroots.addAll(tw.getDomain()); // check
	 * whether the variables are bound for (Term t: tw.getRoots()) if
	 * (cqie.getHead().getTerms().contains(t)) nonrootsbound = false; }
	 * 
	 * System.out.println("      NON-ROOTS ARE " + (nonrootsbound ? "" : "NOT")
	 * + " BOUND"); if (nonrootsbound) for (SubClassAxiomImpl a:
	 * generatingAxioms) { PropertySomeClassRestriction some =
	 * (PropertySomeClassRestriction)a.getSuper(); if (isTreeWitness(some,
	 * roots, edges, endtype)) { TreeWitness tw = new TreeWitness(some, roots,
	 * getRootType(cqie, roots), nonroots); System.out.println(tw);
	 * delta.add(tw); } } }
	 * 
	 * }
	 */
	
	 private boolean isTreeWitness(TreeWitnessGenerator g, Set<Term> roots, List<PredicatePosition> edges, Set<Atom> endtype) { 
		 log.debug("      CHECKING " + g);
		 boolean match = false; 
		 if (isSubsumed(g.getFiller(), endtype)) {
			 log.debug("         ENDTYPE MATCH " + g.getFiller() + " <= " + endtype);
			 match = true; 
		} 
		if (isSubsumed(g.getPredicatePosition().getInverse(), endtype)) {
			log.debug("         ENDARROW MATCH " + g.getPredicatePosition().getInverse() + " <= " + endtype); 
			match = true; 
		} 
		if (!match)
			return false;
	  
		for (PredicatePosition p : edges) { 
			if(isSubsumed(g.getPredicatePosition(), p))
				log.debug("         ROLE MATCH " + g.getPredicatePosition() + " <= " + p); 
			else { 
				log.debug("         ROLE NOT MATCHED " + g.getPredicatePosition() + " !<= " + p); 
				return false; 
			} 
		}
		log.debug("         ALL MATCHED"); 
		return true; 
	}

	 private boolean isSubsumed(Predicate p, TreeWitnessGenerator gen) {
		 return true;
	 }
	 
	 private boolean isSubsumed(OClass c, Set<Atom> list) {
		 return (c == null) && (list.size() == 0);
	 }
	 
	 private boolean isSubsumed(PredicatePosition p, Set<Atom> list) {
		 return (list.size() == 0);
	 }
	 
	 private boolean isSubsumed(PredicatePosition p1, PredicatePosition p2) {
		 return p1.equals(p2);
	 }
	 
	 private Set<Atom> getRootType(CQIE cqie, Set<Term> roots) {
		Set<Atom> roottype = new HashSet<Atom>();
		// System.out.println("         ROOTS " + roots);
		for (Atom a : cqie.getBody())
			if ((a.getArity() == 1) && roots.contains(a.getTerm(0)))
				roottype.add(a);

		// TODO: reflexivity stuff

		// System.out.println("         ROOT TYPE " + roottype);
		return roottype;
	}

}
