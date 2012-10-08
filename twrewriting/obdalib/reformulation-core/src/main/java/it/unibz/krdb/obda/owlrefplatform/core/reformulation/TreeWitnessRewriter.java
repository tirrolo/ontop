package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.BooleanOperationPredicateImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

public class TreeWitnessRewriter implements QueryRewriter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private TreeWitnessReasonerLite reasoner = new TreeWitnessReasonerLite();
	
	private Map<Predicate, Predicate> extPredicateMap = new HashMap<Predicate, Predicate>();
	private Map<Predicate, List<CQIE>> extPredicateDP = new HashMap<Predicate, List<CQIE>>();
	
	private Map<TreeWitnessGenerator, List<Atom>> genconPredicateDP = new HashMap<TreeWitnessGenerator, List<Atom>>();
	
	private Ontology sigma = null;
	
	@Override
	public void setTBox(Ontology ontology) {
		double startime = System.currentTimeMillis();

		reasoner.setTBox(ontology);
		
		extPredicateMap.clear();
		extPredicateDP.clear();
		genconPredicateDP.clear();
		
		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("setTBox time: %.3f s (total %.3f s)", tm, time));
	}
	
	@Override
	public void setCBox(Ontology sigma) {
		log.debug("SET SIGMA");
		this.sigma = sigma;
		for (Axiom ax : sigma.getAssertions()) {
			log.debug("SIGMA: " + ax);
		}
		extPredicateMap.clear();
		extPredicateDP.clear();
		genconPredicateDP.clear();
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}
	
	/*
	 *  EXT atoms cache
	 */

	private  Atom getExtAtom(Atom a, Set<Predicate> usedExts) throws URISyntaxException {
		if (a.getArity() == 1)
			return getExtAtom(a.getPredicate(), a.getTerm(0), usedExts);
		else
			return getExtAtom(a.getPredicate(), a.getTerm(0), a.getTerm(1), usedExts);
	}

	private  Atom getExtAtom(Atom a, Term r0, Set<Predicate> usedExts) throws URISyntaxException {
		if (a.getArity() == 1)
			return getExtAtom(a.getPredicate(), r0, usedExts);
		else {
			// assert(a.getArity() == 2);
			Term t0 = ((a.getTerm(0) instanceof AnonymousVariable) ? fac.getNondistinguishedVariable() : r0);
			Term t1 = ((a.getTerm(1) instanceof AnonymousVariable) ? fac.getNondistinguishedVariable() : r0);
			return getExtAtom(a.getPredicate(), t0, t1, usedExts);
		}
	}

	private Atom getExtAtom(Predicate p, Term t, Set<Predicate> usedExts) throws URISyntaxException {
		Predicate ext = extPredicateMap.get(p);
		if (ext == null) {
			 List<CQIE> dp = new ArrayList<CQIE>(10);
			 ext = fac.getClassPredicate(getEXTname(p.getName()));
			 Term x = fac.getVariable("x");
			 Term y = fac.getNondistinguishedVariable(); // fac.getVariable("y");
			 Atom extAtom = fac.getAtom(ext, x); 		  					 
			 for (ClassDescription subc : reasoner.getSubConcepts(p)) 
				 if (subc instanceof OClass) 
					 dp.add(fac.getCQIE(extAtom, fac.getAtom(((OClass)subc).getPredicate(), x)));
				 else if (subc instanceof PropertySomeRestriction) {
					 PropertySomeRestriction twg = (PropertySomeRestriction)subc;
					 dp.add(fac.getCQIE(extAtom, (!twg.isInverse()) ? 
								fac.getAtom(twg.getPredicate(), x, y) : fac.getAtom(twg.getPredicate(), y, x))); 
				 }					 
			 log.debug("DP FOR " + p + " IS " + dp);
			 if (dp.size() > 1) {			 
				 dp = CQCUtilities.removeContainedQueries(dp, true, sigma, false);
				 log.debug("SIMPLIFIED DP FOR " + p + " IS " + dp);
			 }
			 if (dp.size() == 1) 
				 ext = p;
			 else
				 extPredicateDP.put(p, dp);
			 extPredicateMap.put(p, ext);
		}
		usedExts.add(p);
		return fac.getAtom(ext, t);
	}

	private  Atom getExtAtom(Predicate p, Term t1, Term t2, Set<Predicate> usedExts) throws URISyntaxException {
		if (p instanceof BooleanOperationPredicateImpl) {
			return fac.getAtom(p, t1, t2);			
		}
			
		Predicate ext = extPredicateMap.get(p);
		if (ext == null) {
			 List<CQIE> dp = new ArrayList<CQIE>(10);
			 ext = fac.getObjectPropertyPredicate(getEXTname(p.getName()));
			 Term x = fac.getVariable("x");
			 Term y = fac.getVariable("y");
			 Atom extAtom = fac.getAtom(ext, x, y);					 
			 for (Property sub : reasoner.getSubProperties(p))
				dp.add(fac.getCQIE(extAtom, (!sub.isInverse()) ? 
							fac.getAtom(sub.getPredicate(), x, y) : fac.getAtom(sub.getPredicate(), y, x))); 
			 log.debug("DP FOR " + p + " IS " + dp);
			 if (dp.size() > 1) {
				 dp = CQCUtilities.removeContainedQueries(dp, true, sigma, false);
				 log.debug("SIMPLIFIED DP FOR " + p + " IS " + dp);
			 }
			 if (dp.size() == 1) 
				 ext = p;
			 else
				 extPredicateDP.put(p, dp);
			 extPredicateMap.put(p, ext);
		}
		usedExts.add(p);
		return fac.getAtom(ext, t1, t2);
	}

	private static URI getEXTname(URI name) throws URISyntaxException {
		return new URI(name.getScheme(), name.getSchemeSpecificPart(), "EXT_" + name.getFragment());
	}

	
	/*
	 * Generating Concept atoms cache
	 */
			
	private List<Atom> getGenConAtoms(TreeWitnessGenerator some) throws URISyntaxException  {
		List<Atom> atoms = genconPredicateDP.get(some);
		if (atoms == null) {
			 List<CQIE> dp = new LinkedList<CQIE>();
			 
			 URI propURI = some.getProperty().getPredicate().getName();
			 URI genconURI = new URI(propURI.getScheme(), propURI.getSchemeSpecificPart(), "GEN"); 
			 			//+ propURI.getFragment()
						//+ (some.getProperty().isInverse() ? "_I_" : "_") 
						//+ ((some.getFiller() != null) ?  some.getFiller().getPredicate().getName().getFragment() : "T"));
					 
			 Term x = fac.getVariable("x");
			 Atom genAtom = fac.getAtom(fac.getClassPredicate(genconURI), x);
			 log.debug("RULES FOR GENERATING CONCEPT " + some);
			 for (BasicClassDescription subc : some.getConcepts()) {
				log.debug("  SUBCONCEPT: " + subc);
				if (subc instanceof OClass) {
					 dp.add(fac.getCQIE(genAtom, fac.getAtom(((OClass)subc).getPredicate(), x)));
				}
				else if (subc instanceof PropertySomeRestriction) {
					 PropertySomeRestriction twg = (PropertySomeRestriction)subc;
					 dp.add(fac.getCQIE(genAtom, (!twg.isInverse()) ? 
								fac.getAtom(twg.getPredicate(), x, fac.getNondistinguishedVariable()) : 
									fac.getAtom(twg.getPredicate(), fac.getNondistinguishedVariable(), x)));  						 
				}					
			 }
			 log.debug("DP FOR " + some + " IS " + dp);
			 if (dp.size() > 1) {
				 dp = CQCUtilities.removeContainedQueries(dp, true, sigma, false);
				 log.debug("SIMPLIFIED DP FOR " + some + " IS " + dp);
			 }
			 atoms = new ArrayList<Atom>(dp.size());
			 for (CQIE rule : dp) 
				atoms.add(rule.getBody().get(0));
			 genconPredicateDP.put(some, atoms);
		}
		return atoms;		
	}
	

	
	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private void rewriteCC(QueryConnectedComponent cc, Atom headAtom, DatalogProgram output) throws URISyntaxException {
		Set<Predicate> exts = new HashSet<Predicate>();
		
		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner);

		if (cc.hasNoFreeTerms()) {  
			Term x = fac.getNondistinguishedVariable(); 
			for (TreeWitnessGenerator gen : tws.getGeneratorsOfDetachedCC()) {
				log.debug("DETACHED GENERATED BY: " + gen);
				for (Atom a : getGenConAtoms(gen))
					output.appendRule(fac.getCQIE(headAtom, getExtAtom(a, x, exts))); 
			}				
		}

		// CREATE AND STORE TREE WITNESS FORMULAS
		for (TreeWitness tw : tws.getTWs()) {
			List<List<Atom>> twfs = new LinkedList<List<Atom>>();
			
			MinimalCQProducer twf = new MinimalCQProducer(reasoner, cc.getVariables()); 
			Iterator<Term> i = tw.getRoots().iterator();
			Term r0 = i.next();
			while (i.hasNext()) 
				twf.add(fac.getEQAtom(i.next(), r0));
			
			for (Atom c : tw.getRootAtoms()) {
				if (c.getArity() == 1)
					twf.add(getExtAtom(c.getPredicate(), r0, exts));
				else //(c.getArity() == 2)
					twf.add(getExtAtom(c.getPredicate(), r0, r0, exts));
			}
			List<Atom> twfbody = twf.getBody();		
			for (TreeWitnessGenerator gen : tw.getGenerators())
				for (Atom a : getGenConAtoms(gen)) {
					List<Atom> twfa = new ArrayList<Atom>(twfbody.size() + 1); 
					twfa.add(getExtAtom(a, r0, exts));
					twfa.addAll(twfbody);
					twfs.add(twfa);
				}
			tw.setFormula(twfs);
		}
		
		
		MinimalCQProducer mainbody = new MinimalCQProducer(reasoner, cc.getFreeVariables()); 
		int pairIndex = 0;
		
		for (QueryConnectedComponent.Edge edge : cc.getEdges()) {
			log.debug("EDGE " + edge);
			List<Atom> extAtoms = new ArrayList<Atom>(edge.getBAtoms().size() + edge.getL0Atoms().size() + edge.getL1Atoms().size()); 
			for (Atom aa : edge.getBAtoms()) 
				extAtoms.add(getExtAtom(aa, exts));
			for (Atom aa : edge.getL0Atoms()) 
				extAtoms.add(getExtAtom(aa, exts));
			for (Atom aa : edge.getL1Atoms()) 
				extAtoms.add(getExtAtom(aa, exts));
			
			Collection<TreeWitness> edgetws = tws.getTWs(edge);
			if (edgetws.size() == 0) {	
				// no tree witnesses -- direct insertion into the main body
				mainbody.addAll(extAtoms);
			} 
			else {
				URI atomURI = edge.getBAtoms().iterator().next().getPredicate().getName();
				URI edgeURI = new URI(atomURI.getScheme(), atomURI.getSchemeSpecificPart(), 
										"Q_" + (++pairIndex) + "_" + atomURI.getFragment());

				Atom edgeAtom = fac.getAtom(fac.getPredicate(edgeURI, cc.getVariables().size()), cc.getVariables());
				mainbody.add(edgeAtom);				
				output.appendRule(fac.getCQIE(edgeAtom, extAtoms));						

				for (TreeWitness tw : edgetws) 
					for (List<Atom> twfa : tw.getFormula())
						output.appendRule(fac.getCQIE(edgeAtom, twfa));
			}
		}

		output.appendRule(fac.getCQIE(headAtom, mainbody.getBody())); 
				
		// EXTENSIONS		
		for (Predicate pred : exts) { 
			 List<CQIE> dp = extPredicateDP.get(pred);			 
			 if (dp != null) 
				 output.appendRule(dp); // NEED TO CLONE?				 
		 }
	}
	
	private double time = 0;
	
	@Override
	public OBDAQuery rewrite(OBDAQuery input) throws OBDAException {
		
		double startime = System.currentTimeMillis();
		
		DatalogProgram dp = (DatalogProgram) input;
		DatalogProgram output = fac.getDatalogProgram();

		try {
			for (CQIE cqie : dp.getRules()) {
				List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(cqie);	
				Atom cqieAtom = cqie.getHead();
			
				if (ccs.size() == 1) {
					QueryConnectedComponent cc = ccs.get(0);
					log.debug("CONNECTED COMPONENT (" + cc.getFreeVariables() + ")" + " EXISTS " + cc.getQuantifiedVariables() + " WITH EDGES " + cc.getEdges());
					rewriteCC(cc, cqieAtom, output); 				
				}
				else {
					URI cqieURI = cqieAtom.getPredicate().getName();
					List<Atom> ccBody = new ArrayList<Atom>(ccs.size());
					int ccNumber = 0;
					for (QueryConnectedComponent cc : ccs) {
						log.debug("CONNECTED COMPONENT (" + cc.getFreeVariables() + ")" + " EXISTS " + cc.getQuantifiedVariables() + " WITH EDGES " + cc.getEdges());
				
						URI ccURI = new URI(cqieURI.getScheme(), cqieURI.getSchemeSpecificPart(),  
							 "CC_" + cqieURI.getFragment() + "_" + ccNumber++);					
						Atom ccAtom = fac.getAtom(fac.getPredicate(ccURI, cc.getFreeVariables().size()), cc.getFreeVariables());

						rewriteCC(cc, ccAtom, output); 
						ccBody.add(ccAtom);
					}
					output.appendRule(fac.getCQIE(cqieAtom, ccBody));
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		Predicate queryPredicate = dp.getRules().get(0).getHead().getPredicate();
		log.debug("REWRITTEN PROGRAM\n" + output);			
		if (output.getRules().size() > 1) {
			output = DatalogQueryServices.flatten(output, queryPredicate, "Q_");
			log.debug("Q-FLATTENED PROGRAM\n" + output);
			output = DatalogQueryServices.flatten(output, queryPredicate, "CC_");
			log.debug("CC-FLATTENED PROGRAM\n" + output);
			if (output.getRules().size() > 1) {
				output = CQCUtilities.removeContainedQueriesSorted(output, true, sigma);
				log.debug("PROGRAM AFTER CQC CONTAINMENT\n" + output);			
				if (output.getRules().size() != output.getRules(queryPredicate).size()) {
					output = DatalogQueryServices.flatten(output, queryPredicate, "EXT_");
					log.debug("EXT-FLATTENED PROGRAM\n" + output);
				}
			}
		}
		QueryUtils.copyQueryModifiers(input, output);

		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("Rewriting time: %.3f s (total %.3f s)", tm, time));
		
		return output;
	}
	
	 
}
