package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/**    Retrieve all the connection built in our DAG it provides only the NamedDescription
 * 
 * */
public class TBoxReasonerNamedImpl implements TBoxReasoner {
	
	DAGImpl dagNamed;

//		starting from a dag
		TBoxReasonerNamedImpl(DAGImpl dag){
			
		}

	@Override
	public Set<Set<Description>> getDirectChildren(Description desc) {
		return null;
//		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
//		Description node = replacements.get(desc);
//		if (node == null)
//			node = desc;
//		Set<DefaultEdge> edges = dag.incomingEdgesOf(node);
//		for (DefaultEdge edge : edges) {
//			Description source = dag.getEdgeSource(edge);
//			Set<Description> equivalences = getEquiavlences(source);
//			result.add(equivalences);
//		}
//		return Collections.unmodifiableSet(result);
		
	}

	@Override
	public Set<Set<Description>> getDirectParents(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<Description>> getDescendants(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<Description>> getAncestors(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Description> getEquivalences(Description description) {
		// TODO Auto-generated method stub
		return null;
	}



}
