package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import java.util.LinkedHashSet;
import java.util.Set;

import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.ClassImpl;
import it.unibz.krdb.obda.ontology.impl.PropertyImpl;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/** Use to build a DAG and a named DAG.
 * 
 * We probably don't need this class we can simply used SimpleDirectedGraph<V,E> A directed graph. 
 * @author Sarah
 *
 */

public class DAGImpl extends SimpleDirectedGraph <Description,DefaultEdge> implements DAG {

	boolean dag = false;
	boolean namedDAG = false;
	
	private Set<ClassDescription> classes = new LinkedHashSet<ClassDescription> ();
	private Set<Property> roles = new LinkedHashSet<Property> ();

	public DAGImpl(Class<? extends DefaultEdge> arg0) {
		super(arg0);
		dag=true;
	}

	public DAGImpl(EdgeFactory<Description,DefaultEdge> ef) {
		super(ef);
		dag=true;
	}
	
	//check if the graph is a dag
	public boolean isaDAG(){
		return dag;

	}

	//check if the graph is a named description dag
	public boolean isaNamedDAG(){
		return namedDAG;


	}
	
	//return all roles in the graph
	public Set<Property> getRoles(){
		for (Description r: this.vertexSet()){
			if (r.getClass().equals(PropertyImpl.class)){
				roles.add((Property)r);
			}

		}
		return roles;

	}

	
	//return all classes in the graph
	public Set<ClassDescription> getClasses(){
		for (Description c: this.vertexSet()){
			if (c.getClass().equals(ClassImpl.class)){
				classes.add((ClassDescription)c);
			}

		}
		return classes;

	}




}
