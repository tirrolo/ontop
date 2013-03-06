package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.ClassImpl;
import it.unibz.krdb.obda.ontology.impl.PropertyImpl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
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
	
	private Set<OClass> classes = new LinkedHashSet<OClass> ();
	private Set<Property> roles = new LinkedHashSet<Property> ();
	
	private Map<Description, Description> replacements = new HashMap<Description, Description>();
	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	public DAGImpl(Class<? extends DefaultEdge> arg0) {
		super(arg0);
		dag=true;
	}

	public DAGImpl(EdgeFactory<Description,DefaultEdge> ef) {
		super(ef);
		dag=true;
	}
	
	//set the graph is a dag
	public void setIsaDAG(boolean d){
		
		dag=d;
		namedDAG=!d;

	}

	//set the graph is a named description dag
	public void setIsaNamedDAG(boolean nd){
		
		namedDAG=nd;
		dag=!nd;

	}
	//check if the graph is a dag
	public boolean isaDAG(){
		return dag;

	}

	//check if the graph is a named description dag
	public boolean isaNamedDAG(){
		return namedDAG;


	}
	
	//return all named roles in the dag
	public Set<Property> getRoles(){
		for (Description r: this.vertexSet()){
			
			//check in the equivalent nodes if there are properties
			if(replacements.containsValue(r)){
				for (Description e: equivalencesMap.get(r))	{
					if (e.getClass().equals(PropertyImpl.class)){
						System.out.println("roles: "+ e +" "+ e.getClass());
						if(!((PropertyImpl) e).isInverse())
						roles.add((PropertyImpl)e);
						
				}
				}
			}
			if (r.getClass().equals(PropertyImpl.class)){
				System.out.println("roles: "+ r +" "+ r.getClass());
				if(!((PropertyImpl) r).isInverse())
				roles.add((PropertyImpl)r);
			}

		}
		return roles;

	}

	
	//return all named classes in the dag
	public Set<OClass> getClasses(){
		for (Description c: this.vertexSet()){
			
			//check in the equivalent nodes if there are named classes
			if(replacements.containsValue(c)){
				for (Description e: equivalencesMap.get(c))	{
					if (e.getClass().equals(ClassImpl.class)){
						System.out.println("classes: "+ e +" "+ e.getClass());
						classes.add((ClassImpl)e);
				}
				}
			}
			
			if (c.getClass().equals(ClassImpl.class)){
				System.out.println("classes: "+ c+ " "+ c.getClass());
				classes.add((ClassImpl)c);
			}

		}
		return classes;

	}


	@Override
	public Map<Description, Set<Description>> getMapEquivalences() {
		
		return equivalencesMap;
	}

	@Override
	public Map<Description, Description> getReplacements() {
		return replacements;
	}

	@Override
	public void setMapEquivalences(Map<Description, Set<Description>> equivalences) {
		this.equivalencesMap= equivalences;
		
	}

	@Override
	public void setReplacements(Map<Description, Description> replacements) {
		this.replacements=replacements;
		
	}




}
