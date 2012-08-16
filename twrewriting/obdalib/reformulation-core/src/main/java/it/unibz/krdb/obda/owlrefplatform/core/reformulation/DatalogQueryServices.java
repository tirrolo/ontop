package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.PredicateAtomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.ResolutionEngine;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatalogQueryServices {
	
	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private static final Logger log = LoggerFactory.getLogger(DatalogQueryServices.class);
	
	private static ResolutionEngine resolutionEngine = new ResolutionEngine();
	
	public static DatalogProgram flatten(DatalogProgram dp, Predicate head, String fragment) {
		// contains all definitions of the main predicate
		PriorityQueue<CQIE> queue = new PriorityQueue<CQIE>(10, new Comparator<CQIE> () {
			@Override
			public int compare(CQIE arg0, CQIE arg1) {
				return arg0.getBody().size() - arg1.getBody().size();
			} 
			});

		for (CQIE cqie : dp.getRules()) {
			// main predicate is not replaced
			Predicate predicate = cqie.getHead().getPredicate();
			if (predicate.equals(head) || 
					((fragment != null) && !predicate.getName().getFragment().contains(fragment))) {
				queue.add(cqie);
				log.debug("MAIN PREDICATE RULE " + cqie);
			}
		}
		
		List<CQIE> output = new LinkedList<CQIE>();
		
		while (!queue.isEmpty()) {
			CQIE query = queue.poll();
				
			boolean found = false;
			for (CQIE r2 : output) 
				if (CQCUtilities.isContainedInSyntactic(query,r2)) {
					found = true;
					//log.debug("SUBSUMED " + r + " BY " + r2);
					break;
				}
			if (found)
				continue;
						
			List<Atom> body = query.getBody();
			boolean replaced = false;
			for (int i = 0; i < body.size(); i++) {
				Atom toBeReplaced = body.get(i);				
				List<CQIE> definitions = dp.getRules(toBeReplaced.getPredicate());
				if ((definitions != null) && (definitions.size() != 0)) {
					for (CQIE rule : definitions) {
						CQIE newquery = resolutionEngine.resolve(rule, query, i);
						if (newquery == null)
							continue;
						queue.add(reduce(newquery));
						replaced = true;
					}
					if (replaced)
						break;
				}
			}
			if (!replaced) {
				//log.debug("ADDING TO THE RESULT " + r);
				
				// prune the list				
				ListIterator<CQIE> i = output.listIterator();
				while (i.hasNext()) {
					CQIE q2 = i.next();
					if (CQCUtilities.isContainedInSyntactic(q2, query)) {
						i.remove();				
						//log.debug("   PRUNED " + q2 + " BY " + r);
					}
				}
				
				output.add(query.clone());			
				Collections.sort(output, new Comparator<CQIE> () {
					@Override
					public int compare(CQIE arg0, CQIE arg1) {
						return arg0.getBody().size() - arg1.getBody().size();
					} 
					});				
			}
		}
		
		return fac.getDatalogProgram(output);
	}
	
	private static CQIE removeEQ(CQIE q) {
		boolean replacedEQ = false;
		
		do {
			replacedEQ = false;
		
			Iterator<Atom> i = q.getBody().iterator(); 
			while (i.hasNext()) { 
				Atom a = i.next();
				if (a.getPredicate().equals(OBDAVocabulary.EQ)) {
					Map<Variable, Term> substituition = new HashMap<Variable, Term>(1);
					Term t0 = a.getTerm(0); 
					Term t1 = a.getTerm(1); 					
					if (t0 instanceof Variable)
						substituition.put((Variable)t0, t1);
					else if (t1 instanceof Variable)
						substituition.put((Variable)t1, t0);
					else
						substituition = null;
					if (substituition != null) {
						//log.debug("REMOVE " + a + " IN " + q);
						i.remove();
						q = Unifier.applyUnifier(q, substituition);
						//log.debug(" RESULT: " + q);
						replacedEQ = true;
						break;
					}
				}
			}
		} while (replacedEQ);
		
		return q;
	}
	
	private static CQIE reduce(CQIE q) {
		q = removeEQ(q);	
		makeSingleOccurrencesAnonymous(q.getBody(), q.getHead().getTerms());
		return CQCUtilities.removeRundantAtoms(q);
	}

	//
	// OPTIMISATION
	// replace all existentially quantified variables that occur once with _
	//
	
	private static void makeSingleOccurrencesAnonymous(List<Atom> body, List<Term> freeVariables) {
		Map<Term, Atom> occurrences = new HashMap<Term, Atom>();
		for (Atom a : body)
			for (Term t : a.getTerms())
				if ((t instanceof Variable) && !freeVariables.contains(t))
					if (occurrences.containsKey(t))
						occurrences.put(t, null);
					else
						occurrences.put(t, a);
		
		for (Map.Entry<Term, Atom> e : occurrences.entrySet()) 
			if (e.getValue() != null) {
				ListIterator<Term> i = e.getValue().getTerms().listIterator();
				while (i.hasNext()) {
					Term t = i.next();
					if (t.equals(e.getKey()))
						i.set(fac.getNondistinguishedVariable());
				}
				((PredicateAtomImpl)e.getValue()).listChanged();
		}
		
		Iterator<Atom> i = body.iterator();
		while (i.hasNext()) {
			Atom a = i.next();
			boolean found = false;
			for (Atom aa : body)
				if ((a != aa) && (a.getPredicate().equals(aa.getPredicate()))) {
					// ************
					// a.equals(aa) would be good but does not work, why?
					// ************
					if (a.getTerms().equals(aa.getTerms())) {
						//log.debug("ATOMS " + a + " AND " + aa + " COINCIDE - REMOVE ONE");
						found = true;
						break;
					}
				}	
			if (found) {
				i.remove();
 				break;
			}
		}		
	}
	

}
