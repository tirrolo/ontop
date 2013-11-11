/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.bootstrapping;

import java.sql.SQLException;

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

public class DirectMappingBootstrapper extends AbstractDBMetadata{
	
	
	public DirectMappingBootstrapper() {
		
	}
	
	public DirectMappingBootstrapper(String baseuri, String url, String user, String password, String driver) throws Exception{
		OBDADataFactory fact = OBDADataFactoryImpl.getInstance();
		OBDADataSource source = fact.getJDBCDataSource(url, user, password, driver);
		//create empty ontology and model, add source to model
		OWLOntologyManager mng = OWLManager.createOWLOntologyManager();
		OWLOntology onto = mng.createOntology(IRI.create(baseuri));
		OBDAModel model = fact.getOBDAModel();
		model.addSource(source);
		getOntologyAndDirectMappings(baseuri, onto, model, source);
	}

	public DirectMappingBootstrapper(String baseUri, OWLOntology ontology, OBDAModel model, OBDADataSource source) throws Exception{
		getOntologyAndDirectMappings(baseUri, ontology, model, source);
	}

	/***
	 * Creates an OBDA model using direct mappings
	 */
	public OBDAModel getModel() {
		return getOBDAModel();
	}

	/***
	 * Creates an OBDA file using direct mappings. Internally this one calls the
	 * previous one and just renders the file.
	 */
	public OWLOntology getOntology() {
		return getOWLOntology();
	}

}
