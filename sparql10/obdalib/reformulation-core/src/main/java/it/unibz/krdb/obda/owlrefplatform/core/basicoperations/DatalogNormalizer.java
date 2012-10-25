package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatalogNormalizer {

	private final static OBDADataFactory fac = OBDADataFactoryImpl
			.getInstance();

	/***
	 * Normalizes all the rules in a Datalog program, pushing equalities into
	 * the atoms of the queries, when possible
	 * 
	 * @param dp
	 */
	public static DatalogProgram normalizeDatalogProgram(DatalogProgram dp) {
		DatalogProgram clone = fac.getDatalogProgram();
		clone.setQueryModifiers(dp.getQueryModifiers());
		for (CQIE cq : dp.getRules()) {
			CQIE normalized = normalizeCQIE(cq);
			if (normalized != null) {
				clone.appendRule(normalized);
			}
		}
		return clone;
	}

	public static CQIE normalizeCQIE(CQIE query) {
		CQIE result = normalizeANDTrees(query);
		// result = normalizeEQ(result);
		result = normalizeJoinTrees(result);
		result = pullUpNestedReferences(result, true);
		if (result == null)
			return null;
		return result;
	}

	/***
	 * This expands all AND trees into individual comparison atoms in the body
	 * of the query. Nested AND trees inside Join or LeftJoin atoms are not
	 * touched.
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE normalizeANDTrees(CQIE query) {
		CQIE result = query.clone();
		List<Atom> body = result.getBody();
		/* Collecting all necessary conditions */
		for (int i = 0; i < body.size(); i++) {
			Atom currentAtom = body.get(i);
			if (currentAtom.getPredicate() == OBDAVocabulary.AND) {
				body.remove(i);
				body.addAll(getUnfolderAtomList(currentAtom));
			}
		}
		return result;
	}

	/***
	 * This expands all Join that can be directly added as conjuncts to a
	 * query's body. Nested Join trees inside left joins are not touched.
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE normalizeJoinTrees(CQIE query) {
		return normalizeJoinTrees(query, true);
	}

	/***
	 * This expands all Join that can be directly added as conjuncts to a
	 * query's body. Nested Join trees inside left joins are not touched.
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE normalizeJoinTrees(CQIE query, boolean clone) {
		if (clone)
			query = query.clone();
		List body = query.getBody();
		normalizeJoinTrees(body, true);
		return query;
	}

	/***
	 * This expands all Join that can be directly added as conjuncts to a
	 * query's body. Nested Join trees inside left joins are not touched.
	 * <p>
	 * In addition, we will remove any Join atoms that only contain one single
	 * data atom, i.e., the join is not a join, but a table reference with
	 * conditions. These kind of atoms can result from the partial evaluation
	 * process and should be eliminated. The elimination takes all the atoms in
	 * the join (the single data atom plus possibly extra boolean conditions and
	 * adds them to the node that is the parent of the join).
	 * 
	 * @param query
	 * @return
	 */
	public static void normalizeJoinTrees(List body, boolean isJoin) {
		/* Collecting all necessary conditions */
		for (int i = 0; i < body.size(); i++) {
			Function currentAtom = (Function) body.get(i);
			if (!currentAtom.isAlgebraFunction())
				continue;
			if (currentAtom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN)
				normalizeJoinTrees(currentAtom.getTerms(), false);
			if (currentAtom.getFunctionSymbol() == OBDAVocabulary.SPARQL_JOIN) {
				normalizeJoinTrees(currentAtom.getTerms(), true);
				int dataAtoms = countDataItems(currentAtom.getTerms());
				if (isJoin || dataAtoms == 1) {
					body.remove(i);
					for (int j = currentAtom.getTerms().size() - 1; j >= 0; j--) {
						NewLiteral term = currentAtom.getTerm(j);
						Atom asAtom = term.asAtom();
						if (!body.contains(asAtom))
							body.add(i, asAtom);
					}
					i -= 1;
				}
			}
		}
	}

	public static int countDataItems(List<NewLiteral> terms) {
		int count = 0;
		for (NewLiteral lit : terms) {
			Function currentAtom = (Function) lit;
			if (!currentAtom.isBooleanFunction())
				count += 1;
		}
		return count;
	}

	/***
	 * Eliminates all equalities in the query by applying a substitution to the
	 * database predicates.
	 * 
	 * @param query
	 *            null if there is an unsatisfiable equality
	 * @return
	 */
	public static CQIE pushEqualities(CQIE result, boolean clone) {
		if (clone)
			result = result.clone();

		List body = result.getBody();
		Map<Variable, NewLiteral> mgu = new HashMap<Variable, NewLiteral>();

		/* collecting all equalities as substitutions */

		for (int i = 0; i < body.size(); i++) {
			Function atom = (Function) body.get(i);
			Unifier.applyUnifier(atom, mgu);
			if (atom.getPredicate() == OBDAVocabulary.EQ) {
				Substitution s = Unifier.getSubstitution(atom.getTerm(0),
						atom.getTerm(1));
				if (s == null) {
					return null;
				}

				if (!(s instanceof NeutralSubstitution)) {
					Unifier.composeUnifiers(mgu, s);
				}
				body.remove(i);
				i -= 1;
				continue;
			}
		}
		result = Unifier.applyUnifier(result, mgu, false);
		return result;
	}

	public static DatalogProgram pushEqualities(DatalogProgram dp) {
		DatalogProgram clone = fac.getDatalogProgram();
		clone.setQueryModifiers(dp.getQueryModifiers());
		for (CQIE cq : dp.getRules()) {
			pushEqualities(cq, false);
			clone.appendRule(cq);
		}
		return clone;
	}

	/***
	 * This method introduces new variable names in each data atom and
	 * equalities to account for JOIN operations. This method is called before
	 * generating SQL queries and allows to avoid cross refrences in nested
	 * JOINs, which generate wrong ON or WHERE conditions.
	 * 
	 * 
	 * @param currentTerms
	 * @param substitutions
	 */
	public static void pullOutEqualities(CQIE query) {
		Map<Variable, NewLiteral> substitutions = new HashMap<Variable, NewLiteral>();
		int[] newVarCounter = { 1 };
		Set<Function> booleanAtoms = new HashSet<Function>();
		pullOutEqualities(query.getBody(), substitutions, booleanAtoms,
				newVarCounter);

		/*
		 * All new variables have been generates, the substitutions also, we
		 * need to apply them to the equality atoms and to the head of the
		 * query.
		 */

		Unifier.applyUnifier(query, substitutions, false);

	}

	/***
	 * This method introduces new variable names in each data atom and
	 * equalities to account for JOIN operations. This method is called before
	 * generating SQL queries and allows to avoid cross refrences in nested
	 * JOINs, which generate wrong ON or WHERE conditions.
	 * 
	 * 
	 * @param currentTerms
	 * @param substitutions
	 */
	private static void pullOutEqualities(List currentTerms,
			Map<Variable, NewLiteral> substitutions,
			Set<Function> booleanAtoms, int[] newVarCounter) {
		for (int i = 0; i < currentTerms.size(); i++) {

			NewLiteral term = (NewLiteral) currentTerms.get(i);

			/*
			 * We don't expect any functions as terms, data atoms will only have
			 * variables or constants at this level. This method is only called
			 * exactly before generating the SQL query.
			 */
			if (!(term instanceof Function))
				throw new RuntimeException(
						"Unexpected term found while normalizing (pulling out equalities) the query.");

			Function atom = (Function) term;
			if (atom.isBooleanFunction()) {
				/*
				 * boolean atoms are collected to apply the resulting
				 * substitutions to them in a last step
				 */
				booleanAtoms.add(atom);
				continue;
			}

			List<NewLiteral> subterms = atom.getTerms();

			if (atom.isAlgebraFunction()) {
				pullOutEqualities(subterms, substitutions, booleanAtoms,
						newVarCounter);
				continue;
			}

			/*
			 * This is a data atom, we need to change ALL variables that appear
			 * in it
			 */
			if (!(atom.isDataFunction()))
				throw new RuntimeException(
						"Unpexpected kind of function found while pulling out equalities. Exected data atom");

			for (int j = 0; j < subterms.size(); j++) {
				NewLiteral subTerm = subterms.get(j);
				if (!(subTerm instanceof Variable))
					continue;
				Variable var1 = (Variable) subTerm;

				Variable var2 = (Variable) substitutions.get(var1);

				if (var2 == null) {
					/*
					 * No substitution exists, hence, no action but genrate a
					 * new variable and register in the substitutions, and
					 * replace the current value with a fresh one.
					 */
					var2 = fac.getVariable(var1.getName() + "f" +newVarCounter[0]);

					substitutions.put(var1, var2);
					subterms.set(j, var2);

				} else {

					/*
					 * There already exists one, so we generate a fresh, replace
					 * the current value, and add an equalility between the
					 * substitution and the new value.
					 */

					Variable newVariable = fac.getVariable(var1.getName()
							+ newVarCounter[0]);

					subterms.set(j, newVariable);
					currentTerms.add(fac.getEQFunction(var2, newVariable));

				}
				newVarCounter[0] += 1;
			}
		}
	}

	private static Set<Variable> getCurrentLevelVariables(List atoms) {
		Set<Variable> currentLevelVariables = new HashSet<Variable>();
		for (Object l : atoms) {
			Function atom = (Function) l;
			Predicate functionSymbol = atom.getFunctionSymbol();
			if (functionSymbol instanceof BooleanOperationPredicate)
				continue;
			else if (functionSymbol instanceof AlgebraOperatorPredicate) {
				currentLevelVariables.addAll(getCurrentLevelVariables(atom.getTerms()));
			} else
				currentLevelVariables.addAll(atom.getReferencedVariables());
		}
		return currentLevelVariables;
	}

	/***
	 * This will
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE pullUpNestedReferences(CQIE query, boolean clone) {

		if (clone)
			query = query.clone();

		List<Atom> body = query.getBody();

		Atom head = query.getHead();
		/*
		 * This set is only for reference
		 */
		Set<Variable> currentLevelVariables = getCurrentLevelVariables(body);
		/*
		 * This set will be modified in the process
		 */
		Set<Function> resultingBooleanConditions = new HashSet<Function>();

		/*
		 * Analyze each atom that is a Join or LeftJoin, the process will
		 * replace everything needed.
		 */
		int[] freshVariableCount = { 0 };
		for (Atom atom : body) {
			Function f = atom;
			if (!(f.getFunctionSymbol() instanceof AlgebraOperatorPredicate))
				continue;
			pullUpNestedReferences(f.getTerms(), head, currentLevelVariables,
					resultingBooleanConditions, freshVariableCount);
		}

		/*
		 * Adding any remiding boolean conditions to the top level.
		 */
		for (Function condition : resultingBooleanConditions) {
			body.add(condition.asAtom());
		}

		return query;
	}

	private static void pullUpNestedReferences(
			List<NewLiteral> currentLevelAtoms, Atom head,
			Set<Variable> upperLevelVariables, Set<Function> booleanConditions,
			int[] freshVariableCount) {

		/*
		 * Collecting the variables mentioned in data atoms in the current
		 * level.
		 */
		Set<Variable> currentLevelVariables = getCurrentLevelVariables(currentLevelAtoms);
		
		Set<Variable> mergedVariables = new HashSet<Variable>();

		/*
		 * Call recursively on each atom that is a Join or a LeftJoin passing
		 * the variables of this level
		 */
		for (NewLiteral l : currentLevelAtoms) {
			Function atom = (Function) l;
			if (!(atom.getFunctionSymbol() instanceof AlgebraOperatorPredicate))
				continue;
			List<NewLiteral> terms = atom.getTerms();
			pullUpNestedReferences(terms, head, mergedVariables,
					booleanConditions, freshVariableCount);
		}
		
		Set<Variable> problemVariables = new HashSet<Variable>();
		
		problemVariables.addAll(upperLevelVariables);
		problemVariables.removeAll(currentLevelVariables);

		/*
		 * Add the resulting equalities that belong to the current level. An
		 * equality belongs to this level if ALL its variables are defined at
		 * the current level and not at the upper levels.
		 */
		Set<Function> removedBooleanConditions = new HashSet<Function>();
		for (Function equality : booleanConditions) {
			Set<Variable> atomVariables = equality.getVariables();
			boolean belongsToThisLevel = true;
			for (Variable var : atomVariables) {
				if (problemVariables.contains(var))
					continue;
				belongsToThisLevel = false;
			}
			if (!belongsToThisLevel)
				continue;
			currentLevelAtoms.add(equality);
			removedBooleanConditions.add(equality);
		}
		booleanConditions.removeAll(removedBooleanConditions);

		/*
		 * Review the atoms of the current level and generate any variables,
		 * equalities needed at this level (no further recursive calls).
		 * Generate new variables for each variable that appears at this level,
		 * and also appears at a top level. We do this only for data atoms.
		 * 
		 * We do this by creating a substitution for each of the, and then
		 * applying the substitution. We also add an equality for each
		 * substitution we created.
		 */

		


		/*
		 * Review the current boolean atoms, if the refer to upper level
		 * variables then remove them from the current level and add them to the
		 * equalities set for the upper level.
		 * 
		 * If an contains at least 1 variable that is mentioned in an upper
		 * level, then this condition is removed from the current level and
		 * moved forward by adding it to the booleanConditions set.
		 */

		for (int index = 0; index < currentLevelAtoms.size(); index++) {
			NewLiteral l = currentLevelAtoms.get(index);
			Function atom = (Function) l;
			if (!(atom.getFunctionSymbol() instanceof BooleanOperationPredicate))
				continue;
			Set<Variable> variables = atom.getReferencedVariables();
			boolean belongsUp = false;
			for (Variable var : variables) {
				if (problemVariables.contains(var)) {
					belongsUp = true;
					break;
				}
			}
			if (!belongsUp)
				continue;

			// Belongs up, removing and pushing up
			currentLevelAtoms.remove(index);
			index -= 1;
			booleanConditions.add(atom);
		}

	}

	/***
	 * Takes an AND atom and breaks it into a list of individual condition
	 * atoms.
	 * 
	 * @param atom
	 * @return
	 */
	public static List<Atom> getUnfolderAtomList(Atom atom) {
		if (atom.getPredicate() != OBDAVocabulary.AND) {
			throw new InvalidParameterException();
		}
		List<NewLiteral> innerFunctionalTerms = new LinkedList<NewLiteral>();
		for (NewLiteral term : atom.getTerms()) {
			innerFunctionalTerms.addAll(getUnfolderTermList((Function) term));
		}
		List<Atom> newatoms = new LinkedList<Atom>();
		for (NewLiteral innerterm : innerFunctionalTerms) {
			Function f = (Function) innerterm;
			Atom newatom = fac.getAtom(f.getFunctionSymbol(), f.getTerms());
			newatoms.add(newatom);
		}
		return newatoms;
	}

	/***
	 * Takes an AND atom and breaks it into a list of individual condition
	 * atoms.
	 * 
	 * @param atom
	 * @return
	 */
	public static List<NewLiteral> getUnfolderTermList(Function term) {

		List<NewLiteral> result = new LinkedList<NewLiteral>();

		if (term.getFunctionSymbol() != OBDAVocabulary.AND) {
			result.add(term);
		} else {
			List<NewLiteral> terms = term.getTerms();
			for (NewLiteral currentterm : terms) {
				if (currentterm instanceof Function) {
					result.addAll(getUnfolderTermList((Function) currentterm));
				} else {
					result.add(currentterm);
				}
			}
		}

		return result;
	}
}
