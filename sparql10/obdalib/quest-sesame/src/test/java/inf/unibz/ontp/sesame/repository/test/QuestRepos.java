package inf.unibz.ontp.sesame.repository.test;


import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAModelSynchronizer;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.RepositoryConnection;
import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;

public class QuestRepos {

	//classic
	@Test
	public void test_inmemory() 
	{
		RepositoryConnection con = null;
		try{
		System.out.println("In-memory quest repo.");
		
		String owlfile = "src/test/resources/onto2.owl";
	
		SesameRepositoryConfig config;
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		config = (SesameRepositoryConfig) fact.getConfig();
		config.setType("quest-inmemory");
		config.setName("my_repo");
		config.setOwlFile(owlfile);
		
		
		Repository repo = fact.getRepository(config);		

		repo.initialize();
	
		con = (RepositoryConnection) repo.getConnection();
	
	System.out.println(con.toString());
		
	}catch(Exception e)
	{e.printStackTrace();}
	
	finally{
		try {
			con.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}}
	
	
	//classic
	@Test
	public void test_remote()
	{
		System.out.println("\n\n\nRemote quest repo................");
		RepositoryConnection con = null;
		try{		
		//String owlfile = "src/test/resources/onto2.owl";
	
		setupDB();
		SesameRepositoryConfig config;
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		config = (SesameRepositoryConfig) fact.getConfig();
		config.setType("quest-remote");
		config.setName("my_repo");
		config.setOwlFile(owlfile);
		
		
		Repository repo = fact.getRepository(config);		

		repo.initialize();
	
		con = (RepositoryConnection) repo.getConnection();
	
	System.out.println(con.toString());
		
	}catch(Exception e)
	{e.printStackTrace();}
	
	finally{
		try {
			con.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
	}
	
	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;
	
	final String owlfile = "../quest-owlapi3/src/test/resources/test/stockexchange-unittest.owl";
	final String obdafile = "../quest-owlapi3/src/test/resources/test/stockexchange-h2-unittest.obda";
	
	public void setupDB() throws Exception
	{
		/* * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("../quest-owlapi3/src/test/resources/test/stockexchange-create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		DataManager ioManager = new DataManager(obdaModel, new QueryController());
		ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getOntologyID().getOntologyIRI().toURI(),
				obdaModel.getPrefixManager());

		OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);

	}
	
	//virtual
	@Test
	public void test_virtual()
	{
		RepositoryConnection con = null;
		
		try{	
		System.out.println("\n\n\nVirtual quest repo.....................");
		//setupDB();
	
		SesameRepositoryConfig config;
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		config = (SesameRepositoryConfig) fact.getConfig();
		config.setType("quest-virtual");
		config.setName("my_repo");
		config.setOwlFile(owlfile);
		config.setObdaFile(obdafile);
		
		Repository repo = fact.getRepository(config);		

		repo.initialize();
	
		con = (RepositoryConnection) repo.getConnection();
	
	System.out.println(con.toString());
		
	}catch(Exception e)
	{e.printStackTrace();}
	
	finally{
		try {
			con.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
		
	}
	
	
	
	

}


