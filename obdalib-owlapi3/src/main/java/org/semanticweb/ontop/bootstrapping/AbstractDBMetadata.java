/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.bootstrapping;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.semanticweb.ontop.directmapping.DirectMappingEngine;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.JDBCConnectionManager;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractDBMetadata
{
	
	private OWLOntology onto;
	private OBDAModel model;
	private OBDADataSource source;
	
	protected DBMetadata getMetadata() 
	{
		DBMetadata metadata = null;
		try {
			Class.forName(source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
		}
		catch (ClassNotFoundException e) { /* NO-OP */ }

		try {
			Connection conn = DriverManager.getConnection(source.getParameter(RDBMSourceParameterConstants.DATABASE_URL),
					source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME), source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD));
			metadata = JDBCConnectionManager.getMetaData(conn);
		
		} catch (SQLException e) { 
			e.printStackTrace();
		}
		return metadata;
	}
	
	protected void getOntologyAndDirectMappings(String baseuri, OWLOntology onto, OBDAModel model, OBDADataSource source) throws Exception {
		this.source = source;	
		DirectMappingEngine engine = new DirectMappingEngine(baseuri, model.getMappings(source.getSourceID()).size());
		this.model =  engine.extractMappings(model, source);
		this.onto =  engine.getOntology(onto, onto.getOWLOntologyManager(), model);
	}
	
	protected OBDAModel getOBDAModel()
	{
		return this.model;
	}
	
	protected OWLOntology getOWLOntology()
	{
		return this.onto;
	}
	
}
