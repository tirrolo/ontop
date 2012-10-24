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
import sesameWrapper.SesameClassicJDBCRepo;
import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;
import sesameWrapper.StartJetty;

public class QuestRepos {

	// classic
	// @Test
	public void test_inmemory() {
		RepositoryConnection con = null;
		try {
			System.out.println("In-memory quest repo.");			

			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-inmemory");
			config.setName("my_repo");
			config.setOwlFile("stockexchange-h2-unittest.owl");

			Repository repo = fact.getRepository(config);

			repo.initialize();

			con = (RepositoryConnection) repo.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	 
	// @Test
	public void test_inmemory_localhost() {
		org.openrdf.repository.RepositoryConnection con = null;
		try {
			System.out.println("In-memory quest repo.");
			
			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);

			// create a configuration for the repository implementation
			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-inmemory");
			config.setName("my_remote");
			config.setOwlFile("stockexchange-h2-unittest.owl");
			RepositoryRegistry.getInstance().add(fact);

			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId,
					repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);

			Repository repository = man.getRepository(repositoryId);

			con = repository.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// @Test
	public void test_remote() {
		System.out.println("\n\n\nRemote quest repo................");
		RepositoryConnection con = null;
		try {
			// String owlfile = "src/test/resources/onto2.owl";

			// setupDB();
			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-remote");
			config.setName("my_repo");
			config.setOwlFile("stockexchange-h2-unittest.owl");

			Repository repo = fact.getRepository(config);

			repo.initialize();

			con = (RepositoryConnection) repo.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	 
	// @Test
	public void test_remote_localhost() {
		System.out.println("\n\n\nRemote quest repo................");
		org.openrdf.repository.RepositoryConnection con = null;
		try {
			
			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);

			// create a configuration for the repository implementation
			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-remote");
			config.setName("my_remote");
			config.setOwlFile("stockexchange-h2-unittest.owl");
			RepositoryRegistry.getInstance().add(fact);
			
			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
		
			Repository repository = man.getRepository(repositoryId);
			
			con = repository.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// virtual
	// @Test
	public void test_virtual() {
		RepositoryConnection con = null;

		try {
			System.out.println("\n\n\nVirtual quest repo.....................");
			//setupDB();

			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(fact);
			SesameRepositoryConfig config = new SesameRepositoryConfig();
			config.setQuestType("quest-virtual");
			config.setName("my_repo");
			config.setOwlFile("stockexchange-h2-unittest.owl");
			config.setObdaFile("stockexchange-h2-unittest.obda");

			Repository repo = fact.getRepository(config);

			repo.initialize();

			con = (RepositoryConnection) repo.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

			con.close();
			repo.shutDown();

			// System.out.println(con.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Test
	public void test_virtual_localhost() {

		try {

			System.out.println("\nTEST....");

			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);

			// create a configuration for the repository implementation
			SesameRepositoryFactory f = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(f);
			SesameRepositoryConfig config = new SesameRepositoryConfig();
			config.setQuestType("quest-virtual");
			config.setName("my_repo");
			config.setOwlFile("bsbm.owl");
			config.setObdaFile("bsbm.obda");

			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
		
			Repository repository = man.getRepository(repositoryId);

			org.openrdf.repository.RepositoryConnection con = repository.getConnection();

			String queryString = "select * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

			con.close();
			repository.shutDown();
			man.removeRepositoryConfig("testdb");
			man.shutDown();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
