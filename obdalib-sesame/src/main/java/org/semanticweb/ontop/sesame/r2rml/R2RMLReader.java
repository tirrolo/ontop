/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.sesame.r2rml;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.openrdf.model.Graph;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.OBDASQLQuery;
import org.semanticweb.ontop.model.impl.CQIEImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;

public class R2RMLReader {
	
	private R2RMLManager manager;
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private OBDAModel obdaModel = fac.getOBDAModel();
	
	private Graph graph ;
	
	public R2RMLReader(Graph graph) {
		manager = new R2RMLManager(graph);
		this.graph = graph;
	}
	
	public R2RMLReader(String file)
	{
		this(new File(file));
	}
	
	public R2RMLReader(File file, OBDAModel model)
	{
		this(file);
		obdaModel = model;
	}
	
	public R2RMLReader(File file)
	{
		manager = new R2RMLManager(file);
		graph = manager.getGraph();
	}
	
	public void setOBDAModel(OBDAModel model)
	{
		this.obdaModel = model;
	}
		
	public OBDAModel readModel(URI sourceUri){
		try {
			//add to the model the mappings retrieved from the manager
			obdaModel.addMappings(sourceUri, manager.getMappings(graph));
		} catch (DuplicateMappingException e) {
			e.printStackTrace();
		}
		return obdaModel;
	}
	
	public ArrayList<OBDAMappingAxiom> readMappings(){
		return manager.getMappings(graph);
	}
	

	public static void main(String args[])
	{
		String file = "/Users/timi/Documents/hdd/Project/Test Cases/mapping1.ttl";
	//	"C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/D014/r2rmla.ttl";
	//"C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/D004/WRr2rmlb.ttl";
	
		R2RMLReader reader = new R2RMLReader(file);
		ArrayList<OBDAMappingAxiom> axioms = reader.readMappings();
		for (OBDAMappingAxiom ax : axioms)
			System.out.println(ax);
		
	}

}
