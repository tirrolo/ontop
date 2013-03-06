package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange.Interval;


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
	
	

	
	
	public SemanticIndexEngineImpl(DAGImpl dag) {

if (dag.isaNamedDAG())
{
	namedDag=dag;
	construct();
}

	}
	private void construct(){
		TopologicalOrderIterator<Description, DefaultEdge> orderIterator;

        orderIterator =
           new TopologicalOrderIterator<Description, DefaultEdge>(namedDag);
        
        //add Listener to create the indexes and ranges
        orderIterator.addTraversalListener(new IndexListener(namedDag));
        
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
	
	private void mergeRangeNode(Description d) {

		for (Description ch : Graphs.successorListOf(namedDag, d)) {
			if (ch != d) {
				mergeRangeNode(ch);
				ranges.get(d).addRange(ranges.get(ch));
			}

		}

		
	}

}
