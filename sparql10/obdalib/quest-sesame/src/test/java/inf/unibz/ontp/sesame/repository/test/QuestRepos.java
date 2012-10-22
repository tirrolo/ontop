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
import java.util.Set;

import org.junit.Test;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.RepositoryConnection;
import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;
import sesameWrapper.StartJetty;

public class QuestRepos {

	//classic
	//@Test
	public void test_inmemory() 
	{
		RepositoryConnection con = null;
		try{
		System.out.println("In-memory quest repo.");
		
		String owlfile = "src/test/resources/onto2.owl";
	
		SesameRepositoryConfig config;
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		config = (SesameRepositoryConfig) fact.getConfig();
		config.setQuestType("quest-inmemory");
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
	//@Test
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
		config.setQuestType("quest-remote");
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
		String url = "jdbc:h2:tcp://localhost/quest";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		/*FileReader reader = new FileReader("../quest-owlapi3/src/test/resources/test/stockexchange-create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();
*/
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
	//@Test
	public void test_virtual()
	{
		RepositoryConnection con = null;
		
		try{	
		System.out.println("\n\n\nVirtual quest repo.....................");
		setupDB();
	
		
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		RepositoryRegistry.getInstance().add(fact);
		SesameRepositoryConfig config = new SesameRepositoryConfig();
		config.setQuestType("quest-virtual");
		config.setName("my_repo");
		config.setOwlFile(owlfile);
		config.setObdaFile(obdafile);
		
		Repository repo = fact.getRepository(config);		

		repo.initialize();
	
		con = (RepositoryConnection) repo.getConnection();
		
		
	      String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
	      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
	      TupleQueryResult result = tupleQuery.evaluate();
	      System.out.println("RESULT hasdata: "+result.hasNext());
	    	  while(result.hasNext())
	        System.out.println(result.next());
	    	 
	  con.close();
	  repo.shutDown();
	
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
	
	@Test
	public void test_virtual_localhost()
	{
		
		try{
			setupDB();
			
			System.out.println("\nTEST....");
		//	String owlfile = "onto2.owl";
					//"C:\\Users\\TiBagosi\\Downloads\\openrdf-sesame-2.6.9-sdk\\openrdf-sesame-2.6.9\\bin\\onto2.owl";
			//"src/test/resources/onto2.owl";
		
			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String>ss = man.getRepositoryIDs();
			for (String s: ss)
				System.out.println(s);
			
			// create a configuration for the repository implementation
			SesameRepositoryFactory f = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(f);
			SesameRepositoryConfig config = new SesameRepositoryConfig();
			config.setQuestType("quest-virtual");
			config.setName("my_repo");
			config.setOwlFile("stockexchange-unittest.owl");
			config.setObdaFile("stockexchange-h2-unittest.obda");
			
			RepositoryImplConfig repositoryTypeSpec =  config;
			
			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
			
			RepositoryConfig cnf = man.getRepositoryConfig(repositoryId);
			System.out.println(cnf.getRepositoryImplConfig().toString());
			 
			ss = man.getRepositoryIDs();
			for (String s: ss)
				{
				System.out.println(s);
				}
			
			Repository repository = man.getRepository(repositoryId);
			
			 org.openrdf.repository.RepositoryConnection con = repository.getConnection();
				
			 System.out.println(con.getClass().getName());
			 //File ff = new File("src/test/resources/onto2plus.owl");
					 //"C:\\Users\\TiBagosi\\Downloads\\openrdf-sesame-2.6.9-sdk\\openrdf-sesame-2.6.9\\bin\\onto2plus.rdf");
			 //"src/test/resources/onto2plus.owl");
		
			 
			// con.add(ff, "http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#", RDFFormat.RDFXML);
		
			 
		//	System.out.println("Conn empty: "+con.isEmpty());
			      String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			      System.out.println("RESULT hasdata: "+result.hasNext());
			    	  while(result.hasNext())
			        System.out.println(result.next());
			    	 
			  con.close();
			  repository.shutDown();
			  man.removeRepositoryConfig("testdb");
			  man.shutDown();
			  
		}catch(Exception e)
		{	e.printStackTrace();
		System.out.println(e.getMessage());
		}
	}
	
	
	

}


