package inf.unibz.ontp.sesame.repository.test;


import java.io.File;
import java.util.Set;

import org.junit.Test;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.nativerdf.NativeStore;

import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;


public class RepoTest {

	@Test
	public void test() {
	
		System.out.println("\nTEST1....");
		try {
			
			String sesameServer = "http://localhost:8080/openrdf-sesame";
			String repositoryID = "mytest";

			Repository myRepository = new HTTPRepository(sesameServer, repositoryID);
			myRepository.initialize();
			
			 RepositoryConnection con = myRepository.getConnection();
			   try {
			      String queryString = "SELECT * where {<http://protege.stanford.edu/mv#MiniVan> ?p ?o} Limit 10";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			      try {
			    	  while(result.hasNext()){
			      System.out.println(result.next());
			    	  }
			      }
			      finally {
			         result.close();
			      }
			   }
			   finally {
			      con.close();
			   }
			
			myRepository.shutDown();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	
	@Test
	public void test2()
	{try{

		System.out.println("\nTEST2....");
		RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
		man.initialize();
		String s = man.getServerURL();
		System.out.println(s);
		
		Set<String> ss = man.getRepositoryIDs();
		System.out.println(ss);
		
		Repository mytest = man.getRepository("mytest");
		System.out.println(mytest.getConnection().getContextIDs().asList().toString());

		man.shutDown();
		
		
	}catch(Exception e)
	{	e.printStackTrace();
	System.out.println(e.getMessage());
	}
	}
	
	@Test
	public void test3()
	{
		try{

			System.out.println("\nTEST3....");
			File dataDir = new File("c:\\Project\\Timi\\");
			LocalRepositoryManager man = new LocalRepositoryManager(dataDir);
			man.initialize();
			
			String owlfile = "src/test/resources/stockexchange-unittest.owl";
			String obdafile = "src/test/resources/stockexchange-unittest.obda";
			
			RepositoryImplConfig config;
			RepositoryFactory fact = new SesameRepositoryFactory();
			config =  fact.getConfig();
			((SesameRepositoryConfig)config).setType("quest-virtual");
			((SesameRepositoryConfig)config).setName("my_repo");
			((SesameRepositoryConfig)config).setOwlFile(owlfile);
			((SesameRepositoryConfig)config).setObdaFile(obdafile);
			
			RepositoryConfig rconfig = new RepositoryConfig("test", config);
			man.addRepositoryConfig(rconfig);
			
			Repository rep = man.getRepository("my_repo");
			
			RepositoryConnection conn = rep.getConnection();
			
			System.out.println(conn.isEmpty());
			
			
		/*	Repository myRepository = new SailRepository( new NativeStore(dataDir) );
			myRepository.initialize();
			
			 RepositoryConnection con = myRepository.getConnection();
			
			 File f = new File("C:\\Project\\Obdalib\\Protege\\examples\\rdf\\MotorVehicle.rdf");
		
			 
			 con.add(f, "http://protege.stanford.edu/mv#", RDFFormat.RDFXML);
			 
			
			      String queryString = "SELECT * where {?s ?p ?o} Limit 20";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			    	  while(result.hasNext())
			        System.out.println(result.next());
			    	 
			  con.close();
			  myRepository.shutDown();
			  */
			man.shutDown();
			  
		}catch(Exception e)
		{	e.printStackTrace();
		System.out.println(e.getMessage());
		}
	}

}
