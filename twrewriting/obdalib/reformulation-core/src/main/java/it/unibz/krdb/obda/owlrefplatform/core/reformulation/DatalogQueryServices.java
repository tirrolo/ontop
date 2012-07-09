package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatalogQueryServices {
	
	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private static final Logger log = LoggerFactory.getLogger(DatalogQueryServices.class);

	private static int nextVariableIndex = 1000;
	
	private static List<Atom> unify(CQIE rule, Atom atom) {
		// substituting arguments
		Map<Term,Term> substituition = new HashMap<Term,Term>(2);
		for (int i = 0; i < atom.getArity(); i++)
			substituition.put(rule.getHead().getTerm(i), atom.getTerm(i));
		
		// invent names for other variables
		for (Atom sa : rule.getBody())
			for (Term t : sa.getTerms())
				if (!substituition.containsKey(t) && !rule.getHead().getTerms().contains(t))
					substituition.put(t, fac.getVariable("local" + nextVariableIndex++));
		
		List<Atom> unifiedBody = new ArrayList<Atom>(rule.getBody().size()); 
		for  (Atom a : rule.getBody()) {
			List<Term> terms = new ArrayList<Term>(a.getTerms().size());
			for (Term t: a.getTerms()) {
				terms.add(substituition.get(t));
			}
			unifiedBody.add(fac.getAtom(a.getPredicate(), terms));
		}		
		return unifiedBody;
	}
	
	public static DatalogProgram flatten(DatalogProgram dp, Predicate head) {
		// contains all definitions of the main predicate
		List<CQIE> result = new ArrayList<CQIE>(dp.getRules().size());
		// collects all definitions
		Map<Predicate, List<CQIE> > defined = new HashMap<Predicate, List<CQIE> >();
		for (CQIE cqie : dp.getRules()) {
			// main predicate is not replaced
			Predicate predicate = cqie.getHead().getPredicate();
			if (predicate.equals(head)) {
				result.add(cqie);
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
		
		boolean changed = false;
		do {
			List<CQIE> temp = new ArrayList<CQIE>(dp.getRules().size());
			changed = false;
			
			for (CQIE r : result) {
				List<Atom> bodyCopy = new ArrayList<Atom>(r.getBody().size());
				Atom toBeReplaced = null;
				for (Atom a : r.getBody()) {
					log.debug("TO BE REPLACED? " + a);
					if ((toBeReplaced == null) && defined.containsKey(a.getPredicate())) 
						toBeReplaced = a;
					else
						bodyCopy.add(a);
				}
				if (toBeReplaced != null) {
					log.debug("TO BE REPLACED " + toBeReplaced + " IN " + bodyCopy);
					changed = true;
					for (CQIE rule : defined.get(toBeReplaced.getPredicate())) {
						List<Atom> b = new ArrayList<Atom>(bodyCopy);
						b.addAll(unify(rule,toBeReplaced));
						temp.add(fac.getCQIE(r.getHead(), b));
						log.debug("REPLACED " + b);
					}					
				}
				else
					temp.add(r);
			}		
			result = temp;
		} while (changed);
		
		
		return fac.getDatalogProgram(result);
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
			List<Atom> body = cqie.getBody();
			do {
				List<Atom> newBody = new ArrayList<Atom>(body.size());
				changed = false;
				for (Atom a : body) {
					CQIE rule  = replacement.get(a.getPredicate());
					log.debug("REPLACE " + a + " WITH " + rule + "?");
					if (rule == null)
						newBody.add(a); // NO REPLACEMENT
					else {
						changed = true;
						newBody.addAll(unify(rule, a)); 
					}
				}
				body = newBody;
			} while (changed);
			result.add(fac.getCQIE(cqie.getHead(), body));
		}
		return fac.getDatalogProgram(result);
	}

}
