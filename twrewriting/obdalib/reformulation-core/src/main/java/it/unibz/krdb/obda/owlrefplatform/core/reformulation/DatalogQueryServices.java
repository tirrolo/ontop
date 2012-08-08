package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatalogQueryServices {
	
	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private static final Logger log = LoggerFactory.getLogger(DatalogQueryServices.class);

	private static int nextVariableIndex = 1000;
	
	private static List<Atom> unify(CQIE rule, Atom atom) {
		Set<Term> freeVars = new HashSet<Term>(rule.getHead().getTerms());
		
		// substitute arguments of the head
		Map<Variable,Term> substituition = new HashMap<Variable,Term>(2);
		for (int i = 0; i < atom.getArity(); i++)
			substituition.put((Variable)rule.getHead().getTerm(i), atom.getTerm(i));
		
		// invent names for existentially quantified variables
		for (Atom sa : rule.getBody()) 			
			for (Term t : sa.getTerms())
				if ((t instanceof Variable) && !freeVars.contains(t) && !substituition.containsKey(t)) 
					substituition.put((Variable)t, fac.getVariable("local" + nextVariableIndex++));
		
		// substitute local variables that are EQ to something else
		for (Atom sa : rule.getBody()) 
			if (sa.getPredicate().equals(OBDAVocabulary.EQ)) {
				Term t0 = sa.getTerm(0);
				Term t1 = sa.getTerm(1);
				if ((t0 instanceof Variable) && !freeVars.contains(t0))
					substituition.put((Variable)t0, substituition.containsKey(t1) ? substituition.get(t1) : t1);
				else if ((t1 instanceof Variable) && !freeVars.contains(t1))
					substituition.put((Variable)t1, substituition.containsKey(t0) ? substituition.get(t0) : t0);
			}
		
		return Unifier.applyUnifier(rule, substituition).getBody();
	}
	
	public static DatalogProgram flatten(DatalogProgram dp, Predicate head, String fragment) {
		// contains all definitions of the main predicate
		List<CQIE> queue = new ArrayList<CQIE>();
		// collects all definitions
		Map<Predicate, List<CQIE> > defined = new HashMap<Predicate, List<CQIE> >();
		for (CQIE cqie : dp.getRules()) {
			// main predicate is not replaced
			Predicate predicate = cqie.getHead().getPredicate();
			if (predicate.equals(head) || 
					((fragment != null) && !predicate.getName().getFragment().contains(fragment))) {
				queue.add(cqie);
				log.debug("MAIN PREDICATE DEF " + cqie);
			}
			else {
				List<CQIE> def = defined.containsKey(predicate) 
						? defined.get(predicate) 
								: new ArrayList<CQIE>();
				def.add(cqie);	
				defined.put(predicate, def);
				log.debug("DEFINED " + predicate + " WITH " + cqie);
			}
		}
		log.debug("DEFINITIONS: " + defined);
		
		List<CQIE> output = new LinkedList<CQIE>();
		
		while (!queue.isEmpty()) {
			CQIE r = queue.remove(queue.size() - 1);
			
			boolean found = false;
			for (CQIE r2 : output) 
				if (CQCUtilities.isContainedInSyntactic(r,r2)) {
					found = true;
					log.debug("SUBSUMED " + r + " BY " + r2);
					break;
				}
			if (found)
				continue;
			
			List<Atom> body = r.getBody();
			int idxToBeReplaced = -1;
			for (int i = 0; i < body.size(); i++) 
				if (defined.containsKey(body.get(i).getPredicate())) {
					idxToBeReplaced = i;
					break;
				}
			if (idxToBeReplaced != -1) {
				Atom toBeReplaced = body.get(idxToBeReplaced);
				log.debug("REPLACING " + toBeReplaced + " IN " + body);
				
				for (CQIE rule : defined.get(toBeReplaced.getPredicate())) {
					CQIE qcopy = r.clone();
					qcopy.getBody().remove(idxToBeReplaced);
					qcopy.getBody().addAll(unify(rule,toBeReplaced));

					queue.add(reduce(qcopy));
					Collections.sort(queue, new Comparator<CQIE> () {
						@Override
						public int compare(CQIE arg0, CQIE arg1) {
							return arg1.getBody().size() - arg0.getBody().size();
						} 
						});
				}					
			}
			else {
				// prune the list
				ListIterator<CQIE> i = output.listIterator();
				while (i.hasNext()) {
					CQIE q2 = i.next();
					if (CQCUtilities.isContainedInSyntactic(r, q2)) {
						i.remove();				
						log.debug("   PRUNED " + q2 + " BY " + r);
					}
				}
				output.add(r);			
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
	
	
	private static CQIE reduce(CQIE q) {
		List<Atom> body = q.getBody();

		// EQ elimination 
		// (EQ argument, which is a quantified variable,
		//                  is replaced by the other EQ argument) 
		List<Term> freeVariables = q.getHead().getTerms();
		boolean replacedEQ = false;
		do { 
			replacedEQ = false;
			for (Atom eqa : body) 
				if (eqa.getPredicate().equals(OBDAVocabulary.EQ)) {
					Term t0 = eqa.getTerm(0);
					Term t1 = eqa.getTerm(1);
					if (t0.equals(t1)) {
						log.debug("   ELIMINATING EQUALITY " + eqa);
						body.remove(eqa);
						replacedEQ = true;
						break;						
					}
					if (t0 instanceof Variable && !freeVariables.contains(t0)) {
						log.debug("   ELIMINATING EQUALITY " + eqa);
						body.remove(eqa);
						for (Atom aa : body) 
							for (int i = 0; i <  aa.getTerms().size(); i++)
								if (aa.getTerm(i).equals(t0))
									aa.getTerms().set(i, t1);
						replacedEQ = true;
						break;
					}	
					if (t1 instanceof Variable && !freeVariables.contains(t1)) {
						log.debug("   ELIMINATING EQUALITY " + eqa);
						body.remove(eqa);
						for (Atom aa : body) 
							for (int i = 0; i <  aa.getTerms().size(); i++)
								if (aa.getTerm(i).equals(t1))
									aa.getTerms().set(i, t0);
						replacedEQ = true;
						break;
					}	
				}
		} while (replacedEQ); 
		
		return CQCUtilities.removeRundantAtoms(q);
	}
	
	/** 
	 * simplifies a given datalog query by eliminating predicates 
	 * that have a single rule defining them
	 * 
	 * @param dp: given datalog program
	 * @param head: main predicated defined the by the query
	 * @return simplified datalog program
	 */
	
	public static DatalogProgram simplify(DatalogProgram dp, Predicate head) {
		// for each predicate key, it contains the body to be used instead of the key
		//                                     or null if it is not to be removed
		Map<Predicate, CQIE > replacement = new HashMap<Predicate, CQIE >(4);
		replacement.put(head, null); // head is never replaced
		
		for (CQIE cqie : dp.getRules()) {
			Predicate pred = cqie.getHead().getPredicate(); 
			if (replacement.containsKey(pred))
				replacement.put(pred, null); // not to be replaced
			else
				replacement.put(pred, cqie);
		}
		
		List<CQIE> result = new ArrayList<CQIE>(dp.getRules().size());
		for (CQIE cqie : dp.getRules()) {
			// if to be replaced then no rule is added to the result
			if (replacement.get(cqie.getHead().getPredicate()) != null) 
				continue;
			
			boolean changed = false;
			Set<Atom> body = new HashSet<Atom>(cqie.getBody());
			do {
				Set<Atom> newBody = new HashSet<Atom>(body.size());
				changed = false;
				for (Atom a : body) {
					CQIE rule  = replacement.get(a.getPredicate());
					if (rule == null)
						newBody.add(a); // NO REPLACEMENT
					else {
						changed = true;
						newBody.addAll(unify(rule, a)); 
						log.debug("REPLACE " + a + " WITH " + rule);
					}
				}
				body = newBody;
			} while (changed);
			result.add(fac.getCQIE(cqie.getHead(), new ArrayList<Atom>(body)));
		}
		return fac.getDatalogProgram(result);
	}

}
