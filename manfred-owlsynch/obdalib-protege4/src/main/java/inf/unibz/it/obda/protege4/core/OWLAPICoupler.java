package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.find.EntityFinder;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owl.util.OWLOntologyMerger;

/***
 * 
 * Interacts with OWLAPI objects to inspect the original ontology. Note that
 * this coupler only works fine if the active ontology is setup properly so that
 * the EntityFinder finds the classes.
 * 
 * TODO Needs to be refactored to handle URI's properly. In general, the whole
 * OBDA API needs to be refactored like this.
 * 
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OWLAPICoupler implements APICoupler {

	// OWLModelManager owlman = null;
//	EntityFinder			finder	= null;
	private APIController	apic;

	// private OWLModelManager mmgr;
	
	private OWLOntologyManager mmgr = null;
	
	private OWLOntology merged = null;

	private OWLOntology	mergedOntology;

	private HashSet<String>	dataProperties;

	private HashSet<String>	classesURIs;

	private HashSet<String>	objectProperties;
	
	

	public OWLAPICoupler(APIController apic, OWLOntologyManager mmgr, OWLOntology root) {
		// this.mmgr = manager;
		this.apic = apic;
		this.mmgr = mmgr;
		
//		synchWithOntology(root);
	}

	public void synchWithOntology(OWLOntology root){
		mergedOntology = root;
		
		classesURIs = new HashSet<String>();
		dataProperties = new HashSet<String>();
		objectProperties = new HashSet<String>();
		
		Set<OWLClass> set = mergedOntology.getReferencedClasses();
		Iterator<OWLClass> it = set.iterator();
		while(it.hasNext()){
			classesURIs.add(it.next().getURI().toString());
		}
		for (OWLDataProperty c: mergedOntology.getReferencedDataProperties()) {
			dataProperties.add(c.getURI().toString());
		}
		for (OWLObjectProperty c: mergedOntology.getReferencedObjectProperties()) {
			objectProperties.add(c.getURI().toString());
		}
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

	public OWLOntologyManager getOWLOntologyManager(){
		return mmgr;
	}
}
