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
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		if(getDirectChildren(desc).isEmpty())
			return result;
		else{
			result.addAll(getDirectChildren(desc));
			for (Set<Description> child : getDirectChildren(desc)){

				result.addAll(getDescendants(child.iterator().next()));
			}


			return result;
		}
	}	


	/* recursive function 
	return the ancestors starting from the given node of the dag
	we don't consider as ancestors the equivalent nodes
	 */
	@Override
	public Set<Set<Description>> getAncestors(Description desc) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		if(getDirectParents(desc).isEmpty())
			return result;
		else{
			result.addAll(getDirectParents(desc));
			for (Set<Description> child : getDirectParents(desc)){

				result.addAll(getAncestors(child.iterator().next()));
			}


			return result;
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
