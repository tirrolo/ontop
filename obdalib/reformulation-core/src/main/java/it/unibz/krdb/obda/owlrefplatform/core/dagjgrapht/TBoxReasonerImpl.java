package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import it.unibz.krdb.obda.ontology.Ontology;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 *  Retrieve all the connection built in our DAG 
 * 
 *
 */

public class TBoxReasonerImpl implements TBoxReasoner{

	DAGImpl dag;
	GraphImpl graph;
	AbstractGraphIterator<Description, DefaultEdge> iterator;
	
	private Set<OClass> namedClasses;
	private Set<Property> property;
	


	public TBoxReasonerImpl(DAGImpl dag){
		this.dag=dag;
		namedClasses= dag.getClasses();
		property = dag.getRoles();


	}
	
	//reasoner on the graph (cycles admitted)
	public TBoxReasonerImpl(GraphImpl graph){
		this.graph=graph;
		namedClasses= graph.getClasses();
		property = graph.getRoles();
		


	}

	/**return the direct children starting from the given node of the dag
	 *  @param named when it's true only the children that correspond to named classes and property
	 *   are returned
	 *  @result we return a set of set of description to distinguish between different nodes and equivalent nodes. 
	 *  equivalent nodes will be in the same set of description
	 */
	@Override
	public Set<Set<Description>> getDirectChildren(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){ //direct children over a dag
		
			//take the representative node
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		Set<DefaultEdge> edges = dag.incomingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description source = dag.getEdgeSource(edge);
			
			//get the child node and its equivalent nodes
			Set<Description> equivalences =getEquivalences(source,named);

			
			if (!equivalences.isEmpty())
			result.add(equivalences);
			
		}
		
		}
		else //direct children over a graph
		{

			//get equivalences of the current node
			Set<Description> equivalenceSet= getEquivalences(desc, false);
			Set<DefaultEdge> edges = graph.incomingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description source = graph.getEdgeSource(edge);
				
				//I don't want to consider as children the equivalent node of the current node desc
				if(equivalenceSet.contains(source)){
					continue;
				}
				Set<Description> equivalences =getEquivalences(source,named);

				
				if (!equivalences.isEmpty())
				result.add(equivalences);
			}
			
			 //I want to consider the children of the equivalent nodes
			for (Description e: equivalenceSet){
				if(!e.equals(desc))
				{
					Set<DefaultEdge> edgesEquivalentNode = graph.incomingEdgesOf(e);
					for (DefaultEdge edge : edgesEquivalentNode) {
						Description source = graph.getEdgeSource(edge);
						
						//I don't want to consider as children the equivalent node of the current node desc
						if(equivalenceSet.contains(source)){
							continue;
						}
						Set<Description> equivalences =getEquivalences(source,named);

						
						if (!equivalences.isEmpty())
						result.add(equivalences);
				}
				}
					
			}
			
		}
		return Collections.unmodifiableSet(result);
	}

	/**return the direct parents starting from the given node of the dag
	 *  @param named when it's true only the parents that correspond to named classes or property
	 *  are returned 
	 * */

	@Override
	public Set<Set<Description>> getDirectParents(Description desc, boolean named) {
		
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){ //direct children over a dag
		
			//take the representative node
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		Set<DefaultEdge> edges = dag.outgoingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description target = dag.getEdgeTarget(edge);
			
			//get the child node and its equivalent nodes
			Set<Description> equivalences =getEquivalences(target,named);

			
			if (!equivalences.isEmpty())
			result.add(equivalences);
			
		}
		
		}
		else //direct children over a graph
		{

			//get equivalences of the current node
			Set<Description> equivalenceSet= getEquivalences(desc, false);
			Set<DefaultEdge> edges = graph.outgoingEdgesOf(desc);
			for (DefaultEdge edge : edges) {
				Description target = graph.getEdgeTarget(edge);
				
				//I don't want to consider as parent the equivalent node of the current node desc 
				if(equivalenceSet.contains(target)){
				
					continue;
				}
				Set<Description> equivalences =getEquivalences(target,named);

				
				if (!equivalences.isEmpty())
				result.add(equivalences);
			}
			
			 //I want to consider the parent of the equivalent nodes
			for (Description e: equivalenceSet){
				if(!e.equals(desc))
				{
					Set<DefaultEdge> edgesEquivalentNode = graph.outgoingEdgesOf(e);
					for (DefaultEdge edge : edgesEquivalentNode) {
						Description target = graph.getEdgeTarget(edge);
						
						//I don't want to consider as parent the equivalent node of the current node desc
						if(equivalenceSet.contains(target)){
							continue;
						}
						Set<Description> equivalences =getEquivalences(target,named);

						
						if (!equivalences.isEmpty())
						result.add(equivalences);
				}
				}
					
			}
		}
		return Collections.unmodifiableSet(result);

	}

	/**recursive function 
	return the descendants starting from the given node of the dag
	 @param named when it's true only the descendants that are named classes or property 
	 are returned
	 */
	@Override
	public Set<Set<Description>> getDescendants(Description desc, boolean named){
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){
			Description node = dag.getReplacements().get(desc);
			if (node == null)
				node = desc;
			//reverse the dag
			 DirectedGraph<Description, DefaultEdge> reversed =
			            new EdgeReversedGraph<Description, DefaultEdge>(dag);
			 
			iterator= new BreadthFirstIterator<Description, DefaultEdge>(reversed, node);
			
			//I don't want to consider the current node
			iterator.next();
			
 
			//iterate over the subsequent nodes, they are all descendant of desc
			while(iterator.hasNext()){
				Description child=iterator.next();
					
				//add the node and its equivalent nodes		
				
				Set<Description> sources =getEquivalences(child, named);
				
				if(!sources.isEmpty())
				result.add(sources);
				
				
			}
		}
		else{
		//reverse the graph
		 DirectedGraph<Description, DefaultEdge> reversed =
		            new EdgeReversedGraph<Description, DefaultEdge>(graph);
		 
		iterator= new BreadthFirstIterator<Description, DefaultEdge>(reversed, desc);
		
		//I don't want to consider the current node
		Description current=iterator.next();
		
		//get equivalences of the current node
		Set<Description> equivalenceSet= getEquivalences(current, named);
		//iterate over the subsequent nodes, they are all descendant of desc
		while(iterator.hasNext()){
			Description node=iterator.next();

			//I don't want to add between the descendants a node equivalent to the starting node
					if(equivalenceSet.contains(node))
						continue;
				
					
			if(named){ //add only the named classes and property
				if(namedClasses.contains(node) | property.contains(node)){
				Set<Description> sources = new HashSet<Description>();
				sources.add(node);
				
				result.add(sources);
				}
			}
			else{
			Set<Description> sources = new HashSet<Description>();
			sources.add(node);
			
			result.add(sources);
			}
			
		}
		
		}
		
		
		//add each of them to the result
		return Collections.unmodifiableSet(result);
		
	}


	/** recursive function 
	return the ancestors starting from the given node of the dag
	 @param named when it's true only the ancestors that are named classes or property 
	 are returned
	 */
	@Override
	public Set<Set<Description>> getAncestors(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		
		if(dag!=null){
			Description node = dag.getReplacements().get(desc);
			if (node == null)
				node = desc;
			 
			iterator= new BreadthFirstIterator<Description, DefaultEdge>(dag, node);
			
			//I don't want to consider the current node
			iterator.next();
			
 
			//iterate over the subsequent nodes, they are all ancestor of desc
			while(iterator.hasNext()){
				Description parent=iterator.next();
					
				//add the node and its equivalent nodes		
				
				Set<Description> sources =getEquivalences(parent, named);
				
				if(!sources.isEmpty())
				result.add(sources);
				
				
			}
		}
		else{

		 
		iterator= new BreadthFirstIterator<Description, DefaultEdge>(graph, desc);
		
		//I don't want to consider the current node
		Description current=iterator.next();
		
		//get equivalences of the current node
		Set<Description> equivalenceSet= getEquivalences(current, named);
		//iterate over the subsequent nodes, they are all ancestor of desc
		while(iterator.hasNext()){
			Description node=iterator.next();

			//I don't want to add between the ancestors a node equivalent to the starting node
					if(equivalenceSet.contains(node))
						continue;
				
					
			if(named){ //add only the named classes and property
				if(namedClasses.contains(node) | property.contains(node)){
				Set<Description> sources = new HashSet<Description>();
				sources.add(node);
				
				result.add(sources);
				}
			}
			else{
			Set<Description> sources = new HashSet<Description>();
			sources.add(node);
			
			result.add(sources);
			}
			
		}
		
		}
		
		
		//add each of them to the result
		return Collections.unmodifiableSet(result);
		

	}

	/**return the equivalences starting from the given node of the dag
	 *  @param named when it's true only the equivalences that are named classes or property 
	 are returned
	 */
	@Override
	public  Set<Description> getEquivalences(Description desc, boolean named) {
		//equivalences over a dag
		if(dag!= null){
		Set<Description> equivalents = dag.getMapEquivalences().get(desc);
		
		//if there are no equivalent nodes return the node or nothing
		if (equivalents == null ){
			
			if (named){
			if(namedClasses.contains(desc) | property.contains(desc)){
			return Collections.unmodifiableSet(Collections.singleton(desc));
			}
			else{ //return empty set if the node we are considering (desc) is not a named class or propertu
				Set<Description> equivalences = Collections.emptySet();
				return equivalences;
			}
			}
			return Collections.unmodifiableSet(Collections.singleton(desc));
		}
		Set<Description> equivalences = new LinkedHashSet<Description> ();
		if (named){
			for(Description vertex: equivalents){
				if(namedClasses.contains(vertex) | property.contains(vertex)){
					equivalences.add(vertex);
				}
			}
		}
		else{
			equivalences = equivalents;
		}
		return Collections.unmodifiableSet(equivalences);
		}
		//if equivalences over a graph
		else{

		//search for cycles 
		StrongConnectivityInspector<Description, DefaultEdge> inspector = new StrongConnectivityInspector<Description, DefaultEdge>(graph);
		
		//each set contains vertices which together form a strongly connected component within the given graph
		List<Set<Description>> equivalenceSets = inspector.stronglyConnectedSets();

		Set<Description> equivalences = new LinkedHashSet<Description> ();
			//I want to find the equivalent node of desc
			for (Set<Description> equivalenceSet : equivalenceSets) {
				if (equivalenceSet.size() >= 2){
					if(equivalenceSet.contains(desc)){
						if (named){
							for(Description vertex: equivalenceSet){
								if(namedClasses.contains(vertex) | property.contains(vertex)){
									equivalences.add(vertex);
								}
							}
							return Collections.unmodifiableSet(equivalences);
						}
					
						return Collections.unmodifiableSet(equivalenceSet);
					}
						
				}
					
			}
			
			//if there are not equivalent node return the node or nothing
			if (named){
				if(namedClasses.contains(desc) | property.contains(desc)){
				return Collections.unmodifiableSet(Collections.singleton(desc));
				}
				else{ //return empty set if the node we are considering (desc) is not a named class or propertu
					equivalences = Collections.emptySet();
					return equivalences;
				}
			}
			return Collections.unmodifiableSet(Collections.singleton(desc));
		
		
			
		}
	}

	public Set<Set <Description>> getNodes(){
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		if(dag!=null){
		for (Description vertex: dag.vertexSet()){
			result.add(getEquivalences(vertex,false));
		}
		}
		else{
			for (Description vertex: graph.vertexSet()){
				result.add(getEquivalences(vertex,false));
			}
			}
		
		return result;
		
	}

	@Override
	public DAGImpl getDAG() {

		return dag;
	}



}
