package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.Predicate;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

/***
 * 
 * This class generate two dependency graphs for the datalog program
 * <ul>
 * <li>Predicate dependency graph with Predicate as nodes</li>
 * <li>Rule dependency graph with Rule as nodes</li>
 * </ul>
 * 
 * add some index
 * <ul>
 * <li>Rule Index by Head Predicate</li>
 * <li>extensionalPredicates</li>
 * </ul>
 * 
 * @author xiao
 * 
 */
public class DatalogDependencyGraphGenerator {

	private DirectedGraph<Predicate, DefaultEdge> predicateDependencyGraph = new DefaultDirectedGraph<Predicate, DefaultEdge>(
			DefaultEdge.class);

	private DirectedGraph<CQIE, DefaultEdge> ruleDependencyGraph = new DefaultDirectedGraph<CQIE, DefaultEdge>(
			DefaultEdge.class);

	/***
	 * Map of predicate to the rules defining it
	 */
	private Map<Predicate, List<CQIE>> ruleIndex = new LinkedHashMap<Predicate, List<CQIE>>();

	/**
	 * the predicates in the datalog program without definition
	 */
	private Set<Predicate> extensionalPredicates = new HashSet<Predicate>();

	public DirectedGraph<Predicate, DefaultEdge> getPredicateDependencyGraph() {
		return predicateDependencyGraph;
	}

	public DirectedGraph<CQIE, DefaultEdge> getRuleDependencyGraph() {
		return ruleDependencyGraph;
	}

	public Map<Predicate, List<CQIE>> getRuleIndex() {
		return ruleIndex;
	}

	public Set<Predicate> getExtensionalPredicates() {
		return extensionalPredicates;
	}

	
	public DatalogDependencyGraphGenerator(DatalogProgram program) {
		for (CQIE rule : program.getRules()) {

			Function head = rule.getHead();

			List<CQIE> rules = ruleIndex.get(head.getFunctionSymbol());
			if (rules == null) {
				rules = new LinkedList<CQIE>();
				ruleIndex.put(head.getFunctionSymbol(), rules);
			}
			rules.add(rule);
			
			updatePredicateDependencyGraph(rule);
		}

		generateRuleDependencyGraph(program);

		/**
		 * 
		 * Intuitively, the predicates in the datalog program without
		 * definitions *
		 * 
		 * <pre>
		 * extensionalPredicates = vertices(predicateDependencyGraph) - ruleIndex.keys()
		 * </pre>
		 */
		extensionalPredicates.addAll(predicateDependencyGraph.vertexSet());
		extensionalPredicates.removeAll(ruleIndex.keySet());

	}

	/***
	 * 
	 * generates the {@link #ruleDependencyGraph}
	 * 
	 * @param program
	 */
	private void generateRuleDependencyGraph(DatalogProgram program) {
		for (CQIE rule : program.getRules()) {
			ruleDependencyGraph.addVertex(rule);
			Predicate headPred = rule.getHead().getFunctionSymbol();
			Set<DefaultEdge> ontgoingEdges = predicateDependencyGraph
					.outgoingEdgesOf(headPred);

			for (DefaultEdge e : ontgoingEdges) {
				Predicate edgeTarget = predicateDependencyGraph
						.getEdgeTarget(e);
				List<CQIE> rules = ruleIndex.get(edgeTarget);
				if (rules != null) {
					for (CQIE dependentRule : rules) {
						ruleDependencyGraph.addVertex(dependentRule);
						ruleDependencyGraph.addEdge(rule, dependentRule);
					}
				}
			}
		}
	}

	/**
	 * Updates the {@link #predicateDependencyGraph} by the input rule.
	 * 
	 * It adds all the edges <rule.head.pred, p> to
	 * {@link #predicateDependencyGraph}, for all the p in the predicates of the
	 * rule body
	 * 
	 * 
	 * @param rule
	 */
	private void updatePredicateDependencyGraph(CQIE rule) {

		List<Predicate> dependencyList = new LinkedList<Predicate>();

		for (Function bodyAtom : rule.getBody()) {

			if (bodyAtom.isDataFunction()) {
				dependencyList.add(bodyAtom.getFunctionSymbol());
			} else if (bodyAtom.isAlgebraFunction() || bodyAtom.isBooleanFunction()) {
				generatePredicateDependency_traverseBodyAtom(dependencyList, bodyAtom);
			} else if (bodyAtom.isArithmeticFunction() || bodyAtom.isDataTypeFunction()){
				continue;
			} else {
				throw new IllegalStateException("Unknown Function");
			}
		}

		Predicate headPred = rule.getHead().getFunctionSymbol();
		predicateDependencyGraph.addVertex(headPred);

		for (Predicate dependentPred : dependencyList) {
			predicateDependencyGraph.addVertex(dependentPred);
			predicateDependencyGraph.addEdge(headPred, dependentPred);
		}
	}

	/**
	 * 
	 * This is a helper method for {@link #generatePredicateDependency}.
	 * 
	 * This method traverses in an atom, and put the predicates "ansi" to the
	 * dependentList
	 * 
	 * 
	 * @param dependentList
	 *            dependentList will be updated in the mothod
	 * 
	 * @param bodyAtom
	 */
	private void generatePredicateDependency_traverseBodyAtom(
			List<Predicate> dependentList, Function bodyAtom) {

		Queue<NewLiteral> queueInAtom = new LinkedList<NewLiteral>();

		queueInAtom.add(bodyAtom);
		while (!queueInAtom.isEmpty()) {
			NewLiteral queueHead = queueInAtom.poll();
			if (queueHead instanceof Function) {
				Function funcRoot = (Function) queueHead;
				
				if (funcRoot.isBooleanFunction() || funcRoot.isArithmeticFunction() 
						|| funcRoot.isDataTypeFunction() || funcRoot.isAlgebraFunction()) {
					for (NewLiteral term : funcRoot.getTerms()) {
						queueInAtom.add(term);
					}
				}  else if (funcRoot.isDataFunction()) {
					dependentList.add(funcRoot.getFunctionSymbol());
				}
			} else {
				// NO-OP
			}

		}

	}





}
