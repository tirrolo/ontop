/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package sesameWrapper.example;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.sesame.r2rml.OBDAMappingTransformer;
import it.unibz.krdb.obda.sesame.r2rml.R2RMLReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.turtle.TurtleWriter;



import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;


public class R2RMLToOBDAToR2RMLWriter {
	
	private BufferedWriter out;
	private List<OBDAMappingAxiom> mappings;
	private URI sourceUri;
	private PrefixManager prefixmng;
	
	public R2RMLToOBDAToR2RMLWriter(File file, OBDAModel obdamodel, URI sourceURI)
	{
		try {
			this.out = new BufferedWriter(new FileWriter(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sourceUri = sourceURI;
		this.mappings = obdamodel.getMappings(sourceUri);
		this.prefixmng = obdamodel.getPrefixManager(); 
	}
	
	public R2RMLToOBDAToR2RMLWriter(OBDAModel obdamodel, URI sourceURI)
	{
		this.sourceUri = sourceURI;	
		this.mappings = obdamodel.getMappings(sourceUri);
		this.prefixmng = obdamodel.getPrefixManager(); 
	}

	/**
	 * call this method if you need the RDF Graph
	 * that represents the R2RML mappings
	 * @return an RDF Graph
	 */
	public Graph getGraph() {
		OBDAMappingTransformer transformer = new OBDAMappingTransformer();
		List<Statement> statements = new ArrayList<Statement>();
		
		for (OBDAMappingAxiom axiom: this.mappings) {
			List<Statement> statements2 = transformer.getStatements(axiom,prefixmng);
			statements.addAll(statements2);
		}
		@SuppressWarnings("deprecation")
		Graph g = new GraphImpl(); 
		g.addAll(statements);
		return g;
	}
	
	/**
	 * the method to write the R2RML mappings
	 * from an rdf Graph to a file
	 * @param file the ttl file to write to
	 */
	public void write(File file)
	{
		try {
			//retrieve rdf graph to write
			Graph result = getGraph();
			//open output stream
			this.out = new BufferedWriter(new FileWriter(file));
			//set up turtle writer
			TurtleWriter writer =  new TurtleWriter(this.out);
			writer.startRDF();
			//handle namespaces
			Map<String, String> prefixes = this.prefixmng.getPrefixMap();
			for (String pref : prefixes.keySet()) {
				writer.handleNamespace(pref, prefixes.get(pref));
			}
			//write graph statements
			Iterator<Statement> stIterator = result.iterator();
			while (stIterator.hasNext()) {
				writer.handleStatement(stIterator.next());
			}
			writer.endRDF();
			//close output stream
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String args[]) throws OWLOntologyCreationException, IOException, InvalidPredicateDeclarationException, InvalidMappingException
	{
		String owlfile = "src/main/resources/example/npd-ql.owl";
		String outFile= "src/main/resources/example/mapping1out-2.ttl";

		String file = "src/main/resources/example/mapping1out.ttl";
		R2RMLReader reader = new R2RMLReader(file);
		URI uri = URI.create("blah");
		
		OBDAModel obdaModel =  reader.readModel(uri);
		
		System.out.println();
		for(OBDAMappingAxiom mapping : obdaModel.getMappings(uri)){
			System.out.println(mapping);
		}
		System.out.println();
		
		R2RMLToOBDAToR2RMLWriter writer = new R2RMLToOBDAToR2RMLWriter(obdaModel,uri);
		
		
		File out = null;
		try{
			 out = new File(outFile);
		}catch (Exception e){
			System.out.println("Problems creating the file");
		}
		
		
		Graph g = writer.getGraph();
		Iterator<Statement> st = g.iterator();
		int i = 0;
		
		while (st.hasNext()){
			System.out.println(st.next());
			i++;
		}
		System.out.println("\n We have printed #Mappings: "+i);
		writer.write(out);
		
	}
}
