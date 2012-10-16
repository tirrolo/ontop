package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BNodePredicate;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.NonBooleanOperationPredicate;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.util.LinkedHashSet;
import java.util.Set;

public class ExpressionEvaluator {

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public static void evaluateExpressions(DatalogProgram p) {
		Set<CQIE> toremove = new LinkedHashSet<CQIE>();
		for (CQIE q : p.getRules()) {
			boolean empty = evaluateExpressions(q);
			if (empty)
				toremove.add(q);
		}
		p.removeRules(toremove);
	}

	public static boolean evaluateExpressions(CQIE q) {
		for (int atomidx = 0; atomidx < q.getBody().size(); atomidx++) {
			Atom atom = q.getBody().get(atomidx);
			NewLiteral newatom = eval(atom);
			if (newatom == fac.getTrue()) {
				q.getBody().remove(atomidx);
				atomidx -= 1;
				continue;
			} else if (newatom == fac.getFalse())
				return true;
			q.getBody().remove(atomidx);
			q.getBody().add(atomidx, newatom.asAtom());
		}
		return false;
	}

	public static NewLiteral eval(NewLiteral expr) {

		if (expr instanceof Variable) {

			return eval((Variable) expr);

		} else if (expr instanceof Constant) {

			return eval((Constant) expr);

		} else if (expr instanceof Function) {

			return eval((Function) expr);

		} else {

			throw new RuntimeException("Invalid expression");

		}

	}

	public static NewLiteral eval(Variable expr) {
		return expr;
	}

	public static NewLiteral eval(Constant expr) {
		return expr;
	}

	public static NewLiteral eval(Function expr) {
		Predicate p = expr.getFunctionSymbol();
		if (p instanceof BooleanOperationPredicate) {
			return evalBoolean(expr);
		} else if (p instanceof NonBooleanOperationPredicate) {
			return evalNonBoolean(expr);
		} else if (p == OBDAVocabulary.XSD_BOOLEAN) {
			if (expr.getTerm(0) instanceof Constant) {
				ValueConstant value = (ValueConstant) expr.getTerm(0);
				if (value.equals("1") || value.equals("true")) {
					return fac.getTrue();
				} else if (value.equals("0") || value.equals("false")) {
					return fac.getFalse();
				}
			} else
				return expr;
		}
		return expr;
	}

	public static NewLiteral evalBoolean(Function term) {
		Predicate pred = term.getFunctionSymbol();
		if (pred == OBDAVocabulary.AND) {
			return evalAndOr(term, true);
		} else if (pred == OBDAVocabulary.OR) {
			return evalAndOr(term, false);
		} else if (pred == OBDAVocabulary.EQ) {
			return evalEqNeq(term, true);
		} else if (pred == OBDAVocabulary.GT) {
			return term;
		} else if (pred == OBDAVocabulary.GTE) {
			return term;
		} else if (pred == OBDAVocabulary.IS_NOT_NULL) {
			return evalIsNullNotNull(term, false);
		} else if (pred == OBDAVocabulary.IS_NULL) {
			return evalIsNullNotNull(term, true);
		} else if (pred == OBDAVocabulary.LT) {
			return term;
		} else if (pred == OBDAVocabulary.LTE) {
			return term;
		} else if (pred == OBDAVocabulary.NEQ) {
			return evalEqNeq(term, false);
		} else if (pred == OBDAVocabulary.NOT) {
			return eval(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_LITERAL) {
			return evalIsLiteral(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_BLANK) {
			return evalIsBlank(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_URI) {
			return evalIsUri(term);
		} else if (pred == OBDAVocabulary.SPARQL_IS_IRI) {
			return evalIsIri(term);
		} else if (pred == OBDAVocabulary.SPARQL_LANGMATCHES) {
			return evalLangMatches(term);
		} else {
			throw new RuntimeException(
					"Evaluation of expression not supported: "
							+ term.toString());
		}
	}

	private static NewLiteral evalNonBoolean(Function term) {
		Predicate pred = term.getFunctionSymbol();
		if (pred == OBDAVocabulary.SPARQL_STR) {
			return evalStr(term);
		} else if (pred == OBDAVocabulary.SPARQL_DATATYPE) {
			return evalDatatype(term);
		} else if (pred == OBDAVocabulary.SPARQL_LANG) {
			return evalLang(term);
		} else {
			throw new RuntimeException(
					"Evaluation of expression not supported: "
							+ term.toString());
		}
	}

	/*
	 * Expression evaluator for isLiteral() function
	 */
	private static NewLiteral evalIsLiteral(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof DataTypePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for isBlank() function
	 */
	private static NewLiteral evalIsBlank(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof BNodePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for isURI() function
	 */
	private static NewLiteral evalIsUri(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof URITemplatePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}
		return term;
	}
	
	/*
	 * Expression evaluator for isIRI() function
	 */
	private static NewLiteral evalIsIri(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof URITemplatePredicate) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for str() function
	 */
	private static NewLiteral evalStr(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			NewLiteral parameter = function.getTerm(0);
			if (predicate instanceof DataTypePredicate) {
				String datatype = predicate.toString();
				if (datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)) { 
					return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
							fac.getVariable(parameter.toString()));
				} else if (datatype.equals(OBDAVocabulary.XSD_STRING_URI)) {
					return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
							fac.getVariable(parameter.toString()));
				} else {
					return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
							fac.getFunctionalTerm(OBDAVocabulary.QUEST_CAST, 
									fac.getVariable(parameter.toString()), 
									fac.getValueConstant(OBDAVocabulary.XSD_STRING_URI)));
				}
			} else if (predicate instanceof URITemplatePredicate) {
				return fac.getFunctionalTerm(fac.getDataTypePredicateLiteral(),
						function.clone());
			} else if (predicate instanceof BNodePredicate) {
				return fac.getNULL();
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for datatype() function
	 */
	private static NewLiteral evalDatatype(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof DataTypePredicate) {
				return fac.getFunctionalTerm(fac.getDataTypePredicateLiteral(), 
						fac.getValueConstant(predicate.toString(), COL_TYPE.UNSUPPORTED));
			} else if (predicate instanceof BNodePredicate) {
				return null;
			} else if (predicate instanceof URITemplatePredicate) {
				return null;
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for lang() function
	 */
	private static NewLiteral evalLang(Function term) {
		NewLiteral teval = eval(term.getTerm(0));
		if (teval instanceof Function) {
			Function function = (Function) teval;
			Predicate predicate = function.getFunctionSymbol();
			if (predicate instanceof DataTypePredicate) {
				String datatype = predicate.toString();
				if (datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)) { 
					if (function.getTerms().size() == 2) { // exist lang term
						NewLiteral parameter = function.getTerm(1);
						if (parameter instanceof Variable) {
							return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
									parameter.clone());
						} else if (parameter instanceof Constant) {
							return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
									fac.getValueConstant(parameter.toString()));
						}
					} else {
						return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
								fac.getValueConstant(""));
					}
				} else {
					return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
							fac.getValueConstant(""));
				}
			} else {
				return fac.getFunctionalTerm(fac.getDataTypePredicateString(),
						fac.getValueConstant(""));
			}
		}
		return term;
	}

	/*
	 * Expression evaluator for langMatches() function
	 */
	private static NewLiteral evalLangMatches(Function term) {
		/*
		 * Evaluate the first term
		 */
		NewLiteral teval1 = eval(term.getTerm(0));
		if (teval1 == null) {
			return fac.getFalse();
		}
		/*
		 * Evaluate the second term
		 */
		NewLiteral teval2 = eval(term.getTerm(1));
		if (teval2 == null) {
			return fac.getFalse();
		}

		/*
		 * Term checks
		 */
		if (teval1 instanceof Constant && teval2 instanceof Constant) {
			if (teval1.equals(teval2)) {
				return fac.getTrue();
			} else {
				return fac.getFalse();
			}
		} else if (teval1 instanceof Variable && teval2 instanceof Constant) {
			Variable var = (Variable) teval1;
			Constant lang = (Constant) teval2;
			if (lang.getValue().equals("*")) {
				// The char * means to get all languages
				return fac.getNEQAtom(var, fac.getNULL());
			} else {
				return fac.getEQFunction(var, lang);
			}
		} else if (teval1 instanceof Function && teval2 instanceof Function) {
			Function f1 = (Function) teval1;
			Function f2 = (Function) teval2;
			return evalLangMatches(fac.getLANGMATCHESFunction(f1.getTerm(0), f2.getTerm(0)));
		} else {
			return term;
		}
	}

	public static NewLiteral evalIsNullNotNull(Function term, boolean isnull) {
		NewLiteral result = eval(term.getTerms().get(0));
		if (result == OBDAVocabulary.NULL) {
			if (isnull)
				return fac.getTrue();
			else
				return fac.getFalse();
		} else if (result instanceof Constant) {
			if (!isnull)
				return fac.getTrue();
			else
				return fac.getFalse();
		}

		// TODO improve evaluation of is (not) null
		/*
		 * This can be inproved by evaluating some of the function, e.g,. URI
		 * and Bnodes never return null
		 */

		if (isnull)
			return fac.getIsNullFunction(result);
		else
			return fac.getIsNotNullFunction(result);

	}

	public static NewLiteral evalEqNeq(Function term, boolean eq) {
		/* Normalizing the locatino of terms, functions first */

		/*
		 * Evaluate the first term
		 */
		NewLiteral teval1 = eval(term.getTerm(0));
		if (teval1 == null) {
			return fac.getFalse();
		}
		
		/*
		 * Evaluate the second term
		 */
		NewLiteral teval2 = eval(term.getTerm(1));
		if (teval2 == null) {
			return fac.getFalse();
		}

		NewLiteral eval1 = teval1 instanceof Function ? teval1 : teval2;
		NewLiteral eval2 = teval1 instanceof Function ? teval2 : teval1;

		if (eval1 instanceof Variable || eval2 instanceof Variable) {
			// no - op
		} else if (eval1 instanceof Constant && eval2 instanceof Constant) {
			if (eval1.equals(eval2))
				if (eq)
					return fac.getTrue();
				else
					return fac.getFalse();
			else if (eq)
				return fac.getFalse();
			else
				return fac.getTrue();

		} else if (eval1 instanceof Function) {
			Function f1 = (Function) eval1;

			Predicate pred1 = f1.getFunctionSymbol();

			if (pred1.getType(0) == COL_TYPE.UNSUPPORTED)
				throw new RuntimeException("Unsupported type: " + pred1);

			if (eval2 instanceof Function) {
				Function f2 = (Function) eval2;
				Predicate pred2 = f2.getFunctionSymbol();

				if (pred2.getType(0) == COL_TYPE.UNSUPPORTED)
					throw new RuntimeException("Unsupported type: " + pred2);

				if (pred1 == OBDAVocabulary.RDFS_LITERAL
						&& pred2 == OBDAVocabulary.RDFS_LITERAL) {
					/*
					 * Special code to handle quality of Literals (plain, and
					 * with language)
					 */

					if (f1.getTerms().size() != f2.getTerms().size()) {
						// case one is with language another without
						if (eq)
							return fac.getFalse();
						else
							return fac.getTrue();
					} else if (f1.getTerms().size() == 2) {
						// SIZE == 2
						// these are literals with languages, wee need to
						// return the
						// evaluation of the values and the languages
						// case literals without language, its exactly as
						// normal datatypes
						// this is copy paste code
						Function eqValues = null;
						Function eqLang = null;
						Function comparison = null;
						if (eq) {
							eqValues = fac.getEQFunction(f1.getTerm(0),
									f2.getTerm(0));
							eqLang = fac.getEQFunction(f1.getTerm(1),
									f2.getTerm(1));
							comparison = fac.getANDFunction(eqValues, eqLang);
							return evalAndOr(comparison, true);
						}
						eqValues = fac.getNEQFunction(f1.getTerm(0),
								f2.getTerm(0));
						eqLang = fac.getNEQFunction(f1.getTerm(1),
								f2.getTerm(1));
						comparison = fac.getORFunction(eqValues, eqLang);
						return evalAndOr(comparison, false);

					}
					// case literals without language, its exactly as normal
					// datatypes
					// this is copy paste code
					Function neweq = null;
					if (eq) {
						neweq = fac.getEQFunction(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, true);
					} else {
						neweq = fac
								.getNEQFunction(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, false);
					}

				} else if (pred1.equals(pred2)) {

					Function neweq = null;
					if (eq) {
						neweq = fac.getEQFunction(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, true);
					} else {
						neweq = fac
								.getNEQFunction(f1.getTerm(0), f2.getTerm(0));
						return evalEqNeq(neweq, false);
					}

				} else {
					if (eq)
						return fac.getFalse();
					else
						return fac.getTrue();
				}
			}

		}

		/* eval2 is not a function */
		if (eq)
			return fac.getEQAtom(eval1, eval2);
		else
			return fac.getNEQAtom(eval1, eval2);

	}

	public static NewLiteral evalAndOr(Function term, boolean and) {

		/* Normalizing the locatino of terms, constants first */

		NewLiteral teval1 = eval(term.getTerm(0));
		NewLiteral teval2 = eval(term.getTerm(1));

		NewLiteral eval1 = teval1 instanceof Constant ? teval1 : teval2;
		NewLiteral eval2 = teval1 instanceof Constant ? teval2 : teval1;

		/* Implementing boolean logic */

		if (eval1 == fac.getTrue()) {
			if (eval2 == fac.getTrue())
				if (and)
					return fac.getTrue();
				else
					return fac.getFalse();
			else if (eval2 == fac.getFalse())
				if (and)
					return fac.getFalse();
				else
					return fac.getTrue();
			else if (and)
				/* if its an and we still need to evaluate eval2 */
				return eval2;
			else
				/*
				 * Its an Or, and the first was true, so it doesn't matter whats
				 * next.
				 */
				return fac.getTrue();

		} else if (eval1 == fac.getFalse()) {
			if (eval2 == fac.getTrue())
				if (and)
					return fac.getFalse();
				else
					return fac.getTrue();
			else if (eval2 == fac.getFalse())
				if (and)
					return fac.getFalse();
				else
					return fac.getFalse();
			else if (and)
				/*
				 * Its an And, and the first was false, so it doesn't matter
				 * whats next.
				 */
				return fac.getFalse();
			else
				return eval2;

		}
		/*
		 * None of the subnodes evaluated to true or false, we have functions
		 * that need to be evaluated
		 */
		// TODO check if we can further optimize this
		if (and)
			return fac.getANDAtom(eval1, eval2);
		else
			return fac.getORAtom(eval1, eval2);

	}
}
