package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.ontology.Description;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

/** 
 * Starting from a graph build a DAG. 
 * Consider equivalences, redundancies and transitive reduction */

public class GraphDAGImpl implements GraphDAG{

	//contains the representative node with the set of equivalent mapping.
	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	/***
	 * A map to keep the relationship between 'eliminated' and 'remaining'
	 * nodes, created while computing equivalences by eliminating cycles in the
	 * graph.
	 */
	private Map<Description, Description> replacements = new HashMap<Description, Description>();

	private DAGImpl dag= new DAGImpl(DefaultEdge.class);
	
	//graph that will be transformed in a dag
	private GraphImpl modifiedGraph;

	
	public GraphDAGImpl (GraphImpl graph){
		System.out.println(graph);
		
		modifiedGraph=(GraphImpl) graph.clone();
		eliminateCycles();
		eliminateRedundantEdges();
		
//		System.out.println("modified graph "+modifiedGraph);
		
		dag= new DAGImpl( DefaultEdge.class);
		
		//change the graph in a dag
		Graphs.addGraph(dag, modifiedGraph);
		
//		System.out.println(dag);
		dag.setMapEquivalences(equivalencesMap);
		dag.setReplacements(replacements);
		dag.setIsaDAG(true);

	}


	@Override
	public DAGImpl getDAG() {

		return dag;
	}


	/***
	 * Eliminates redundant edges to ensure that the remaining DAG is the
	 * transitive reduction of the original DAG.
	 * 
	 * <p>
	 * This is done with an ad-hoc algorithm that functions as follows:
	 * 
	 * <p>
	 * Compute the set of all nodes with more than 2 outgoing edges (these have
	 * candidate redundant edges.) <br>
	 */
	private void eliminateRedundantEdges() {
		/* Compute the candidate nodes */
		List<Description> candidates = new LinkedList<Description>();
		Set<Description> vertexes = modifiedGraph.vertexSet();
		for (Description vertex : vertexes) {
			int outdegree = modifiedGraph.outDegreeOf(vertex);
			if (outdegree > 1) {
				candidates.add(vertex);
			}
		}

		/*
		 * for each candidate x and each outgoing edge x -> y, we will check if
		 * y appears in the set of redundant edges
		 */

		for (Description candidate : candidates) {
			
			Set<DefaultEdge> possiblyRedundantEdges = new LinkedHashSet<DefaultEdge>();
			
			possiblyRedundantEdges.addAll(modifiedGraph.outgoingEdgesOf(candidate));
			
			Set<DefaultEdge> eliminatedEdges = new HashSet<DefaultEdge>();
			
			// registering the target of the possible redundant targets for this
			// node
			Set<Description> targets = new HashSet<Description>();
			
			Map<Description, DefaultEdge> targetEdgeMap = new HashMap<Description, DefaultEdge>();
			
			for (DefaultEdge edge : possiblyRedundantEdges) {
				Description target = modifiedGraph.getEdgeTarget(edge);
				targets.add(target);
				targetEdgeMap.put(target, edge);
			}

			for (DefaultEdge currentPathEdge : possiblyRedundantEdges) {
				Description currentTarget = modifiedGraph.getEdgeTarget(currentPathEdge);
				if (eliminatedEdges.contains(currentPathEdge))
					continue;
				eliminateRedundantEdge(currentPathEdge, targets, targetEdgeMap, currentTarget, eliminatedEdges);
			}

		}

	}

	private void eliminateRedundantEdge(DefaultEdge safeEdge, Set<Description> targets, Map<Description, DefaultEdge> targetEdgeMap,
			Description currentTarget, Set<DefaultEdge> eliminatedEdges) {
		if (targets.contains(currentTarget)) {
			DefaultEdge edge = targetEdgeMap.get(currentTarget);
			if (!edge.equals(safeEdge)) {
				/*
				 * This is a redundant edge, removing it.
				 */
				modifiedGraph.removeEdge(edge);
				eliminatedEdges.add(edge);
			}
		}

		// continue traversing the dag up
		Set<DefaultEdge> edgesInPath = modifiedGraph.outgoingEdgesOf(currentTarget);
		for (DefaultEdge outEdge : edgesInPath) {
			Description target = modifiedGraph.getEdgeTarget(outEdge);
			eliminateRedundantEdge(safeEdge, targets, targetEdgeMap, target, eliminatedEdges);
		}

	}

	/***
	 * Eliminates all cycles in the graph by computing all strongly connected
	 * components and eliminating all but one node in each of the components
	 * from the graph. The result of this transformation is that the graph
	 * becomes a DAG.
	 * 
	 * <p>
	 * In the process two objects are generated, an 'Equivalence map' and a
	 * 'replacementMap'. The first can be used to get the implied equivalences
	 * of the TBox. The second can be used to locate the node that is
	 * representative of an eliminated node.
	 * 
	 * <p>
	 * Computation of the strongly connected components is done using the
	 * StrongConnectivityInspector from JGraphT.
	 * 
	 */
	private void eliminateCycles() {
		StrongConnectivityInspector<Description, DefaultEdge> inspector = new StrongConnectivityInspector<Description, DefaultEdge>(modifiedGraph);
		
		//each set contains vertices which together form a strongly connected component within the given graph
		List<Set<Description>> equivalenceSets = inspector.stronglyConnectedSets();

		for (Set<Description> equivalenceSet : equivalenceSets) {
			if (equivalenceSet.size() < 2)
				continue;
			Iterator<Description> iterator = equivalenceSet.iterator();
			Description representative = iterator.next();
			equivalencesMap.put(representative, equivalenceSet);

			while (iterator.hasNext()) {
				Description eliminatedNode = iterator.next();
				replacements.put(eliminatedNode, representative);
				equivalencesMap.put(eliminatedNode, equivalenceSet);

				/*
				 * Re-pointing all links to and from the eliminated node to the
				 * representative node
				 */

				Set<DefaultEdge> edges = new HashSet<DefaultEdge>(modifiedGraph.incomingEdgesOf(eliminatedNode));

				for (DefaultEdge incEdge : edges) {
					Description source = modifiedGraph.getEdgeSource(incEdge);

					modifiedGraph.removeAllEdges(source, eliminatedNode);

					if (source.equals(representative))
						continue;

					modifiedGraph.addEdge(source, representative);
				}

				edges = new HashSet<DefaultEdge>(modifiedGraph.outgoingEdgesOf(eliminatedNode));

				for (DefaultEdge outEdge : edges) {
					Description target = modifiedGraph.getEdgeTarget(outEdge);

					modifiedGraph.removeAllEdges(eliminatedNode, target);

					if (target.equals(representative))
						continue;
					modifiedGraph.addEdge(representative, target);

				}

				modifiedGraph.removeVertex(eliminatedNode);

			}
		}

	}

	
	
	
	
	





}
