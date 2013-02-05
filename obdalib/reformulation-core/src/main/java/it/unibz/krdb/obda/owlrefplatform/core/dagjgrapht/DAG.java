package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;

import java.util.Map;
import java.util.Set;

public interface DAG {


	public boolean isaDAG();

	//check if the graph is a named description dag
	public boolean isaNamedDAG();
	
	public Set<Property> getRoles();
	public Set<ClassDescription> getClasses();
	
	//set the map of equivalences
	public void setMapEquivalences(Map<Description, Set<Description>> equivalences);
		
	//set the map of replacements
	public void setReplacements( Map<Description,Description> replacements);
	
	//return the map set of equivalences
	public Map<Description, Set<Description>> getMapEquivalences();
	
	//return the map set of replacements
	public Map<Description,Description> getReplacements();
	
	





}
