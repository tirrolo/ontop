package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/** Build a DAG with only the named descriptions*/

public class NamedDescriptionDAGImpl implements NamedDescriptionDAG {

	private Set<OClass> namedClasses;
	private Set<Property> property;
	
	private Map<Description, Set<Description>> equivalencesMap;

	private Map<Description, Description> replacements;
	DAGImpl namedDag;

	public NamedDescriptionDAGImpl(DAG dag) {

		
		namedDag=  (DAGImpl) ((DAGImpl) dag).clone();
		
		//take classes, roles, equivalencesmap and replacements from the DAG
		namedClasses= namedDag.getClasses();
		property = namedDag.getRoles();
		equivalencesMap= namedDag.getMapEquivalences();
		replacements = namedDag.getReplacements();
				
		for( Description vertex: ((DAGImpl) dag).vertexSet()){
				//if the node is named keep it
		if(namedClasses.contains(vertex) | property.contains(vertex)){
			
			continue;
		}
		
		Description reference = replacements.get(vertex);
		
		//if it's not a representative node, delete it
		if(reference != null){
			
			namedDag.removeVertex(vertex);						
			
			//delete from replacements
			replacements.remove(vertex);
			
			//delete from equivalencesMap
			Set<Description> equivalences = equivalencesMap.get(reference);
			equivalences.remove(vertex);
			equivalencesMap.put(reference, equivalences);
			for(Description e : equivalences){
				equivalencesMap.put(e, equivalences);
			}
			
		}
		//if the node is not named and it's representative delete it and repoint all links
		else{

			//delete from equivalencesMap and assign a new representative node
			Set<Description> equivalences =equivalencesMap.get(vertex);
	
			if(equivalences!=null){
			equivalencesMap.remove(vertex);
			
			equivalences.remove(vertex);
			
			//change the representative node
			Iterator<Description> e=equivalences.iterator();
			Description newReference=  e.next();
			replacements.remove(newReference);
			if(equivalences.size()>1)
			equivalencesMap.put(newReference, equivalences);
			
			while(e.hasNext()){
				Description node =e.next();
				replacements.put(node, newReference);
				equivalencesMap.put(node, equivalences);
			}
			
			/*
			 * Re-pointing all links to and from the eliminated node to the new
			 * representative node
			 */
			reference=newReference;
			
			Set<DefaultEdge> edges = new HashSet<DefaultEdge>(namedDag.incomingEdgesOf(vertex));
			for (DefaultEdge incEdge : edges) {
				Description source = namedDag.getEdgeSource(incEdge);
				namedDag.removeAllEdges(source, vertex);
				
				if (source.equals(reference))
					continue;
				
				namedDag.addEdge(source, reference);
			}

			edges = new HashSet<DefaultEdge>(namedDag.outgoingEdgesOf(vertex));
			for (DefaultEdge outEdge : edges) {
				Description target = namedDag.getEdgeTarget(outEdge);
				namedDag.removeAllEdges(vertex, target);
				
				if (target.equals(reference))
					continue;
				namedDag.addEdge(reference, target);
			}
			
			namedDag.removeVertex(vertex);
			}
			
			else{
			//add edge between the first of the ancestor that it's still present and its child
			
			Set<DefaultEdge> edges = new HashSet<DefaultEdge>(namedDag.incomingEdgesOf(vertex));
			
			//I do a copy of the dag not to remove edges that I still need to consider in the loops
			DAGImpl copyDAG=(DAGImpl) namedDag.clone();
			for (DefaultEdge incEdge : edges) {
				
				Description source = namedDag.getEdgeSource(incEdge);
				namedDag.removeAllEdges(source, vertex);
				
				edges = new HashSet<DefaultEdge>(copyDAG.outgoingEdgesOf(vertex));
				for (DefaultEdge outEdge : edges) {
					Description target = copyDAG.getEdgeTarget(outEdge);
					namedDag.removeAllEdges(vertex, target);
				
					namedDag.addEdge(source, target);
				}
				
				
			}
			
			namedDag.removeVertex(vertex);
			}
			
			

			}
			}
					
		
		namedDag.setMapEquivalences(equivalencesMap);
		namedDag.setReplacements(replacements);
		namedDag.setIsaNamedDAG(true);
		
	}
	
	@Override
	public DAGImpl getDAG() {
		return namedDag;
	}

}
