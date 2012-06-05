package it.unibz.krdb.obda.api.io;

import it.unibz.krdb.obda.io.IOManager;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.parser.TurtleSyntaxParser;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

public class IOManagerTest extends TestCase {
	
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	
	private OBDAModel model;
	
	private IOManager ioManager;
	
	private TurtleSyntaxParser parser;
	
	private String[][] mappings = {
		{"M1", "select id, fname, lname, age from student", "<\"&:;P{$id}\"> a :Student; :firstName $fname; :lastName $lname; :age $age^^xsd:int ."},
		{"M2", "select id, title, lecturer, description from course", "<\"&:;C{$id}\"> a :Course; :title $title; :lecturer $lecturer; :description $description@en-US ."},
		{"M3", "select sid, cid from enrollment", "<\"&:;P{$sid}\"> :hasEnrollment <\"&:;C{$cid}\"> ."},
		
		{"M4", "select id, nome, cognome, eta from studenti", "<\"&:;P{$id}\"> a :Student; :firstName $nome; :lastName $cognome; :age $eta^^xsd:int ."},
		{"M5", "select id, titolo, professore, descrizione from corso", "<\"&:;C{$id}\"> a :Course; :title $titolo; :lecturer $professore; :description $decrizione@it ."},
		{"M6", "select sid, cid from registrare", "<\"&:;P{$sid}\"> :hasEnrollment <\"&:;C{$cid}\"> ."},
	};
	
	@Override
	public void setUp() throws Exception {
		// Setting up the prefixes
		PrefixManager prefixManager = new SimplePrefixManager();
		prefixManager.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://www.example.org/test#");
		prefixManager.addPrefix("quest:", "http://obda.org/quest#");
		prefixManager.addPrefix("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixManager.addPrefix("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
		prefixManager.addPrefix("owl:", "http://www.w3.org/2002/07/owl#");	
		prefixManager.addPrefix("xsd:", "http://www.w3.org/2001/XMLSchema#");
		
		// Setting up the data source
		URI sourceId = URI.create("http://www.example.org/db/dummy/");
		OBDADataSource datasource = dfac.getDataSource(sourceId);
		datasource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, "jdbc:postgresql://www.example.org/dummy");
		datasource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, "dummy");
		datasource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, "dummy");
		datasource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, "org.postgresl.Driver");
		
		// Setting up the CQ parser
		parser = new TurtleSyntaxParser(prefixManager);
		
		// Construct the model
		model = dfac.getOBDAModel();		
		model.setPrefixManager(prefixManager);
		model.addSource(datasource);
		
		// Add some mappings		
		model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[0][0], mappings[0][1], parser.parse(mappings[0][2])));
		model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[1][0], mappings[1][1], parser.parse(mappings[1][2])));
		model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[2][0], mappings[2][1], parser.parse(mappings[2][2])));
	}
	
	/*
	 * Test saving to a file
	 */
	
	public void testBlankSave() throws IOException {
		model.reset(); // clear the model
		ioManager = new IOManager(model);
		ioManager.save("src/test/java/it/unibz/krdb/obda/api/io/TestBlank.obda");
	}
	
	public void testSave() throws IOException {
		ioManager = new IOManager(model);
		ioManager.save("src/test/java/it/unibz/krdb/obda/api/io/TestSave.obda");
	}
	
	public void testMultipleDatasourcesSave() throws Exception {
		
		// Setting up the data source
		URI sourceId2 = URI.create("http://www.example.org/db/dummy2/");
		OBDADataSource datasource2 = dfac.getDataSource(sourceId2);
		datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, "jdbc:postgresql://www.example.org/dummy2");
		datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, "dummy2");
		datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, "dummy2");
		datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, "org.postgresl.Driver");
		
		// Add another data source
		model.addSource(datasource2);
		
		// Add some mappings		
		model.addMapping(sourceId2, dfac.getRDBMSMappingAxiom(mappings[3][0], mappings[3][1], parser.parse(mappings[3][2])));
		model.addMapping(sourceId2, dfac.getRDBMSMappingAxiom(mappings[4][0], mappings[4][1], parser.parse(mappings[4][2])));
		model.addMapping(sourceId2, dfac.getRDBMSMappingAxiom(mappings[5][0], mappings[5][1], parser.parse(mappings[5][2])));

		// Save the model
		ioManager = new IOManager(model);
		ioManager.save("src/test/java/it/unibz/krdb/obda/api/io/TestSaveMultipleSources.obda");
	}
	
	/*
	 * Test loading the file
	 */
	
	public void testBlankLoad() throws IOException {
		OBDAModel emptyModel = dfac.getOBDAModel();		
		ioManager = new IOManager(emptyModel);
		ioManager.load("src/test/java/it/unibz/krdb/obda/api/io/TestBlank.obda");

		// Everything should be blank
		assertTrue(emptyModel.getPrefixManager().getPrefixMap().size() == 0);
		assertTrue(emptyModel.getSources().size() == 0);
		assertTrue(countElement(emptyModel.getMappings()) == 0);
	}

	public void testLoad() throws IOException {
		OBDAModel emptyModel = dfac.getOBDAModel();
		ioManager = new IOManager(emptyModel);
		ioManager.load("src/test/java/it/unibz/krdb/obda/api/io/TestSave.obda");
		
		// Check the content
		assertTrue(emptyModel.getPrefixManager().getPrefixMap().size() == 6);
		assertTrue(emptyModel.getSources().size() == 1);
		assertTrue(countElement(emptyModel.getMappings()) == 3);
	}
	
	public void testMultipleDatasourcesLoad() throws IOException {
		OBDAModel emptyModel = dfac.getOBDAModel();
		ioManager = new IOManager(emptyModel);
		ioManager.load("src/test/java/it/unibz/krdb/obda/api/io/TestSaveMultipleSources.obda");
		
		// Check the content
		assertTrue(emptyModel.getPrefixManager().getPrefixMap().size() == 6);
		assertTrue(emptyModel.getSources().size() == 2);
		assertTrue(countElement(emptyModel.getMappings()) == 6);
	}
	
	private int countElement(Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings) {
		int total = 0;
		for (List<OBDAMappingAxiom> list : mappings.values()) {
		    total += list.size();
		}
		return total;
	}
}
