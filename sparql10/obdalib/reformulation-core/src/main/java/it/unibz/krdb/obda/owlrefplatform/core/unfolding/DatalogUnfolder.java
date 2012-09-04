package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryAnonymizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Generates partial evaluations of rules (the queries), with respect to a set
 * of (partial) facts (set of rules). The procedure uses extended resolution
 * such that inner terms are also evaluated.
 * 
 * <p/>
 * The input fact rules must be non-cyclic otherwise the procedures in this
 * class will not terminate.
 * 
 * 
 * @author mariano
 * 
 */
public class DatalogUnfolder implements UnfoldingMechanism {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6088558456135748487L;

	private DatalogProgram unfoldingProgram;

	private static final OBDADataFactory termFactory = OBDADataFactoryImpl
			.getInstance();

	private static final Logger log = LoggerFactory
			.getLogger(DatalogUnfolder.class);

	private enum UnfoldingMode {
		UCQ, DATALOG
	};

	private UnfoldingMode unfoldingMode = UnfoldingMode.UCQ;

	private Map<Predicate, List<Integer>> primaryKeys = new HashMap<Predicate, List<Integer>>();

	private Map<Predicate, List<CQIE>> ruleIndex = new LinkedHashMap<Predicate, List<CQIE>>();

	public DatalogUnfolder(DatalogProgram unfoldingProgram,
			Map<Predicate, List<Integer>> primaryKeys) throws Exception {
		this.primaryKeys = primaryKeys;
		this.unfoldingProgram = unfoldingProgram;

		/*
		 * Creating a local index for the rules according to their predicate
		 */

		for (CQIE mappingrule : unfoldingProgram.getRules()) {
			Atom head = mappingrule.getHead();

			List<CQIE> rules = ruleIndex.get(head.getFunctionSymbol());
			if (rules == null) {
				rules = new LinkedList<CQIE>();
				ruleIndex.put(head.getFunctionSymbol(), rules);
			}
			rules.add(mappingrule);
		}
	}

	/***
	 * Computes a UCQ unfolding. This is will resolve each atom of each CQ in
	 * the input with the rules of the unfolding program. The original queries
	 * get removed from the result if their bodies contain atoms that have no
	 * matching rule the unfolding program.
	 * <p>
	 * Unfolding Program<br>
	 * <br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * B(x) :- table3(x,z)<br>
	 * <br>
	 * Query<br>
	 * <br>
	 * Q(x) :- A(x),B(x)<br>
	 * <br>
	 * Initially produces: <br>
	 * Q(x1) :- table1(x1,y1),B(x1)<br>
	 * Q(x1) :- table2(x1,z1),B(x1)<br>
	 * Q(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * Q(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
	 * But the final result is<br>
	 * Q(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * Q(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
	 * 
	 * <p>
	 * The strategy of this unfolding is simple, we cycle through all the
	 * queries and attempt to unfold atom 0 in the body. The resolution engine
	 * will generate 1 or more CQs as result. The original atom is removed, and
	 * the results are appended to the end of each query (hence its always safe
	 * to unfold atom 0). The new queries are kept for a next cycle. We stop
	 * when no new queries are produced.
	 * 
	 * <p>
	 * We need to extend this procedure optimizations to avoid unnecessary self
	 * joins and redundant CQs.
	 * 
	 * @param inputquery
	 * @return
	 */
	private DatalogProgram unfoldToUCQ(DatalogProgram inputquery,
			String targetPredicate) throws OBDAException {

		// LinkedHashSet<CQIE> evaluation = new LinkedHashSet<CQIE>();
		// evaluation.addAll(inputquery.getRules());

		List<CQIE> workingSet = new LinkedList<CQIE>();
		workingSet.addAll(inputquery.getRules());

		inputquery = DatalogNormalizer.normalizeDatalogProgram(inputquery);

		int[] rcount = { 0 };

		for (int queryIdx = 0; queryIdx < workingSet.size(); queryIdx++) {

			CQIE currentQuery = workingSet.get(queryIdx);

			for (int atomIdx = 0; atomIdx < currentQuery.getBody().size(); atomIdx++) {
				Stack<Integer> location = new Stack<Integer>();
				location.add(atomIdx);
				List<CQIE> result = resolve(currentQuery, location, rcount);

				if (result != null) {
					workingSet.remove((int) queryIdx);
					for (CQIE newquery : result) {
						if (!workingSet.contains(newquery)) {
							workingSet.add(queryIdx, newquery);
						}
					}
					/*
					 * it will cycle in the same atom until it can no longer be
					 * unfolder, then it moves to the next atom
					 */
					currentQuery = workingSet.get(queryIdx);
					atomIdx -= 1;

				}
				/* once the current query is exausted we move to the next atom */
			}
		}

		LinkedHashSet<CQIE> result = new LinkedHashSet<CQIE>();
		for (CQIE query : workingSet) {
			unfoldNestedJoin(query);
			result.add(query);
		}

		DatalogProgram resultdp = termFactory.getDatalogProgram(result);

		log.debug("Initial unfolding size: {} cqs", resultdp.getRules().size());
		// TODO make this a switch
		resultdp = CQCUtilities.removeContainedQueriesSorted(resultdp, true);
		log.debug("Resulting unfolding size: {} cqs", resultdp.getRules()
				.size());

		log.debug(resultdp.toString());

		return resultdp;
	}

	/***
	 * Computes a Datalog unfolding. This is simply the original query, plus all
	 * the rules in the unfolding program that can be applied in a resolution
	 * step to the atoms in the original query. For example:
	 * <p>
	 * Unfolding Program<br>
	 * <br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * B(x) :- table3(x,z)<br>
	 * <br>
	 * Query<br>
	 * <br>
	 * Q(x) :- A(x)<br>
	 * <br>
	 * Unfolding:<br>
	 * Q(x) :- A(x)<br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * 
	 * @param inputquery
	 * @return
	 */
	private DatalogProgram unfoldToDatalog(DatalogProgram inputquery) {
		HashSet<CQIE> relevantrules = new HashSet<CQIE>();
		for (CQIE cq : inputquery.getRules()) {
			for (Atom atom : cq.getBody()) {
				for (CQIE rule : unfoldingProgram.getRules(atom.getPredicate())) {
					/*
					 * No repeteatin is assured by the HashSet and the hashing
					 * implemented in each CQIE
					 */
					relevantrules.add(rule);
				}
			}
		}
		/**
		 * Done collecting relevant rules, appending the original query.
		 */
		LinkedList<CQIE> result = new LinkedList<CQIE>();
		result.addAll(inputquery.getRules());
		result.addAll(relevantrules);
		return termFactory.getDatalogProgram(result);
	}

	@Override
	public DatalogProgram unfold(DatalogProgram inputquery,
			String targetPredicate) throws OBDAException {

		log.debug("Unfolding mode: {}. Initial query size: {}", unfoldingMode,
				inputquery.getRules().size());

		// inputquery = replaceURIsForFunctions(inputquery);

		long startime = System.nanoTime();

		// log.debug("Computing partial evaluation for: \n{}", inputquery);
		inputquery = QueryAnonymizer.deAnonymize(inputquery);

		DatalogProgram partialEvaluation = null;
		if (unfoldingMode == UnfoldingMode.UCQ) {
			partialEvaluation = unfoldToUCQ(inputquery, targetPredicate);
		} else if (unfoldingMode == UnfoldingMode.DATALOG) {
			partialEvaluation = unfoldToDatalog(inputquery);
		} else {
			throw new RuntimeException("Unknown unfolding mode");
		}

		DatalogProgram dp = termFactory.getDatalogProgram();
		QueryUtils.copyQueryModifiers(inputquery, dp);
		dp.appendRule(partialEvaluation.getRules());

		long endtime = System.nanoTime();
		long timeelapsedseconds = (endtime - startime) / 1000000;
		log.debug("Unfolding size: {}   Time elapsed: {} ms", dp.getRules()
				.size(), timeelapsedseconds);
		return dp;
	}

	// /***
	// * Unfolds the atom in position pos. The procedure will remove the atom in
	// * pos from the query, an for each rule in the unfoldingProgram that
	// matches
	// * the predicate of the atom, it will generate a new query in which the
	// body
	// * of the matching rule is appended to the input CQIE.
	// * <p/>
	// * Optimization, this method will use the Primary keys of the DB
	// predicates
	// * as follows: Given a primary Key on A, on columns 1,2, and an atom
	// * A(x,y,z) added by the resolution engine (always added at the end of the
	// * CQ body), we will look for other atom A(x,y,z') if the atom exists, we
	// * can unify both atoms, apply the MGU to the query and remove one of the
	// * atoms.
	// *
	// * <p/>
	// * If there is no rule that can be used to unfold the atom it will return
	// an
	// * empty list.
	// *
	// *
	// * @param pos
	// * @param currentQuery
	// * @return
	// */
	// private List<CQIE> unfoldAtom(int pos, CQIE currentQuery, int count) {
	// LinkedList<CQIE> partialEvaluations = new LinkedList<CQIE>();
	//
	// if (pos >= currentQuery.getBody().size())
	// return partialEvaluations;
	//
	// /* Do not unfold operator atoms */
	// Atom atom = (Atom) currentQuery.getBody().get(pos);
	//
	// // if (atom.getPredicate() instanceof OperationPredicate) {
	// // currentQuery.getBody().remove(pos);
	// // currentQuery.getBody().add(atom);
	// // partialEvaluations.add(currentQuery);
	// // return partialEvaluations;
	// // }
	//
	// Predicate atomPredicate = atom.getPredicate();
	//
	// String name = atomPredicate.getName().toString();
	// if (atomPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
	// /*
	// * This is a nested atom, we need to handle it using the
	// */
	// partialEvaluations = unfoldNestedJoin(pos, currentQuery, count);
	//
	// } else if (atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI)) {
	//
	// } else {
	//
	// /*
	// * This is the normal case where there is no nesting and we resolve
	// * an atom with normal resolution steps
	// */
	//
	// List<CQIE> ruleList = unfoldingProgram.getRules(atomPredicate);
	//
	// for (CQIE mappingRule : ruleList) {
	// CQIE freshMappingRule = getFreshRule(mappingRule, count);
	//
	// CQIE pev = resolutionEngine.resolve(freshMappingRule,
	// currentQuery, pos);
	//
	// if (pev != null) {
	//
	// /*
	// * The following blocks eliminate redundant atoms w.r.t.
	// * query containment by doing syntactic checks on the atoms.
	// * This saves us from requiring full CQC checks. They are a
	// * bit hacky, and they change the cardinality of
	// * non-distinct queries.
	// */
	//
	// List<Atom> newbody = pev.getBody();
	// int newatomcount = mappingRule.getBody().size();
	// int oldatoms = newbody.size() - newatomcount - 1;
	// for (int newatomidx = oldatoms + 1; newatomidx < newbody
	// .size(); newatomidx++) {
	// Atom newatom = newbody.get(newatomidx);
	// if (newatom.getPredicate() instanceof BooleanOperationPredicate)
	// continue;
	//
	// /*
	// * OPTIMIZATION 1: PRIMARY KEYS
	// *
	// * We now take into account Primary Key constraints on
	// * the database to avoid adding redundant atoms to the
	// * query. This could also be done as an afterstep, using
	// * unification and CQC checks, however, its is much more
	// * expensive that way.
	// */
	//
	// /*
	// * Given a primary Key on A, on columns 1,2, and an atom
	// * A(x,y,z) added by the resolution engine (always added
	// * at the end of the CQ body), we will look for other
	// * atom A(x,y,z') if the atom exists, we can unify both
	// * atoms, apply the MGU to the query and remove one of
	// * the atoms.
	// */
	//
	// List<Integer> pkey = primaryKeys.get(newatom
	// .getPredicate());
	// if (pkey != null && !pkey.isEmpty()) {
	// /*
	// * the predicate has a primary key, looking for
	// * candidates for unification, when we find one we
	// * can stop, since the application of this
	// * optimization at each step of the derivation tree
	// * guarantees there wont be any other redundant
	// * atom.
	// */
	// Atom replacement = null;
	//
	// Map<Variable, NewLiteral> mgu = null;
	// for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// Atom tempatom = newbody.get(idx2);
	//
	// if (tempatom.getPredicate().equals(
	// newatom.getPredicate())) {
	//
	// boolean redundant = true;
	// for (Integer termidx : pkey) {
	// if (!newatom.getTerm(termidx - 1)
	// .equals(tempatom
	// .getTerm(termidx - 1))) {
	// redundant = false;
	// break;
	// }
	// }
	// if (redundant) {
	// /* found a candidate replacement atom */
	// mgu = Unifier.getMGU(newatom, tempatom);
	// if (mgu != null) {
	// replacement = tempatom;
	// break;
	// }
	// }
	//
	// }
	// }
	//
	// if (replacement != null) {
	//
	// if (mgu == null)
	// throw new RuntimeException(
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// pev = Unifier.applyUnifier(pev, mgu);
	// newbody = pev.getBody();
	// newbody.remove(newatomidx);
	// newatomidx -= 1;
	// continue;
	// }
	// }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// */
	//
	// // Atom replacement = null;
	// // Map<Variable,Integer> variableCount =
	// // pev.getVariableCount();
	// //
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// // Atom tempatom = newbody.get(idx2);
	// //
	// // if
	// // (tempatom.getPredicate().equals(newatom.getPredicate()))
	// // {
	// //
	// // /*
	// // * Checking if all terms are the same, or if they
	// // * are different, the all variables in the current
	// // * atom are free variables (they do not appear
	// // anywhere
	// // else)
	// // */
	// // int termindex = 0;
	// // boolean redundant = true;
	// // for (termindex = 0; termindex <
	// // tempatom.getTerms().size(); termindex++) {
	// // Term currenTerm = newatom.getTerm(termindex);
	// // if (!currenTerm.equals(tempatom.getTerm(termindex))
	// // &&
	// // !(variableCount.get(currenTerm) == 1))
	// // {
	// // redundant = false;
	// // break;
	// // }
	// // }
	// //
	// // if (redundant) {
	// // /* found a candidate replacement atom */
	// // mgu = Unifier.getMGU(newatom, tempatom);
	// // if (mgu != null) {
	// // replacement = tempatom;
	// // break;
	// // }
	// // }
	// //
	// // }
	// // }
	// //
	// // if (replacement != null) {
	// //
	// // if (mgu == null)
	// // throw new RuntimeException(
	// //
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// *
	// * b1 = r(x,y,z), r(m,n,o) b1 = r(x,y,z), r(x,n,o) b1 =
	// * r(x,y,z), r(x,y,o)
	// *
	// * In all these bodies, the second atoms is redundant
	// * w.r.t. set semantics. Note that with bag semantics,
	// * removing the atoms changes the cardinality of the
	// * query. E.g., let the data for r be:
	// *
	// * r(1,2,3), r(4,5,6), r(1,7,8).
	// *
	// * Then we have that |b1| = 9, |b2| = 5 and |b3| = 3.
	// * However, since the current implementation of the
	// * system is relaxed w.r.t. count of non-distinct values
	// * (it will probably stay like that), we dont care and
	// * we can remove these atoms.
	// *
	// * The condition is, given 2 atoms A, B, freeze B, and
	// * try to unify A,B, if true, then B is redundant,
	// * eliminate, else non redundant.
	// */
	//
	// // boolean redundant = false;
	// // List<Atom> body = pev.getBody();
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// //
	// // Atom atom2 = body.get(idx2);
	// // Atom frozenAtom = atom2.clone();
	// // CQCUtilities.getCanonicalAtom(frozenAtom, 1, new
	// // HashMap<Variable, Term>());
	// // if (Unifier.getMGU(frozenAtom, newatom) != null) {
	// // System.out.println("Redundant");
	// // mgu = Unifier.getMGU(atom2, newatom);
	// // redundant = true;
	// // break;
	// // }
	// // }
	// // if (redundant) {
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// }
	//
	// partialEvaluations.add(pev);
	// }
	// }
	// }
	//
	// return partialEvaluations;
	// }

	/***
	 * This method will attempt to unfold nested joins </ul>
	 * 
	 * <p>
	 * 
	 * @param pos
	 * @param currentQuery
	 * @param count
	 * @return
	 */
	private void unfoldNestedJoin(CQIE currentQuery) {
		for (int atomIdx = 0; atomIdx < currentQuery.getBody().size(); atomIdx++) {
			Atom function = currentQuery.getBody().get(atomIdx);

			/*
			 * DUplicated code WE NEED TO JOIN BY MODIFYING THE QUERY API
			 */

			for (int termIdx = 0; termIdx < function.getTerms().size(); termIdx++) {
				NewLiteral innerTerm = function.getTerm(termIdx);
				unfoldNestedJoin(innerTerm);

				if (!(innerTerm instanceof Function)) {
					continue;
				}

				Function innerFunction = ((Function) innerTerm);
				Predicate innerPredicate = innerFunction.getFunctionSymbol();
				if (!(innerPredicate.getName().toString()
						.equals(OBDAVocabulary.SPARQL_JOIN_URI)))
					continue;

				/*
				 * Found a join, removing the Join term and assimilating its
				 * terms
				 */
				List<NewLiteral> innerTerms = innerFunction.getTerms();
				function.getTerms().remove(termIdx);
				function.getTerms().addAll(termIdx, innerTerms);

			}

			/*
			 * Unfolding the Join atom
			 */

			Predicate innerPredicate = function.getFunctionSymbol();
			if (!(innerPredicate.getName().toString()
					.equals(OBDAVocabulary.SPARQL_JOIN_URI)))
				continue;

			/* Found a join, removing the Join term and assimilating its terms */
			List<Atom> body = currentQuery.getBody();
			body.remove(atomIdx);

			List<Atom> newAtoms = new LinkedList<Atom>();
			for (NewLiteral innerTerm : function.getTerms()) {
				Function asFunction = (Function) innerTerm;
				Atom newatom = termFactory.getAtom(
						asFunction.getFunctionSymbol(), asFunction.getTerms());
				newAtoms.add(newatom);
			}
			body.addAll(atomIdx, newAtoms);
		}
	}

	/***
	 * This method will attempt to unfold nested joins </ul>
	 * 
	 * <p>
	 * 
	 * @param pos
	 * @param currentQuery
	 * @param count
	 * @return
	 */
	private void unfoldNestedJoin(NewLiteral term) {
		if (!(term instanceof Function)) {
			return;
		}

		Function function = (Function) term;

		for (int termIdx = 0; termIdx < function.getTerms().size(); termIdx++) {
			NewLiteral innerTerm = function.getTerm(termIdx);
			unfoldNestedJoin(innerTerm);

			if (!(innerTerm instanceof Function)) {
				continue;
			}

			Function innerFunction = ((Function) innerTerm);
			Predicate innerPredicate = innerFunction.getFunctionSymbol();
			if (!(innerPredicate.getName().toString()
					.equals(OBDAVocabulary.SPARQL_JOIN_URI)))
				continue;

			/* Found a join, removing the Join term and assimilating its terms */
			List<NewLiteral> innerTerms = innerFunction.getTerms();
			function.getTerms().remove((int) termIdx);
			function.getTerms().addAll((int) termIdx, innerTerms);

		}
	}

	// /***
	// * Unfolds the inner terms of a literal. If the literal is not a function,
	// * then returns the same literal. If the literal is a function, it will
	// try
	// * to recursively unfold each term, until no term is unfoldable.
	// */
	// private NewLiteral unfoldLiteral(NewLiteral literal, int count) {
	//
	// if (!(literal instanceof Function))
	// return literal;
	//
	// Function function = (Function) literal;
	// Predicate mainPredicate = function.getFunctionSymbol();
	//
	// if (mainPredicate instanceof DataTypePredicate
	// || mainPredicate instanceof URITemplatePredicate
	// || mainPredicate instanceof BooleanOperationPredicate) {
	// /*
	// * This is a casting, comparison or tempalte, nothing to unfold
	// */
	// return literal;
	// }
	//
	// /*
	// * All the inner terms are no longer unfoldable. Now we should unfold
	// * the current function.
	// */
	//
	// if (!(mainPredicate instanceof BuiltinPredicate)) {
	// /*
	// * This is a data atom, it should be unfolded with the usual
	// * resolution algorithm.
	// */
	//
	// /**
	// * TODODODOODDODOODODDO
	// */
	// }
	//
	// int numberofTerms = function.getTerms().size();
	// for (int idx = 0; idx < numberofTerms; idx++) {
	// while (isUnfoldable(function.getTerm(idx))) {
	// unfoldLiteral(function.getTerm(idx), count);
	// }
	// }
	//
	// /*
	// * We are in the case of Joins and LeftJoins. The inner terms are
	// * already not unfoldable.
	// *
	// *
	// * LeftJoins cannot be modified if the inner children are not
	// * unfoldable.
	// *
	// * If the current terms is a Join, If one of the childs is a join, and
	// * this is a Join, we can remove the inner join and incorporate it into
	// * the current join.
	// */
	//
	// if (mainPredicate.getName().toString()
	// .equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI))
	// return function;
	//
	// /*
	// * This is a join, so any inner join should be broken and it's terms
	// * integrated.
	// */
	// Map<Integer, List<NewLiteral>> newTerms = new LinkedHashMap<Integer,
	// List<NewLiteral>>();
	//
	// for (int idx = 0; idx < numberofTerms; idx++) {
	// NewLiteral innerTerm = function.getTerm(idx);
	// if (!(innerTerm instanceof Function))
	// continue;
	// Function innerFunction = (Function) innerTerm;
	// if (!innerFunction.getFunctionSymbol().toString()
	// .equals(OBDAVocabulary.SPARQL_JOIN_URI))
	// continue;
	//
	// /* we found a Join, break it and assimilate */
	//
	// }
	//
	// Predicate atomPredicate = atom.getPredicate();
	//
	// String name = atomPredicate.getName().toString();
	// if (atomPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
	// /*
	// * This is a nested atom, we need to handle it using the
	// */
	// partialEvaluations = unfoldNestedJoin(pos, currentQuery, count);
	//
	// } else if (atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI)) {
	//
	// } else {
	//
	// /*
	// * This is the normal case where there is no nesting and we resolve
	// * an atom with normal resolution steps
	// */
	//
	// List<CQIE> ruleList = unfoldingProgram.getRules(atomPredicate);
	//
	// for (CQIE mappingRule : ruleList) {
	// CQIE freshMappingRule = getFreshRule(mappingRule, count);
	//
	// CQIE pev = resolutionEngine.resolve(freshMappingRule,
	// currentQuery, pos);
	//
	// if (pev != null) {
	//
	// /*
	// * The following blocks eliminate redundant atoms w.r.t.
	// * query containment by doing syntactic checks on the atoms.
	// * This saves us from requiring full CQC checks. They are a
	// * bit hacky, and they change the cardinality of
	// * non-distinct queries.
	// */
	//
	// List<Atom> newbody = pev.getBody();
	// int newatomcount = mappingRule.getBody().size();
	// int oldatoms = newbody.size() - newatomcount - 1;
	// for (int newatomidx = oldatoms + 1; newatomidx < newbody
	// .size(); newatomidx++) {
	// Atom newatom = newbody.get(newatomidx);
	// if (newatom.getPredicate() instanceof BooleanOperationPredicate)
	// continue;
	//
	// /*
	// * OPTIMIZATION 1: PRIMARY KEYS
	// *
	// * We now take into account Primary Key constraints on
	// * the database to avoid adding redundant atoms to the
	// * query. This could also be done as an afterstep, using
	// * unification and CQC checks, however, its is much more
	// * expensive that way.
	// */
	//
	// /*
	// * Given a primary Key on A, on columns 1,2, and an atom
	// * A(x,y,z) added by the resolution engine (always added
	// * at the end of the CQ body), we will look for other
	// * atom A(x,y,z') if the atom exists, we can unify both
	// * atoms, apply the MGU to the query and remove one of
	// * the atoms.
	// */
	//
	// List<Integer> pkey = primaryKeys.get(newatom
	// .getPredicate());
	// if (pkey != null && !pkey.isEmpty()) {
	// /*
	// * the predicate has a primary key, looking for
	// * candidates for unification, when we find one we
	// * can stop, since the application of this
	// * optimization at each step of the derivation tree
	// * guarantees there wont be any other redundant
	// * atom.
	// */
	// Atom replacement = null;
	//
	// Map<Variable, NewLiteral> mgu = null;
	// for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// Atom tempatom = newbody.get(idx2);
	//
	// if (tempatom.getPredicate().equals(
	// newatom.getPredicate())) {
	//
	// boolean redundant = true;
	// for (Integer termidx : pkey) {
	// if (!newatom.getTerm(termidx - 1)
	// .equals(tempatom
	// .getTerm(termidx - 1))) {
	// redundant = false;
	// break;
	// }
	// }
	// if (redundant) {
	// /* found a candidate replacement atom */
	// mgu = Unifier.getMGU(newatom, tempatom);
	// if (mgu != null) {
	// replacement = tempatom;
	// break;
	// }
	// }
	//
	// }
	// }
	//
	// if (replacement != null) {
	//
	// if (mgu == null)
	// throw new RuntimeException(
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// pev = Unifier.applyUnifier(pev, mgu);
	// newbody = pev.getBody();
	// newbody.remove(newatomidx);
	// newatomidx -= 1;
	// continue;
	// }
	// }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// */
	//
	// // Atom replacement = null;
	// // Map<Variable,Integer> variableCount =
	// // pev.getVariableCount();
	// //
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// // Atom tempatom = newbody.get(idx2);
	// //
	// // if
	// // (tempatom.getPredicate().equals(newatom.getPredicate()))
	// // {
	// //
	// // /*
	// // * Checking if all terms are the same, or if they
	// // * are different, the all variables in the current
	// // * atom are free variables (they do not appear
	// // anywhere
	// // else)
	// // */
	// // int termindex = 0;
	// // boolean redundant = true;
	// // for (termindex = 0; termindex <
	// // tempatom.getTerms().size(); termindex++) {
	// // Term currenTerm = newatom.getTerm(termindex);
	// // if (!currenTerm.equals(tempatom.getTerm(termindex))
	// // &&
	// // !(variableCount.get(currenTerm) == 1))
	// // {
	// // redundant = false;
	// // break;
	// // }
	// // }
	// //
	// // if (redundant) {
	// // /* found a candidate replacement atom */
	// // mgu = Unifier.getMGU(newatom, tempatom);
	// // if (mgu != null) {
	// // replacement = tempatom;
	// // break;
	// // }
	// // }
	// //
	// // }
	// // }
	// //
	// // if (replacement != null) {
	// //
	// // if (mgu == null)
	// // throw new RuntimeException(
	// //
	// "Unexcpected case found while performing JOIN elimination. Contact the authors for debugging.");
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// /*
	// * We remove all atoms that do not impose extra
	// * conditions on existing data. These are atoms that are
	// * implied by other atoms, and only check for the
	// * existance of data. They are redundant because there
	// * exists another atom that guarnatees satisfiabiliy of
	// * this atom. E.g.,
	// *
	// * b1 = r(x,y,z), r(m,n,o) b1 = r(x,y,z), r(x,n,o) b1 =
	// * r(x,y,z), r(x,y,o)
	// *
	// * In all these bodies, the second atoms is redundant
	// * w.r.t. set semantics. Note that with bag semantics,
	// * removing the atoms changes the cardinality of the
	// * query. E.g., let the data for r be:
	// *
	// * r(1,2,3), r(4,5,6), r(1,7,8).
	// *
	// * Then we have that |b1| = 9, |b2| = 5 and |b3| = 3.
	// * However, since the current implementation of the
	// * system is relaxed w.r.t. count of non-distinct values
	// * (it will probably stay like that), we dont care and
	// * we can remove these atoms.
	// *
	// * The condition is, given 2 atoms A, B, freeze B, and
	// * try to unify A,B, if true, then B is redundant,
	// * eliminate, else non redundant.
	// */
	//
	// // boolean redundant = false;
	// // List<Atom> body = pev.getBody();
	// // Map<Variable, Term> mgu = null;
	// // for (int idx2 = 0; idx2 <= oldatoms; idx2++) {
	// //
	// // Atom atom2 = body.get(idx2);
	// // Atom frozenAtom = atom2.clone();
	// // CQCUtilities.getCanonicalAtom(frozenAtom, 1, new
	// // HashMap<Variable, Term>());
	// // if (Unifier.getMGU(frozenAtom, newatom) != null) {
	// // System.out.println("Redundant");
	// // mgu = Unifier.getMGU(atom2, newatom);
	// // redundant = true;
	// // break;
	// // }
	// // }
	// // if (redundant) {
	// // pev = Unifier.applyUnifier(pev, mgu);
	// // newbody = pev.getBody();
	// // newbody.remove(newatomidx);
	// // newatomidx -= 1;
	// // continue;
	// // }
	//
	// }
	//
	// partialEvaluations.add(pev);
	// }
	// }
	// }
	//
	// return partialEvaluations;
	// }

	// /***
	// * Checks if a literal is unfoldable, i.e., a rule can be resolved against
	// * the literal or against an inner literal. Or, the literal can be
	// "broken"
	// * into its subcomponents.
	// *
	// * @param term
	// * @return
	// */
	// private boolean isUnfoldable(NewLiteral term) {
	// if (term instanceof Variable || term instanceof Constant) {
	// return false;
	// } else if (term instanceof Function) {
	// Predicate p = ((Function) term).getFunctionSymbol();
	// if (p instanceof BooleanOperationPredicate
	// || p instanceof DataTypePredicate
	// || p instanceof URITemplatePredicate) {
	// return false;
	// } else if (p instanceof AlgebraOperatorPredicate) {
	// // /*
	// // * This is an algebra operator. If its a Join, is unfoldable
	// // if
	// // * a) its parent is not a LeftJoin or b) at least one of its
	// // * inner terms is unfoldable If its a Left Join, it is
	// // * unfoldable is at least one of the inner terms is
	// // unfoldable.
	// // */
	// // String name = p.getName().toString();
	// // if (name.equals(OBDAVocabulary.SPARQL_JOIN_URI)
	// // && (term.getParent() == null || !((Function) term
	// // .getParent()).getFunctionSymbol().getName()
	// // .toASCIIString()
	// // .equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI)))
	// // return true;
	// for (NewLiteral innerLit : ((Function) term).getTerms()) {
	// if (isUnfoldable(innerLit))
	// return true;
	// }
	// return false;
	// } else {
	// /*
	// * This is a data atom/term, it is unfoldable only if there is a
	// * rule int he mapping program that can be unified with the term
	// */
	// List<CQIE> rules = unfoldingProgram.getRules(p);
	// for (CQIE rule : rules) {
	// Map<Variable, NewLiteral> mgu = Unifier.getMGU(
	// rule.getHead(), (Function) term);
	// if (mgu != null)
	// return true;
	// }
	// return false;
	// }
	// }
	// throw new RuntimeException("Term type not supported: "
	// + term.getClass() + " Term: " + term.toString());
	//
	// }

	/***
	 * Replaces each variable 'v' in the query for a new variable constructed
	 * using the name of the original variable plus the counter. For example
	 * 
	 * q(x) :- C(x)
	 * 
	 * results in
	 * 
	 * q(x_1) :- C(x_1)
	 * 
	 * if counter = 1.
	 * 
	 * This method can be used to generate "fresh" rules from a datalog program
	 * that is going to be used during a resolution procedure.
	 * 
	 * @param rule
	 * @param suffix
	 * @return
	 */
	public CQIE getFreshRule(CQIE rule, int suffix) {
		// This method doesn't support nested functional terms
		CQIE freshRule = rule.clone();
		Atom head = freshRule.getHead();
		List<NewLiteral> headTerms = head.getTerms();
		for (int i = 0; i < headTerms.size(); i++) {
			NewLiteral term = headTerms.get(i);
			NewLiteral newTerm = getFreshTerm(term, suffix);
			if (newTerm != null)
				headTerms.set(i, newTerm);
		}

		List<Atom> body = freshRule.getBody();
		for (Atom atom : body) {

			List<NewLiteral> atomTerms = ((Atom) atom).getTerms();
			for (int i = 0; i < atomTerms.size(); i++) {
				NewLiteral term = atomTerms.get(i);
				NewLiteral newTerm = getFreshTerm(term, suffix);
				if (newTerm != null)
					atomTerms.set(i, newTerm);
			}
		}
		return freshRule;

	}

	public NewLiteral getFreshTerm(NewLiteral term, int suffix) {
		NewLiteral newTerm = null;
		if (term instanceof VariableImpl) {
			VariableImpl variable = (VariableImpl) term;
			newTerm = termFactory
					.getVariable(variable.getName() + "_" + suffix);
		} else if (term instanceof Function) {
			Function functionalTerm = (Function) term;
			List<NewLiteral> innerTerms = functionalTerm.getTerms();
			List<NewLiteral> newInnerTerms = new LinkedList<NewLiteral>();
			for (int j = 0; j < innerTerms.size(); j++) {
				NewLiteral innerTerm = innerTerms.get(j);
				newInnerTerms.add(getFreshTerm(innerTerm, suffix));
			}
			Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
			Function newFunctionalTerm = (Function) termFactory
					.getFunctionalTerm(newFunctionSymbol, newInnerTerms);
			newTerm = newFunctionalTerm;
		} else if (term instanceof Constant) {
			newTerm = term.clone();
		} else {
			throw new RuntimeException("Unsupported term: " + term);
		}
		return newTerm;

	}

	/***
	 * This method asumes that the inner term (termidx) of term is a Function,
	 * otherwise it does nothing (i.e., variables, constants, etc cannot be
	 * resolved against rule.
	 * 
	 * @param resolvent
	 * @param term
	 * @param termidx
	 * @return
	 */
	public List<CQIE> resolve(CQIE rule, Stack<Integer> termidx,
			int[] resolutionCount) {

		/*
		 * locating the inner term specified by termidx
		 */
		NewLiteral focusLiteral = null;
		for (Integer i : termidx) {
			if (focusLiteral == null)
				focusLiteral = (Function) rule.getBody().get(i);
			else
				focusLiteral = ((Function) focusLiteral).getTerm(i);
		}

		if (!(focusLiteral instanceof Function))
			return null;

		Function focusFunction = (Function) focusLiteral;

		/* If the function is a built in, we need to ignore it */

		Predicate mainPredicate = focusFunction.getFunctionSymbol();
		String predicateName = mainPredicate.getName().toString();

		if (mainPredicate instanceof DataTypePredicate
				|| mainPredicate instanceof URITemplatePredicate
				|| mainPredicate instanceof BooleanOperationPredicate) {
			/*
			 * This is a casting, comparison or tempalte, nothing to unfold
			 */
			return null;
		}

		if (predicateName.equals(OBDAVocabulary.SPARQL_JOIN_URI)
				|| predicateName.equals(OBDAVocabulary.SPARQL_LEFTJOIN_URI)) {
			/*
			 * These may contain data atoms that need to be unfolded, we need to
			 * recursively unfold each term.
			 */
			for (int i = 0; i < focusFunction.getTerms().size(); i++) {
				termidx.push(i);
				List<CQIE> result = resolve(rule, termidx, resolutionCount);
				termidx.pop();
				if (result != null) {
					return result;
				}
			}

		}
		/*
		 * This is a data atom, it should be unfolded with the usual resolution
		 * algorithm.
		 */

		List<CQIE> result = new LinkedList<CQIE>();
		List<CQIE> candidateMatches = ruleIndex.get(focusFunction
				.getFunctionSymbol());

		if (candidateMatches == null)
			return null;

		for (CQIE candidateRule : candidateMatches) {

			CQIE freshRule = getFreshRule(candidateRule, resolutionCount[0]);
			resolutionCount[0] += 1;

			Map<Variable, NewLiteral> mgu = Unifier.getMGU(freshRule.getHead(),
					focusFunction);

			if (mgu == null) {
				/* Failed attempt */
				continue;
			}

			CQIE temprule = rule.clone();
			/*
			 * locating the inner term specified by termidx in the clone
			 */

			if (termidx.size() > 1) {

				NewLiteral newfocusLiteral = null;
				for (int y = 0; y < termidx.size() - 1; y++) {
					int i = termidx.get(y);
					if (newfocusLiteral == null)
						newfocusLiteral = (Function) temprule.getBody().get(i);
					else
						newfocusLiteral = ((Function) newfocusLiteral)
								.getTerm(i);
				}
				Function newfocusFunction = (Function) newfocusLiteral;

				List<NewLiteral> innerTerms = newfocusFunction.getTerms();
				innerTerms.remove((int) termidx.peek());
				innerTerms.addAll((int) termidx.peek(), freshRule.getBody());
			} else {
				temprule.getBody().remove((int) termidx.peek());
				temprule.getBody().addAll((int) termidx.peek(),
						freshRule.getBody());
			}

			CQIE newrule = Unifier.applyUnifier(temprule, mgu, false);
			result.add(newrule);
		}

		if (result.size() == 0)
			return null;
		return result;
	}
	
}
