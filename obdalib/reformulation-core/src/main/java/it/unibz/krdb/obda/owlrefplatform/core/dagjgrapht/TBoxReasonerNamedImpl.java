package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDescriptionDAGImpl;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/**    Retrieve all the connection built in our DAG it provides only the NamedDescription
 * 
 * */
public class TBoxReasonerNamedImpl implements TBoxReasoner {
	
	DAGImpl dag;
	private Set<OClass> namedClasses;
	private Set<Property> property;

//		starting from a dag
		TBoxReasonerNamedImpl(DAGImpl dag){
			this.dag=dag;
			
			namedClasses= dag.getClasses();
			property = dag.getRoles();
			
		}

	@Override
	public Set<Set<Description>> getDirectChildren(Description desc) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		Set<DefaultEdge> edges = dag.incomingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description source = dag.getEdgeSource(edge);
			Set<Description> equivalences = new LinkedHashSet<Description> ();
			for(Description vertex: getEquivalences(source)){
				if(namedClasses.contains(vertex) | property.contains(vertex)){
					equivalences.add(vertex);
				}
			}
			result.add(equivalences);

		}
		return Collections.unmodifiableSet(result);
		
	}

	@Override
	public Set<Set<Description>> getDirectParents(Description desc) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;
		Set<DefaultEdge> edges = dag.outgoingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description target = dag.getEdgeTarget(edge);
			Set<Description> equivalences = new LinkedHashSet<Description> ();
			for(Description vertex: getEquivalences(target)){
				if(namedClasses.contains(vertex) | property.contains(vertex)){
					equivalences.add(vertex);
				}
			}
			result.add(equivalences);
		}
		return Collections.unmodifiableSet(result);
	}

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

	@Override
	public Set<Description> getEquivalences(Description desc) {
		Set<Description> equivalents = dag.getMapEquivalences().get(desc);
		if (equivalents == null)
			return Collections.unmodifiableSet(Collections.singleton(desc));
		return Collections.unmodifiableSet(equivalents);
	}



}
