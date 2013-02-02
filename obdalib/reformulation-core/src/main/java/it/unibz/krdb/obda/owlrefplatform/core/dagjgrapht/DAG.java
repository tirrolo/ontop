package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Property;

import java.util.Set;

public interface DAG {


	public boolean isaDAG();

	//check if the graph is a named description dag
	public boolean isaNamedDAG();
	
	public Set<Property> getRoles();
	public Set<ClassDescription> getClasses();





}
