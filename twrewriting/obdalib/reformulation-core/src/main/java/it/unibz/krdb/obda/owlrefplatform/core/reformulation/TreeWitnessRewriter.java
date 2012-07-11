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
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

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
	private Set<PropertySomeClassRestriction> generators;
	private Map<ClassDescription, HashSet<ClassDescription> > subconcepts; // reflexive closure of the relation!
	private Map<Property, HashSet<Property> > subproperties; // reflexive closure
	
	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");
	
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private static ClassDescription getConceptFromClassDescription(ClassDescription c) {
		//log.debug("CONCEPT TYPE: " + c.getClass());
		if (c instanceof PropertySomeClassRestriction) {
			PropertySomeClassRestriction some = (PropertySomeClassRestriction) c;
			log.debug("  SOME CONCEPT " + some.getPredicate() + (some.isInverse() ? "^-" : "") + ", FILLER " + some.getFiller());
			//return new TreeWitnessGenerator
			//		(ontFactory.createProperty(some.getPredicate(), some.isInverse()), some.getFiller());
			return some;
		} 
		else if (c instanceof PropertySomeRestriction) {
			PropertySomeRestriction some = (PropertySomeRestriction) c;
			log.debug("  SOME " + some.getPredicate() + (some.isInverse() ? "^-" : "") + ", FILLER TOP");
			//return new TreeWitnessGenerator
			//		(ontFactory.createProperty(some.getPredicate(), some.isInverse()));
			return ontFactory.createPropertySomeClassRestriction(some.getPredicate(), some.isInverse(), owlThing);
		} 
		else if (c instanceof OClass) {
			OClass oc = (OClass)c;
			log.debug("  CONCEPT " + oc);
			return oc;
		}
		log.debug("UNKNONW TYPE: " + c);
		return null; 
	}
	
	private void addSubConcept(ClassDescription subConcept, ClassDescription superConcept) {
		if (!subconcepts.containsKey(superConcept)) {
			HashSet<ClassDescription> set = new HashSet<ClassDescription>();
			set.add(subConcept);
			set.add(superConcept);
			subconcepts.put(superConcept, set);
		}
		else
			subconcepts.get(superConcept).add(subConcept);		
	}
	
	@Override
	public void setTBox(Ontology ontology) {
		this.tbox = ontology;
		log.debug("SET ONTOLOGY " + ontology);
		// collect generating axioms
		generators = new HashSet<PropertySomeClassRestriction>();
		subconcepts = new HashMap<ClassDescription, HashSet<ClassDescription> >();
		subproperties = new HashMap<Property, HashSet<Property> >();
		
		log.debug("AXIOMS");
		for (Axiom ax : tbox.getAssertions()) {
			if (ax instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl sax = (SubClassAxiomImpl) ax;
				log.debug("CI AXIOM: " + sax);
				ClassDescription superConcept = getConceptFromClassDescription(sax.getSuper());
				if (superConcept instanceof PropertySomeClassRestriction)
					generators.add((PropertySomeClassRestriction)superConcept);
				
				addSubConcept(getConceptFromClassDescription(sax.getSub()), superConcept);
			} 
			else if (ax instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl sax = (SubPropertyAxiomImpl) ax;
				log.debug("RI AXIOM: " + sax);
				Property superProperty = sax.getSuper();
				Property subProperty = sax.getSub();
				Property superInverseProperty = ontFactory.createProperty(superProperty.getPredicate(), !superProperty.isInverse());
				Property subInverseProperty = ontFactory.createProperty(subProperty.getPredicate(), !subProperty.isInverse());
				if (!subproperties.containsKey(superProperty)) {
					HashSet<Property> set = new HashSet<Property>();
					set.add(superProperty);
					set.add(subProperty);
					subproperties.put(superProperty, set);
					HashSet<Property> setInverse = new HashSet<Property>();
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
		
		for (Object k : subconcepts.keySet())
			log.debug("SUBCONCEPTS OF " + k + " are " + subconcepts.get(k));
		
		for (Property prop : subproperties.keySet()) 
			for (Property subproperty : subproperties.get(prop)) {
				addSubConcept(ontFactory.createPropertySomeClassRestriction(subproperty.getPredicate(), subproperty.isInverse(), owlThing), 
						ontFactory.createPropertySomeClassRestriction(prop.getPredicate(), prop.isInverse(), owlThing));
			}
		
		boolean changed = false;
		do {
			changed = false;
			for (ClassDescription o1 : subconcepts.keySet())
				for (ClassDescription o2 : subconcepts.keySet())
					if (subconcepts.get(o2).contains(o1)) {
						if (subconcepts.get(o2).addAll(subconcepts.get(o1))) {
							log.debug("ALL " + o2 + " ARE EXTENDED WITH ALL " + o1);
							changed = true;
						}
					}
		} while (changed);
		
		for (ClassDescription k : subconcepts.keySet())
			log.debug("SATURATED SUBCONCEPTS OF " + k + " are " + subconcepts.get(k));
	}

	@Override
	public void setCBox(Ontology sigma) {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	private static Atom getAtom(URI name, List<Term> terms) {
		return fac.getAtom(fac.getPredicate(name, terms.size()), terms);
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
		Set<Term> variablesSet = new HashSet<Term>();
		
		for (Atom a: cqie.getBody()) {
			if (a.getArity() == 2 && !a.getTerm(0).equals(a.getTerm(1))) {
				if (a.getTerm(0) instanceof Variable)
					variablesSet.add(a.getTerm(0));
				if (a.getTerm(1) instanceof Variable)
					variablesSet.add(a.getTerm(1));
				
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
				if (key instanceof Variable)
					variablesSet.add(key);
				
				if (!loops.containsKey(key)) {
					Set<Atom> atoms = new HashSet<Atom>();
					atoms.add(a);
					loops.put(key, atoms);
				}
				else
					loops.get(key).add(a);			
					
			}
		}
		
		List<Term> variables = new ArrayList<Term>(variablesSet);
		
		Set<TreeWitness> tws = getReducedSetOfTreeWitnesses(cqie);	
		
		{
			List<Atom> mainbody = new ArrayList<Atom>(edges.size());
			for (AdjacentTermsPair pair : edges.keySet())
				mainbody.add(getAtom(pair.getName(), variables /*pair.getTerms()*/));
		
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
			for (Atom aa : atoms) {
				extAtoms.add(getAtom(getExtName(aa.getPredicate().getName()), aa.getTerms()));
				//log.debug("PREDICATE: " + aa.getPredicate());
				//for (Term t : aa.getTerms())
				//	log.debug("   TERM " +  t + " OF TYPE " + t.getClass());
			}

			output.appendRule(fac.getCQIE(getAtom(edge.getName(),variables/*edge.getTerms()*/), extAtoms));
					
			for (TreeWitness tw : tws)
				if (tw.getDomain().containsAll(edge.getTerms())) {
					// TREE WITNESS FORMULAS
					List<Atom> twf = new LinkedList<Atom>();
					List<Term> roots = new LinkedList<Term>(tw.getRoots());
					Term r0 = roots.get(0);
					twf.add(fac.getAtom(fac.getClassPredicate(getExtName(tw.getGenerator())), r0));
					for (Term rt : roots)
						if (!rt.equals(roots.get(0)))
							twf.add(fac.getEQAtom(rt, r0));
					for (Atom c : tw.getRootType()) {
						// TODO: REWRITE TO TAKE REFLEXIVITY INTO ACCOUNT
						if (c.getArity() == 1)
							twf.add(fac.getAtom(fac.getClassPredicate(getExtName(c.getPredicate().getName())), r0));
					}
					output.appendRule(fac.getCQIE(getAtom(edge.getName(), variables /*edge.getTerms()*/ ), twf));
				}
				
		}

		Set<PropertySomeClassRestriction> gen = new HashSet<PropertySomeClassRestriction>();
		for (TreeWitness tw : tws)
			gen.add(tw.getGenerator());
		
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
//		return simplified;
	}

	private List<CQIE> getExtPredicates(CQIE cqie, Set<PropertySomeClassRestriction> gencon) throws URISyntaxException {																															

		List<CQIE> list = new LinkedList<CQIE>();
		
		Set<Predicate> exts = new HashSet<Predicate>();
		for (Atom a : cqie.getBody())
			exts.add(a.getPredicate());

		// GENERATING CONCEPTS

		for (PropertySomeClassRestriction some : gencon) {
			log.debug("GEN CON EXT: " + gencon);
			
			//exts.add(some.getPredicate());
			
			Term x = fac.getVariable("x");
			Atom genAtom = fac.getAtom(fac.getClassPredicate(getExtName(some)), x);

			//for (Predicate c : tbox.getConcepts())
			//	if (isSubsumed(c, some))
			//	{
			//		log.debug("EXT COMPUTE SUBCLASSES: " + c);
			//		list.add(fac.getCQIE(extAtom, fac.getAtom(c, x)));
			//	}

			//{
			//	Term w = fac.getVariable("w");
			//	Predicate someExt = fac.getObjectPropertyPredicate(getExtName(some.getPredicate().getName()));
			//	Atom ra = //getRoleAtom(some, x, w);
			//			(!some.isInverse()) ? fac.getAtom(someExt, x, w) : fac.getAtom(someExt, w, x);
			//	log.debug("ROLE ATOM: " + ra);
			//	// COMM
			//	// if(r.isSubsumed(f.getOWLObjectSomeValuesFrom(some.getProperty(),
			//	// f.getOWLThing()), some))
			//	list.add(fac.getCQIE(extAtom, ra));
			//}

			for (Property subprop : getSubProperties(ontFactory.createObjectProperty(some.getPredicate().getName(), some.isInverse()))) {
				log.debug("PROPERTY: " + subprop);
				PropertySomeClassRestriction subgenconcept = ontFactory.createPropertySomeClassRestriction(subprop.getPredicate(), subprop.isInverse(), some.getFiller());
				for (ClassDescription subc : getSubConcepts(subgenconcept)) {
					log.debug("  SUBCONCEPT: " + subc);
					 if (subc instanceof OClass) {
						log.debug("RULE FOR " + some + " DEFINED BY " + subc);
						list.add(fac.getCQIE(genAtom, fac.getAtom(((OClass)subc).getPredicate(), x)));
					 }
					 else if (subc instanceof PropertySomeClassRestriction) {
						 PropertySomeClassRestriction twg = (PropertySomeClassRestriction)subc;
						 Term w = fac.getVariable("w");
						 Atom a = (!twg.isInverse()) ? 
									fac.getAtom(twg.getPredicate(), x, w) : fac.getAtom(twg.getPredicate(), w, x);
						 list.add(fac.getCQIE(genAtom, a)); // TODO: filler 						 
					 }				
				}
			}
			
			for (Predicate role : tbox.getRoles()) { 
			//	if (!p.isOWLBottomObjectProperty()) {
			//		if(isSubsumed(f.getOWLObjectSomeValuesFrom(p, f.getOWLThing()), some)) {
				{
					PropertySomeClassRestriction twg = ontFactory.createPropertySomeClassRestriction(role, false, owlThing);
					if (subconcepts.get(some).contains(twg)) {
						log.debug("RULE FOR " + some + " DEFINED BY " + twg);
						exts.add(role);
						Predicate someExt = fac.getObjectPropertyPredicate(getExtName(role.getName()));
						list.add(fac.getCQIE(genAtom, fac.getAtom(someExt, x, fac.getVariable("w"))));
					}
				}
				{
					PropertySomeClassRestriction twg = ontFactory.createPropertySomeClassRestriction(role, true, owlThing);
					if (subconcepts.get(some).contains(twg)) {
						log.debug("RULE FOR " + some + " DEFINED BY " + twg);
						exts.add(role);
						Predicate someExt = fac.getObjectPropertyPredicate(getExtName(role.getName()));
						list.add(fac.getCQIE(genAtom, fac.getAtom(someExt, fac.getVariable("w"), x)));
					}
				}
			//	list.add(fac.getCQIE(extAtom, getRoleAtom(p, x,
			// * w))); continue; }
			// * 
			// * for (OWLClassExpression c: getClasses()) if (!c.isOWLNothing() &&
			// * isSubsumed(f.getOWLObjectSomeValuesFrom(p, c), some))
			// * list.add(getCQIE(new Atom(uri, x), getRoleAtom(p, x, w), new
			// * Atom(new URI(c.asOWLClass().getIRI().toString()), w))); }
			}
		}

		
		 for (Predicate pred : exts) { 
			 log.debug("EXT PREDICATE: " + pred);
			 if (pred.getArity() == 1) { 
				 Term x = fac.getVariable("x");
				 Atom extAtom = fac.getAtom(fac.getClassPredicate(getExtName(pred.getName())), x); 		  
				 list.add(fac.getCQIE(extAtom, fac.getAtom(pred, x)));
				 
				 for (ClassDescription subc : getSubConcepts(ontFactory.createClass(pred))) 
					 if (subc instanceof OClass) 
						 list.add(fac.getCQIE(extAtom, fac.getAtom(((OClass)subc).getPredicate(), x)));
					 else if (subc instanceof PropertySomeClassRestriction) {
						 PropertySomeClassRestriction twg = (PropertySomeClassRestriction)subc;
						 Term w = fac.getVariable("w");
						 Atom a = (!twg.isInverse()) ? 
									fac.getAtom(twg.getPredicate(), x, w) : fac.getAtom(twg.getPredicate(), w, x);
						 list.add(fac.getCQIE(extAtom, a)); // TODO: filler 						 
					 }
		  
				 //for (OWLObjectPropertyExpression p: r.getProperties()) 
				//	 if (!p.isOWLBottomObjectProperty() && r.isSubsumed(f.getOWLObjectSomeValuesFrom(p, f.getOWLThing()), ac))
				//		 list.add(getCQIE(getAtom(ext, x), getRoleAtom(p, x, fac.getVariable("w")))); 
			 } 
			 else if (pred.getArity() == 2) { 
				Term x = fac.getVariable("x");
				Term y = fac.getVariable("y"); 
				Atom extAtom = fac.getAtom(fac.getObjectPropertyPredicate(getExtName(pred.getName())), x, y);
				 
				for (Property sub : getSubProperties(ontFactory.createProperty(pred)))
					list.add(fac.getCQIE(extAtom, (!sub.isInverse()) ? 
								fac.getAtom(sub.getPredicate(), x, y) : fac.getAtom(sub.getPredicate(), y, x))); 
			 } 
		 }
		 return list;
	}

	private Set<Property> getSubProperties(Property prop) {
		return (!subproperties.containsKey(prop)) ? Collections.singleton(prop) : subproperties.get(prop);
	}
	
	private Set<ClassDescription> getSubConcepts(ClassDescription con) {
		return (!subconcepts.containsKey(con)) ? Collections.singleton(con) : subconcepts.get(con);
	}
	
	
	private static URI getExtName(URI name) throws URISyntaxException {
		return new URI(name.getScheme(), name.getSchemeSpecificPart(), "EXT_" + name.getFragment());
	}

	private static URI getExtName(PropertySomeClassRestriction some) throws URISyntaxException {
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

		Set<TreeWitness> subtws = new HashSet<TreeWitness>(treewitnesses.size());
		for (TreeWitness tw : treewitnesses) {
			boolean subsumed = false;
			for (TreeWitness tw1 : treewitnesses)
				if (!tw.equals(tw1) && tw.getDomain().equals(tw1.getDomain()) && tw.getRoots().equals(tw1.getRoots()))
					if(getSubConcepts(tw.getGenerator()).contains(tw1.getGenerator())) {
						log.debug("SUBSUMED: " + tw + " BY " + tw1);
						subsumed = true;
						break;
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
			List<Property> edges = new LinkedList<Property>(); 
			Set<ClassDescription> endtype = new HashSet<ClassDescription>(); 
			Set<Term> roots = new HashSet<Term>(); 
			
			for (Atom a : cqie.getBody()) { 
				if ((a.getArity() == 1) && a.getTerm(0).equals(v))  {
					endtype.add(ontFactory.createClass(a.getPredicate())); 
				}						
				else if	((a.getArity() == 2) && a.getTerm(0).equals(v) && a.getTerm(1).equals(v)) {
					// TODO: binary predicates!
					log.debug("LOOP: " + a);
				}
				else if ((a.getArity() == 2) && a.getTerm(1).equals(v)) {
					edges.add(ontFactory.createProperty(a.getPredicate(), false));
					roots.add(a.getTerm(0)); 
				} 
				else if ((a.getArity() == 2) && a.getTerm(0).equals(v)) {
					edges.add(ontFactory.createProperty(a.getPredicate(), true)); 
					roots.add(a.getTerm(1)); 
				} 
			} 
			log.debug("  EDGES " + edges);
			log.debug("  ENDTYPE " + endtype);
		 
			for (PropertySomeClassRestriction g: generators) 
				if (isTreeWitness(g, roots, edges, endtype)) { 
					TreeWitness tw = new TreeWitness(g, roots, getRootType(cqie, roots), Collections.singleton(v)); 
					log.debug("TREE WITNESS: " + tw);
					treewitnesses.add(tw); 
				} 
		}
		 
		 Set<TreeWitness> delta = new HashSet<TreeWitness>(); 
		 do 
			 for (TreeWitness tw : treewitnesses) { 
				 Set<TreeWitness> twa = new HashSet<TreeWitness>(); 
				 twa.add(tw);
				 saturateTreeWitnesses(cqie, treewitnesses, delta, new HashSet<Term>(), new ArrayList<Property>(), twa); 
			 } 
		 while (treewitnesses.addAll(delta));
	
		return treewitnesses;
	}

	private void saturateTreeWitnesses(CQIE cqie, Set<TreeWitness> treewitnesses, Set<TreeWitness> delta, Set<Term> roots, List<Property> edges, Set<TreeWitness> tws) { 
		boolean saturated = true; 
		
		for (Atom a: cqie.getBody()) { 
			if (a.getArity() == 2) {
				Term t0 = a.getTerm(0);
				Term t1 = a.getTerm(1);
				for (TreeWitness tw : tws) { 
					Term r = null; 
					Term nonr = null;
					Property edge = null; 
					if (tw.getRoots().contains(t0) && 
							!tw.getDomain().contains(t1) && !roots.contains(t1)) { 
						r = a.getTerm(0); 
						nonr = a.getTerm(1); 
						edge = ontFactory.createProperty(a.getPredicate(), true); 
					} 
					else if (tw.getRoots().contains(t1) &&
							!tw.getDomain().contains(t0) && !roots.contains(t0)) { 
						r = a.getTerm(1); 
						nonr = a.getTerm(0); 
						edge = ontFactory.createProperty(a.getPredicate(), false); 
					} else 
						continue;
				
					log.debug("ATOM " + a + " IS ADJACENT TO THE TREE WITNESS " + tw); 
					saturated = false; 
					for (TreeWitness twa : treewitnesses) { 
						if (twa.getRoots().contains(r) && tw.getDomain().contains(nonr)) {
							Set<TreeWitness> tws2 = new HashSet<TreeWitness>(tws); 
							tws2.add(twa);
							log.debug("    ATTACHING THE TREE WITNESS " + twa);
							saturateTreeWitnesses(cqie, treewitnesses, delta, roots, edges, tws2); 
						} 
					}
					Set<Term> roots2 = new HashSet<Term>(roots); 
					roots2.add(nonr);
					List<Property> edges2 = new ArrayList<Property>(edges); 
					edges2.add(edge);
					log.debug("    ATTACHING THE HANDLE " + edge);
					saturateTreeWitnesses(cqie, treewitnesses, delta, roots2, edges2, tws); 
				} 
			} 
		}
		
		if (saturated && (roots.size() != 0)) { 
			log.debug("CHEKCING WHETHER THE ROOTS " + roots + " WITH EDGES " + edges + " CAN BE ATTACHED TO THE FOLLOWING: "); 
			for (TreeWitness tw : tws) 
				log.debug("  " + tw);
			
			// collect the type of the root 
			Set<ClassDescription> endtype = new HashSet<ClassDescription>(); 
			Set<Term> nonroots = new HashSet<Term>();
			boolean nonrootsbound = true; 
			for (TreeWitness tw : tws) {
				endtype.add(tw.getGenerator()); 
				nonroots.addAll(tw.getDomain()); 
				// check whether the variables are bound 
				for (Term  t : tw.getRoots()) 
					if (cqie.getHead().getTerms().contains(t)) 
						nonrootsbound = false; 
			}
	  
			log.debug("      NON-ROOTS ARE " + (nonrootsbound ? "" : "NOT ") + "BOUND"); 
			if (nonrootsbound) 
				for (PropertySomeClassRestriction gen : generators) { 
					if (isTreeWitness(gen, roots, edges, endtype)) { 
						TreeWitness tw = new TreeWitness(gen, roots, getRootType(cqie, roots), nonroots); 
						log.debug(" " + tw);
						delta.add(tw); 
					} 
				} 
		}
	 
	  }
	 
	
	 private boolean isTreeWitness(PropertySomeClassRestriction g, Set<Term> roots, List<Property> edges, Set<ClassDescription> endtype) { 
		 log.debug("      CHECKING " + g);
		 boolean match = false; 
		 if (g.getFiller().equals(owlThing)) {
			 log.debug("         ENDTYPE TRIVIAL MATCH " + g.getFiller() + " <= " + endtype);
			 match = true; 
		} 
		 if (getSubConcepts(g.getFiller()).containsAll(endtype)) {
			 log.debug("         ENDTYPE MATCH " + g.getFiller() + " <= " + endtype);
			 match = true; 
		} 
		if (getSubConcepts(ontFactory.createPropertySomeClassRestriction(g.getPredicate(), 
									!g.isInverse(), owlThing)).containsAll(endtype)) {
			log.debug("         ENDARROW MATCH inv(" + g + ") <= " + endtype); 
			match = true; 
		} 
		if (!match)
			return false;
	  
		for (Property p : edges) { 
			Property pp = ontFactory.createProperty(g.getPredicate(), g.isInverse());
			if(getSubProperties(p).contains(pp))
				log.debug("         ROLE MATCH " + pp + " <= " + p); 
			else { 
				log.debug("         ROLE NOT MATCHED " + pp + " !<= " + p); 
				return false; 
			} 
		}
		log.debug("         ALL MATCHED"); 
		return true; 
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
