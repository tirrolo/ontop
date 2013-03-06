package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.Map;
import java.util.Set;

public interface DAG {



	//set the map of equivalences
	public void setMapEquivalences(Map<Description, Set<Description>> equivalences);
		
	//set the map of replacements
	public void setReplacements( Map<Description,Description> replacements);
	
	//return the map set of equivalences
	public Map<Description, Set<Description>> getMapEquivalences();
	
	//return the map set of replacements
	public Map<Description,Description> getReplacements();
	
	//return the named properties in the dag
	public Set<Property> getRoles();
	
	//return the named classed in the dag
	public Set<OClass> getClasses();
	
	





}
