package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.ClassImpl;
import it.unibz.krdb.obda.ontology.impl.PropertyImpl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/** Use to build a simple graph.
 * 
 * We probably don't need this class we can simply used DefaultDirectedGraph<V,E>
 * A directed graph multiple edges are not permitted, but loops are. 
 *
 */

public class GraphImpl extends DefaultDirectedGraph<Description,DefaultEdge> implements Graph {

	private Set<OClass> classes = new LinkedHashSet<OClass> ();

	private Set<Property> roles = new LinkedHashSet<Property> ();



	public GraphImpl(Class<? extends DefaultEdge> arg0) {
		super(arg0);

		initialize();
	}


	private void initialize(){


	}


	//return all roles in the graph
	public Set<Property> getRoles(){
		for (Description r: this.vertexSet()){
			if (r.getClass().equals(PropertyImpl.class)){
//				if(!((PropertyImpl) r).isInverse())
				roles.add((PropertyImpl)r);
			}

		}
		return roles;

	}

	
	//return all named classes in the graph
	public Set<OClass> getClasses(){
		for (Description c: this.vertexSet()){
			if (c.getClass().equals(ClassImpl.class)){
				classes.add((ClassImpl)c);
			}

		}
		return classes;

	}




}
