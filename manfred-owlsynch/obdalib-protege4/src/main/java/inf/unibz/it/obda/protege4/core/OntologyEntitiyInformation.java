package inf.unibz.it.obda.protege4.core;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

public class OntologyEntitiyInformation {

	private OWLOntology	ontology;

	private HashSet<String>	dataProperties;

	private HashSet<String>	classesURIs;

	private HashSet<String>	objectProperties;
	
	public OntologyEntitiyInformation(OWLOntology o){
	
		ontology = o;
		updateOntologyInfo();
	}
	
	public void refresh(){
		updateOntologyInfo();
	}
	
	public boolean isDatatypeProperty(URI propertyURI) {
		return dataProperties.contains(propertyURI.toString());
	}

	public boolean isNamedConcept(URI propertyURI) {
		return classesURIs.contains(propertyURI.toString());
	}

	public boolean isObjectProperty(URI propertyURI) {
		return objectProperties.contains(propertyURI.toString());
	}
	
	
	
	private void updateOntologyInfo(){
		
		classesURIs = new HashSet<String>();
		dataProperties = new HashSet<String>();
		objectProperties = new HashSet<String>();
		
		Set<OWLClass> set = ontology.getReferencedClasses();
		Iterator<OWLClass> it = set.iterator();
		while(it.hasNext()){
			classesURIs.add(it.next().getURI().toString());
		}
		for (OWLDataProperty c: ontology.getReferencedDataProperties()) {
			dataProperties.add(c.getURI().toString());
		}
		for (OWLObjectProperty c: ontology.getReferencedObjectProperties()) {
			objectProperties.add(c.getURI().toString());
		}
	}
}
