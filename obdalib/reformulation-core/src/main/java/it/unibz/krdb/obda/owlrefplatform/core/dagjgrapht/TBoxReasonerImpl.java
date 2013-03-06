package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;

import it.unibz.krdb.obda.ontology.Ontology;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/**
 *  Retrieve all the connection built in our DAG 
 * 
 *
 */

public class TBoxReasonerImpl implements TBoxReasoner{

	DAGImpl dag;


	public TBoxReasonerImpl(DAGImpl dag){
		this.dag=dag;


	}

	//return the direct children starting from the given node of the dag
	@Override
	public Set<Set<Description>> getDirectChildren(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;
		System.out.println("node "+node +"dag "+ dag);
		Set<DefaultEdge> edges = dag.incomingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description source = dag.getEdgeSource(edge);
			Set<Description> equivalences = getEquivalences(source);
			result.add(equivalences);
		}
		return Collections.unmodifiableSet(result);
	}

	//return the direct parents starting from the given node of the dag
	@Override
	public Set<Set<Description>> getDirectParents(Description desc) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;
		Set<DefaultEdge> edges = dag.outgoingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description target = dag.getEdgeTarget(edge);
			Set<Description> equivalences = getEquivalences(target);
			result.add(equivalences);
		}
		return Collections.unmodifiableSet(result);
	}

	/*recursive function 
	return the descendants starting from the given node of the dag
	we don't consider as descendants the equivalent nodes
	 */
	@Override
	public Set<Set<Description>> getDescendants(Description desc) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Set<Set<Description>> children;
		children= getDirectChildren(desc);
		if(children.isEmpty())
			return result;
		else{
			result.addAll(children);
			for (Set<Description> child : children){

				result.addAll(getDescendants(child.iterator().next()));
			}


			return Collections.unmodifiableSet(result);
		}
	}	


	/* recursive function 
	return the ancestors starting from the given node of the dag
	we don't consider as ancestors the equivalent nodes
	 */
	@Override
	public Set<Set<Description>> getAncestors(Description desc) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Set<Set<Description>> parents;
		parents=getDirectParents(desc);
		if(parents.isEmpty())
			return result;
		else{
			result.addAll(parents);
			for (Set<Description> child : parents){

				result.addAll(getAncestors(child.iterator().next()));
			}


			return Collections.unmodifiableSet(result);
		}
	}

	//return the equivalences starting from the given node of the dag
	@Override
	public Set<Description> getEquivalences(Description desc) {
		Set<Description> equivalents = dag.getMapEquivalences().get(desc);
		if (equivalents == null)
			return Collections.unmodifiableSet(Collections.singleton(desc));
		return Collections.unmodifiableSet(equivalents);
	}



}
