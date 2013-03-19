package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange.Interval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;


/** Build the indexes for the DAG
 * create a map with the index and and the intervals for each node in the graph
 * 
 * 
 */
public class SemanticIndexEngineImpl implements SemanticIndexEngine{

	private DAGImpl namedDag;
	private Map< Description, Integer> indexes = new HashMap<Description, Integer>();
	private Map< Description, SemanticIndexRange> ranges = new HashMap<Description, SemanticIndexRange>();

	//listener on the topological sort of the graph
	public class IndexListener extends TraversalListenerAdapter<Description, DefaultEdge> {


		private int index_counter = 1;



		DirectedGraph <Description, DefaultEdge> g;
		private boolean newComponent;

		//last root node
		private Description reference;

		public IndexListener(DirectedGraph<Description, DefaultEdge> g) {
			this.g = g;
		}

		//search for the new root in the graph
		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			newComponent = true;

		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Description> e) {

			Description vertex = e.getVertex();

			if (newComponent) {
				reference = vertex;
				newComponent = false;
			}

			indexes.put(vertex, index_counter);
			ranges.put(vertex, new SemanticIndexRange(index_counter, index_counter));
			index_counter++;


		}

		public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			//merge all the interval for the current root of the graph
			mergeRangeNode(reference);

		}
	}

 /**  Merge the indexes of the current connected component 
  * @param Description d  is the root node */
	private void mergeRangeNode(Description d) {

		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(namedDag);
		//successorList gives the direct children of the node without the equivalences
		for (Description ch : Graphs.successorListOf(reversed, d)) {
			if (ch != d) {
				mergeRangeNode(ch);
				
				//merge the index of the node with the index of his child
				ranges.get(d).addRange(ranges.get(ch));
			}

		}


	}

	/**
	 * Assign indexes for the named DAG
	 * @param reasoner used to know ancestors and descendants of the dag
	 */


	public SemanticIndexEngineImpl(TBoxReasonerImpl reasoner) {

		
		namedDag=reasoner.getDAG();
		if (namedDag.isaNamedDAG())
		{
			construct();
		}

	}
	private void construct(){
		TopologicalOrderIterator<Description, DefaultEdge> orderIterator;

		//test with a reversed graph so that the smallest index will be given to the higher ancestor
		DirectedGraph<Description, DefaultEdge> reversed =
				new EdgeReversedGraph<Description, DefaultEdge>(namedDag);
		orderIterator =
				new TopologicalOrderIterator<Description, DefaultEdge>(reversed);

		//add Listener to create the indexes and ranges
		orderIterator.addTraversalListener(new IndexListener(reversed));

		System.out.println("\nIndexing:");
		while (orderIterator.hasNext()) {
			orderIterator.next();

		}
		System.out.println(indexes);
		System.out.println(ranges);

	}

	@Override
	public int getIndex(Description d) {
		return indexes.get(d);


	}

	@Override
	public List<Interval> getIntervals(Description d) {

		return ranges.get(d).getIntervals();

	}
	
	@Override
	public Map<Description, Integer> getIndexes() {
		return indexes;


	}

	@Override
	public Map<Description, SemanticIndexRange> getIntervals() {

		return ranges;

	}


}
